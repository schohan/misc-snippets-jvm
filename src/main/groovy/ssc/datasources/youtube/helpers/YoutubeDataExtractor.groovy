package ssc.datasources.youtube.helpers

import groovy.json.JsonSlurper
import ssc.utils.FileUtils
/**
 *
 * This class extract data from youtube json files and write it to specified stream.
 * These streams contain transformed data that is ready to be stored to ElasticSearch
 *
 * Extracted information is:
 * a. Title
 * b. Description
 * c. Source
 *
 *
 * Created by schohan on 3/13/2016.
 */
class YoutubeDataExtractor {

    private String rootDirLoc
    private String saveExtractedDataLoc

    private String searchListRespConst = "youtube#searchListResponse"



    /*
    * Extract search result item into YoutubeSRSnippet and return it
    **/
    Closure<List<YoutubeSRSnippet>> extractFromSearchResult = { def respJson ->
        List<YoutubeSRSnippet> snippets = []

        // Iterate through all items and extract snippet objects
        respJson.items.each { jsonObj ->
            snippets << new YoutubeSRSnippet(
                    etag: jsonObj.etag,
                    videoId: jsonObj.id.videoId,
                    publishedAt: jsonObj.snippet.publishedAt,
                    channelId: jsonObj.snippet.channelId,
                    title: jsonObj.snippet.title,
                    description: jsonObj.snippet.description,
                    thumbnailDefUrl: jsonObj.snippet.thumbnails.default.url,
                    thumbnailMedUrl: jsonObj.snippet.thumbnails.medium.url,
                    channelTitle: jsonObj.snippet.channelTitle,
                    liveBroadcastContent: jsonObj.snippet.liveBroadcastContent
            )
        }
        return snippets
    }


    /*
     * Common Youtube Response processor that would extract useful common attributes
     * from individual items like SearchResults, Playlist etc.
     * Processed data is written under 'processed' directory under input files parent directory.
     *
     * @ytRespFile File containing raw Json search result response
     **/
    def youtubeRespProcessor = { File ytRespFile ->
        println("Input File " + ytRespFile.getAbsolutePath())

        // Create 'processed' directory under ytRespFile's parent directory
        if (!ytRespFile) throw IllegalArgumentException("Input to this closure has to be of File type")
        String processedDirLoc = ytRespFile.getParent() + "/processed"
        File processedDir = new File(processedDirLoc)
        if (!processedDir.exists()) processedDir.mkdir()

        // if file has .json extension, load it using slurper. otherwise ignore this file.
        if (ytRespFile.getName().endsWith(".json")) {
            println("Processing JSON file ")
            String text = ytRespFile.text
            def json = new JsonSlurper().parseText(text)

            // Extract Fields as key/value pairs
            if (json.kind == searchListRespConst) {
                println("Processing Search Response for Json Object " + json)
                List<YoutubeSRSnippet> snippets = extractFromSearchResult(json)
                println("Snippet == " + snippets)

                // write output to processed directory
                FileWriter fo = new FileWriter(processedDirLoc + "/" + ytRespFile.getName() + ".csv")
                fo.println(new YoutubeSRSnippet().header())
                snippets.each { YoutubeSRSnippet snippet ->
                    fo.println(snippet.toCsv())
                }
                fo.close()
            }

        } else {
            // Ignore this file
            println("Ignore this non-json file " )
        }

    }


    /*
     * Extract data from all files inside root directory
     * Save extracted data in CSV format in outFile
     **/
    public void extract(File inpFileDir, File outFile) {
        if (!inpFileDir || !inpFileDir.isDirectory())
            throw IllegalArgumentException("Input File Directory cannnot be empty")

        // Recurse through files and allow youtubeRespProcessor closure process the files
        FileUtils.processDir(inpFileDir, youtubeRespProcessor)

        println "File Processed"
    }




    /* Class to extract data and save it */
    public static void main(String[] args) {
        YoutubeDataExtractor yt = new YoutubeDataExtractor()

        // Search Response
        yt.youtubeRespProcessor(new File("c:/data/youtube/youtube-playlist.json"))


    }
}


