package org.example.model;

import org.example.Connector.DBLoader;

import java.time.LocalDate;

public class CrawlLog extends ALogs{

    public CrawlLog(int count, String status, String errorMessage, String createBy, LocalDate dateUpdate, LocalDate dateGetData) {
        super(count, status, errorMessage, createBy, dateUpdate, dateGetData);
    }

    public CrawlLog(String status, String errorMessage, String createBy, LocalDate dateUpdate, LocalDate dateGetData) {
        super(status, errorMessage, createBy, dateUpdate, dateGetData);
    }
}
