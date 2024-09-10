package groovy.text
/**
 * Client to invoke Calais service.
 *
 */
public class CalaisClient {

    // Defaults
    private static String calaisUrl = "http://api.opencalais.com/tag/rs/enrich"
    private static String key = "qxzfbv4ydwfygabejwuhjeqs"

    private File inputDir = "textInDir"
    private File outputDir = "textOutDir"



    // headers expected by opencalais service
    def getHeader = {
        ["x-calais-licenseID":key,
         "Content-Type": "text/raw; charset=UTF-8",
         "enableMetadataType":"SocialTags",
         "Accept":"application/json"
        ]
    }

    // Read files from input directory and pass them through
    // processing pipeline
    def processInput = {


    }




}
