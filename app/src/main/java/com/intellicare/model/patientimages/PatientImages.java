package com.intellicare.model.patientimages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PatientImages {

    @SerializedName("membership_card")
    @Expose
    private String membershipCard;
    @SerializedName("profile")
    @Expose
    private String profile;
    @SerializedName("dl")
    @Expose
    private String dl;
    @SerializedName("insurance_card_front")
    @Expose
    private String insuranceCardFront;
    @SerializedName("insurance_card_back")
    @Expose
    private String insuranceCardBack;
    @SerializedName("current_medication")
    @Expose
    private String currentMedication;

    public String getMembershipCard() {
        return membershipCard;
    }

    public void setMembershipCard(String membershipCard) {
        this.membershipCard = membershipCard;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getDl() {
        return dl;
    }

    public void setDl(String dl) {
        this.dl = dl;
    }

    public String getInsuranceCardFront() {
        return insuranceCardFront;
    }

    public void setInsuranceCardFront(String insuranceCardFront) {
        this.insuranceCardFront = insuranceCardFront;
    }

    public String getInsuranceCardBack() {
        return insuranceCardBack;
    }

    public void setInsuranceCardBack(String insuranceCardBack) {
        this.insuranceCardBack = insuranceCardBack;
    }

    public String getCurrentMedication() {
        return currentMedication;
    }

    public void setCurrentMedication(String currentMedication) {
        this.currentMedication = currentMedication;
    }

}