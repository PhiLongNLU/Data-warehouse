package org.example.Connector;

import org.example.model.ConfigData;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBLoader {
    protected String port, database, host, configID;

    public static final DBLoader instance = new DBLoader();

    private DBLoader() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            port = properties.getProperty("port");
            database = properties.getProperty("db_name");
            host = properties.getProperty("host_name");
            configID = properties.getProperty("config_id");
        } catch (Exception e) {
            System.out.println("Exception at line 26 in DBLoader : " + e.getMessage());
        }
    }

    public static DBLoader getInstance() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        System.out.println(url);
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    public List<ConfigData> getConfigData() {
        List<ConfigData> configDatas = new ArrayList<>();
        try {
            Connection con = getConnection();

            CallableStatement cs = con.prepareCall("{CALL get_config_data(?)}");
            cs.setInt(1, Integer.parseInt(configID));
            ResultSet rs2 = cs.executeQuery();

            while (rs2.next()) {
                String sourceFile = rs2.getString("source_file");
                String url = rs2.getString("url");
                configDatas.add(new ConfigData(url, sourceFile));
            }
            con.close();

        } catch (Exception e) {
            System.out.println("error at line 57 DBLoader: " + e.getMessage());
        }

        return configDatas;
    }

    public String getFilePath(){
        try{
            Connection connection = getConnection();
            var ps = connection.prepareStatement("SELECT source_file FROM configs WHERE id = ?");
            ps.setInt(1, Integer.parseInt(configID));
            ResultSet resultSet = ps.executeQuery();

            if(resultSet.next()){
                return resultSet.getString("source_file");
            }
        } catch (SQLException e) {
            System.out.println("error at DbLoader line 74 : " + e.getMessage());
        }

        return "";
    }

//    public static void main(String[] args) throws SQLException {
////        var connection = DBLoader.getInstance().getConnection();
//        System.out.println(DBLoader.getInstance().getFilePath());
//    }
}
