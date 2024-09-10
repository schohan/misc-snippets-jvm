package ssc.search.elasticsearch

import ssc.search.ESSearcher

/**
 * Test class
 * Created by schohan on 3/20/2016.
 */
class ESSearcherTest {
    ESSearcher es = new ESSearcher("localhost",9200,"elasticsearch")


    void testIndexCreationAndDocInserts() {
        // Create some indexes
        es.createIndex("test1","test2","test3")

        // Add few collections/models
        es.addDoc("test1", "locations", new HashMap(name:"reston",lat:1234.333, lng: 1244.444))
        es.addDoc("test2","apps", new HashMap(appName: "app1", updatedAt: new Date(), active: true))
        es.addDoc("test3","apps", new HashMap(appName: "app2", updatedAt: new Date(), active: true))


        Map location = es.getDoc("test1", "locations","reston", HashMap.class)
        print("Location " + location)
        assert (location.name == "reston")

        Map app1= es.getDoc("test2", "apps","app1", HashMap.class)
        assertTrue(app1.appName == "app1")

        Map app2= es.getDoc("test3", "apps","app2", HashMap.class)
        assertTrue(app2.appName== "app2")
    }

    void testBulkOperations() {
/*
        // Add using bulk
        es.addDocs("test1","apps", [
                new ESApp(appName: "bulkapp1", updatedAt: new Date(), active: true),
                new ESApp(appName: "bulkapp2", updatedAt: new Date(), active: false),
                new ESApp(appName: "bulkapp3", updatedAt: new Date(), active: true)
        ])




        // Get a document
        ESLocation location = es.getDoc("test1", "locations","reston", ESLocation.class)
        LogWrapper.log("Location=" + location)

        // Delete a document
        LogWrapper.log es.deleteDoc("yt", "search", "AVN_5CKhiCRUvWt3P-kW")

        *//*

        // Delete using bulk
        */
/* es.addDocs("test1","apps", [
                 new ESApp(appName: "bulkapp1", updatedAt: new Date(), active: true),
                 new ESApp(appName: "bulkapp1", updatedAt: new Date(), active: false),
                 new ESApp(appName: "bulkapp3", updatedAt: new Date(), active: true)
         ])*//*

        // Search index




        // Add mappings for a collection
        */
/*String mappingJson = """{
                    'document':{
                            'properties': {
                                'name': {'type':'string', 'store':'yes'}
                            }
                        }
                    } """

        LogWrapper.log("Mapping Added. Response=" + es.putMapping("test1","person",mappingJson))*/

    }

}
