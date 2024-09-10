package ssc.datasources.khanacademy

import groovy.json.JsonSlurper
import ssc.utils.FileUtils
import ssc.utils.LogWrapper

/**
 * Process:
 * 1. Get topictree by downloading it from "http://www.khanacademy.org/api/v1/topictree" url
 * 2. Process topic tree and fetch topic details for each topic
 *
 * Created by schohan on 3/6/2016.
 */
class KhanAcademyDownloader {
    String rootDir
    String topicTreeFileName = "topictree.json"

    String topicTreeUrl = "http://www.khanacademy.org/api/v1/topictree"

    // Pass video id (end of url) retrieved from topictree call to grab video details
    String videoDetails = "http://www.khanacademy.org/api/v1/videos/t3MxJEbJN3k"

    // Pass 'slug' (end of the url) value retrieved from topictree call to grab topic details.
    String topicDetailsUrl = "http://www.khanacademy.org/api/v1/topic/"

    /* Constructor with root directory location. Everything under it would be relative to this root */
    public KhanAcademyDownloader(String rootDir) {
        this.rootDir = rootDir
    }

    /* Fetch and save Topic Tree in root directory location */
    File fetchTopicTree() {
        File monYrFile = FileUtils.createDirByDate(new File(rootDir))
        String topicTree = topicTreeUrl.toURL().text
        File topicTreeFile = new File(monYrFile, topicTreeFileName)
        FileUtils.writeTextToFile(topicTreeFile, topicTree)
        return monYrFile
    }

    /* Iterate through topic tree, extract topic and fetch details */
    void fetchAndSaveTopicDetails(File file) {
        def jsonData = new JsonSlurper().parseText(file.text)
        extractTopicFromChildren(jsonData.children)
    }


    def extractTopicFromChildren(children) {
        println "Children Size=" + children?.size()

        children.each { child ->

            String text = "Id=" + child.id +
                    ", youtube_id=" + child.youtube_id +
                    ", content_kind=" + child.content_kind+
                    ", description="+child.description+
                    ", creation_date=" + child.creation_date +
                    ", duration=" + child.duration +
                    ", ka_url=" + child.ka_url +
                    ", keywords=" + child.keywords +
                    ", translated_title=" + child.translated_title +
                    ", slug="+ child.slug+
                    ", title=" + child.title +
                    ", download_urls="+ child.download_urls+
                    ", thumbnail_urls=" + child.thumbnail_urls +
                    ", author_names=" + child.author_names +
                    ", image_url=" + child.image_url +
                    ", translated_youtube_lang=" + child.translated_youtube_lang +
                    ", kind=" + child.kind

            String fileName = child?.kind.toString() + "-" + child.slug + ".json"
            LogWrapper.log("Topic FileName =" + fileName)
            try {
                if ("topic".equals(child.content_kind.toLowerCase())) {
                    String topicDetails = (topicDetailsUrl + child.slug).toURL().text
                    //LogWrapper.log("topicDetails " + topicDetails)
                    File f = new File(FileUtils.createDirByDate(new File(rootDir)).getAbsolutePath(), fileName)
                    FileWriter fw = new FileWriter(f, true)

                    fw.append(text)
                    fw.append("\n")
                    fw.close()
                } else {
                    // Process Video content here
                }
            } catch (e) {
                LogWrapper.log(e.getMessage())
            }
            // If there are more children, make a recursive call
            if (child.children) {
                extractTopicFromChildren(child.children)
            }
        }
    }



    public static void main(String[] args) {
        KhanAcademyDownloader kd = new KhanAcademyDownloader("C:\\data\\khanacademy\\")
        //File f = kd.fetchTopicTree()
        kd.fetchAndSaveTopicDetails(new File("C:\\data\\khanacademy\\topictree.json"))


    }

}

