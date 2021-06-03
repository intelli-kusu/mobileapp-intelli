package com.intellicare.model.pastvisits;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Consult {

    @SerializedName("consult_id")
    @Expose
    private String consultId;
    @SerializedName("complaint")
    @Expose
    private String complaint;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("assigned_at")
    @Expose
    private String assignedAt;
    @SerializedName("waiting_time_in_secs")
    @Expose
    private Integer waitingTimeInSecs;
    @SerializedName("cancelled_at")
    @Expose
    private String cancelledAt;
    @SerializedName("session_started_at")
    @Expose
    private String sessionStartedAt;
    @SerializedName("session_ended_at")
    @Expose
    private String sessionEndedAt;
    @SerializedName("session_duration_in_secs")
    @Expose
    private Integer sessionDurationInSecs;
    @SerializedName("consult_patient_id")
    @Expose
    private Integer consultPatientId;
    @SerializedName("consult_first_name")
    @Expose
    private String consultFirstName;
    @SerializedName("consult_last_name")
    @Expose
    private String consultLastName;
    @SerializedName("relationship")
    @Expose
    private String relationship;
    @SerializedName("bp")
    @Expose
    private String bp;
    @SerializedName("temperature")
    @Expose
    private String temperature;
    @SerializedName("consult_image")
    @Expose
    private String consultImage;
    @SerializedName("notes")
    @Expose
    private String notes;
    @SerializedName("clinician_name")
    @Expose
    private String clinicianName;

    // State of the item
    private boolean expanded;

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }

    public String getConsultId() {
        return consultId;
    }

    public void setConsultId(String consultId) {
        this.consultId = consultId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(String assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Integer getWaitingTimeInSecs() {
        return waitingTimeInSecs;
    }

    public void setWaitingTimeInSecs(Integer waitingTimeInSecs) {
        this.waitingTimeInSecs = waitingTimeInSecs;
    }

    public String getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(String cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getSessionStartedAt() {
        return sessionStartedAt;
    }

    public void setSessionStartedAt(String sessionStartedAt) {
        this.sessionStartedAt = sessionStartedAt;
    }

    public String getSessionEndedAt() {
        return sessionEndedAt;
    }

    public void setSessionEndedAt(String sessionEndedAt) {
        this.sessionEndedAt = sessionEndedAt;
    }

    public Integer getSessionDurationInSecs() {
        return sessionDurationInSecs;
    }

    public void setSessionDurationInSecs(Integer sessionDurationInSecs) {
        this.sessionDurationInSecs = sessionDurationInSecs;
    }

    public Integer getConsultPatientId() {
        return consultPatientId;
    }

    public void setConsultPatientId(Integer consultPatientId) {
        this.consultPatientId = consultPatientId;
    }

    public String getConsultFirstName() {
        return consultFirstName;
    }

    public void setConsultFirstName(String consultFirstName) {
        this.consultFirstName = consultFirstName;
    }

    public String getConsultLastName() {
        return consultLastName;
    }

    public void setConsultLastName(String consultLastName) {
        this.consultLastName = consultLastName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
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

    public String getConsultImage() {
        return consultImage;
    }

    public void setConsultImage(String consultImage) {
        this.consultImage = consultImage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClinicianName() {
        return clinicianName;
    }

    public void setClinicianName(String clinicianName) {
        this.clinicianName = clinicianName;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}