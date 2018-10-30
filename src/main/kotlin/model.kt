import java.io.File
//todo find a way around the #base stuff in rayshud and see how important it is to add that
//todo maybe trickery like this will be hard to fix https://www.youtube.com/watch?v=B3Qf2CGsrUs

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())

    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
        walk(root)
    }
    val hudlayout = find(query = "hudlayout.res") ?: throw HudFileNotFoundException("hudlayout.res")
    val clientscheme = find(query = "clientscheme.res") ?: throw HudFileNotFoundException("clientscheme.res")

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

    private fun find(domain: folder = root, query: String): hudfile? {
        domain.children.forEach {
            if(it is folder) { find(it, query)?.let { return it }  }
            else {
                if(it.file.name.equals(query, ignoreCase = true)) {
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

interface hudfile {
    val file: File
}

class resfile(override val file : File) : hudfile {
    var items = parseFile(file)
}
class otherfile(override val file : File) : hudfile
class folder(override val file : File, var children: MutableList<hudfile>) : hudfile


class HudFileNotFoundException(message:String): Exception(message)
