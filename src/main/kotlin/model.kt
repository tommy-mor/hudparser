import com.sun.javaws.exceptions.InvalidArgumentException
import java.io.File
//todo find a way around the #base stuff in rayshud and see how important it is to add that
//todo maybe trickery like this will be hard to fix https://www.youtube.com/watch?v=B3Qf2CGsrUs

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())

    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
        walk(root)
        clean(root)
    }
    val hudlayout = find(query = "hudlayout.res") ?: throw HudFileNotFoundException("hudlayout.res")
    val clientscheme = find(query = "clientscheme.res") ?: throw HudFileNotFoundException("clientscheme.res")
    fun export() {
        //todo change to proper filename
        
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

    private fun findFileRelative(basefname: String, relativefname: String): hudfile? {
        var chunks = basefname.trim('\"').split("/")
        var relChunks = relativefname.trim('\"').split("/")
        chunks = chunks.dropLast(1) // get rid of current file name
        while(relChunks[0] == "..") {
            relChunks = relChunks.drop(1)
            chunks = chunks.dropLast(1)
        }
        var finalChunks = chunks + relChunks
        val finalFname = finalChunks.joinToString(separator = "/")
        return find(finalFname, useFullPath = true)
    }

    private fun find(query: String, domain: folder = root, useFullPath : Boolean = false): hudfile? {
        domain.children.forEach {
            if(it is folder) { find(query, it, useFullPath)?.let { return it }  }
            else {
                var fname = if (useFullPath) it.file.absolutePath else it.file.name
                if(fname.equals(query, ignoreCase = true)) {
                    return it
                }
            }
        }
        return null
    }

    fun getFont(query: String): Chunk {
        throw NotImplementedError()
    }

    fun getLayout(query: String): Chunk {
        throw NotImplementedError()
    }
    //list of files, list of res files
    //map of filename to chunk objects
    //methods to manipulate those things
}

fun findChunk(target: Chunk, query: String) : Chunk? {
    if(target.title.trim('\"').equals(query, ignoreCase = true)) {
        return target
    } else {
        target.children.forEach {
            if(it is Chunk) { findChunk(it, query)?.let { return it} }
        }
    }
    return null
}
fun mergeChunk(base: Chunk, new: Chunk): Unit {
    base.merge(new)
}

fun mergeFile(base: resfile, new: resfile) {
    // walk the tree pair wise and if they both have a field, take new, if only base has it, keep it

    var baseChunk = base.items.filter { it is Chunk }.getOrNull(0)
    if(baseChunk == null) base.items = base.items + new.items.filter { it is Chunk}[0] as Chunk

    //todo make this safer
    mergeChunk(base.items.filter{ it is Chunk}[0] as Chunk, new.items.filter { it is Chunk }[0] as Chunk)
}

interface hudfile {
    val file: File
    fun clean(relFinder: (String, String) -> hudfile?): Unit
}

class resfile(override val file : File) : hudfile {
    var items = parseFile(file)
    init {

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
}

class otherfile(override val file : File) : hudfile {
    override fun clean(relFinder: (String, String) -> hudfile?) {
        //do nothing
    }
}

class folder(override val file : File, var children: MutableList<hudfile>) : hudfile {
    override fun clean(relFinder: (String, String) -> hudfile?) {
        children.forEach { it.clean(relFinder) }
    }
}


class HudFileNotFoundException(message:String): Exception(message)
class CouldNotConvertHudFileException(message:String): Exception(message)
