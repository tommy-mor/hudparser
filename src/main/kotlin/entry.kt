import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import java.io.File


// todo
// establish hud object/library, (read frome file, inport methods, export}
// establish change model
// ui
fun main(args: Array<String>) {
    val small = false
    if (small) {
        parseFile(File("/Users/tommy/programming/parser/src/main/resources/easy.res"))
    } else {
        File("/Users/tommy/Downloads/rayshud-master").walkTopDown().filter { it.name.endsWith(".res") }.forEach {
            parseFile(it)
        }
    }
}

fun parseFile(file: File) {
    try {
        val cleanfile = file.readLines()
        val a = ItemsParser.tryParseToEnd(cleanfile.joinToString("\n"))
        println(file.name)
        val b = a.toParsedOrThrow()
    } catch(e: Exception) {
        println(e.toString())
    }
}