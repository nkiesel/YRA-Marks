import com.charleskorn.kaml.Yaml
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Latitude
import io.jenetics.jpx.Longitude
import io.jenetics.jpx.WayPoint
import kotlinx.serialization.SerialName
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

@Serializable
data class NoaaMark(
    @SerialName("features")
    val features: List<Feature>,
    @SerialName("type")
    val type: String
) {
    @Serializable
    data class Feature(
        @SerialName("id")
        val id: Int,
        @SerialName("geometry")
        val geometry: Geometry,
        @SerialName("properties")
        val properties: Properties,
    ) {
        @Serializable
        data class Geometry(
            @SerialName("coordinates")
            val coordinates: List<Double>,
        )

        @Serializable
        data class Properties(
            @SerialName("LIGHT_LIST_NUMBER")
            val lightListNumber: Double,
            @SerialName("ASSIGNED_LATITUDE")
            val assignedLatitude: String,
            @SerialName("ASSIGNED_LONGITUDE")
            val assignedLongitude: String,
            @SerialName("COLOR")
            val color: String?,
            @SerialName("DAYMARK_COLOR")
            val daymarkColor: String?,
            @SerialName("DAYMARK_REMARK")
            val daymarkRemark: String?,
            @SerialName("DAYMARK_SHAPE")
            val daymarkShape: String?,
            @SerialName("DECIMAL_LATITUDE")
            val decimalLatitude: Double,
            @SerialName("DECIMAL_LONGITUDE")
            val decimalLongitude: Double,
            @SerialName("DESCRIPTION_TYPE")
            val descriptionType: String,
            @SerialName("LIGHT_CHAR")
            val lightChar: String?,
            @SerialName("NAME")
            val name: String,
            @SerialName("SECONDARY_UNIT_NAME")
            val secondaryUnitName: String?,
        )

        fun toMark(id: String, names: Map<String, String>): Mark {
            val color = properties.color.takeUnless { it == "N/A" } ?: digitsRegex.find(id)?.value?.toInt()
                ?.let { if (it % 2 == 0) "R" else "G" }
            val description = listOf(
                color,
                nameRegex.find(properties.name)?.groupValues?.get(1)?.let { "'$it'" },
                properties.lightChar,
                trimAll(properties.name),
                names[id]?.let { "($it)" },
            ).filterNot { it.isNullOrEmpty() || it == "N/A" }.joinToString(" ") { it!!.trim() }
            return Mark(
                id,
                properties.lightListNumber.toString(),
                Latitude.ofDegrees(geometry.coordinates[1]),
                Longitude.ofDegrees(geometry.coordinates[0]),
                description,
            )
        }
    }
}

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

typealias Marks = Map<String, Mark>
typealias MutableMarks = MutableMap<String, Mark>

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

fun addMarksFromCSV(marks: MutableMarks, csv: Path): Int {
    var added = 0
    csvReader().readAllWithHeader(csv.toFile()).forEach { row ->
        val id = row["Name"]!!
        marks[id] = Mark(
            id,
            null,
            Latitude.ofDegrees(row["Latitude"]!!.toDouble()),
            Longitude.ofDegrees(row["Longitude"]!!.toDouble()),
            row["Description"]!!,
        )
        added++
    }
    return added
}

fun addMarksFromYRA(marks: MutableMarks, yra: Mapping, noaa: Mapping): Int {
    var added = 0
    val excluded = setOf("YRA-A", "YRA-B", "YRA-BON-R2", "YRA-D", "BC#2")
    gpxMarks(fetchString(yra.urls[0])).forEach { mark ->
        var id = mark.id.uppercase()
        if (id !in noaa.entries.keys) id = "YRA-${id}"
        if (id !in excluded) {
            marks[id] = mark.copy(id = id)
            added++
        }
    }
    return added
}

fun addMarksFromNOAA(marks: MutableMarks, yra: Mapping, noaa: Mapping): Int {
    val json = Json { ignoreUnknownKeys = true }
    val noaaMarks = noaa.urls
        .map { url -> fetchString(url) }
        .flatMap { json.decodeFromString<NoaaMark>(it).features }
        .associateBy { it.properties.lightListNumber.toString() }

    var added = 0
    for ((id, llnr) in noaa.entries) {
        val mark = noaaMarks[llnr]
        if (mark == null) {
            println("No NOAA LLNR $llnr found for mark $id")
        } else {
            marks[id] = mark.toMark(id, yra.entries)
            added++
        }
    }
    return added
}

fun updateReadMe(marks: Marks) {
    val readMe = Path("README.md")
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

fun getMapping(path: Path): Mapping = Yaml.default.decodeFromString(Mapping.serializer(), path.readText())

const val baseName = "San_Francisco"

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


fun main() {
    val csv = Path("$baseName.csv")
    val yra = getMapping(Path("yra.yaml"))
    val noaa = getMapping(Path("noaa.yaml"))

    // The order of adding marks is critical because the later overwrite the earlier marks, and thus
    // NOAA as the ultimate source of truth must be the last one.
    val marks = mutableMapOf<String, Mark>()
    val csvCount = addMarksFromCSV(marks, csv)
    val yraCount = addMarksFromYRA(marks, yra, noaa)
    val noaaCount = addMarksFromNOAA(marks, yra, noaa)

    println("csv: $csvCount yra: $yraCount noaa: $noaaCount")

    writeCSV(marks, "csv")
    writeCSV(marks, "dmm")
    writeCSV(marks, "dms")
    writeCSV(marks, "noaa")
    writeCSV(marks, "poi", false)
    writeGpx(marks)
    updateReadMe(marks)
}
