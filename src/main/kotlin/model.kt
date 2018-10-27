import java.io.File

class Hud(filename: String) {
    val rootfile = File(filename)
    val root = folder(rootfile)
    init {
        if (!rootfile.isDirectory) throw IllegalArgumentException("Hud needs to be given a directory")
    }

    fun walk(file: File) : hudfile {
        file.walkTopDown().maxDepth(1).forEach { if }
    }
    //list of files, list of res files
    //map of filename to chunk objects
    //methods to manipulate those things
}


abstract class hudfile

class resfile(val file : File) : hudfile()
class otherfile(val file : File) : hudfile()
class folder(val file : File, var children: List<hudfile>) : hudfile()
