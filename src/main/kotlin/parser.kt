import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parser


interface Item {
    fun print(indent: String)
}
data class Chunk(val title: String, val children: List<Item>, val comment: Comment?, val bracketcomment: Comment?) : Item {
    override fun print(indent: String) {
        println("$indent$title${comment?.value ?: ""}")
        println("$indent{${bracketcomment?.value ?: ""}")
        children.forEach { it.print(indent + "  ") }
        println("$indent}")
    }
}
data class Entry(val title: String, val value: String, val comment: Comment?) : Item {
    override fun print(indent: String) {
        println ("$indent$title    $value ${comment?.value ?: ""}")
    }
}

data class Comment(val value: String) : Item {
    override fun print(indent: String) {
        println ("$indent$value")
    }
}


// todo
// deal with [XBOX] stuff
// fix current crash
// make it less lossy
// reproducing functions


object ItemsParser : Grammar<List<Item>>() {
    val ws by token("\\s+", ignore = true)

    val comment by token("\\/\\/.*")
    val num by token("\\d+")


    val LCURL by token("\\{")
    val RCURL by token("}")

    val word by token("""("[^"]+")|[^\s]+""")

    val commentparser = comment map { Comment(it.text) }
    val entryParser = word and word and optional(commentparser) map { (a,b ,c) -> Entry(a.text, b.text, c)}
    val chunkParser = word and optional(commentparser) and -LCURL and optional(commentparser) and zeroOrMore(parser { itemparser }) and -RCURL map
            { (title, comment, lowercomment, items) -> Chunk(title.text, items, comment, lowercomment) }
    val itemparser : Parser<Item> = chunkParser or entryParser or commentparser
    override val rootParser = zeroOrMore(itemparser)
}

val ast = ItemsParser.parseToEnd("\"a3rtarst\" {  \"zts\" \"xtst\" st  st } stst { } stst stst stst { sts stst } ")


