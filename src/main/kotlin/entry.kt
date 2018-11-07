import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import java.io.File



// todo
// establish hud object/library, (read frome file, inport methods, export}
// establish change model
// ui
fun main(args: Array<String>) {
    var mainhud = Hud("/Users/tommy/Downloads/rayshud-master")
    mainhud.export("/Users/tommy/Downloads/outputhud/")
    //finds a chunk in hudlayout

}

fun parseFile(file: File): List<Item> {
    try {
        val a = ItemsParser.tryParseToEnd(file.readText())
        val b = a.toParsedOrThrow()
        return b.value
    } catch(e: Exception) {
        println(e.toString())
    }
    return emptyList()
}