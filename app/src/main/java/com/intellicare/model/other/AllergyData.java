package com.intellicare.model.other;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AllergyData {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("allergies")
    private List<Allergy> allergyList;

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

    public List<Allergy> getAllergyList() {
        return allergyList;
    }

    public void setAllergyList(List<Allergy> allergyList) {
        this.allergyList = allergyList;
    }

    public static class Allergy {
        @SerializedName("allergy_id")
        private String id;
        @SerializedName("allergy")
        private String allergyName;

        private int severitySelectionId;
        private String severity;
        private boolean isChecked;

        public Allergy(String id, String allergyName, boolean isChecked) {
            this.id = id;
            this.allergyName = allergyName;
            this.isChecked = isChecked;
        }

        public int getSeveritySelectionId() {
            return severitySelectionId;
        }

        public void setSeveritySelectionId(int severitySelectionId) {
            this.severitySelectionId = severitySelectionId;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAllergyName() {
            return allergyName;
        }

        public void setAllergyName(String allergyName) {
            this.allergyName = allergyName;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }
    }
}
