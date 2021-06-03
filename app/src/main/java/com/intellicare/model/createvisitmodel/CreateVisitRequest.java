package com.intellicare.model.createvisitmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreateVisitRequest {
    @SerializedName("patient_id")
    @Expose
    private String patientId;
    @SerializedName("complaint_id")
    @Expose
    private String complaintId;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("complaint_others")
    @Expose
    private String complaintOthers = "";
    @SerializedName("allergies")
    @Expose
    private List<Allergy> allergies = null;
    @SerializedName("pharmacy_id")
    @Expose
    private String pharmacyId;
    @SerializedName("patient_image")
    @Expose
    private String patientImage;
    @SerializedName("bp")
    @Expose
    private String bp;
    @SerializedName("temperature")
    @Expose
    private String temperature;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getComplaintOthers() {
        return complaintOthers;
    }

    public void setComplaintOthers(String complaintOthers) {
        this.complaintOthers = complaintOthers;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getPatientImage() {
        return patientImage;
    }

    public void setPatientImage(String patientImage) {
        this.patientImage = patientImage;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

}