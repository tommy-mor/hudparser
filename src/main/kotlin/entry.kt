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
    var mainhud = Hud("/Users/tommy/Downloads/rayshud-master")
}

fun parseFile(file: File): List<Item> {
    try {
        val cleanfile = file.readLines()
        val a = ItemsParser.tryParseToEnd(cleanfile.joinToString("\n"))
        println(file.name)
        val b = a.toParsedOrThrow()
        return b.value
    } catch(e: Exception) {
        println(e.toString())
    }
    return emptyList()
}