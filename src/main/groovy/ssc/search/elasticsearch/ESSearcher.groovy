package ssc.search

import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.JestResult
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.Bulk
import io.searchbox.core.Delete
import io.searchbox.core.Get
import io.searchbox.core.Index
import io.searchbox.core.Search
import io.searchbox.core.SearchResult
import io.searchbox.indices.CreateIndex
import ssc.datasources.youtube.helpers.YoutubeSRSnippet
import ssc.utils.LogWrapper
import ssc.search.elasticsearch.ESDocument

/**
 * Class that leverages Elastic Search to index and search documents
 *
 * Created by schohan on 11/13/2015.
 */
class ESSearcher {
    private String httpUrl
    private String clusterName
    private JestClient client
    LogWrapper log


    /* Only constructor */
    public ESSearcher(String host, int port, String clusterName) {
        this.httpUrl = "http://" + host + ":" + port
        this.clusterName = clusterName
        log = new LogWrapper(true)

        JestClientFactory factory = new JestClientFactory();
         factory.setHttpClientConfig(new HttpClientConfig
                                .Builder(httpUrl)
                                .multiThreaded(true)
                                .build());
         client = factory.getObject();

    }


    /* Create specified index. If it already exists, exception is ignored */
    public void createIndex(String... index) {
        if (index) index.each {
            JestResult result = client.execute(new CreateIndex.Builder(it).build());
            LogWrapper.log("CreateIndex Response=" + result.getResponseCode())

            // If index already exists, ignore it. Otherwise, throw an exception
            if (result.getResponseCode() != 200 && !("index_already_exists_exception".equals(result.getJsonObject().get("error").get("type")).toString()))
                    throw new Exception("Could not create index " + result.getJsonString())
        }
    }

    /* Add mapping for a given collection */
    /*public void putMapping(String index, String colType, String mappingJson) {
        PutMapping putMapping = new PutMapping.Builder(
                index,
                colType,
                mappingJson
        ).build();
        JestResult result = client.execute(putMapping).getJsonString()

        if (result.getResponseCode() != 200) throw new Exception("Could not put mapping " + result.getJsonString())

    }*/


    //- ----------------------- Add Operations ----------------------------
    /* Add docs. Returns True if document has been added, false otherwise. NOTE: It doesn't throw exception. */
    public boolean addDoc(String indexName, String typeName, ESDocument doc) {
        Index index = new Index.Builder(doc).index(indexName).type(typeName).id(doc.getId()).build();
        JestResult result = client.execute(index);
        if (result.getResponseCode() == 200) {
            return true
        } else {
            // Write exception as debug statement
            LogWrapper.log(result.getJsonString())
            return false
        }
    }

    /* Add documents in bulk operation */
    public void addDocs(String indexName, String typeName, List<ESDocument> docs) {
        bulkOperation(indexName, typeName, docs, { doc ->
                            return new Index.Builder(doc).build()
                        })
    }




    // -------------------- Search Operations ------------------
    /* Given an id, returns an ESDocument object back. */
    public Object getDoc(String indexName, String typeName, String id, Class clazz) {
        println("index=" + indexName + ", type=" + typeName + ", id=" + id + ", clazz=" + clazz.getName())
        Get get = new Get.Builder(indexName, id).type(typeName).build()
        JestResult result = client.execute(get)

        return result.getSourceAsObject(clazz)
    }


    /* Search documents using query string */
    List<Object> search(String index, String typeName, String queryStr, List<String> fields) {
        //TODO sanitize the query string

        String template = """{
                            "query": {
                                "multi_match": {
                                   "query": "${queryStr}",
                                   "fields": ${fields.collect{'"'+it+'"'}}
                                }
                              }
                            }"""

        println(template.toString())

        Search search = new Search.Builder(template.toString())
        // multiple index or types can be added.
                .addIndex(index)
                .addType(typeName)
                .build();

        SearchResult result = client.execute(search)

        if (result.getResponseCode() >= 400)
            throw new Exception("Illegal Query. Response Code= " + result.getResponseCode())

        if (result.getTotal() > 0)
            LogWrapper.log("Search Results " + result.getHits(YoutubeSRSnippet.class))
        else
            LogWrapper.log("No result found")

        return result.getSourceAsObjectList(YoutubeSRSnippet.class)
    }



    // ----------------- Delete Operations ----------------------

    /* Delete a document */
    public String deleteDoc(String indexName, String typeName, String id) {
        return client.execute(new Delete.Builder(id)
                        .index(indexName)
                        .type(typeName)
                        .build()).getJsonString()
    }

    /* Delete documents in bulk operation */
    public void deleteDocs(String indexName, String typeName, List<ESDocument> docs) {
        bulkOperation(indexName, typeName, docs, { doc ->
                            return new Delete.Builder(doc).build()
                        })
    }

    /* Delete specified index */
    public boolean deleteIndex(String... index) {
        if (index) index.each {
            client.execute(new CreateIndex.Builder(it).build());
        }
    }

    // ---------------------- Private methods -----------------
    private void bulkOperation(String indexName, String typeName, List<ESDocument> docs, Closure operation) {
           Bulk.Builder builder = new Bulk.Builder()
           builder.defaultIndex(indexName)
           builder.defaultType(typeName)

           docs.each { doc ->
               //builder.addAction(new Index.Builder(doc).build())
               builder.addAction(operation(doc))
           }
           client.execute(builder.build());
       }





    // Test Driver
    public static void main(String[] args) {
        ESSearcher es = new ESSearcher("localhost",9200,"elasticsearch")

        // Create some indexes
      /*  es.createIndex("videos")

        // Add using bulk
        es.addDocs("videos","youtube", [
                YoutubeSRSnippet.sampleDoc(),
                YoutubeSRSnippet.sampleDoc(),
                YoutubeSRSnippet.sampleDoc()
        ])*/

        // Get a document
      /*  YoutubeSRSnippet snippet = es.getDoc("videos", "youtube","vid1", YoutubeSRSnippet.class)
        LogWrapper.log("Snippet =" + snippet)
*/

        // Search index
        List<YoutubeSRSnippet> snippets = es.search("videos","youtube","vid1",["videoId","title"])
        snippets.each { YoutubeSRSnippet y ->
            println ("snippet=" + y.toString())
        }
        /*def a = ["a","b"]
        String n = a.collect{'"'+it+'"'}
        println n
*/
    }

}

