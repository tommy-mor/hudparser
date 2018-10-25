import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import java.io.File


// todo
// test on every file in hud
// establish hud object/library, (read frome file, inport methods, export}
// establish change model
// ui
fun main(args: Array<String>) {

    File("/Users/tommy/Downloads/rayshud-master").walkTopDown().filter { it.name.endsWith(".res") }.forEach {
        try {
            val cleanfile = it.readLines()
            val a = ItemsParser.tryParseToEnd(cleanfile.joinToString("\n"))
            println(it.name)
            val b = a.toParsedOrThrow()
        } catch(e: Exception) {
            println(e.toString())
        }
     }
}