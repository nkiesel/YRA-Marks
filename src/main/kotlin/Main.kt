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
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeLines
import kotlin.math.abs

const val baseName = "San_Francisco"

@Serializable
data class Mapping(val entries: Map<String, String>, val uris: List<String>)

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
            val color = properties.color.takeUnless { it == "N/A" } ?: Regex("""\d+""").find(id)?.value?.toInt()
                ?.let { if (it % 2 == 0) "R" else "G" }
            val description = listOf(
                color,
                Regex("""\b([\dA-Z]+)$""").find(properties.name)?.groupValues[1]?.let { "'$it'" },
                properties.lightChar,
                trim(properties.name),
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

fun trim(s: String) = s.replace(Regex("""\s+"""), " ").trim()

fun gpxMarks(input: String): List<Mark> {
    var xml = input.split("\n").filterNot(String::isEmpty).joinToString("").trim()
    val gpxRegex = Regex("""<gpx .+?>""")
    val gpx = gpxRegex.find(xml)
    if (gpx == null) {
        println("Invalid GPX file")
        return emptyList()
    }

    // The "creator" attribute is a required by jpx
    if ("creator=" !in gpx.value) {
        xml = xml.replace(gpxRegex, gpx.value.replace(">", """ creator="YRA">"""))
    }

    // jpx cannot handle extra metadata
    xml = xml.replace(Regex("""<metadata>.+</metadata>"""), "")

    return GPX.Reader.DEFAULT.fromString(xml).wayPoints().toList()
        .map {
            Mark(
                trim(it.name.get()).replace(" ", "-"),
                null,
                it.latitude,
                it.longitude,
                trim(it.description.get().replace(Regex("""["\u201c\u201d]"""), "'"))
            )
        }
        .toList()
}

fun Latitude.toFormat(fmt: String): Any {
    val d = toDouble()
    return toFormat(d, if (d > 0) "N" else "S", fmt)
}

fun Longitude.toFormat(fmt: String): Any {
    val d = toDouble()
    return toFormat(d, if (d > 0) "E" else "W", fmt)
}

fun Double.fmt(decs: Int) = String.format("%.${decs}f", this)

fun toFormat(d: Double, s: String, fmt: String): Any {
    val degrees = abs(d.toInt())
    val minsecs = abs(d - d.toInt()) * 60
    val minutes = minsecs.toInt()
    val seconds = abs(minsecs - minutes) * 60
    return when (fmt) {
        "dmm" -> "$s$degrees ${minsecs.fmt(6)}"
        "dms" -> "$s$degrees $minutes ${seconds.fmt(3)}"
        "noaa" -> "$degrees-$minutes-${seconds.fmt(3)}$s"
        "poi" -> d.fmt(5)
        else -> d.fmt(6)
    }
}

fun writeCSV(marks: Marks, fmt: String, header: Boolean = true) {
    val name = "$baseName.$fmt"
    val writer = csvWriter { lineTerminator = "\n" }
    if (header) writer.writeAll(listOf(listOf("Name", "Latitude", "Longitude", "Description")), name, append = false)
    writer.writeAll(marks.values.map { it.toCSV(fmt) }, name, append = header)
}

fun getYRAGpx(uri: String): String {
    val process = ProcessBuilder("rclone", "cat", uri).start()
    val xml = process.inputStream.bufferedReader().use { it.readText() }
    process.waitFor()
    return xml
}

fun addMarksFromCSV(marks: MutableMarks, csv: Path): Int {
    var added = 0
    csvReader().readAllWithHeader(csv.toFile()).forEach { m ->
        val m = Mark(
            m["Name"]!!,
            null,
            Latitude.ofDegrees(m["Latitude"]!!.toDouble()),
            Longitude.ofDegrees(m["Longitude"]!!.toDouble()),
            m["Description"]!!,
        )
        marks[m.id] = m
        added++
    }
    return added
}

fun addMarksFromYRA(marks: MutableMarks, yra: Mapping, noaa: Mapping): Int {
    var added = 0
    val excluded = setOf("YRA-A", "YRA-B", "YRA-BON-R2", "YRA-D", "BC#2")
    gpxMarks(getYRAGpx(yra.uris[0])).forEach {
        var id = it.id.uppercase()
        if (id !in noaa.entries.keys) id = "YRA-${id}"
        if (id !in excluded) {
            marks[id] = it.copy(id = id)
            added++
        }
    }
    return added
}

@OptIn(ExperimentalXmlUtilApi::class)
fun addMarksFromNOAA(marks: MutableMarks, yra: Mapping, noaa: Mapping): Int {
    val json = Json { ignoreUnknownKeys = true }
    val noaaMarks = noaa.uris
        .map { u -> URI(u).toURL().openStream().use { it.readAllBytes().decodeToString() } }
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

fun writeGpx(marks: Marks) {
    val wayPoints = marks.values.map(Mark::toWayPoint)
    val gpx = GPX.builder().version(GPX.Version.V11).wayPoints(wayPoints).build()
    GPX.write(gpx, Path("$baseName.gpx"))
}

fun main() {
    val input = Path("$baseName.csv")
    val yra = getMapping(Path("yra.yaml"))
    val noaa = getMapping(Path("noaa.yaml"))


    // The order of adding marks is critical because the later overwrite the earlier marks, and thus
    // NOAA as the ultimate source of truth must be the last one.
    val marks = mutableMapOf<String, Mark>()
    val csvCount = addMarksFromCSV(marks, input)
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
