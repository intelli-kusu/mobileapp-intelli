package com.intellicare.model.other;

public class VisitDetails {
    private String doctor;
    private String date;
    private String time;
    private String patient;
    private String notes;
    private String complaint;

    public VisitDetails(String doctor, String date, String time, String patient, String notes, String complaint) {
        this.doctor = doctor;
        this.date = date;
        this.time = time;
        this.patient = patient;
        this.notes = notes;
        this.complaint = complaint;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getComplaint() {
        return complaint;
    }

    public void setComplaint(String complaint) {
        this.complaint = complaint;
    }
}
