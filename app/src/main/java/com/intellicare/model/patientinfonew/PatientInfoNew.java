package com.intellicare.model.patientinfonew;

import com.google.gson.annotations.SerializedName;
import com.intellicare.model.patientinfo.Patient;

public class PatientInfoNew {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("patient")
    private Patient patient;

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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}