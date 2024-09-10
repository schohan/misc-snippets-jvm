package ssc.datasources.youtube.helpers

import ssc.search.elasticsearch.ESDocument

/**
 * This class represents a youtube search results snippet extracted from raw JSON
 * Following fields are extracted:
 *
 *
 etag
 id.videoId
 snippet.publishedAt
 snippet.channelId
 snippet.title
 snippet.description
 snippet.thumbnails.default.url
 snippet.thumbnails.medium.url
 snippet.channelTitle
 snippet.liveBroadcastContent

 * Created by schohan on 3/13/2016.
 */
final class YoutubeSRSnippet implements ESDocument{
    String etag, videoId, publishedAt,channelId,
           title,description,thumbnailDefUrl,thumbnailMedUrl,
           channelTitle,liveBroadcastContent

    @Override
    String getId() {
        return videoId
    }

    /* Returns a header for Csv file */
    String header(String separator = "|") {
        return getKeyOrVal(true,separator)
    }

    /* Returns this object as a CSV string using separator argument */
    String toCsv(String separator = "|") {
        return getKeyOrVal(false, separator)
    }

    // Private method that extract key or value from current class
    private String getKeyOrVal(boolean headerOnly, String separator) {
        StringBuilder sb = new StringBuilder()
        this.properties.each { k, v ->
           // println("key=" + k + "=>" + v)
            if (k in ["metaClass","class"]) return
            sb.append(headerOnly?k: (v?:"")).append(separator) // Add property value. Replace null with empty string
        }
        sb.delete(sb.lastIndexOf(separator),sb.lastIndexOf(separator)+1) // remove redundant last separator
        return sb.toString()
    }


    @Override
    public String toString() {
        return "YoutubeSRSnippet{" +
                "etag='" + etag + '\'' +
                ", videoId='" + videoId + '\'' +
                ", publishedAt='" + publishedAt + '\'' +
                ", channelId='" + channelId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", thumbnailDefUrl='" + thumbnailDefUrl + '\'' +
                ", thumbnailMedUrl='" + thumbnailMedUrl + '\'' +
                ", channelTitle='" + channelTitle + '\'' +
                ", liveBroadcastContent='" + liveBroadcastContent + '\'' +
                '}';
    }

    def static YoutubeSRSnippet sampleDoc() {
        Date d = new Date()
        return new YoutubeSRSnippet(etag:"etag" + d.time, videoId:"vid"+Random.newInstance().nextInt(10), publishedAt:d.toString(),
                channelId:"chid" + d.time,title:"title " + d.getTimeString(),description:"description " + d.time,
                thumbnailDefUrl:"",thumbnailMedUrl:"",channelTitle:"channel title" + d.time,liveBroadcastContent:"live broad cast content" + d.time)
    }

    // Test Driver
    public static void main(String[] args) {
        YoutubeSRSnippet yt = new YoutubeSRSnippet(etag: "etag1",title: "some title")

        // Get header
        println("Headers=" + yt.header())

        // Get CSV values
        println("Value=" + yt.toCsv())

        (1..20).each{
            println("number= " + Random.newInstance().nextInt(5))
        }

    }


}
