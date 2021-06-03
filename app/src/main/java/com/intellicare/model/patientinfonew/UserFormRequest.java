package com.intellicare.model.patientinfonew;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.intellicare.model.createvisitmodel.Allergy;

import java.util.List;

public class UserFormRequest {

    @SerializedName("member_id")
    @Expose
    private String memberId;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("ethnicity")
    @Expose
    private String ethnicity;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("dl_image")
    @Expose
    private String dlImage;
    @SerializedName("insurance_card_back_image")
    @Expose
    private String insuranceCardBackImage;
    @SerializedName("current_medication_image_1")
    @Expose
    private String currentMedicationImage1;
    @SerializedName("middle_name")
    @Expose
    private String middleName;
    @SerializedName("current_medication_image_2")
    @Expose
    private String currentMedicationImage2;
    @SerializedName("current_medication_image_3")
    @Expose
    private String currentMedicationImage3;
    @SerializedName("current_medication_image_4")
    @Expose
    private String currentMedicationImage4;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("pharmacy_id")
    @Expose
    private String pharmacyId;
    @SerializedName("insurance_card_image")
    @Expose
    private String insuranceCardImage;
    @SerializedName("patient_id")
    @Expose
    private String patientId;
    @SerializedName("phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("relationship")
    @Expose
    private String relationship;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("email")
    @Expose
    private String email;

   /* @SerializedName("profile_image")
    @Expose
    public String profile_image = "74b1f0fa58166213255";*/
    @SerializedName("allergies")
    @Expose
    private List<Allergy> allergies = null;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDlImage() {
        return dlImage;
    }

    public void setDlImage(String dlImage) {
        this.dlImage = dlImage;
    }

    public String getInsuranceCardBackImage() {
        return insuranceCardBackImage;
    }

    public void setInsuranceCardBackImage(String insuranceCardBackImage) {
        this.insuranceCardBackImage = insuranceCardBackImage;
    }

    public String getCurrentMedicationImage1() {
        return currentMedicationImage1;
    }

    public void setCurrentMedicationImage1(String currentMedicationImage1) {
        this.currentMedicationImage1 = currentMedicationImage1;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getCurrentMedicationImage2() {
        return currentMedicationImage2;
    }

    public void setCurrentMedicationImage2(String currentMedicationImage2) {
        this.currentMedicationImage2 = currentMedicationImage2;
    }

    public String getCurrentMedicationImage3() {
        return currentMedicationImage3;
    }

    public void setCurrentMedicationImage3(String currentMedicationImage3) {
        this.currentMedicationImage3 = currentMedicationImage3;
    }

    public String getCurrentMedicationImage4() {
        return currentMedicationImage4;
    }

    public void setCurrentMedicationImage4(String currentMedicationImage4) {
        this.currentMedicationImage4 = currentMedicationImage4;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPharmacyId() {
        return pharmacyId;
    }

    public void setPharmacyId(String pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public String getInsuranceCardImage() {
        return insuranceCardImage;
    }

    public void setInsuranceCardImage(String insuranceCardImage) {
        this.insuranceCardImage = insuranceCardImage;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }
}