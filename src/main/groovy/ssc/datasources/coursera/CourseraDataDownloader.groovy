package ssc.datasources.coursera

import groovy.json.JsonSlurper
import ssc.utils.FileUtils
import ssc.utils.LogWrapper

/**
 * Download courses from Coursera and save them
 *
 * See https://building.coursera.org/app-platform/catalog/ for details
 * Created by schohan on 3/27/2016.
 */
class CourseraDataDownloader {
    int waitBetweenCallsInSecs = 2
    int limit = 100

    // Add this param string to all queries to fetch specified fields on
    String courseFields = "fields=language,shortDescription,description,primaryLanguages,subtitleLanguages,instructorIds,photoUrl,certificates,startDate,workload,previewLink,specializations,s12nIds,domainTypes"
    String instructorFields = "fields=firstName,lastName,shortName,photo,bio,title,department,website,websiteTwitter,websiteLinkedin,websiteFacebook,websiteGplus"

    String baseCourseUrl = "https://api.coursera.org/api/courses.v1?"
    String baseInstructorUrl = "https://api.coursera.org/api/instructors.v1?"

    // Fetch courseListUrl. p1 is start page
    String courseListUrl = baseCourseUrl + courseFields + "&start={p1}&limit=${limit}"



    /* Download courses in root directory inside a subdirectory with a name using month and day*/
    void downloadAllCourses(File rootDir) {
        File root = FileUtils.createDirByDate(rootDir)
        saveCourses(root)
    }

    /* Download courses by the page and save them inside the passed root directory */
    void saveCourses(File root) {
        int startResultIndex = 1
        boolean hasMore = true

        while (hasMore) {
            String nextPageUrl = courseListUrl.replace("{p1}",startResultIndex.toString())
            Writer writer = new FileWriter(new File(root, startResultIndex + ".json"))

            // Fetch Json
            String json = fetchCoursePage(nextPageUrl)
            LogWrapper.log("Retrieved Json " + json)
            writer.append(json).append("\n")

            // extract pagination related data or any other meta data
            def jsonData = new JsonSlurper().parseText(json)
            // TODO Fetch instructor details here and add it to jsonData object before saving it to disk

            // get next page token and count of records fetched in this call

            println("Total=" + jsonData.paging.total + ", Next =" + jsonData.paging.next )
            if (jsonData.paging.next) {
                startResultIndex += limit  // Next result index
                Thread.sleep(waitBetweenCallsInSecs)
            } else {
                hasMore = false
            }
            writer.close()
        }
    }

    /* Closure to fetch youtube videos given query. nextPageToken is value received in JSON response from youtube.*/
    def fetchCoursePage = { String url ->
        LogWrapper.log("Fetching using Url=" + url)
        if (!url) return

        // Simple Integer enhancement to make
        // 10.seconds be 10 * 1000 ms.
        Integer.metaClass.getSeconds = { ->
            delegate * 1000
        }

        // Construct query URL
        String resp  = url.toURL().getText(connectTimeout: 10.seconds, readTimeout: 10.seconds)

        // Make API call and return raw content
        return resp
    }


    // TODO Implement in phase2
    void fetchInstructorDetails() {}



    // Driver class
    public static void main(String[] args) {
        CourseraDataDownloader coursera  = new CourseraDataDownloader()
        coursera.downloadAllCourses(new File("C:\\data\\coursera"))
    }
}
