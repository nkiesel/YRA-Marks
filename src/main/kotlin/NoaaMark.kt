import io.jenetics.jpx.Latitude
import io.jenetics.jpx.Longitude
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoaaMark(
    val features: List<Feature>,
    val type: String
) {
    @Serializable
    data class Feature(
        val id: Int,
        val geometry: Geometry,
        val properties: Properties,
    ) {
        @Serializable
        data class Geometry(
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
