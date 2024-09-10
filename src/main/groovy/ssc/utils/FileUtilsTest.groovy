package ssc.utils

/**
 * Created by schohan on 3/5/2016.
 */
class FileUtilsTest {

    def testProcessDir() {
        def printFilePath = {  File f -> println("File processed " + f) }
        new FileUtils().processDir(new File("C:\\workspace\\apis\\src\\main\\groovy\\ssc\\search"), printFilePath)
        return this
    }

    def testWriteTextToFile() {
        File file = new File("C:\\Temp\\test.txt")
        new FileUtils().writeTextToFile(file, "This is test file").writeTextToFile(file, "Added another string to it")
        return this
    }

    def testCreateDirByDate() {
       File f =  new FileUtils().createDirByDate(new File("C:\\Temp\\"))
       File f2 = new File(f.getAbsolutePath())
       assert(f == f2)
    }

    /* Tester method */
    public static void main(String[] args) {
        new FileUtilsTest()
                //.testProcessDir()
                //.testWriteTextToFile()
                .testCreateDirByDate()
    }
}
