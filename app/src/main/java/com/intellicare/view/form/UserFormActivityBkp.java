package com.intellicare.view.form;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.intellicare.R;
import com.intellicare.databinding.ActivityUserFormBkpBinding;
import com.intellicare.model.other.AppConstants;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.fileupload.FileUploadResponse;
import com.intellicare.model.patientinfo.Field;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.response.UserFormData;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.dashboard.DashboardActivity;

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFormActivityBkp extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UserFormActivity";
    //new addition for Storage
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //new addition for storage
    private static final int PERMISSION_REQ_ID = 10088;
    private final String[] mimeTypes = {"image/png", "image/jpg", "image/jpeg"};
    private final Context CONTEXT = UserFormActivityBkp.this;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateButtonState();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    private ActivityUserFormBkpBinding binding;
    private boolean isInsuranceCardPhotoPicked = false;
    private boolean isInsuranceCardBackPhotoPicked = false;
    private boolean isMemberPhotoPicked = false;
    private boolean isDLPhotoPicked = false;
    private boolean isCurrentMedicationPhotoPicked = true;
    private boolean isLegalTermsAccepted = false;
    private boolean isHipaaTermsAccepted = false;
    private boolean isFirstNameRequired = false;
    private boolean isMiddleNameRequired = false;
    private boolean isLastNameRequired = false;
    private boolean isDobRequired = false;
    private boolean isPhoneNumberRequired = false;
    private boolean isEmailRequired = false;
    private boolean isGenderRequired = false;
    private boolean isMemberPhotoRequired = false;
    private boolean isInsuranceCardRequired = false;
    private boolean isInsuranceCardBackRequired = false;
    private boolean isDLRequired = false;
    private boolean isCurrentMedicationRequired = false;
    private String imageType = "";
    private String insurance_card_image = "";
    private String insurance_card_back_image = "";
    private String profile_image = "";
    private String dl_image = "";
    private String current_medication_image = "";
    private File file;
    private String mimeType;
    private int selectedPhotoId = -1;
    private String gender;
    //new changes
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private boolean isDateSelected;
    private boolean isProfilePic;

    private void updateButtonState() {
//        binding.btnSubmit.setEnabled(isValidForm());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserFormBkpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            requestPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setListeners();
        setClickableTexts();
        setCheckableTextListeners();
        try{
            Intent data = getIntent();
            if(data != null) {
                boolean isFromDashboard = data.getBooleanExtra("isFromDashboard", false);
                if(isFromDashboard) {
                    checkUserCredentialsServiceCall();
                } else {
                    updateUI();
                }
                    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

    private void updateUI() {
        if (AppConstants.userFormData != null
                && AppConstants.userFormData.getFieldMap() != null
                && AppConstants.userFormData.getFieldMap().size() > 0) {
            Map<String, Field> fieldMap = AppConstants.userFormData.getFieldMap();
            Field firstNameField = fieldMap.get("first_name");
            Field middleNameField = fieldMap.get("middle_name");
            Field lastNameField = fieldMap.get("last_name");
            Field dobField = fieldMap.get("dob");
            Field mobilePhoneField = fieldMap.get("emergency_phone");
            Field emailField = fieldMap.get("email");
            Field genderField = fieldMap.get("gender");
            Field insuranceCardPhotoField = fieldMap.get("insurance_card_image");
            Field insuranceCardPhotoBackField = fieldMap.get("insurance_card_back_image");
            Field memberPhotoField = fieldMap.get("profile_image");
            Field dlPhotoField = fieldMap.get("dl_image");
            Field currentMedicationPhotoField = fieldMap.get("current_medication_image");

            //update labels based on required

            //first_name
            if (firstNameField.getRequired().equalsIgnoreCase("yes")) {
                isFirstNameRequired = true;
                binding.tvFirstName.setText(getResources().getText(R.string.first_name_star));
            } else {
                binding.tvFirstName.setText(getResources().getText(R.string.first_name));
            }

            //middle_name
            if (middleNameField.getRequired().equalsIgnoreCase("yes")) {
                isMiddleNameRequired = true;
                binding.tvMiddleName.setText(getResources().getText(R.string.middle_name_star));
            } else {
                binding.tvMiddleName.setText(getResources().getText(R.string.middle_name));
            }
            //last_name
            if (lastNameField.getRequired().equalsIgnoreCase("yes")) {
                isLastNameRequired = true;
                binding.tvLastName.setText(getResources().getText(R.string.last_name_star));
            } else {
                binding.tvLastName.setText(getResources().getText(R.string.last_name));
            }

            //dob
            if (dobField.getRequired().equalsIgnoreCase("yes")) {
                isDobRequired = true;
                binding.tvDOB.setText(getResources().getText(R.string.date_of_birth_star));
            } else {
                binding.tvDOB.setText(getResources().getText(R.string.date_of_birth));
            }

            //emergency_phone
            if (mobilePhoneField.getRequired().equalsIgnoreCase("yes")) {
                isPhoneNumberRequired = true;
                binding.tvMobile.setText(getResources().getText(R.string.mobile_phone_star));
            } else {
                binding.tvMobile.setText(getResources().getText(R.string.mobile_phone));
            }
            //email
            if (emailField.getRequired().equalsIgnoreCase("yes")) {
                isEmailRequired = true;
                binding.tvEmail.setText(getResources().getText(R.string.email_star));
            } else {
                binding.tvEmail.setText(getResources().getText(R.string.email));
            }

            //gender
            if (genderField.getRequired().equalsIgnoreCase("yes")) {
                isGenderRequired = true;
                binding.tvGender.setText(getResources().getText(R.string.select_gender_star));
            } else {
                binding.tvGender.setText(getResources().getText(R.string.select_gender));
            }


            //insurance_card_image
            if (insuranceCardPhotoField.getRequired().equalsIgnoreCase("yes")) {
                isInsuranceCardRequired = true;
                binding.tvInsuranceCardPhoto.setText(getResources().getText(R.string.insurance_card_photo_star));
            } else {
                binding.tvInsuranceCardPhoto.setText(getResources().getText(R.string.insurance_card_photo));
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(insuranceCardPhotoField.getValue())) {
                isInsuranceCardPhotoPicked = true;
                insurance_card_image = insuranceCardPhotoField.getValue();
            }

            //insurance_card_back_image
            if (insuranceCardPhotoBackField.getRequired().equalsIgnoreCase("yes")) {
                isInsuranceCardBackRequired = true;
                binding.tvInsuranceCardPhotoBack.setText(getResources().getText(R.string.insurance_card_photo_back_star));
            } else {
                binding.tvInsuranceCardPhotoBack.setText(getResources().getText(R.string.insurance_card_photo_back));
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(insuranceCardPhotoBackField.getValue())) {
                isInsuranceCardBackPhotoPicked = true;
                insurance_card_back_image = insuranceCardPhotoBackField.getValue();
            }

            //profile_image
            if (memberPhotoField.getRequired().equalsIgnoreCase("yes")) {
                isMemberPhotoRequired = true;
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(memberPhotoField.getValue())) {
                isMemberPhotoPicked = true;
                profile_image = memberPhotoField.getValue();
            }

            //dl_image
            if (dlPhotoField.getRequired().equalsIgnoreCase("yes")) {
                isDLRequired = true;
                binding.tvDLPhoto.setText(getResources().getText(R.string.driving_license_photo_star));
            } else {
                binding.tvDLPhoto.setText(getResources().getText(R.string.driving_license_photo));
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(dlPhotoField.getValue())) {
                isDLPhotoPicked = true;
                dl_image = dlPhotoField.getValue();
            }
            //current_medication_image
            if (currentMedicationPhotoField.getRequired().equalsIgnoreCase("yes")) {
                isCurrentMedicationRequired = true;
                binding.tvMedicationPhoto.setText(getResources().getText(R.string.current_medication_photo_star));
            } else {
                binding.tvMedicationPhoto.setText(getResources().getText(R.string.current_medication_photo));
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(currentMedicationPhotoField.getValue())) {
                isCurrentMedicationPhotoPicked = true;
                current_medication_image = currentMedicationPhotoField.getValue();
            }
            //Fill the available fields
            binding.etFirstName.setText(firstNameField.getValue());
            binding.etMiddleName.setText(middleNameField.getValue());
            binding.etLastName.setText(lastNameField.getValue());
            binding.etDOB.setText(dobField.getValue());
            binding.etMobile.setText(mobilePhoneField.getValue());
            binding.etEmail.setText(emailField.getValue());
            //cursor position
            try {
                /*binding.etFirstName.setSelection(firstNameField.getValue().length());
                binding.etMiddleName.setSelection(middleNameField.getValue().length());
                binding.etLastName.setSelection(lastNameField.getValue().length());
                binding.etDOB.setSelection(dobField.getValue().length());
                binding.etMobile.setSelection(mobilePhoneField.getValue().length());
                binding.etEmail.setSelection(emailField.getValue().length());*/
            } catch (Exception e) {
                e.printStackTrace();
            }

            //check for DOD field
            if (dobField != null && !TextUtils.isEmpty(dobField.getValue())) {
                isDateSelected = true;
                String dobString = dobField.getValue(); //Format is "MM/DD/YYYY"
                if (dobString.contains("/")) {
                    String[] splitDate = dobString.split("/");
                    try {
                        selectedMonth = Integer.parseInt(splitDate[0]);
                        selectedDay = Integer.parseInt(splitDate[1]);
                        selectedYear = Integer.parseInt(splitDate[2]);
                    } catch (Exception e) {
                        isDateSelected = false;
                        e.printStackTrace();
                    }
                }
            }
            doRadioThing(genderField);
            loadImage("insurance", insuranceCardPhotoField, binding.ivInsuranceCardPhoto);
            loadImage("insuranceback", insuranceCardPhotoBackField, binding.ivInsuranceCardPhotoBack);
            loadImage("profile", memberPhotoField, binding.ivMemberPhoto);
            loadImage("dl", dlPhotoField, binding.ivDLPhoto);
            loadImage("current-medication", currentMedicationPhotoField, binding.ivMedicationPhoto);

        } else {
            finish(); //call service required
        }
    }

    private void loadImage(String type, Field f, ImageView imageView) {
        if (!TextUtils.isEmpty(f.getValue())) {
            getImage(type, f.getValue(), imageView);
        }
    }

    private void getImage(String type, String imageId, ImageView imageView) {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivityBkp.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivityBkp.this);
        Call<ResponseBody> apiCall = new ServiceHelper().getViewImage(patient_id, type, imageId, token);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bm);
                } else {
                    Log.e(TAG, "image download failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    private void setCheckableTextListeners() {
        binding.cbLegalTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLegalTermsAccepted = isChecked;
                updateButtonState();
            }
        });
        binding.cbHipaaTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHipaaTermsAccepted = isChecked;
                updateButtonState();
            }
        });
    }

    private void setListeners() {
        binding.btnSubmit.setOnClickListener(this);
        binding.etDOB.setOnClickListener(this);
        binding.ivInsuranceCardPhoto.setOnClickListener(this);
        binding.ivInsuranceCardPhotoBack.setOnClickListener(this);
        binding.ivMemberPhoto.setOnClickListener(this);
        binding.ivDLPhoto.setOnClickListener(this);
        binding.ivMedicationPhoto.setOnClickListener(this);
        binding.tvEditImage.setOnClickListener(this);


        binding.etMemberId.addTextChangedListener(textWatcher);
        binding.etLastName.addTextChangedListener(textWatcher);
        binding.etEmail.addTextChangedListener(textWatcher);
        binding.etDOB.addTextChangedListener(textWatcher);
        binding.etMobile.addTextChangedListener(new TextChangeWatcher("US"));
    }

    private void doRadioThing(Field f) {
        if (f != null) {
            if (f.getValue().equalsIgnoreCase("male")) {
                binding.rbMale.setChecked(true);
                gender = "Male";
            }
            else if (f.getValue().equalsIgnoreCase("female")) {
                binding.rbFemale.setChecked(true);
                gender = "Female";
            }
            else {
                binding.rbOther.setChecked(true);
                gender = "Other";
            }
        }
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMale) {
                gender = "Male";
            } else if (checkedId == R.id.rbFemale) {
                gender = "Female";
            } else {
                gender = "Other";
            }
        });
    }

    private void setClickableTexts() {
        //https://stackoverflow.com/questions/8184597/how-do-i-make-a-portion-of-a-checkboxs-text-clickable
        String htmlStar = "<font color='#FF4455'>&nbsp;*</font>";
        String legalTerms = getResources().getString(R.string.agree_to_intellicare_legal_terms);
        SpannableString ss = new SpannableString(Html.fromHtml(legalTerms));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View checkbox) {
                // Prevent CheckBox state from being toggled when link is clicked
                checkbox.cancelPendingInputEvents();
                Utils.toast("Legal Terms", UserFormActivityBkp.this);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ss.setSpan(clickableSpan, 9, 32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.cbLegalTerms.setText(ss);
        binding.cbLegalTerms.append(Html.fromHtml(htmlStar));
        binding.cbLegalTerms.setMovementMethod(LinkMovementMethod.getInstance());
        binding.cbLegalTerms.setHighlightColor(Color.TRANSPARENT);

        String hipaaTerms = getResources().getString(R.string.agree_to_hipaa_terms);
        SpannableString ss2 = new SpannableString(Html.fromHtml(hipaaTerms));
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View checkbox) {
                // Prevent CheckBox state from being toggled when link is clicked
                checkbox.cancelPendingInputEvents();
                Utils.toast("HIPAA Terms", UserFormActivityBkp.this);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        ss2.setSpan(clickableSpan2, 9, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.cbHipaaTerms.setText(ss2);
        binding.cbHipaaTerms.append(Html.fromHtml(htmlStar));
        binding.cbHipaaTerms.setMovementMethod(LinkMovementMethod.getInstance());
        binding.cbHipaaTerms.setHighlightColor(Color.TRANSPARENT);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnSubmit) {
            if (isValidForm())
                validateDetailsAndSubmit();
            else
                Utils.toast("Please provide all the mandatory fields", UserFormActivityBkp.this);
        } else if (id == R.id.etDOB) {
            showDatePicker();
        } else if (id == R.id.ivInsuranceCardPhoto
                || id == R.id.ivInsuranceCardPhotoBack
                || id == R.id.ivMemberPhoto
                || id == R.id.tvEditImage
                || id == R.id.ivDLPhoto
                || id == R.id.ivMedicationPhoto) {
            isProfilePic = id == R.id.tvEditImage || id == R.id.ivMemberPhoto;

            showPickerDialog("Choose Image");
            if (id == R.id.tvEditImage)
                id = R.id.ivMemberPhoto;
            selectedPhotoId = id;
        }
    }

    @Override
    protected void onPositiveClick() { //camera
        float crop_x;
        float crop_y;

        if (isProfilePic) {
            crop_x = 1f;
            crop_y = 1f;
        } else {
            crop_x = 3f;
            crop_y = 2f;
        }


        ImagePicker.Companion.with(UserFormActivityBkp.this)
                .galleryMimeTypes(mimeTypes)
                .cameraOnly()
                .crop(crop_x, crop_y)
                .compress(512)
                .maxResultSize(720, 720)
                .start();
    }

    @Override
    protected void onNegativeClick() { //gallery
        float crop_x;
        float crop_y;

        if (isProfilePic) {
            crop_x = 1f;
            crop_y = 1f;
        } else {
            crop_x = 3f;
            crop_y = 2f;
        }
        ImagePicker.Companion.with(UserFormActivityBkp.this)
                .galleryMimeTypes(mimeTypes)
                .galleryOnly()
                .crop(crop_x, crop_y)
                .compress(512)
                .maxResultSize(720, 720)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK && intent != null) {
            //Image Uri will not be null for RESULT_OK
            Uri fileUri = intent.getData();
            setImage(fileUri);
            mimeType = getMimeType(fileUri.toString());

            //You can get File object from intent
            file = ImagePicker.Companion.getFile(intent);
            //You can also get File Path from intent
            String filePath = ImagePicker.Companion.getFilePath(intent);
            uploadImage();
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Utils.toast(ImagePicker.Companion.getError(intent), CONTEXT);
            selectedPhotoId = -1;
        } else {
            selectedPhotoId = -1;
            Log.e("UserForm", "Task Cancelled");
        }
    }

    private void uploadImage() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivityBkp.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivityBkp.this);
        Call<FileUploadResponse> call = new ServiceHelper().uploadFile(patient_id,
                imageType,
                token,
                file,
                mimeType);

        call.enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(Call<FileUploadResponse> call, Response<FileUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FileUploadResponse response1 = response.body();
                    String imageId = response1.getId();
                    if (selectedPhotoId == R.id.ivInsuranceCardPhoto) {
                        insurance_card_image = imageId;
                        isInsuranceCardPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivInsuranceCardPhotoBack) {
                        insurance_card_back_image = imageId;
                        isInsuranceCardBackPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivMemberPhoto) {
                        profile_image = imageId;
                        isMemberPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivDLPhoto) {
                        dl_image = imageId;
                        isDLPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivMedicationPhoto) {
                        current_medication_image = imageId;
                        isCurrentMedicationPhotoPicked = true;
                    }

                    //reset helper fields
                    selectedPhotoId = -1;
                    imageType = "";
                }
            }

            @Override
            public void onFailure(Call<FileUploadResponse> call, Throwable t) {

            }
        });
    }

    private void setImage(Uri imageUri) {
        if (selectedPhotoId != -1 && imageUri != null) {
            if (selectedPhotoId == R.id.ivInsuranceCardPhoto) {
                binding.ivInsuranceCardPhoto.setImageURI(imageUri);
                imageType = "insurance";
            } else if (selectedPhotoId == R.id.ivInsuranceCardPhotoBack) {
                binding.ivInsuranceCardPhotoBack.setImageURI(imageUri);
                imageType = "insuranceback";
            } else if (selectedPhotoId == R.id.ivMemberPhoto) {
                binding.ivMemberPhoto.setImageURI(imageUri);
                imageType = "profile";
            } else if (selectedPhotoId == R.id.ivDLPhoto) {
                binding.ivDLPhoto.setImageURI(imageUri);
                imageType = "dl";
            } else if (selectedPhotoId == R.id.ivMedicationPhoto) {
                binding.ivMedicationPhoto.setImageURI(imageUri);
                imageType = "current-medication";
            }
        }
    }
    /*{
    "first_name": "Franklin",
    "middle_name": "",
    "last_name": "Hall",
    "dob": "01/15/1979",
    "email": "halln@email",
    "emergency_phone": "",
    "gender": "Male",
    "member_id": "12345",
    "membership_card_image": "84aeb40331a758dc3",
    "profile_image": "9c228d7a9a79c27d2",
    "insurance_card_image": "f087027c5d27eca85",
    "dl_image": "123fcae191db891014",
    "current_medication_image": "820bf5e7d475859f15",
    "patient_id": 9
}*/

    private String getMimeType(String url) {
        if (url.lastIndexOf(".") != -1) {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getMimeTypeFromExtension(ext);
        } else {
            return null;
        }
    }

    private void validateDetailsAndSubmit() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivityBkp.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivityBkp.this);
        Map<String, String> body = new HashMap<>();
        body.put("first_name", binding.etFirstName.getText().toString().trim());
        body.put("middle_name", binding.etMiddleName.getText().toString().trim());
        body.put("last_name", binding.etLastName.getText().toString().trim());
        body.put("dob", binding.etDOB.getText().toString().trim());
        body.put("email", binding.etEmail.getText().toString().trim());
        body.put("emergency_phone", binding.etMobile.getText().toString().trim());//.replaceAll("\\D",""));
        body.put("gender", gender);
        //body.put("member_id", ""); //TODO @remove
        //body.put("membership_card_image", ""); //TODO @remove
        body.put("profile_image", profile_image); //TODO @remove
        body.put("insurance_card_image", insurance_card_image);
        body.put("insurance_card_back_image", insurance_card_back_image);
        body.put("dl_image", dl_image);
        body.put("current_medication_image", current_medication_image);
        body.put("patient_id", patient_id);
        Call<CommonResponse> call = new ServiceHelper().updatePatientProfile(null, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        Utils.toast(response.body().getMessage(), CONTEXT);
                        savePrefsAfterUpdateService();
                        moveToHome();
                    } else {
                        Utils.toast("Server error", CONTEXT);
                    }
                } else {
                    Utils.toast("Server error", CONTEXT);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                Utils.toast("Network error", CONTEXT);
            }
        });
    }

    private void moveToHome() {
        Intent intent = new Intent(UserFormActivityBkp.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidForm() {
        boolean isValid = true;

        if (isFirstNameRequired) {
            isValid = !TextUtils.isEmpty(binding.etFirstName.getText().toString().trim());
        }
        if (isMiddleNameRequired) {
            isValid = isValid && !TextUtils.isEmpty(binding.etMiddleName.getText().toString().trim());
        }
        if (isLastNameRequired) {
            isValid = isValid && !TextUtils.isEmpty(binding.etLastName.getText().toString().trim());
        }
        if (isDobRequired) {
            isValid = isValid && !TextUtils.isEmpty(binding.etDOB.getText().toString().trim());
        }
        if (isPhoneNumberRequired) {
            String phoneNumber = binding.etMobile.getText().toString().trim().replaceAll("\\D", "");
            isValid = isValid && !TextUtils.isEmpty(phoneNumber) && phoneNumber.length() >= 10;
        }
        if (isEmailRequired) { //email validation
            isValid = isValid && !TextUtils.isEmpty(binding.etEmail.getText().toString().trim());
        }
        if (isGenderRequired) {
            isValid = isValid && !TextUtils.isEmpty(gender);
        }
        if (isInsuranceCardRequired) {
            isValid = isValid && isInsuranceCardPhotoPicked;
        }
        if (isInsuranceCardBackRequired) {
            isValid = isValid && isInsuranceCardBackPhotoPicked;
        }
        if (isMemberPhotoRequired) {
            isValid = isValid && isMemberPhotoPicked;
        }
        if (isDLRequired) {
            isValid = isValid && isDLPhotoPicked;
        }
        if (isCurrentMedicationRequired) {
            isValid = isValid && isCurrentMedicationPhotoPicked;
        }
        return isValid && isHipaaTermsAccepted && isLegalTermsAccepted;
    }

    private void showDatePicker() {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int mYear;
        int mMonth;
        int mDay;
        if (isDateSelected) {
            mYear = selectedYear;
            mMonth = selectedMonth - 1;
            mDay = selectedDay;
            c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 10);
        } else {
            mYear = c.get(Calendar.YEAR) - 10;
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            c.set(Calendar.YEAR, mYear);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedYear = year;
                        selectedMonth = monthOfYear + 1;
                        selectedDay = dayOfMonth;
                        isDateSelected = true;
                        binding.etDOB.setText(String.format(Locale.US, "%02d/%02d/%d", monthOfYear + 1, dayOfMonth, year));
                    }
                }, mYear, mMonth, mDay);

//        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime()); //Max date to today
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    private void requestPermissions() {
        // Ask for permissions at runtime.
        // This is just an example set of permissions. Other permissions
        // may be needed, and please refer to our online documents.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_ID) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Utils.toast("Need permissions " + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        UserFormActivityBkp.this);
                finish();
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
        }
    }

    class TextChangeWatcher extends PhoneNumberFormattingTextWatcher {
        public TextChangeWatcher(String us) {
            super(us);
        }

        @Override
        public synchronized void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
//            binding.btnSubmit.setEnabled(isValidForm());
        }
    }


    //21Feb2021 New service flow
    private void checkUserCredentialsServiceCall() {
        String phoneNumber = PreferenceUtil.getData(PrefConstants.PREF_PHONE_NUMBER, UserFormActivityBkp.this);//.replaceAll("\\D","");
        String dob = PreferenceUtil.getData(PrefConstants.PREF_DOB, UserFormActivityBkp.this);
        String firstName = PreferenceUtil.getData(PrefConstants.PREF_FIRST_NAME, UserFormActivityBkp.this);
        String lastName = PreferenceUtil.getData(PrefConstants.PREF_LAST_NAME, UserFormActivityBkp.this);
        ServiceHelper helper = new ServiceHelper();
        Call<UserFormData> call = null; //helper.checkCredentials2(firstName, lastName, dob, phoneNumber, null);
        call.enqueue(new Callback<UserFormData>() {
            @Override
            public void onResponse(@NonNull Call<UserFormData> call, @NonNull Response<UserFormData> response) {
                if (response.isSuccessful()) {
                    UserFormData result = response.body();
                    if (result != null && result.getStatus().equalsIgnoreCase("success")) {
                        analyzeTheUserCredentials(result);
                        savePrefs(result);
                        updateUI();
                    } else {
                        Utils.toast("Server error", UserFormActivityBkp.this);
                        savePrefs(null);
                    }
                } else {
                    if (response.code() == 404) {
                        String errorMsg = "Your member verification has failed. Our Care Coordinator will get in touch with you shortly.";
                        Utils.toast(errorMsg, UserFormActivityBkp.this);
                    } else {
                        Utils.toast("Server error", UserFormActivityBkp.this);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserFormData> call, Throwable t) {
                Utils.toast("Network error", UserFormActivityBkp.this);
            }
        });
    }

    private void analyzeTheUserCredentials(UserFormData result) {
        //TODO: @demo
        AppConstants.userFormData = result;
        if (result.getEmptyFields() == 0) {
            PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, false, UserFormActivityBkp.this);
        } else {
            AppConstants.userFormData = result;
            PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, true, UserFormActivityBkp.this);
        }
    }

    private void savePrefs(UserFormData result) {
        if (result != null) {//update prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, result.getToken(), UserFormActivityBkp.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, result.getPatientId(), UserFormActivityBkp.this);
        }
    }

    private void savePrefsAfterUpdateService() {
        PreferenceUtil.saveData(PrefConstants.PREF_DOB, binding.etDOB.getText().toString().trim(), UserFormActivityBkp.this);
        PreferenceUtil.saveData(PrefConstants.PREF_FIRST_NAME, binding.etFirstName.getText().toString().trim(), UserFormActivityBkp.this);
        PreferenceUtil.saveData(PrefConstants.PREF_LAST_NAME, binding.etLastName.getText().toString().trim(), UserFormActivityBkp.this);
    }

}