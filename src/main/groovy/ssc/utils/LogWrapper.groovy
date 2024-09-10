package ssc.utils
/**
 * Created by schohan on 3/14/2016.
 */
class LogWrapper {
    private boolean printToConsole = true
    //private Logger logger


    LogWrapper(boolean printToConsole = true) {
        this.printToConsole = printToConsole
    }


    // Print to system console
    static log(String text) {
        println(text)
    }

    static print(String text) {
        println(text)
    }
}
