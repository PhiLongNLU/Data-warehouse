package org.example.Connector;

import org.example.model.ConfigData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBLoader {
    protected String port, database, host, configID;

    public static final DBLoader instance = new DBLoader();

    private DBLoader() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            Document document = builder.parse(new FileInputStream("src/main/resources/config.xml"));

            Element root = document.getDocumentElement();

            this.port = root.getElementsByTagName("port").item(0).getTextContent();
            this.configID = root.getElementsByTagName("config-id").item(0).getTextContent();
            this.host = root.getElementsByTagName("host-name").item(0).getTextContent();
            this.database = root.getElementsByTagName("db-name").item(0).getTextContent();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static DBLoader getInstance() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        String user = "root";
        String password = "";
        return DriverManager.getConnection(url, user, password);
    }

    public List<ConfigData> getConfigData(){
        List<ConfigData> configDatas = new ArrayList<>();
        try {
            Connection con = null;
            con = getConnection();

            CallableStatement cs = con.prepareCall("{CALL get_config_data(?)}");
            cs.setInt(1,1);
            ResultSet rs2 = cs.executeQuery();

            while (rs2.next()){
                String sourceFile = rs2.getString("source_file");
                String url = rs2.getString("url");
                configDatas.add(new ConfigData(url, sourceFile));
            }
            con.close();

        } catch (Exception e) {

        }

        return configDatas;
    }
}
