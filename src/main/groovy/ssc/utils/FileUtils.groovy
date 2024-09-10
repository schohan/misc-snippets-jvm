package ssc.utils

/**
 * Following helper closures are available for others to use:
 * a. Create a directory by given Date in yyyyMMMdd format
 * b. Write text to a given file. You can append it or overwrite it.
 * c. Iterate through and process all files inside a given directory tree recursively.
 *    Closure that acts of the files is passed as argument.
 *
 * Created by schohan on 3/5/2016.
 */
class FileUtils {

    /* Create directory using current system date under the given root directory */
    static createDirByDate = { File rootDir ->
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException("Argument is not a directory= " + rootDir)
        }
        String today = new Date().format("yyyyMMMdd")
        File f = new File(rootDir.getAbsolutePath() + "/" + today, )
        f.mkdir()
        return f
    }



    /* Write text to file and return the file back.  */
    static def writeTextToFile = { File file, String textToWrite, boolean append=true, boolean appendNewLine=true ->
        if (!textToWrite || !file) throw IllegalArgumentException("Json and File need to be passed as argument")

        // Open File, write content and close it
        FileWriter fileWriter = new FileWriter(file,append)
        fileWriter.write(textToWrite)

        if (appendNewLine) {
            fileWriter.append("\r\n")
        }
        fileWriter.close()
        return this
    }


    /* Process a given directory. This closure would iterate through directories and
     *  pass the files found to the argument closure one at a time. It will recurse through nested directories
     */
    static def processDir = { File inpFileOrDir, Closure fileProcessingClosure  ->
        println("inpFileOrDir=" +inpFileOrDir)

        if (inpFileOrDir.isDirectory()) {
            println("isDir=" +inpFileOrDir.isDirectory())

            inpFileOrDir.eachFile{ f ->
                println("Recurse FileOrDir =" + f.getAbsolutePath())
                processDir(f, fileProcessingClosure)
            }
        } else { // it is a file so process it
            println("Processing File =" + inpFileOrDir.isFile())
            fileProcessingClosure.call(inpFileOrDir)
        }
    }


    /* Return current directory name*/
    public static String currentDir(){
        return new File(".").absolutePath
    }

}
