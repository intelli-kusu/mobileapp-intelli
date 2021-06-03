package com.intellicare.model.patientinfonew;

import com.google.gson.annotations.SerializedName;

public class Relation {
    @SerializedName("patient_id")
    private String patientId;
    @SerializedName("relation")
    private String relation;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}