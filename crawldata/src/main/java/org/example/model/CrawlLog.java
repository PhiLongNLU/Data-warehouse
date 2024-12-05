package org.example.model;

import org.example.Connector.DBLoader;

import java.time.LocalDate;

public class CrawlLog extends ALogs{

    public CrawlLog(int count, String status, String errorMessage, LocalDate dateUpdate, LocalDate dateGetData) {
        super(count, status, errorMessage, dateUpdate, dateGetData);
    }
}
