import java.io.File
//todo maybe trickery like this will be hard to fix https://www.youtube.com/watch?v=B3Qf2CGsrUs
//todo replace all printlns with logs
//todo add protection against adding transfers that are not parsed
//todo add tests
//todo restructure code into more files

typealias Logger = (String) -> Unit

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
    private fun find(query: String, domain: folder = root, useFullPath : Boolean = false): hudfile? {
        domain.children.forEachIndexed { index, child ->
            if(child is folder) {
                find(query, child, useFullPath)?.let { return it }
            } else {
                var fname = if (useFullPath) child.file.absolutePath else child.file.name
                if(fname.endsWith(query, ignoreCase = true)) {
                    return child
                }
            }
        }
        return null
    }

    fun getFontDef(query: String): Chunk? {
        return clientscheme.findChunk("Fonts")?.findChunk(query)
    }

    fun getColorDef(query: String): Item? {
        return clientscheme.findChunk("Colors")?.lookup(query)
    }

    fun importFontDefs(logger: Logger,fonts: Map<String, Chunk?>) {
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
            } else {
                logger("Imported font definition $name")
            }
        }
    }

    fun getFontFileDef(logger: Logger, query: String): Chunk? {
        //only gets font's that are chunks, I'm pretty sure all custom fonts must be chunks (or else they can't have a name), but not sure
        var map = clientscheme.findChunk("CustomFontFiles")?.children?.map { it as? Chunk }?.find { (it?.lookup("name") as? Entry)?.value?.trimQuotes().equals(query, true) }
        if(map == null) logger("Could not find font definiton for: $query")
        return map //l not trigger??
    }

    fun importFontFileDef(logger: (String) -> Unit, fontDefs: Map<String, Chunk?>) {
        val customFontDefs = clientscheme.findChunk("CustomFontFiles")
        var nextNumber = customFontDefs?.children?.map { it.title.trimQuotes().toInt() }?.max()?.plus(1) ?: throw CreateHudException("Could not read CustomFontFiles correctly")
        val existingFonts = customFontDefs?.children?.map { ((it as? Chunk)?.lookup("name") as? Entry)?.value?.trimQuotes() }
        fontDefs.forEach { (name, chunk) ->
            if(existingFonts.find { it.equals(name, true) } == null) { //check if hud already has a font with same name
                val fnameEntry = (chunk?.lookup("font") as? Entry)
                fnameEntry?.value = "\"resource/fonts/" + fnameEntry?.value!!.split("/").last()
                customFontDefs.children.add(Chunk("\"$nextNumber\"", chunk?.children ?: mutableListOf(), null, null))
                nextNumber += 1
                logger("Imported font file definition $name")
            } else {
                logger("Base hud already had font file definition $name")
            }
        }
    }

    fun getFontFiles(relFilenames: List<String>): List<hudfile> {
        return relFilenames.map { find(it, root, useFullPath = true) }.filterNotNull()
    }

    fun importFontFile(fontFiles: List<hudfile>) {
        //go to/create resoruce/fonts
        var resource = this.root.findFolder("resource")
        this.rootfile.parent
        if(resource == null) {
            resource = folder(File(this.rootfile.parent + "/resource/"), mutableListOf())
            this.root.addChild(resource)
        }
        var fonts = resource!!.findFolder("fonts")
        if (fonts == null) {
            fonts = folder(File(resource.file.path + "/fonts"), mutableListOf())
            resource.addChild(fonts)
        }

        fontFiles.forEach {
            fonts.addChild(it)
        }
        //then change font references in customfonts to be "resource/fonts/$fontname"
    }

    fun importColorDefs(colors: List<Entry>) {
        clientscheme.findChunk("Colors")?.children?.addAll(colors)
    }

    //list of files, list of res files
    //map of filename to chunk objects
    //methods to manipulate those things

    fun importHudResFile(relfilename: String, file: hudfile) {
        (find(relfilename.trim(), useFullPath = true) as resfile).items = (file as resfile).items
    }

    fun importNewHudFile(relfilename: String) {
        throw NotImplementedError()
    }

    fun getHudFile(filename: String): hudfile {
        println("-$filename-")
        return find(filename.trim(), useFullPath = true) ?: throw CreateHudException("could not find file $filename in hud $this")
    }

    override fun toString(): String = hudname
}

//todo fix this function, idk how it managed to be so bad
fun hudfile.findFolder(query: String): folder? {
    return when (this) {
        is folder -> {
            if(this.file.absolutePath.endsWith(query, true)) {
                return this
            } else {
                var ret: folder? = null
                children.forEach {
                    ret = it.findFolder(query)
                    ret?.let { return ret }
                }
                return ret
            }
        }
        else -> return null
    }
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
    fun deepCopy(): hudfile
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
            ret.addAll(recSearch(item, "font", broadSearch = true))
        }
        return ret
    }

    fun getColors(): List<String> {
        var ret = mutableListOf<String>()
        items.forEach { item ->
            ret.addAll(recSearch(item, "color", broadSearch = true))
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

    override fun deepCopy(): resfile {
        var new = resfile(File(file.absolutePath))
        new.items = items.map { it.deepCopy() }
        new.firstChunk = firstChunk?.deepCopy() //because the constructor does not really construct it all the way, clean does
        return new
    }
}

class otherfile(override val file : File) : hudfile {
    override fun clean(relFinder: (String, String) -> hudfile?) {
        //do nothing
    }

    override fun export(folder: File) {
        var newfile = File(folder.absolutePath + "/" + file.name)
        if (!newfile.exists()) file.copyTo(newfile)
    }

    override fun deepCopy(): otherfile {
        return otherfile(File(file.absolutePath))
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

    fun addChild(newfile: hudfile) {
        //don't need to change absoluetpath, because it is never used
        //however, before export, absolutepath of new file is wrong which is gross
        this.children.add(newfile)
    }

    override fun deepCopy(): folder {
        return folder(File(file.absolutePath), children.map { it.deepCopy() }.toMutableList())
    }
}


fun recSearch(base: Item, query: String, broadSearch: Boolean = false): List<String> {
    if(base is Chunk) {
        return base.children.map { recSearch(it, query, broadSearch) }.reduceRight { l, r -> l + r }
    } else if(base is Entry) {
        var match = if(broadSearch)
            base.title.trimQuotes().contains(query, true)
        else base.title.trimQuotes().equals(query, true)
        if(match) {
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