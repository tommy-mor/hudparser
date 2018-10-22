import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.parseToEnd
import com.github.h0tk3y.betterParse.parser.tryParseToEnd
import java.io.File

fun main(args: Array<String>) {
    var file = File("/Users/tommy/programming/parser/src/main/resources/hudlayout.res")
    //file.forEachLine { println(it) }
    //println(ast.toString())
    val cleanfile = file.readLines().map { it.replace("\\/\\/.*".toRegex(), "") }
    println(cleanfile)
    val a = ItemsParser.tryParseToEnd(cleanfile.joinToString("\n"))
    println(a)
}