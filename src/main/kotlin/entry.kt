import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.toParsedOrThrow
import java.io.File



// todo
// establish hud object/library, (read frome file, inport methods, export}
// establish change model
// ui
fun main(args: Array<String>) {
    //var mainhud = Hud("/Users/tommy/Downloads/tf2basehud/")
    //mainhud.export("/Users/tommy/Downloads/outputhud/")
    //finds a chunk in hudlayout
    parseSpec("/home/tommy/programming/hudparser/src/main/resources/features.txt")
}

fun parseFile(file: File): List<Item> {
    try {
        val a = ItemsParser.tryParseToEnd(file.readText())
        if(a is ErrorResult) {
            println("-------------parse ERROR")
        }
        val b = a.toParsedOrThrow()
        return b.value
    } catch(e: Exception) {
        println(file.absolutePath)
        println(e.toString())
    }

    //this is checked for in model > resfile > export
    return emptyList()
}