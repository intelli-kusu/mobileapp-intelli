package com.intellicare.model.visitinfo;

import com.google.gson.annotations.SerializedName;

public class VisitInfoResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("last_consult_time")
    private String lastConsultTime;
    @SerializedName("empty_fields")
    private String emptyFields;
    @SerializedName("member_id")
    private String memberId;
    @SerializedName("consult")
    private ConsultDetails consult;


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

    public String getLastConsultTime() {
        return lastConsultTime;
    }

    public void setLastConsultTime(String lastConsultTime) {
        this.lastConsultTime = lastConsultTime;
    }

    public String getEmptyFields() {
        return emptyFields;
    }

    public void setEmptyFields(String emptyFields) {
        this.emptyFields = emptyFields;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public ConsultDetails getConsult() {
        return consult;
    }

    public void setConsult(ConsultDetails consult) {
        this.consult = consult;
    }

}