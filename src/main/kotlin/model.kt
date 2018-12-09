import java.io.File
//todo find a way around the #base stuff in rayshud and see how important it is to add that
//todo maybe trickery like this will be hard to fix https://www.youtube.com/watch?v=B3Qf2CGsrUs
//todo replace all printlns with logs
//todo add protection against adding transfers that are not parsed
//todo add tests
//todo restructure code into more files

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())
    val hudname = rootfile.name
    lateinit var hudlayout: resfile
    lateinit var clientscheme: resfile

    var parsed = false

    init {
        try {
            if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
            walk(root)
            clean(root)
            hudlayout = find(query = "hudlayout.res") as? resfile ?: throw HudFileNotFoundException("hudlayout.res")
            clientscheme = find(query = "clientscheme.res") as? resfile ?: throw HudFileNotFoundException("clientscheme.res")
            parsed = true
        } catch (e: Exception) {
            println(e)
            //todo make this report to ui what problem its having
            parsed = false
            throw e
        }
    }


    fun export(pathname: String) {
        //todo change to proper filename
        var dir = File(pathname)
        dir.deleteRecursively()
        dir.mkdirs()
        root.export(dir)
        println("exported new hud to ${pathname}")
    }

    private fun walk(file: folder) : hudfile {
        file.file.walkTopDown().maxDepth(1).drop(1).forEach {
            if(it.isDirectory) {
                var folder = folder(it, mutableListOf())
                walk(folder)
                file.children.add(folder)
            } else {
                if(it.absolutePath.endsWith(".res")) {
                    file.children.add(resfile(it))
                } else {
                    file.children.add(otherfile(it))
                }
            }
        }
        return file
    }

    private fun clean(domain: folder) {
        domain.children.forEach { it.clean(this::findFileRelative) }
    }

    //todo test this on windows (they use different things)
    private fun findFileRelative(basefname: String, relativefname: String): hudfile? {
        var chunks = basefname.trimQuotes().split("/")
        var relChunks = relativefname.trimQuotes().split("/")
        chunks = chunks.dropLast(1) // get rid of current file name
        while(relChunks[0] == "..") {
            relChunks = relChunks.drop(1)
            chunks = chunks.dropLast(1)
        }
        var finalChunks = chunks + relChunks
        val finalFname = finalChunks.joinToString(separator = "/")
        return find(finalFname, useFullPath = true)
    }

    //find hudfile with given filename (query) in given folder (domain)
    private fun find(query: String, domain: folder = root, useFullPath : Boolean = false, replaceWith: hudfile? = null): hudfile? {
        domain.children.forEachIndexed { index, child ->
            if(child is folder) {
                find(query, child, useFullPath)?.let { return it }
            } else {
                var fname = if (useFullPath) child.file.absolutePath else child.file.name
                if(fname.endsWith(query, ignoreCase = true)) {
                    replaceWith?.let { domain.children[index] = replaceWith }
                    return child
                }
            }
        }
        return null
    }

    fun getFontDef(query: String): Chunk? {
        return clientscheme.findChunk("Fonts")?.findChunk(query)
    }

    fun importFontDefs(logger: (String) -> Unit,fonts: Map<String, Chunk?>) {
        //this.clientscheme.firstChunk is the "Scheme" chunk found in clientschemes
        var fontsList = this.clientscheme.firstChunk!!.lookup("Fonts").let { it as? Chunk }?.also { it.children.addAll(fonts.values.filterNotNull()) } // add all fonts form args to own "Font" chunk
        if (fontsList == null) { //if fonts are in another file using #base
            var fChunk =  Chunk("Fonts", fonts.values.filterNotNull().toMutableList(), Comment(toolTag), null)
            fontsList = fChunk
            this.clientscheme.firstChunk!!.children.add(fChunk)
        }
        fonts.forEach { (name, chunk) ->
            if(chunk == null) {
                logger("Could not find font $name")
            }
        }
    }

    fun getFontFileDef(query: String): Chunk? {
        //only gets font's that are chunks, I'm pretty sure all custom fonts must be chunks (or else they can't have a name), but not sure
        var map = clientscheme.findChunk("CustomFontFiles")?.children?.map { (it as? Chunk) }?.find { (it?.lookup("name") as? Entry)?.value?.trimQuotes().equals(query, true) }
        return map //l not trigger??
    }

    fun getLayout(query: String): Chunk {
        throw NotImplementedError()
    }
    //list of files, list of res files
    //map of filename to chunk objects
    //methods to manipulate those things

    fun importHudFile(relfilename: String, file: hudfile) {
        find(relfilename.trim(), useFullPath = true, replaceWith = file)
    }

    fun getHudFile(filename: String): hudfile {
        println("-$filename-")
        return find(filename.trim(), useFullPath = true) ?: throw CreateHudException("could not find file $filename in hud $this")
    }

    override fun toString(): String = hudname
}

fun findChunkIn(target: Chunk, query: String): Chunk? {
    if(target.title.trimQuotes().equals(query, ignoreCase = true)) {
        return target
    } else {
        target.children.forEach {
            if(it is Chunk) { findChunkIn(it, query)?.let { return it} }
        }
    }
    return null
}

fun Chunk.findChunk(query: String): Chunk? {
    return findChunkIn(this, query)
}

fun mergeChunk(base: Chunk, new: Chunk): Unit {
    base.merge(new)
}

fun mergeFile(base: resfile, new: resfile) {
    // walk the tree pair wise and if they both have a field, take new, if only base has it, keep it

    var baseChunk = base.items.filter { it is Chunk }.getOrNull(0)
    if(base.firstChunk == null) {
        base.items = base.items + new.items.filter { it is Chunk}[0] as Chunk
        base.firstChunk = new.firstChunk!!
    }

    //todo make this safer
    mergeChunk(base.firstChunk!!, new.firstChunk!!)
}

interface hudfile {
    val file: File
    fun clean(relFinder: (String, String) -> hudfile?): Unit
    fun export(folder: File): Unit
}

class resfile(override val file : File) : hudfile {
    var items = parseFile(file)

    //the first/main chunk in a file(usually the only one)
    var firstChunk: Chunk? = items.filter { it is Chunk }.getOrNull(0) as? Chunk

    fun findChunk(query: String): Chunk? {
        var ret: Chunk? = null
        items.forEach { item ->
            if(item is Chunk) {
                ret = findChunkIn(item, query)
            }
            ret?.let { return it }
            //if clientschemes top level items are not chunks, don't search them
        }
        return ret
    }

    fun getFonts(): List<String> {
        var ret = mutableListOf<String>()
        items.forEach { item ->
            ret.addAll(recSearch(item, "font"))
        }
        return ret
    }


    fun followBase(filefinder: (String, String) -> hudfile?, relname: String): Unit {
        //todo clean this up
        //use parent finder class to find the file, then merge its contents as its base
        val foundfile = filefinder(file.absolutePath, relname)
        foundfile ?: println("could not find file $relname")
        foundfile ?: return

        mergeFile(this, foundfile as? resfile ?: throw CouldNotConvertHudFileException("$foundfile"))
    }

    override fun clean(relFinder: (String, String) -> hudfile?) {
        items.forEach {
            it.clean { relname -> followBase(relFinder, relname) }
        }
        //only top level Entry's
        items = items.dropWhile { it is Entry } // remove #base's at the end after following them

    }

    override fun export(folder: File) {
        val newfile = File(folder.absolutePath + "/" + file.name)

        if(items.isEmpty()) {
            //parse failed
            file.copyTo(newfile)
        } else {
            newfile.printWriter().use {
                out -> items.forEach { it.print(out, "") }
            }
        }
    }
}

class otherfile(override val file : File) : hudfile {
    override fun clean(relFinder: (String, String) -> hudfile?) {
        //do nothing
    }

    override fun export(folder: File) {
        var newfile = File(folder.absolutePath + "/" + file.name)
        file.copyTo(newfile)
    }
}

class folder(override val file : File, var children: MutableList<hudfile>) : hudfile {
    override fun export(folder: File) {
        var newfile = File(folder.absolutePath + "/" + file.name + "/")
        newfile.mkdir()
        children.forEach { it.export(newfile) }
    }

    override fun clean(relFinder: (String, String) -> hudfile?) {
        children.forEach { it.clean(relFinder) }
    }
}


fun recSearch(base: Item, query: String): List<String> {
    if(base is Chunk) {
        return base.children.map { recSearch(it, query) }.reduceRight { l, r -> l + r }
    } else if(base is Entry) {
        if(base.title.trimQuotes().equals(query, true)) {
            return listOf<String>(base.value.trimQuotes())
        }
    }
    return listOf<String>()
}

fun String.trimQuotes(): String {
    return this.trim('\"')
}

class HudFileNotFoundException(message: String): Exception(message)
class CouldNotConvertHudFileException(message: String): Exception(message)
class CreateHudException(message: String): Exception(message)