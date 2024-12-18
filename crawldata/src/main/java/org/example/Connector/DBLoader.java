package org.example.Connector;

import org.example.assets.CrawlProcessStatus;
import org.example.model.ALogs;
import org.example.model.ConfigData;
import org.example.model.CrawlData;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBLoader {
    protected String port, database, host, username,password;
    public String configID;

    public static final DBLoader instance = new DBLoader();

    private DBLoader() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            port = properties.getProperty("db.port");
            database = properties.getProperty("db.name");
            host = properties.getProperty("db.host");
            configID = properties.getProperty("config_id");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
        } catch (Exception e) {
            System.out.println("Exception at line 26 in DBLoader : " + e.getMessage());
        }
    }

    public static DBLoader getInstance() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        return DriverManager.getConnection(url, username, password);
    }

    public CrawlData getDateCrawlData(){
        try{
            Connection connection = getConnection();

            var ps = connection.prepareStatement("SELECT date_get_data, status FROM logs WHERE date_update = ?");
            ps.setDate(1, Date.valueOf(LocalDate.now()));

            var resultSet = ps.executeQuery();
            if(resultSet.next()){
                 return new CrawlData(resultSet.getDate("date_get_data").toLocalDate(), resultSet.getString("status"));
            }
        } catch (SQLException e) {
            return null;
        }

        return null;
    }

    public void insertLog(ALogs log){
        try{
            var conn = getConnection();
            var ps = conn.prepareStatement("INSERT INTO logs (configs_id, count, status, date_update, date_get_data, error_message, create_by) VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1, Integer.parseInt(configID));
            ps.setInt(2, log.getCount());
            ps.setString(3, log.getStatus());
            ps.setDate(4, Date.valueOf(log.getDateUpdate()));
            ps.setDate(5, Date.valueOf(log.getDateGetData()));
            ps.setString(6, log.getErrorMessage());
            ps.setString(7, log.getCreateBy());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
//        System.out.println(DBLoader.getInstance().getDateCrawlData());
//    }
}
