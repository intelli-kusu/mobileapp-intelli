package com.intellicare.model.createvisitmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Allergy {
    @Expose()
    private int severitySelectedPosition = 0;
    @Expose()
    private boolean isSelected = false;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @SerializedName("allergy_id")
    @Expose
    private String allergyId;
    @SerializedName("allergy")
    @Expose
    private String allergy;
    @SerializedName("severity_id")
    @Expose
    private String severityId;
    @SerializedName("severity")
    @Expose
    private String severity;

    public String getAllergyId() {
        return allergyId;
    }

    public void setAllergyId(String allergyId) {
        this.allergyId = allergyId;
    }

    public String getAllergy() {
        return allergy;
    }

    public void setAllergy(String allergy) {
        this.allergy = allergy;
    }

    public String getSeverityId() {
        return severityId;
    }

    public void setSeverityId(String severityId) {
        this.severityId = severityId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getSeveritySelectedPosition() {
        return severitySelectedPosition;
    }

    public void setSeveritySelectedPosition(int severitySelectedPosition) {
        this.severitySelectedPosition = severitySelectedPosition;
    }

}