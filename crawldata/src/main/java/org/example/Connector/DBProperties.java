package org.example.Connector;

import java.io.IOException;
import java.util.Properties;

public class DBProperties {
    private static Properties propStaging = new Properties();

    static {
        try {
            propStaging.load(DBProperties.class.getClassLoader().getResourceAsStream("staging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    // Sử dụng propStaging cho staging environment
    public static String hostStaging = propStaging.getProperty("db.host");
    public static String portStaging = propStaging.getProperty("db.port");
    public static String usernameStaging = propStaging.getProperty("db.username");
    public static String passStaging = propStaging.getProperty("db.password");
    public static String dbnameStaging = propStaging.getProperty("db.name");

}
