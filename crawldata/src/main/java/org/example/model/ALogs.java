package org.example.model;

import org.example.Connector.DBLoader;

import java.time.LocalDate;

public abstract class ALogs {
    protected int configId;
    protected int count;
    protected LocalDate dateUpdate, dateGetData;
    protected String status, errorMessage, createBy;

    public ALogs(int count,String status, String errorMessage, String createBy, LocalDate dateUpdate, LocalDate dateGetData){
        this.configId = Integer.parseInt(DBLoader.instance.configID);
        this.count = count;
        this.status = status;
        this.errorMessage = errorMessage;
        this.dateUpdate = dateUpdate;
        this.dateGetData = dateGetData;
        this.createBy = createBy;
    }

    public int getConfigId() {
        return configId;
    }

    public int getCount() {
        return count;
    }

    public LocalDate getDateUpdate() {
        return dateUpdate;
    }

    public LocalDate getDateGetData() {
        return dateGetData;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getCreateBy() {
        return createBy;
    }
}
