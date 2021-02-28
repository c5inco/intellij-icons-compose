package intellijicons.models

data class DataIcon (
    val name: String,
    val area: String,
    val section: String,
    val variants: Int,
    val dark: Boolean,
    val hiDPI: Boolean,
    val sizes: List<List<Int>>,
    val kind: String,
    val java: String
)