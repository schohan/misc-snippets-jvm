package groovy.text

/**
 * Extract emails from MBox (dump of emails from Gmail account) text file.
 * Writes the extracted email address and company name to a CSV file
 *
 * Created by shailender on 11/28/2014.
 */
class MboxExtractor {

    def extract(filein, fileout) {
        File inp = new File(filein)
        File out = new File(fileout)

        String from, replyTo, subject=null

        // Read file
        inp.eachLine { line ->

            if (line.startsWith("From:")) {
                from = line.subSequence(5, line.length())
            }
            if (line.startsWith("Reply-To:")) {
                replyTo = line.substring(9, line.length())
            }
            if (line.startsWith("Subject:")) {
                subject = line.substring(8, line.length())
                println("${from},${replyTo}, ${subject}\n")
            }

            if (subject != null) {
                println("writing to file")
                out.append("${from}|${replyTo}|${subject}\n")
                out.append(System.lineSeparator())
                subject=null
            }
        }


    }


    public static void main(String[] args) {
        MboxExtractor m = new MboxExtractor()
        m.extract ("data/jobs.mbox","out/emails.csv")
    }


}
