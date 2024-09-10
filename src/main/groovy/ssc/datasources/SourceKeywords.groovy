package ssc.datasources

/**
 * Source of keywords for all topics. This class loads requested topic and provide it as stream or string
 *
 * Created by schohan on 5/15/2016.
 */
class SourceKeywords {
    String topicDir = "sources.topics"
    String fname
    FileInputStream fstream

    public SourceKeywords(String sourceName) {
        fname = sourceName
    }


    public InputStream stream() {
        fstream = new FileInputStream(fname)
        return fstream
    }

    public void close() {
        fstream.close()
    }

    // Test driver
    public static final main(String...args) {

    }
}
