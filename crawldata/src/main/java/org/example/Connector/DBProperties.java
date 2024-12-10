package org.example.Connector;

import java.io.IOException;
import java.util.Properties;

public class DBProperties {
    private static Properties propStaging = new Properties();
    private static Properties propDataWarehouse = new Properties();
    private static Properties propControl = new Properties();

    static {
        try {
            propStaging.load(DBProperties.class.getClassLoader().getResourceAsStream("staging.properties"));
            propDataWarehouse.load(DBProperties.class.getClassLoader().getResourceAsStream("data_warehouse.properties"));
            propControl.load(DBProperties.class.getClassLoader().getResourceAsStream("config.properties"));
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

    // Sử dụng propStaging cho DataWarehouse environment
    public static String hostData = propDataWarehouse.getProperty("db.host");
    public static String portData = propDataWarehouse.getProperty("db.port");
    public static String usernameData = propDataWarehouse.getProperty("db.username");
    public static String passData = propDataWarehouse.getProperty("db.password");
    public static String dbnameData = propDataWarehouse.getProperty("db.name");

    // Sử dụng propControl cho control enviroment
    public static String hostControl = propControl.getProperty("db.host");
    public static String portControl = propControl.getProperty("db.port");
    public static String usernameControl = propControl.getProperty("db.username");
    public static String passControl = propControl.getProperty("db.password");
    public static String dbnameControl = propControl.getProperty("db.name");
}
