package ssc

import ssc.utils.FileUtils

import java.io.*

/**
 * Class to get application properties that might be stored in a file and/or in a database table
 *
 * Created by schohan on 5/6/2016.
 */
public class Configs {
    private static Configs configs = null
    private static Properties defaultProps = new Properties();

    /* Load config from application.properties file */
    private Configs() {
        try {
            defaultProps.load(this.class.classLoader.getResourceAsStream("application.properties"))
            print("Properties loaded")
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Get instance of Configs object */
    synchronized public static Configs getInstance() {
        if (configs == null) {
            configs = new Configs()
        }
        return configs
    }

    public String property(String key) {
        return defaultProps.getProperty(key);
    }

    public static void main(String[] args) {
        Configs c = new Configs()
        println "Property AWSAccessKeyId = " + c.property("AWSAccessKeyId")
    }
}