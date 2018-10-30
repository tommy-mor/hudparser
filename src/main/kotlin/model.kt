import java.io.File

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())
    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
        walk(root)

        println(root)
    }

    private fun walk(file: folder) : hudfile {
        file.file.walkTopDown().maxDepth(1).drop(1).forEach {
            if(it.isDirectory) {
                var folder = folder(it, mutableListOf())
                walk(folder)
                file.children.add(folder)
            } else {
                if(it.endsWith(".res")) {
                    file.children.add(resfile(it))
                } else {
                    file.children.add(otherfile(it))
                }
            }
        }
        return file
    }

    private fun find(domain: folder = root, query: String): hudfile {
        domain.children.forEach {
            if(it is folder) { return find(it, query) }
            else {
                if(it.file.name == query) {
                    return it
                }
            }
        }
        throw HudFileNotFoundException("file $query not found")
    }



    val clientscheme = find(query = "clienscheme.res") as resfile
    val hudlayout = find(query = "hudlayout.res") as resfile

    fun getFont(query: String): Chunk {

    }

    fun getLayout(query: String): Chunk {

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
