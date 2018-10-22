import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.Parser


interface Item
data class Chunk(val title: String, val children: List<Item>) : Item
data class Entry(val title: String, val value: String) : Item

// todo
// deal with [XBOX] stuff
// fix current crash
// make it less lossy
// reproducing functions


object ItemsParser : Grammar<List<Item>>() {
    val ws by token("\\s+", ignore = true)

    val num by token("\\d+")
    val word by token("""(".+")|[^\s]+""")

    val LCURL by token("\\{")
    val RCURL by token("}")

    val entryParser = word and word map { (a,b) -> Entry(a.text, b.text)}
    val chunkParser = word and -LCURL and zeroOrMore(parser { itemparser }) and -RCURL map { (a,b) -> Chunk(a.text, b) }
    val itemparser : Parser<Item> = entryParser or chunkParser
    override val rootParser = zeroOrMore(itemparser)
}

val ast = ItemsParser.parseToEnd("\"a3rtarst\" { starst arsarstars sstst ststst } strstrst { stst stst stst stst sts { stst ststst } }")
