package org.example.model;

import org.example.Connector.DBLoader;

import java.time.LocalDate;

public abstract class ALogs {
    protected int configId;
    protected int count;
    protected LocalDate dateUpdate, dateGetData;
    protected String status, errorMessage, createBy;
    protected static final int DEFAULT_COUNT = 0;

    public ALogs(int count,String status, String errorMessage, String createBy, LocalDate dateUpdate, LocalDate dateGetData){
        this.configId = Integer.parseInt(DBLoader.instance.configID);
        this.count = count;
        this.status = status;
        this.errorMessage = errorMessage;
        this.dateUpdate = dateUpdate;
        this.dateGetData = dateGetData;
        this.createBy = createBy;
    }

    public ALogs(String status, String errorMessage, String createBy, LocalDate dateUpdate, LocalDate dateGetData){
        this.configId = Integer.parseInt(DBLoader.instance.configID);
        this.count = DEFAULT_COUNT;
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

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setDateUpdate(LocalDate dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public void setDateGetData(LocalDate dateGetData) {
        this.dateGetData = dateGetData;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }
}
