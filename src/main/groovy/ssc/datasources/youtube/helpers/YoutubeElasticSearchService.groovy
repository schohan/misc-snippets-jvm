package ssc.datasources.youtube.helpers

import ssc.search.ESSearcher

/**
 * This class helps in managing youtube data with elasticsearch
 *
 * Created by schohan on 3/14/2016.
 */
class YoutubeElasticSearchService {
    String indexName = "youtube"
    String typeName = "searchsnippet"
    ESSearcher esSearcher



    /* Get Url of ElasticSearch server's HTTP endpoint */
    YoutubeElasticSearchService(String host, int port, String clusterName) {
        esSearcher = new ESSearcher(host,port,clusterName)

        // Create index and collection for Youtube
        esSearcher.createIndex(indexName)
    }



    /* Insert youtube search snippets from JSON files that are saved in the given root directory */
    void insertOrUpdate(String rootDir) {

        // Create a closure to handle each CSV file and invoke it. CSV file contain headers
        /*FileUtils.processDir(rootDir,{File file ->
            // TODO Find a external script to load CSV data to ElasticSearch in bulk
            List<ESDocument> docs = file.eachLine { firstLine, line ->
                if (line.contains("channelId"))
            }

            esSearcher.addDocs(indexName, typeName, )
        })*/
    }




    // Test Driver
    public static void main(String[] args) {
        YoutubeElasticSearchService es = new YoutubeElasticSearchService("http://localhost:9200")
    }

}
