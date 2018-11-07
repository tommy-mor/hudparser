import java.io.File

fun parseSpec(filename: String) {
    println(filename)
    val file = File(filename)
    file.readText().split("\n\n").let{println(it)}
}