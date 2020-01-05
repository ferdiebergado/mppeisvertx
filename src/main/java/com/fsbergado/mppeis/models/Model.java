package com.fsbergado.mppeis.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model
 */
public abstract class Model {

    @JsonProperty("created_at")
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm z")
    protected String createdAt;

    @JsonProperty("updated_at")
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm z")
    protected String updatedAt;

    @JsonProperty("deleted_at")
    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm z")
    protected String deletedAt;

    public Model() {}

    public String getCreatedAt() {
        return createdAt;        
    }

    public void setCreatedAt(String date) {
        this.createdAt = date;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setUpdatedAt(String timestamp) {
        this.updatedAt = timestamp;
    }

    public void setDeletedAt(String timestamp) {
        this.deletedAt = timestamp;
    }
}