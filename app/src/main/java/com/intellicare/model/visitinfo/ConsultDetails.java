package com.intellicare.model.visitinfo;

import com.google.gson.annotations.SerializedName;

public class ConsultDetails {

    @SerializedName("consult_id")
    private String consultId;
    @SerializedName("status")
    private String status;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("assigned_at")
    private String assignedAt;
    @SerializedName("waiting_time_in_secs")
    private Integer waitingTimeInSecs;
    @SerializedName("cancelled_at")
    private String cancelledAt;
    @SerializedName("session_started_at")
    private String sessionStartedAt;
    @SerializedName("session_ended_at")
    private String sessionEndedAt;
    @SerializedName("session_duration_in_secs")
    private Integer sessionDurationInSecs;
    @SerializedName("consult_patient_id")
    private Integer consultPatientId;
    @SerializedName("consult_first_name")
    private String consultFirstName;
    @SerializedName("consult_last_name")
    private String consultLastName;
    @SerializedName("relationship")
    private String relationship;
    @SerializedName("bp")
    private String bp;
    @SerializedName("temperature")
    private String temperature;
    @SerializedName("consult_image")
    private String consultImage;
    @SerializedName("token")
    private String token;
    @SerializedName("channel")
    private String channel;
    @SerializedName("patient_id")
    private String patientId;
    @SerializedName("wait_time")
    private String waitTime;

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public String getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(String waitTime) {
        this.waitTime = waitTime;
    }
}