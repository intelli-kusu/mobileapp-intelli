package com.intellicare.model.other;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SeverityData {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("severities")
    private List<Severity> severityList;

    public List<Severity> getSeverityList() {
        return severityList;
    }

    public void setSeverityList(List<Severity> severityList) {
        this.severityList = severityList;
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

    public static class Severity {
        @SerializedName("severity_id")
        private String id;
        @SerializedName("severity")
        private String severityName;
        private boolean isChecked;

        public Severity(String id, String severityName, boolean isChecked) {
            this.id = id;
            this.severityName = severityName;
            this.isChecked = isChecked;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSeverityName() {
            return severityName;
        }

        public void setSeverityName(String severityName) {
            this.severityName = severityName;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
