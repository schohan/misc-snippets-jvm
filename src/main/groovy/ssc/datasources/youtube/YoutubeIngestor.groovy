package ssc.datasources.youtube

import io.vertx.core.eventbus.EventBus
import ssc.Configs
import ssc.datasources.youtube.helpers.YoutubeDataDownloader
import ssc.jobs.ScheduledTask
import ssc.utils.LogWrapper
import io.vertx.groovy.core.Vertx

/**
 * This is main Youtube Ingestor class. It is used to:
 * a. Download and store raw youtube content to temp storage
 * b. Push transformed/processed content to ElasticSerach storage
 *
 * Created by schohan on 5/6/2016.
 */
class YoutubeIngestor {
    /* Name of this source that will be used to store to fetch configurations etc. */
    static final String sourcename = "youtube"

    Configs configs         // Configs
    ScheduledTask downloadJob    // Content Downloader
    ScheduledTask indexerJob   // ElasticSerach indexer
    Vertx vertx

    public YoutubeIngestor() {
        vertx = Vertx.vertx()
        configs = Configs.getInstance()
        long downloadInterval = configs.property(sourcename + ".downloadIntervalMins").toLong()
        long indexerInterval = configs.property(sourcename + ".indexerIntervalMins").toLong()

        downloadInterval *= 60 * 1000
        indexerInterval *= 60 * 1000

        downloadJob = new ScheduledTask(sourcename + "Downloader", 0, downloadInterval, downloadClosure)
        indexerJob = new ScheduledTask(sourcename + "Indexer", 10000, indexerInterval, indexerClosure)
    }


    def downloadClosure = {
        LogWrapper.println("Youtube Download started at " + new Date())
        //downloadFromSource()

        // TODO publish message that new file has been processed
        def eb = vertx.eventBus()
        eb.send("download.yt.success","file downloaded at ${new Date()} location ", { ack ->
            if (ack.succeeded()) {
                println("Received reply: ${ack.result().body()}")
            } else {
                println("Message not sent? " + ack.failed())
            }

        })

        LogWrapper.println("Youtube Download finished at " + new Date())
    }


    def indexerClosure = {
        LogWrapper.println("Youtube Indexer started at " + new Date())
        //ingest()

        // TODO consume message that new file has been processed
        def eb = vertx.eventBus()
        eb.consumer("download.yt.success", { message ->
            println("Received message : ${message.body()}")
            message.reply("Received OK at " + new Date())
        })

        LogWrapper.println("Youtube indexer finished at " + new Date())
    }



    /* Download data from youtube */
    public void downloadFromSource() {
        LogWrapper.println("downloadfromsource method invoked")
        YoutubeDataDownloader d = new YoutubeDataDownloader(configs.property(sourcename + ".baseSearchUrl"),
                configs.property(sourcename + ".apiKey"),
                configs.property(sourcename + ".baseSearchUrl.resultsPerPage").toInteger(),
                configs.property(sourcename + ".baseSearchUrl.maxResultsToFetchPerQuery").toInteger())

        // Iterate over topics, download and fetch content and save it.

        // TODO Fetch the keywords to use for fetching content
        def jsonData = d.fetchSnippetPage("nodejs tutorials", "")
        println "Json Retrieved " + jsonData

        LogWrapper.println("downloadfromsource finished for ")
    }


    /* Ingest unprocessed data */
    public void ingest() {
        LogWrapper.println("Ingest method invoked")
    }



    /* test driver */
    public static void main(String[] args) {
        YoutubeIngestor y = new YoutubeIngestor()
        Thread.sleep(1000000)

    }

}
