import java.io.File

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile, mutableListOf())
    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
        walk(root)

        println(root)
    }

    fun walk(file: folder) : hudfile {
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
    //list of files, list of res files
    //map of filename to chunk objects
    //methods to manipulate those things
}


abstract class hudfile

class resfile(val file : File) : hudfile() {
    var items = parseFile(file)
}
class otherfile(val file : File) : hudfile()
class folder(val file : File, var children: MutableList<hudfile>) : hudfile()
