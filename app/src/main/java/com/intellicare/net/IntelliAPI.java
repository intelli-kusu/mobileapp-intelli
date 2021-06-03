package com.intellicare.net;

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

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface IntelliAPI {
    @Headers("Content-Type: application/json")
    @POST("patient/send-otp")
    Call<SendOTPResponse> sendOtp(@Header("x-api-key") String xapiKey, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/send-otp")
    Call<SendOTPResponse2> sendOtp2(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/verify-otp")
    Call<CommonResponse> verifyOtp(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/check-credentials")
    Call<UserFormData> checkCredentials(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/check-credentials")
    Call<CheckCredentialsResponse> checkCredentials2(@Header("x-api-key") String xapiKey, @Body Map<String, String> body);


    @Headers("Content-Type: application/json")
    @GET("allergy/list")
    Call<AllergyData> getAllergies(@Header("x-api-key") String xapiKey);

    @Headers("Content-Type: application/json")
    @GET("severity/list")
    Call<SeverityData> getSeverities(@Header("x-api-key") String xapiKey);

    @Headers("Content-Type: application/json")
    @GET("ethnicity/list")
    Call<EthinicityResponse> getEthnicities(@Header("x-api-key") String xapiKey);

    @Headers("Content-Type: application/json")
    @POST("complaint/list")
    Call<ComplaintData> getComplaints(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("pharmacy/list")
    Call<PharmacyData> getPharmacies(@Header("x-api-key") String xapiKey, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/info")
    Call<PatientInfoNew> getPatientInfo(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/update-profile")
    Call<CommonResponse> updatePatientProfile(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body UserFormRequest body);

    @Headers("Content-Type: application/json")
    @POST("visit/list")
    Call<PastVisitsResponse> getPastVisits(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("visit/info")
    Call<VisitInfoResponse> getVisitInfo(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("visit/create")
    Call<CreateVisitResponse> createVisit(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body CreateVisitRequest body);

    @Headers("Content-Type: application/json")
    @POST("visit/cancel")
    Call<CommonResponse> cancelVisit(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    @POST("patient/feedback")
    Call<CommonResponse> feedback(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);


    @Headers("Content-Type: application/json")
    @POST("patient/images")
    Call<PatientImages> getPatientImages(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);


    @Headers("Content-Type: application/json")
    @POST("patient/available")
    Call<CommonResponse> getPatientAvailable(@Header("x-api-key") String xapiKey, @Header("token") String token, @Body Map<String, String> body);


    @Headers("Content-Type: application/json")
    //https://staging.intellicare.health/api/v1/patient/file/74b1f0fa58166213255
//    @POST("patient/file/{image_id}")
//    https://staging.intellicare.health/api/v1/file/view/b603b3e25ab161d760
    @POST("file/view/{image_id}")
    Call<ResponseBody> getViewImage(@Header("x-api-key") String xapiKey, @Header("token") String token,
                                    @Path(value = "image_id") String imageId,
                                    @Body Map<String, String> body);

    @Headers("Content-Type: application/json")
    //https://staging.intellicare.health/api/v1/file/remove/e6bb7a417f180d1344
    @POST("file/remove/{image_id}")
    Call<CommonResponse> removeImage(@Header("x-api-key") String xapiKey, @Header("token") String token,
                                    @Path(value = "image_id") String imageId,
                                    @Body Map<String, String> body);

    @Multipart
    @POST("file/upload")
    Call<FileUploadResponse> uploadFile(
            @Header("x-api-key") String xapiKey,
            @Header("token") String token,
            @Part("type") RequestBody type,
            @Part("patient_id") RequestBody patientId,
            @Part MultipartBody.Part image
    );
}
