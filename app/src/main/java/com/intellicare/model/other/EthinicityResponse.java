package com.intellicare.model.other;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EthinicityResponse {
    @SerializedName("ethnicity")
    private List<String> ethnicity = null;
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    public List<String> getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(List<String> ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}