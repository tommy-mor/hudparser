import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.Parser
import java.io.PrintWriter


interface Item {
    var title: String
    fun print(out: PrintWriter, indent: String)
    // take function that handles when one is found
    fun clean(baseFollower : (String) -> Unit)
    fun merge(new: Item)
}

//todo follow #base functions
//todo merge function
data class Chunk(override var title: String, var children: MutableList<Item>, var comment: Comment?, var bracketcomment: Comment?) : Item {
    override fun print(out: PrintWriter, indent: String) {
        out.println("$indent$title ${comment?.value ?: ""}")
        out.println("$indent{\n$indent${bracketcomment?.value ?: ""}")
        children.forEach { it.print(out,indent + "  ") }
        out.println("$indent}\n")
    }

    override fun clean(baseFollower: (String) -> Unit) {
        children.forEach { it.clean(baseFollower) }
    }

    override fun merge(new: Item) {
        var new = new as? Chunk ?: throw MergeMismatchException("foreign item should be Chunk is $new")
        //if(!title.equals(new.title, ignoreCase = true)) { // actually is not a problem }
        new.children.forEach {
            var ouritem = lookup(it.title)
            if(ouritem == null) {
                children = (children + it).toMutableList() //gross workaround
            } else {
                ouritem.merge(it)
            }
        }

    }

    fun lookup(query: String): Item? {
        return children.find { it.title.trimQuotes().equals(query, ignoreCase = true) }
    }
}

data class Entry(override var title: String, var value: String, var comment: Comment?) : Item {
    override fun print(out: PrintWriter, indent: String) {
        out.println ("$indent$title    $value ${comment?.value ?: ""}")
    }

    override fun clean(baseFollower: (String) -> Unit) {
        if(title.equals("#base", ignoreCase = true)) {
            //the entry does not need to know what the chunk is, the resfile does
            baseFollower(value)
        }
    }

    override fun merge(new: Item) {
        //precondition new.title and this.title are the same
        var new = new as? Entry ?: throw MergeMismatchException("foreign item should be Entry is $new")
        assert(new.title.equals(title, ignoreCase = true))
        value = new.value
        comment = new.comment
    }
}

data class Comment(var value: String) : Item {
    override var title = ""
    override fun print(out: PrintWriter, indent: String) {
        out.println ("$indent$value")
    }

    override fun clean(baseFollower: (String) -> Unit) {
        //do nothing
    }

    override fun merge(new: Item) {
        //do nothing
    }
}



// todo
// deal properly with [$XBOX] tags
// test if tf2 is newline sensitive, then make parser simpler maybe?


//TODO
// investigate parse errors

object ItemsParser : Grammar<List<Item>>() {
    val ws by token("\\s+", ignore = true)


    val LCURL by token("\\{")
    val RCURL by token("}")

    //val weirdness by token ("\\[.+\\]", ignore=true)

    val comment by token("\\/\\/.*|\\[.*\\].*")
    //val comment by token("\\/\\/.*")

    val word by token("""("[^"{}]+")|[^\s{}]+""")

    val commentparser = comment map { Comment(it.text) }
    val entryParser = word and word and optional(commentparser) map { (a,b ,c) -> Entry(a.text, b.text, c)}
    val chunkParser = word and optional(commentparser) and -LCURL and optional(commentparser) and zeroOrMore(parser { itemparser }) and -RCURL map
            { (title, comment, lowercomment, items) -> Chunk(title.text, items.toMutableList(), comment, lowercomment) }
    val itemparser : Parser<Item> = chunkParser or entryParser or commentparser
    override val rootParser = zeroOrMore(itemparser)
}

val ast = ItemsParser.parseToEnd("\"a3rtarst\" {  \"zts\" \"xtst\" st  st } stst { } stst stst stst { sts stst } ")

class MergeMismatchException(message:String): Exception(message)

