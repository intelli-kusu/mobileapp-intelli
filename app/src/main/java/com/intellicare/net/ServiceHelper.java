package com.intellicare.net;

import android.os.Build;

import com.intellicare.model.other.AllergyData;
import com.intellicare.model.other.ComplaintData;
import com.intellicare.model.other.EthinicityResponse;
import com.intellicare.model.other.PharmacyData;
import com.intellicare.model.other.SeverityData;
import com.intellicare.model.createvisitmodel.CreateVisitRequest;
import com.intellicare.model.createvisitmodel.CreateVisitResponse;
import com.intellicare.model.fileupload.FileUploadResponse;
import com.intellicare.model.pastvisits.PastVisitsResponse;
import com.intellicare.model.patientimages.PatientImages;
import com.intellicare.model.patientinfonew.PatientInfoNew;
import com.intellicare.model.patientinfonew.UserFormRequest;
import com.intellicare.model.response.CheckCredentialsResponse;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.response.SendOTPResponse;
import com.intellicare.model.response.SendOTPResponse2;
import com.intellicare.model.response.UserFormData;
import com.intellicare.model.visitinfo.VisitInfoResponse;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class ServiceHelper {
    private final IntelliAPI service;
    private String xApi = "nNhcqUdA9RsW34pZfZCGCWJKDQZYhUTS";

    public ServiceHelper() {
        service = IntelliClient.getInstance().getService();
    }

    public ServiceHelper(String requestTag) {
        service = IntelliClient.getInstance().getService(requestTag);
    }

    public Call<SendOTPResponse> sendOtp(String last_name, String dob, String phone_number) {
        /*{
            "last_name":"Hall",
                "dob": "01/15/1979",
                "phone_number": "8888888888",
                "device_type": "A",
                "device_token": "gQPVFuKboqYHfuZvtF4qV282OQ0wLNwG",
                "os_version": "11.1",
                "device_model": "MiA3"
        }*/

        Map<String, String> body = new HashMap<>();
        body.put("last_name", last_name);
        body.put("dob", /*"01/15/1979"*/dob);
        body.put("phone_number", phone_number);
        body.put("device_type", "A");
        body.put("device_token", "gQPVFuKboqYHfuZvtF4qV282OQ0wLNwG");
        body.put("os_version", "8.1");
        body.put("device_model", "Samsung");
        return service.sendOtp(xApi, body);
    }

    public Call<SendOTPResponse2> sendOtp2(String patient_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        return service.sendOtp2(xApi, token, body);
    }

    public Call<CommonResponse> verifyOtp(String patient_id, String otp, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("otp", otp);
        return service.verifyOtp(xApi, token, body);
    }

    public Call<UserFormData> checkCredentials(String patient_id, String last_name, String dob, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("last_name", last_name);
        body.put("dob", dob);
        return service.checkCredentials(xApi, token, body);
    }

    public Call<CheckCredentialsResponse> checkCredentials2(String first_name, String last_name, String dob, String phone_number, String device_token) {
        /*{
    "last_name": "Pagee",
    "dob": "12/28/1986",
    "phone_number": "(222) 222-2211",
    "device_type": "I",
    "device_token": "gQPVFuKboqYHfuZvtF4qV282OQ0wLNwG",
    "os_version": "14.4",
    "device_model": "iPhone6S"
}*/

        Map<String, String> body = new HashMap<>();
        body.put("first_name", first_name);
        body.put("last_name", last_name);
        body.put("dob", dob);
        body.put("phone_number", phone_number);
        body.put("device_type", "A");
        body.put("device_token", device_token);
        body.put("os_version", Build.VERSION.RELEASE);
        body.put("device_model", Build.BRAND.toUpperCase() + "_" + Build.MODEL);
        return service.checkCredentials2(xApi, body);
    }

    public Call<AllergyData> getAllergies() {
        return service.getAllergies(xApi);
    }

    public Call<SeverityData> getSeverities() {
        return service.getSeverities(xApi);
    }

    public Call<EthinicityResponse> getEthnicities() {
        return service.getEthnicities(xApi);
    }

    public Call<ComplaintData> getComplaints(String patient_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        return service.getComplaints(xApi, token, body);
    }

    public Call<PharmacyData> getPharmacies(String zip) {
        Map<String, String> body = new HashMap<>();
        body.put("q", zip);
        return service.getPharmacies(xApi, body);
    }

    public Call<PatientInfoNew> getPatientInfo(String patient_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        return service.getPatientInfo(xApi, token, body);
    }

    public Call<PastVisitsResponse> getPastVisits(String patient_id, String page, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("page", page);
        return service.getPastVisits(xApi, token, body);
    }

    public Call<VisitInfoResponse> getVisitInfo(String patient_id, String consult_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("consult_id", consult_id);
        return service.getVisitInfo(xApi, token, body);
    }

    public Call<CommonResponse> cancelVisit(String patient_id, String consult_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("consult_id", consult_id);
        return service.cancelVisit(xApi, token, body);
    }

    public Call<CommonResponse> getPatientAvailable(String patient_id, String consult_id, String status, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("consult_id", consult_id);
        body.put("status", status);
        return service.getPatientAvailable(xApi, token, body);
    }

    public Call<CreateVisitResponse> createVisit(CreateVisitRequest body, String token) {
        return service.createVisit(xApi, token, body);
    }

    public Call<CommonResponse> updatePatientProfile(UserFormRequest body, String token) {
        return service.updatePatientProfile(xApi, token, body);
    }

    public Call<CommonResponse> feedback(String patient_id, String stars, String feedback, String consult_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("stars", stars);
        body.put("feedback", feedback);
        body.put("consult_id", consult_id);
        return service.feedback(xApi, token, body);
    }

    public Call<PatientImages> getPatientImages(String patient_id, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        return service.getPatientImages(xApi, token, body);
    }

    //image or file related calls
    public Call<ResponseBody> getViewImage(String patient_id, String type, String imageId, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        body.put("type", type);
        return service.getViewImage(xApi, token, imageId, body);
    }

    public Call<CommonResponse> removeImage(String patient_id, String imageId, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("patient_id", patient_id);
        return service.removeImage(xApi, token, imageId, body);
    }

    public Call<FileUploadResponse> uploadFile(String patient_id, String type, String token, File file, String mimeType) {
        RequestBody imageBody = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", /*file.getName()*/"intelli_" + System.currentTimeMillis() + ".jpg", imageBody);
        RequestBody typeBody = RequestBody.create(MultipartBody.FORM, type);
        RequestBody patientIdBody = RequestBody.create(MultipartBody.FORM, patient_id);
        return service.uploadFile(xApi, token, typeBody, patientIdBody, imagePart);
    }
}
