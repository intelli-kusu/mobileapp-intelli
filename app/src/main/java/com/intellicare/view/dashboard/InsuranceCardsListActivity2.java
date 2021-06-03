package com.intellicare.view.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.intellicare.R;
import com.intellicare.databinding.ActivityInsuranceCardsList2Binding;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.fileupload.FileUploadResponse;
import com.intellicare.model.patientimages.PatientImages;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InsuranceCardsListActivity2 extends BaseActivity implements View.OnClickListener {
    //new addition
    private final String[] mimeTypes = {"image/png", "image/jpg", "image/jpeg"};
    private ActivityInsuranceCardsList2Binding binding;
    private boolean isNext = true;
    private Uri fileUri = null;
    private File file;
    private String mimeType;
    private String clickTag = "";
    private String imageType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInsuranceCardsList2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.back.setOnClickListener(this);
        binding.ivEdit1.setOnClickListener(this);
        binding.ivEdit2.setOnClickListener(this);
        binding.ivEdit3.setOnClickListener(this);

        getPatientImages();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back) {
            finish();
        } else if (v.getId() == R.id.ivEdit1) {
            clickTag = "membership";
            imageType = "membership";
            showPickerDialog("Choose Image");
        } else if (v.getId() == R.id.ivEdit2) {
            clickTag = "front";
            imageType = "insurance";
            showPickerDialog("Choose Image");
        } else if (v.getId() == R.id.ivEdit3) {
            clickTag = "back";
            imageType = "insuranceback";
            showPickerDialog("Choose Image");
        }
    }


    @Override
    protected void onPositiveClick() { //camera
        ImagePicker.Companion.with(InsuranceCardsListActivity2.this)
                .galleryMimeTypes(mimeTypes)
                .cameraOnly()
                .crop()
                .compress(512)
                .maxResultSize(720, 720)
                .start();
    }

    @Override
    protected void onNegativeClick() { //gallery
        ImagePicker.Companion.with(InsuranceCardsListActivity2.this)
                .galleryMimeTypes(mimeTypes)
                .galleryOnly()
                .crop()
                .compress(512)
                .maxResultSize(720, 720)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && intent != null) {
            //Image Uri will not be null for RESULT_OK
            fileUri = intent.getData();

            mimeType = getMimeType(fileUri.toString());

            //You can get File object from intent
            file = ImagePicker.Companion.getFile(intent);
            //You can also get File Path from intent
            String filePath = ImagePicker.Companion.getFilePath(intent);
            uploadImage();
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Utils.toast(ImagePicker.Companion.getError(intent), InsuranceCardsListActivity2.this);
            clickTag = "";
            imageType = "";
        } else {
            clickTag = "";
            imageType = "";
            Log.e("UserForm", "Task Cancelled");
        }
    }

    private void uploadImage() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, InsuranceCardsListActivity2.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, InsuranceCardsListActivity2.this);
        Call<FileUploadResponse> call = new ServiceHelper().uploadFile(patient_id,
                imageType,
                token,
                file,
                mimeType);

        call.enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FileUploadResponse result = response.body();
                    if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                        setImage(fileUri);
                    } else {
                        setDefaultImage(clickTag);
                    }

                    //reset helper fields
                    clickTag = "";
                    imageType = "";
                }
            }

            @Override
            public void onFailure(Call<FileUploadResponse> call, Throwable t) {

            }
        });
    }

    private void setImage(Uri imageUri) {
        if (!TextUtils.isEmpty(clickTag) && imageUri != null) {
            if (clickTag.equalsIgnoreCase("membership")) {
                binding.ivMembershipCard.setImageURI(imageUri);
                binding.ivEdit1.setVisibility(View.VISIBLE);
                binding.tv1.setVisibility(View.VISIBLE);
            } else if (clickTag.equalsIgnoreCase("front")) {
                binding.ivInsuranceCardFront.setImageURI(imageUri);
                binding.ivEdit2.setVisibility(View.VISIBLE);
                binding.tv2.setVisibility(View.VISIBLE);
            } else if (clickTag.equalsIgnoreCase("back")) {
                binding.ivInsuranceCardBack.setImageURI(imageUri);
                binding.ivEdit3.setVisibility(View.VISIBLE);
                binding.tv3.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getMimeType(String url) {
        if (url.lastIndexOf(".") != -1) {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getMimeTypeFromExtension(ext);
        } else {
            return null;
        }
    }

    private void getPatientImages() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, InsuranceCardsListActivity2.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, InsuranceCardsListActivity2.this);
        Call<PatientImages> call = new ServiceHelper().getPatientImages(patient_id, token);

        call.enqueue(new Callback<PatientImages>() {
            @Override
            public void onResponse(Call<PatientImages> call, Response<PatientImages> response) {

                if (response.isSuccessful()) {
                    PatientImages patientImages = response.body();
                    loadImages(patientImages);
                }

            }

            @Override
            public void onFailure(Call<PatientImages> call, Throwable t) {
                Log.e("Cards", t.getMessage());
            }
        });
    }

    private void loadImages(PatientImages patientImages) {
        //check if image id is available for each image to be loaded. Load the image accordingly
        if (patientImages != null) {
//            String membershipCard = patientImages.getMembershipCard();
            String insuranceCardFront = patientImages.getInsuranceCardFront();
            String insuranceCardBack = patientImages.getInsuranceCardBack();

            /*if (!TextUtils.isEmpty(membershipCard)) {
                getImage(patientImages.getMembershipCard(), "membership", "membership");
            } else {
                setDefaultImage("membership");
            }*/
            if (!TextUtils.isEmpty(insuranceCardFront)) {
                getImage(patientImages.getInsuranceCardFront(), "front", "insurance");
            } else {
                setDefaultImage("front");
            }
            if (!TextUtils.isEmpty(insuranceCardBack)) {
                getImage(patientImages.getInsuranceCardBack(), "back", "insuranceback");
            } else {
                setDefaultImage("back");
            }
        }
    }

    private void getImage(String imageId, String requestTag, String type) {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, InsuranceCardsListActivity2.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, InsuranceCardsListActivity2.this);
        Call<ResponseBody> apiCall = new ServiceHelper(requestTag).getViewImage(patient_id, type, imageId, token);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String requestTag = ((String[]) Objects.requireNonNull(call.request().tag()))[0];
                Log.e("TAG", requestTag + "");
                if (response.isSuccessful()) {
                    try {
                        InputStream inputStream = response.body().byteStream();
                        Bitmap bm = BitmapFactory.decodeStream(inputStream);
                        if (requestTag.equalsIgnoreCase("membership")) {
                            binding.ivMembershipCard.setImageBitmap(bm);
                            binding.ivEdit1.setVisibility(View.VISIBLE);
                            binding.tv1.setVisibility(View.VISIBLE);
                        } else if (requestTag.equalsIgnoreCase("front")) {
                            binding.ivInsuranceCardFront.setImageBitmap(bm);
                            binding.ivEdit2.setVisibility(View.VISIBLE);
                            binding.tv2.setVisibility(View.VISIBLE);
                        } else if (requestTag.equalsIgnoreCase("back")) {
                            binding.ivInsuranceCardBack.setImageBitmap(bm);
                            binding.ivEdit3.setVisibility(View.VISIBLE);
                            binding.tv3.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("TAG", "image download failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "onFailure: " + t.getMessage());
            }
        });
    }

    private void setDefaultImage(String tag) {
        if (tag.equalsIgnoreCase("membership")) {
            binding.ivMembershipCard.setImageResource(R.drawable.ic_credit_card2);
            binding.ivEdit1.setVisibility(View.VISIBLE);
            binding.tv1.setVisibility(View.VISIBLE);
        } else if (tag.equalsIgnoreCase("front")) {
            binding.ivInsuranceCardFront.setImageResource(R.drawable.ic_credit_card2);
            binding.ivEdit2.setVisibility(View.VISIBLE);
            binding.tv2.setVisibility(View.VISIBLE);
        } else if (tag.equalsIgnoreCase("back")) {
            binding.ivInsuranceCardBack.setImageResource(R.drawable.ic_credit_card2);
            binding.ivEdit3.setVisibility(View.VISIBLE);
            binding.tv3.setVisibility(View.VISIBLE);
        }
    }
}