package ssc.datasources.youtube.helpers
/**
 *
 *
 * Created by schohan on 3/6/2016.
 */
class YoutubeDataDownloaderTest {
    String apiKey = "<apikey>"
    String youtubeUrl = "https://www.googleapis.com/youtube/v3/search?type=video&videoCaption=closedCaption&part=snippet"


    def testFetchYoutubeVideos() {
        YoutubeDataDownloader d = new YoutubeDataDownloader(youtubeUrl, apiKey, 1,1)
        String jsonData = d.fetchSnippetPage("nodejs tutorials", "")
        assert jsonData != null && jsonData.contains("items")

        Writer writer1 = new StringWriter()
        def writer = d.saveSnippetToWriter("nodejs tutorials", writer1)
        //println(writer1.toString())
        assert writer1.toString().contains("items")

    }


    // Test driver
    public static void main(String[] args) {
       YoutubeDataDownloaderTest yt = new YoutubeDataDownloaderTest()
        yt.testFetchYoutubeVideos()
    }
}
