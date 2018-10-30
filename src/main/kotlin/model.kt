import java.io.File

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())




    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
        walk(root)

        val hudlayout = find(query = "hudlayout.res") ?: throw HudFileNotFoundException("hudlayout.res")
        val clientscheme = find(query = "clientscheme.res") ?: throw HudFileNotFoundException("clientscheme.res")
        println(hudlayout.file.absolutePath)
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


interface hudfile {
    val file: File
}

class resfile(override val file : File) : hudfile {
    var items = parseFile(file)
}
class otherfile(override val file : File) : hudfile
class folder(override val file : File, var children: MutableList<hudfile>) : hudfile


class HudFileNotFoundException(message:String): Exception(message)
