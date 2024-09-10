package ssc.utils

import groovy.json.JsonOutput

/**
 * Utility to explore files containing json records. It also filters Json records
 *
 * Created by schohan on 3/7/2016.
 */
class JsonUtils {



    /* Takes a Json file as input and writes formatted json file */
    static JsonUtils fixFormatting(File inpJsonFile,  File outFormattedFile) {
        String inpJson = new File(inpJsonFile).text
        def f = new FileWriter(outFormattedFile)
        f.write(JsonOutput.prettyPrint(inpJson))
        f.close()
    }
}
