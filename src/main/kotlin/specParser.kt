import java.io.File

typealias Spec =  List<Task>
data class Task(val feature: String, val hudlayout: List<String>?, val filenames: List<String>) {
    override fun toString(): String = "$feature"
}

fun makeTask(title: String, files: String, hudlayouts: String): Task {
    if(files.equals("None", ignoreCase = true)) throw SpecParseException("file cannot be empty field")
    val hudlayout = if(!hudlayouts.equals("None", true)) { hudlayouts.split(",") } else { null }
    val filenames = files.split(",")

    if(!filenames.get(0).startsWith("File:", true))
        throw SpecParseException("First line of element $title's definition must start with \"File:\" instead of ${filenames.getOrNull(0)}")
    if(!(hudlayout?.get(0)?.startsWith("Hudlayout:", true) ?: false))
        throw SpecParseException("Second line of element $title's definition must start with \"Hudlayout:\" instaed of ${filenames.getOrNull(0)}")

    return Task(title, hudlayout, filenames)
}

fun parseSpec(filename: String): Spec {
    try {

        println(filename)
        val file = File(filename)
        val split = file.readText().split("\n\n")
        val specs = split.map { it.lines() }.map {
            makeTask(
                    it.getOrNull(0) ?: throw SpecParseException("definition\"\"\"$split\"\"\" is incomplete"),
                    it.getOrNull(1) ?: throw SpecParseException("definition\"\"\"$split\"\"\" is incomplete"),
                    it.getOrNull(2) ?: throw SpecParseException("definition\"\"\"$split\"\"\" is incomplete"))
        }
        return specs
    } catch (e: SpecParseException) {
        throw e
    } catch (e: Exception) {
        throw SpecParseException("Unknown Error: ${e.message}")
    }
}

class SpecParseException(msg: String) : Exception(msg)