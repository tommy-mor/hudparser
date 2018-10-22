import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parser


interface Item {
    fun print(indent: String)
}
data class Chunk(val title: String, val children: List<Item>) : Item {
    override fun print(indent: String) {
        println("$indent\"$title\" {")
        children.forEach { it.print(indent + "  ") }
        println("$indent}")
    }
}
data class Entry(val title: String, val value: String) : Item {
    override fun print(indent: String) {
        println ("$indent$title    $value")
    }
}


// todo
// deal with [XBOX] stuff
// fix current crash
// make it less lossy
// reproducing functions


object ItemsParser : Grammar<List<Item>>() {
    val ws by token("\\s+", ignore = true)

    val comment by token("\\/\\/.*", ignore = true)
    val num by token("\\d+")


    val LCURL by token("\\{")
    val RCURL by token("}")

    val word by token("""("[^"]+")|[^\s]+""")

    val entryParser = word and word map { (a,b) -> Entry(a.text, b.text)}
    val chunkParser = word and -LCURL and zeroOrMore(parser { itemparser }) and -RCURL map { (a,b) -> Chunk(a.text, b) }
    val itemparser : Parser<Item> = chunkParser or entryParser
    override val rootParser = zeroOrMore(itemparser)
}

val ast = ItemsParser.parseToEnd("\"a3rtarst\" {  \"zts\" \"xtst\" st  st } stst { } stst stst stst { sts stst } ")
val ast2 = ItemsParser.parseToEnd("\t\"BasicCrossLarge\"\n" +
        "\t{\n" +
        "\t\t\"controlName\"\t\"CExLabel\"\n" +
        "\t\t\"fieldName\"\t \t\"BasicCrossLarge\"\n" +
        "\t\t\"visible\"\t\t\"0\"\n" +
        "\t\t\"enabled\"\t\t\"0\"\n" +
        "\t\t\"zpos\"\t\t\t\"2\"\n" +
        "\t\t\"xpos\"\t\t\t\"c-102\"\n" +
        "\t\t\"ypos\"\t\t\t\"c-98\"\n" +
        "\t\t\"wide\"\t\t\t\"200\"\n" +
        "\t\t\"tall\"\t\t\t\"200\"\n" +
        "\t\t\"font\"\t\t\t\"Crosshairs32\"\n" +
        "\t\t\"labelText\"\t\t\"2\"\n" +
        "\t\t\"textAlignment\"\t\"center\"\n" +
        "\t\t\"fgcolor\"\t\t\"Crosshair\"\n" +
        "\t}")

