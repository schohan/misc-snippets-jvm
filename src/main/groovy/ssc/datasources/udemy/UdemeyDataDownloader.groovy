package ssc.datasources.udemy

import groovy.json.JsonSlurper
import ssc.utils.FileUtils
import ssc.utils.LogWrapper

/**
 * Courses are fetched with best sellers being fetched first. Use this order to give higher boost to these when ingesting in ElasticSearch.
 * Course Details and Ratings can be utilized to improve upon the course object.
 *
 * Created by schohan on 3/24/2016.
 */
class UdemeyDataDownloader {
    String authorizationHeader = "Basic <AuthHash>"
    String coursesUrl = "https://www.udemy.com/api-2.0/courses/?page_size=100&page=1&ordering=best_seller"
    int waitBetweenCallsInSecs = 2



    /* Downloads all courses as JSON files into given directory */
    void downloadAllCourses(File rootDir) {
        File root = FileUtils.createDirByDate(rootDir)
        //Writer writer = new FileWriter(new File(dir, "courses.json"))
        saveCourses(root)
    }


    /* Fetch and Save Udemy courses, one page at a time and save them to a file per page*/
    public void saveCourses(File root) {
        String nextPage = coursesUrl
        int pageCount = 0

        while (nextPage) {
            pageCount += 1
            Writer writer = new FileWriter(new File(root, pageCount + ".json"))

            // Fetch Json
            String json = fetchCoursePage(nextPage)
            LogWrapper.log("Retrieved Json " + json)
            writer.append(json).append("\n")

            // extract pagination related data or any other meta data
            def jsonData = new JsonSlurper().parseText(json)

            // get next page token and count of records fetched in this call
            nextPage = jsonData.next
            println("NextPage=" + nextPage)
            if (nextPage) Thread.sleep(waitBetweenCallsInSecs)
            writer.close()
        }
    }

    /* Closure to fetch youtube videos given query. nextPageToken is value received in JSON response from youtube.*/
    def fetchCoursePage = { String url ->
        if (!url) return

        // Simple Integer enhancement to make
        // 10.seconds be 10 * 1000 ms.
        Integer.metaClass.getSeconds = { ->
            delegate * 1000
        }

        // Construct query URL
        String resp  = url.toURL().getText(connectTimeout: 10.seconds,
                                            readTimeout: 10.seconds,
                                            requestProperties:[Authorization:authorizationHeader])

        // Make API call and return raw content
        return resp
    }


    // Test driver
    public static void main(String[] args) {
        UdemeyDataDownloader udemy = new UdemeyDataDownloader()=
        udemy.downloadAllCourses(new File("C:\\data\\udemy"))

    }

}
