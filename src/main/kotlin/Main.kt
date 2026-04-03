import com.charleskorn.kaml.Yaml
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Latitude
import io.jenetics.jpx.Longitude
import io.jenetics.jpx.WayPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeLines
import kotlin.math.abs

val digitsRegex = Regex("""\d+""")
val nameRegex = Regex("""\b([\dA-Z]+)$""")
val gpxRegex = Regex("""<gpx .+?>""")
val metadataRegex = Regex("""<metadata>.+</metadata>""")
val quoteRegex = Regex("""["\u201c\u201d]""")
val whitespaceRegex = Regex("""\s+""")

val httpClient = OkHttpClient()

@Serializable
data class Mapping(val entries: Map<String, String>, val urls: List<String>)

fun Path.toMapping() = Yaml.default.decodeFromString(Mapping.Companion.serializer(), readText())

data class Mark(
    val id: String,
    val llnr: String?,
    val latitude: Latitude,
    val longitude: Longitude,
    val description: String
) {
    override fun toString(): String {
        return """"$id",${latitude.toDouble().fmt(6)},${longitude.toDouble().fmt(6)},"$description""""
    }

    fun toCSV(fmt: String = "csv"): List<Any> {
        return when (fmt) {
            "poi" -> listOf(longitude.toFormat(fmt), latitude.toFormat(fmt), id, description)
            else -> listOf(id, latitude.toFormat(fmt), longitude.toFormat(fmt), description)
        }
    }

    fun toWayPoint(): WayPoint =
        WayPoint.builder().name(id).lat(latitude).lon(longitude).desc(description).sym("activepoint").build()
}

fun trimAll(s: String) = s.replace(whitespaceRegex, " ").trim()

fun gpxMarks(input: String): List<Mark> {
    val gpx = gpxRegex.find(input)
    if (gpx == null) {
        println("Invalid GPX file")
        return emptyList()
    }

    var xml = input

    // The "creator" attribute is a required by jpx
    if ("creator=" !in gpx.value) {
        xml = xml.replace(gpxRegex, gpx.value.replace(">", """ creator="YRA">"""))
    }

    // jpx cannot handle extra metadata
    xml = xml.replace(metadataRegex, "")

    return GPX.Reader.DEFAULT.fromString(xml).wayPoints().toList()
        .map {
            Mark(
                trimAll(it.name.get()).replace(" ", "-"),
                null,
                it.latitude,
                it.longitude,
                trimAll(it.description.get().replace(quoteRegex, "'"))
            )
        }
}

fun Latitude.toFormat(fmt: String): Any {
    val d = toDouble()
    return toFormat(d, if (d > 0) "N" else "S", fmt)
}

fun Longitude.toFormat(fmt: String): Any {
    val d = toDouble()
    return toFormat(d, if (d > 0) "E" else "W", fmt)
}

fun Double.fmt(decs: Int) = String.format(Locale.US, "%.${decs}f", this)

fun toFormat(d: Double, s: String, fmt: String): Any {
    val degrees = abs(d.toInt())
    val minSecs = abs(d - d.toInt()) * 60
    val minutes = minSecs.toInt()
    val seconds = abs(minSecs - minutes) * 60
    return when (fmt) {
        "dmm" -> "$s$degrees ${minSecs.fmt(6)}"
        "dms" -> "$s$degrees $minutes ${seconds.fmt(3)}"
        "noaa" -> "$degrees-$minutes-${seconds.fmt(3)}$s"
        "poi" -> d.fmt(5)
        else -> d.fmt(6)
    }
}

fun fetchString(url: String): String {
    httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
        if (!response.isSuccessful) error("Server returned: ${response.code}")
        return response.body.string()
    }
}

fun fetchMarksFromCSV(csv: Path): List<Mark> {
    if (!csv.toFile().exists()) return emptyList()
    return csvReader().readAllWithHeader(csv.toFile()).map { row ->
        val id = row["Name"]!!
        Mark(
            id,
            null,
            Latitude.ofDegrees(row["Latitude"]!!.toDouble()),
            Longitude.ofDegrees(row["Longitude"]!!.toDouble()),
            row["Description"]!!,
        )
    }
}

fun fetchMarksFromYRA(yra: Mapping, noaa: Mapping): List<Mark> {
    val excluded = setOf("YRA-A", "YRA-B", "YRA-BON-R2", "YRA-D", "BC#2")
    return gpxMarks(fetchString(yra.urls[0])).mapNotNull { mark ->
        var id = mark.id.uppercase()
        if (id !in noaa.entries.keys) id = "YRA-${id}"
        if (id !in excluded) {
            mark.copy(id = id)
        } else {
            null
        }
    }
}

fun fetchMarksFromNOAA(yra: Mapping, noaa: Mapping): List<Mark> {
    val json = Json { ignoreUnknownKeys = true }
    val noaaMarks = noaa.urls
        .map { url -> fetchString(url) }
        .flatMap { json.decodeFromString<NoaaMark>(it).features }
        .associateBy { it.properties.lightListNumber.toString() }

    return noaa.entries.mapNotNull { (id, llnr) ->
        val mark = noaaMarks[llnr]
        if (mark == null) {
            println("No NOAA LLNR $llnr found for mark $id")
            null
        } else {
            mark.toMark(id, yra.entries)
        }
    }
}

const val baseName = "San_Francisco"

typealias Marks = Map<String, Mark>

fun updateReadMe(marks: Marks) {
    val readMe = Path("README.md")
    if (!readMe.toFile().exists()) return
    val updated = buildList {
        readMe.readLines().filterNot { it.startsWith("|") }.forEach { line ->
            add(line)
            if (line == "## All The Marks") {
                add("|Name|Latitude|Longitude|Description|")
                add("|----|--------|---------|-----------|")
                marks.values.forEach {
                    add(it.toCSV().joinToString("|", prefix = "|", postfix = "|"))
                }
            }
        }
    }
    readMe.writeLines(updated)
}

fun writeCSV(marks: Marks, fmt: String, header: Boolean = true) {
    val name = "$baseName.$fmt"
    val writer = csvWriter { lineTerminator = "\n" }
    if (header) writer.writeAll(listOf(listOf("Name", "Latitude", "Longitude", "Description")), name, append = false)
    writer.writeAll(marks.values.map { it.toCSV(fmt) }, name, append = header)
}

fun writeGpx(marks: Marks) {
    val wayPoints = marks.values.map(Mark::toWayPoint)
    val gpx = GPX.builder().version(GPX.Version.V11).wayPoints(wayPoints).build()
    GPX.write(gpx, Path("$baseName.gpx"))
}

fun main(): Unit = runBlocking {
    val csv = Path("$baseName.csv")
    val yra = Path("yra.yaml").toMapping()
    val noaa = Path("noaa.yaml").toMapping()

    // Fetch concurrently
    withContext(Dispatchers.IO) {
        val allMarksLists = awaitAll(
            async { fetchMarksFromCSV(csv) },
            async { fetchMarksFromYRA(yra, noaa) },
            async { fetchMarksFromNOAA(yra, noaa) }
        )

        println("csv: ${allMarksLists[0].size} yra: ${allMarksLists[1].size} noaa: ${allMarksLists[2].size}")

        // The order of adding marks is critical because the later overwrite the earlier marks, and thus
        // NOAA as the ultimate source of truth must be the last one.
        val marks = allMarksLists.flatten().associateBy { it.id }

        writeCSV(marks, "csv")
        writeCSV(marks, "dmm")
        writeCSV(marks, "dms")
        writeCSV(marks, "noaa")
        writeCSV(marks, "poi", false)
        writeGpx(marks)
        updateReadMe(marks)
    }
}
