package org.example.Connector;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.jdbi.v3.core.Jdbi;

import java.sql.SQLException;

public class JDBIConnector {
    private static Jdbi jdbiStaging;
    private static Jdbi jdbiControl;

    private static void connectStaging() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL("jdbc:mysql://" + DBProperties.hostStaging + ":" + DBProperties.portStaging + "/" + DBProperties.dbnameStaging);
        dataSource.setUser(DBProperties.usernameStaging);
        dataSource.setPassword(DBProperties.passStaging);
        try {
            dataSource.setAutoReconnect(true);
            dataSource.setUseCompression(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        jdbiStaging = Jdbi.create(dataSource);
    }

    private static void connectControl() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL("jdbc:mysql://" + DBProperties.hostStaging + ":" + DBProperties.portStaging + "/" + DBProperties.dbnameStaging);
        dataSource.setUser(DBProperties.usernameStaging);
        dataSource.setPassword(DBProperties.passStaging);
        try {
            dataSource.setAutoReconnect(true);
            dataSource.setUseCompression(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        jdbiControl = Jdbi.create(dataSource);
    }

    public static Jdbi getStagingJdbi() {
        if (jdbiStaging == null) {
            connectStaging();
        }
        return jdbiStaging;
    }

    public static Jdbi getControlJdbi() {
        if (jdbiControl == null) {
            connectControl();
        }
        return jdbiControl;
    }

    public static void main(String[] args) {
       connectStaging();
       connectControl();
    }

}

