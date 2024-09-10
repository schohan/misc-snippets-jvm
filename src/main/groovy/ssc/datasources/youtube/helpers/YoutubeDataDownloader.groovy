package ssc.datasources.youtube.helpers

import groovy.json.JsonSlurper
import ssc.utils.FileUtils

/**
 * Downloads data from Youtube and store it in given files.
 * BaseURL and API Key is required to use this class.
 *
 *
 * Created by schohan on 3/5/2016.
 */
final class YoutubeDataDownloader {
    private String apiKey
    private String baseUrl
    private int maxPageSize = 50 // cannot be more than 50
    private int maxResultsToFetch = 50 // No need to fetch more than these results



    /* Construct YoutubeDataDownLoader for a given URL and API Key */
    public YoutubeDataDownloader(String baseUrl, String apiKey, int maxPageSize,
                                                    int maxResultsToFetch) {
        this.baseUrl = baseUrl
        this.apiKey = apiKey
        this.maxPageSize = maxPageSize
        this.maxResultsToFetch = maxResultsToFetch
    }


    /*
    * Search by given keyword, download and save snippets from youtube.
    *
    * @param query Search keyword/query
    * @param outDir Directory where files are to be stored
    *
    *
    * */
    public void saveSnippetToDir(String query, File outDir) {
        File dir = FileUtils.createDirByDate(outDir)
        Writer writer = new FileWriter(new File(dir, query + ".json"))
        saveSnippetToWriter(query, writer)
        writer.close()
    }



    /* Method to search and download youtube video snippets
    *
    * @param query Search keyword/query
    * @param writer Writer object to write Youtube response to.
    * */
    public void saveSnippetToWriter(String query, Writer writer) {
        boolean hasMoreRecords = true
        String nextPageToken = ""
        int fetchCount = 0

        while (hasMoreRecords) {
            // Fetch Json
            String json = fetchSnippetPage(query, nextPageToken)
            println("Retrieved Json " + json)
            writer.append(json)

            // extract pagination related data or any other meta data
            def jsonData = new JsonSlurper().parseText(json)

            // get next page token and count of records fetched in this call
            nextPageToken = jsonData.nextPageToken
            fetchCount += jsonData.pageInfo.resultsPerPage

            println("fetchCount " + fetchCount + " NextPageToken == " + nextPageToken + " JsonData " + jsonData + " jsonData.pageInfo.resultsPerPage " + jsonData.pageInfo.resultsPerPage)

            if (fetchCount < maxResultsToFetch){
                writer.append("\r\n----\r\n") // separate json records
                hasMoreRecords = nextPageToken
            } else {
                hasMoreRecords = false
            }
        }
        writer.flush()
    }



    /*
    *  Closure to fetch youtube videos given query.
    *  'nextPageToken' is value received in JSON response from youtube.
    * */
    def fetchSnippetPage = { String q, String nextPageToken ->
        if (!q) return

        // Construct query URL
        StringBuilder b = new StringBuilder(baseUrl)
                .append("&q=").append(URLEncoder.encode(q,"UTF-8"))
                .append("&key=").append(apiKey)
                .append("&maxResults=").append(maxPageSize)

        // If this is not a first call for the query, append nextPageToken value in query
        if (nextPageToken) b.append("&pageToken=").append(nextPageToken)

        println("Making call using URL " + b.toString())

        // Make API call and return raw content
        return new URL(b.toString()).text
    }



    // Test driver
    public static void main(String[] args) {
        String apiKey = "<apikey>"
        String youtubeUrl = "https://www.googleapis.com/youtube/v3/search?type=video&videoCaption=closedCaption&part=snippet"
        String outDir = "c://temp//"
        YoutubeDataDownloader d = new YoutubeDataDownloader(youtubeUrl, apiKey,50,50)

        def jsonData = d.fetchSnippetPage("nodejs tutorials", "")

        Writer writer1 = new StringWriter()
        def writer = d.saveSnippetToWriter("nodejs tutorials", writer1)
        println(writer1.toString())

        //d.saveSnippetToDir("guitar learn", new File(outDir))
        // println URLEncoder.encode("hell there","UTF-8")
    }

}


