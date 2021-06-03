package com.intellicare.view.form;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.gson.Gson;
import com.intellicare.R;
import com.intellicare.databinding.ActivityUserFormBinding;
import com.intellicare.model.createvisitmodel.Allergy;
import com.intellicare.model.fileupload.FileUploadResponse;
import com.intellicare.model.other.AllergyData;
import com.intellicare.model.other.AppConstants;
import com.intellicare.model.other.EthinicityResponse;
import com.intellicare.model.other.PrefConstants;
import com.intellicare.model.other.SeverityData;
import com.intellicare.model.patientinfo.Field;
import com.intellicare.model.patientinfonew.PatientInfoNew;
import com.intellicare.model.patientinfonew.UserFormRequest;
import com.intellicare.model.response.CommonResponse;
import com.intellicare.model.response.UserFormData;
import com.intellicare.net.ServiceHelper;
import com.intellicare.utils.DialogUtil;
import com.intellicare.utils.LocaleManager;
import com.intellicare.utils.PreferenceUtil;
import com.intellicare.utils.Utils;
import com.intellicare.view.base.BaseActivity;
import com.intellicare.view.dashboard.DashboardActivity;
import com.intellicare.view.search.PharmacySearchActivity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class UserFormActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UserFormActivity";
    //new addition for Storage
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //new addition for storage
    private static final int PERMISSION_REQ_ID = 10088;
    private final String[] mimeTypes = {"image/png", "image/jpg", "image/jpeg"};
    private final Context CONTEXT = UserFormActivity.this;
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
    private final boolean isCurrentMedicationRequired = false;
    private final boolean isRelationRequired = true; //default
    private final boolean isMemberIdRequired = true; //default
    private final List<String> currentMedImages = new ArrayList<>();
    private ActivityUserFormBinding binding;
    private boolean isInsuranceCardPhotoPicked = false;
    private boolean isInsuranceCardBackPhotoPicked = false;
    private boolean isOfficialPhotoPicked = false;
    private boolean isCurrentMedicationPhoto1Picked = false;
    private boolean isCurrentMedicationPhoto2Picked = false;
    private boolean isCurrentMedicationPhoto3Picked = false;
    private boolean isCurrentMedicationPhoto4Picked = false;
    private boolean isLegalTermsAccepted = true;
    private boolean isHipaaTermsAccepted = true;
    private boolean isFirstNameRequired = false;
    private boolean isMiddleNameRequired = false;
    private boolean isLastNameRequired = false;
    private boolean isDobRequired = false;
    private boolean isPhoneNumberRequired = false;
    private boolean isEmailRequired = false;
    private boolean isGenderRequired = false;
    private boolean isInsuranceCardRequired = false;
    private boolean isInsuranceCardBackRequired = false;
    private boolean isOfficialPhotoRequired = false;
    private boolean isPharmacySelected = false;
    private String imageType = "";
    private String insurance_card_image = "";
    private String insurance_card_back_image = "";
    private String official_id_photo = "";
    private File file;
    private String mimeType;
    private int selectedPhotoId = -1;
    private String gender;
    //new changes
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;
    private boolean isDateSelected;
    private boolean isCurrentMedPhoto;
    //new UI realted fields
    private String pharmacy;
    private String pid;
    private AllergyData allergyData;
    private SeverityData severityData;
    private List<Allergy> allergyWithSeverityList;
    private boolean isAllergyLoaded = false;
    private boolean isSeverityLoaded = false;
    private boolean isEthnicityLoaded = false;
    private String ethnicity = "";
    private String memberId = "";
    private String relationShip = "";
    private PatientInfoNew patientInfoNew;
    private EthinicityResponse ethinicityResponse;
    private List<Allergy> previouslySelectedAllergies;

    private void updateButtonState() {
//        binding.btnSubmit.setEnabled(isValidForm());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        loadFormattedLabels();
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
        try {
            Intent data = getIntent();
            if (data != null) {
                /*boolean isFromDashboard = data.getBooleanExtra("isFromDashboard", false);
                if(isFromDashboard) {
                    checkUserCredentialsServiceCall();
                } else {
                    updateUI();
                }*/

                allergyWithSeverityList = new ArrayList<>();
                getAllergies();
                getSeverities();
                getEthnicities();
                getPatientInfoNew();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFormattedLabels() {
        binding.tvCapturePicInsuranceCardDescription.setText(
                HtmlCompat.fromHtml(getString(R.string.formatted_capture_pic_insurance_card_description),
                        HtmlCompat.FROM_HTML_MODE_COMPACT));

        binding.tvCapturePicMedicationDescription.setText(
                HtmlCompat.fromHtml(getString(R.string.formatted_capture_pic_medication_description),
                        HtmlCompat.FROM_HTML_MODE_COMPACT));
        binding.tvCapturePicOfficialPhotoDescription.setText(
                HtmlCompat.fromHtml(getString(R.string.formatted_capture_pic_official_photo_description),
                        HtmlCompat.FROM_HTML_MODE_COMPACT));
    }

    private void updateUI() {
        if (patientInfoNew != null
                && patientInfoNew.getPatient() != null
                && patientInfoNew.getPatient().getFieldMap() != null
                && patientInfoNew.getPatient().getFieldMap().size() > 0) {
            Map<String, Field> fieldMap = patientInfoNew.getPatient().getFieldMap();
            Field firstNameField = fieldMap.get("first_name");
            Field middleNameField = fieldMap.get("middle_name");
            Field lastNameField = fieldMap.get("last_name");
            Field dobField = fieldMap.get("dob");
            Field mobilePhoneField = fieldMap.get("phone_number");
            Field emailField = fieldMap.get("email");
            Field genderField = fieldMap.get("gender");
            Field insuranceCardPhotoField = fieldMap.get("insurance_card_image");
            Field insuranceCardPhotoBackField = fieldMap.get("insurance_card_back_image");
            Field dlPhotoField = fieldMap.get("dl_image");
//            Field currentMedicationPhotoField = fieldMap.get("current_medication_image");

            //new fields as of 24Mar2021

            Field memberIdField = fieldMap.get("member_id");
            Field relationShipField = fieldMap.get("relationship");
            Field pharmacyIdField = fieldMap.get("pharmacy_id");
            Field pharmacyNameField = fieldMap.get("pharmacy_name");
            Field ethnicityField = fieldMap.get("ethnicity");
//            Field allergiesField = fieldMap.get("allergies");

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

            //phone_number
            if (/*mobilePhoneField.getRequired().equalsIgnoreCase("yes")*/ true) { //commented for demo
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

            //dl_image
            if (dlPhotoField.getRequired().equalsIgnoreCase("yes")) {
                isOfficialPhotoRequired = true;
                binding.tvOfficialPhoto.setText(getResources().getText(R.string.official_photo_star));
            } else {
                binding.tvOfficialPhoto.setText(getResources().getText(R.string.official_photo));
            }
            //check for image field value is available or not
            if (!TextUtils.isEmpty(dlPhotoField.getValue())) {
                isOfficialPhotoPicked = true;
                official_id_photo = dlPhotoField.getValue();
            }
            /*//current_medication_image
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
            }*/


            //member_id
            if (!TextUtils.isEmpty(memberIdField.getValue())) {
                memberId = memberIdField.getValue();
            }

            //pharmacy_id
            if (!TextUtils.isEmpty(pharmacyIdField.getValue())) {
                isPharmacySelected = true;
                pid = pharmacyIdField.getValue();
            }
            //pharmacy_name
            if (!TextUtils.isEmpty(pharmacyNameField.getValue())) {
                pharmacy = pharmacyNameField.getValue();
            }

            //relationship
            if (!TextUtils.isEmpty(relationShipField.getValue())) {
                relationShip = relationShipField.getValue();
            }
            //ethnicity
            if (!TextUtils.isEmpty(ethnicityField.getValue())) {
                ethnicity = ethnicityField.getValue();
            }

            //allergies
            /*if (!TextUtils.isEmpty(allergiesField.getValue())) {

                previouslySelectedAllergies = new ArrayList<>();

                try {
                    JSONArray array = new JSONArray(allergiesField.getValue());
                    for (int i = 0; i < array.length(); i++) {
                        Allergy a = new Gson().fromJson(array.getString(i), Allergy.class);
                        previouslySelectedAllergies.add(a);
                    }
                    updateAllergyUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/

            //allergies
            previouslySelectedAllergies = patientInfoNew.getPatient().getPreviouslySelectedAllergies();
            updateAllergyUI();

            fetchCurrentMedicationImageUrls(fieldMap);

            //Fill the available fields
            binding.etFirstName.setText(firstNameField.getValue());
            binding.etMiddleName.setText(middleNameField.getValue());
            binding.etLastName.setText(lastNameField.getValue());
            binding.etDOB.setText(dobField.getValue());
            binding.etMobile.setText(mobilePhoneField.getValue());
            binding.etEmail.setText(emailField.getValue());
            binding.etPharmacy.setText(pharmacy);
            binding.eetMemberId.setText(memberId);
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

            //check for DOB field
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
            updateEthnicitySpinner();
            updateRelationshipSpinner();
            loadImage("insurance", insurance_card_image, binding.ivInsuranceCardPhoto);
            loadImage("insuranceback", insurance_card_back_image, binding.ivInsuranceCardPhotoBack);
            loadImage("dl", official_id_photo, binding.ivOfficialPhoto);
//            loadImage("current-medication", currentMedicationPhotoField, binding.ivMedPhoto1);

        } else {
            finish(); //call service required
        }
    }

    private void fetchCurrentMedicationImageUrls(Map<String, Field> fieldMap) {
        currentMedImages.add("");
        currentMedImages.add("");
        currentMedImages.add("");
        currentMedImages.add("");

        Field currentMedicationPhotoField1 = fieldMap.get("current_medication_image_1");
        Field currentMedicationPhotoField2 = fieldMap.get("current_medication_image_2");
        Field currentMedicationPhotoField3 = fieldMap.get("current_medication_image_3");
        Field currentMedicationPhotoField4 = fieldMap.get("current_medication_image_4");

        //current_medication_image_1
        //check for image field value is available or not
        if (!TextUtils.isEmpty(currentMedicationPhotoField1.getValue())) {
            isCurrentMedicationPhoto1Picked = true;
            currentMedImages.set(0, currentMedicationPhotoField1.getValue());
            loadImage("current-medication", currentMedicationPhotoField1.getValue(), binding.ivMedPhoto1);
        }
        //current_medication_image_2
        //check for image field value is available or not
        if (!TextUtils.isEmpty(currentMedicationPhotoField2.getValue())) {
            isCurrentMedicationPhoto2Picked = true;
            currentMedImages.set(1, currentMedicationPhotoField2.getValue());
            loadImage("current-medication", currentMedicationPhotoField2.getValue(), binding.ivMedPhoto2);
        }

        //current_medication_image_3
        //check for image field value is available or not
        if (!TextUtils.isEmpty(currentMedicationPhotoField3.getValue())) {
            isCurrentMedicationPhoto3Picked = true;
            currentMedImages.set(2, currentMedicationPhotoField3.getValue());
            loadImage("current-medication", currentMedicationPhotoField3.getValue(), binding.ivMedPhoto3);

        }

        //current_medication_image_4
        //check for image field value is available or not
        if (!TextUtils.isEmpty(currentMedicationPhotoField4.getValue())) {
            isCurrentMedicationPhoto4Picked = true;
            currentMedImages.set(3, currentMedicationPhotoField4.getValue());
            loadImage("current-medication", currentMedicationPhotoField4.getValue(), binding.ivMedPhoto4);
        }
    }

    private void updateEthnicitySpinner() {
        List<String> ethnicities;
        if (ethinicityResponse != null && ethinicityResponse.getEthnicity() != null && ethinicityResponse.getEthnicity().size() > 0)
            ethnicities = ethinicityResponse.getEthnicity();
        else
            ethnicities = Arrays.asList("Caucasian", "African American", "American Indian",
                    "Latino", "Asian", "Pacific Islander or Hawaiian");

        int ethinicitySelectedPosition = ethnicities.contains(ethnicity) ? ethnicities.indexOf(ethnicity) : 0;
        binding.ethnicity.setAdapter(new ArrayAdapter<>(UserFormActivity.this, android.R.layout.simple_spinner_item, ethnicities));
        binding.ethnicity.setSelection(ethinicitySelectedPosition);

        binding.ethnicity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ethnicity = ((TextView) view).getText().toString().replace(".", "").trim();
                ((TextView) view).setText(ethnicity);
                try {
                    TypedValue typedValue = new TypedValue();
                    getResources().getValue(R.dimen.spinner_text_size, typedValue, true);
                    float myFloatValue = typedValue.getFloat();
                    ((TextView) view).setTextSize(myFloatValue);
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void updateRelationshipSpinner() {
        List<String> relations = Arrays.asList("Self", "Spouse", "Child", "Other");

        int relationSelectedPosition = relations.contains(relationShip) ? relations.indexOf(relationShip) : 0;
        binding.patientRelationship.setAdapter(new ArrayAdapter<>(UserFormActivity.this, android.R.layout.simple_spinner_item, relations));
        binding.patientRelationship.setSelection(relationSelectedPosition);

        binding.patientRelationship.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                relationShip = ((TextView) view).getText().toString().replace(".", "").trim();
                ((TextView) view).setText(relationShip);
                try {
                    TypedValue typedValue = new TypedValue();
                    getResources().getValue(R.dimen.spinner_text_size, typedValue, true);
                    float myFloatValue = typedValue.getFloat();
                    ((TextView) view).setTextSize(myFloatValue);
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadImage(String type, String url, ImageView imageView) {
        if (!TextUtils.isEmpty(url)) {
            getImage(type, url, imageView);
        }
    }

    private void getImage(String type, String imageId, ImageView imageView) {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivity.this);
        Call<ResponseBody> apiCall = new ServiceHelper().getViewImage(patient_id, type, imageId, token);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bm = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bm);
                } else {
                    Log.e(TAG, "image download failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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
        binding.etPharmacy.setOnClickListener(this);
        binding.ivInsuranceCardPhoto.setOnClickListener(this);
        binding.ivInsuranceCardPhotoBack.setOnClickListener(this);
        binding.ivOfficialPhoto.setOnClickListener(this);
        binding.ivMedPhoto1.setOnClickListener(this);
        binding.ivMedPhoto2.setOnClickListener(this);
        binding.ivMedPhoto3.setOnClickListener(this);
        binding.ivMedPhoto4.setOnClickListener(this);

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
            } else if (f.getValue().equalsIgnoreCase("female")) {
                binding.rbFemale.setChecked(true);
                gender = "Female";
            } else {
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
//                Utils.toast("Legal Terms", UserFormActivity.this);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        int startPos = 0;
        int endPos = 0;

        if(LocaleManager.getLanguage(UserFormActivity.this).equalsIgnoreCase(LocaleManager.ENGLISH)) {
            startPos = 11;
            endPos = 34;
        } else {
            startPos = 11;
            endPos = 42;
        }

        ss.setSpan(clickableSpan, startPos, endPos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
//                Utils.toast("HIPAA Terms", UserFormActivity.this);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
        int startPos1 = 0;
        int endPos1 = 0;

        if(LocaleManager.getLanguage(UserFormActivity.this).equalsIgnoreCase(LocaleManager.ENGLISH)) {
            startPos1 = 11;
            endPos1 = 22;
        } else {
            startPos1 = 11;
            endPos1 = 28;
        }
        ss2.setSpan(clickableSpan2, startPos1, endPos1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
                Utils.toastLong(/*"Please provide all the mandatory fields"*/ getString(R.string.fields_error_note), UserFormActivity.this);
        } else if (id == R.id.etDOB) {
            showDatePicker();
        } else if (id == R.id.ivInsuranceCardPhoto
                || id == R.id.ivInsuranceCardPhotoBack
                || id == R.id.ivOfficialPhoto) {
            selectedPhotoId = id;
            showPickerDialog(getString(R.string.image_picker_title));
        } else if (id == R.id.ivMedPhoto1
                || id == R.id.ivMedPhoto2
                || id == R.id.ivMedPhoto3
                || id == R.id.ivMedPhoto4) {
            isCurrentMedPhoto = true;
            selectedPhotoId = id;
            if (showDeletePhotoOption())
                showPickerDialog(getString(R.string.image_picker_title), true);
            else
                showPickerDialog(getString(R.string.image_picker_title));
        } else if (id == R.id.etPharmacy) {
            Intent intent = new Intent(UserFormActivity.this, PharmacySearchActivity.class);
            startActivityForResult(intent, 9003);
        }
    }

    @Override
    protected void onDeletePhotoClick() {
        super.onDeletePhotoClick();
        if (selectedPhotoId == R.id.ivMedPhoto1) {
            removeImageServiceCall(currentMedImages.get(0));
        } else if (selectedPhotoId == R.id.ivMedPhoto2) {
            removeImageServiceCall(currentMedImages.get(1));
        } else if (selectedPhotoId == R.id.ivMedPhoto3) {
            removeImageServiceCall(currentMedImages.get(2));
        } else if (selectedPhotoId == R.id.ivMedPhoto4) {
            removeImageServiceCall(currentMedImages.get(3));
        }
    }

    @Override
    protected void onPositiveClick() { //camera
        float crop_x;
        float crop_y;

        if (isCurrentMedPhoto) {
            crop_x = 2f;
            crop_y = 3f;
        } else {
            crop_x = 3f;
            crop_y = 2f;
        }


        ImagePicker.Companion.with(UserFormActivity.this)
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

        if (isCurrentMedPhoto) {
            crop_x = 2f;
            crop_y = 3f;
        } else {
            crop_x = 3f;
            crop_y = 2f;
        }
        ImagePicker.Companion.with(UserFormActivity.this)
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
            if (requestCode == 9003) {
                pharmacy = intent.getStringExtra("pharmacy");
                pid = intent.getStringExtra("pid");
                if (!pharmacy.isEmpty()) {
                    binding.etPharmacy.setText(pharmacy);
                    isPharmacySelected = true;
                    AppConstants.createVisitRequest.setPharmacyId(pid);
                    AppConstants.createVisitRequest.setAllergies(getSelectedAllergies());
                }
            } else {
                //Image Uri will not be null for RESULT_OK
                Uri fileUri = intent.getData();
                setImage(fileUri);
                mimeType = getMimeType(fileUri.toString());

                //You can get File object from intent
                file = ImagePicker.Companion.getFile(intent);
                //You can also get File Path from intent
                String filePath = ImagePicker.Companion.getFilePath(intent);
                uploadImage();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Utils.toast(ImagePicker.Companion.getError(intent), CONTEXT);
            selectedPhotoId = -1;
        } else {
            selectedPhotoId = -1;
            Log.e("UserForm", "Task Cancelled");
        }
    }

    private void uploadImage() {
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<FileUploadResponse> call = new ServiceHelper().uploadFile(patient_id,
                imageType,
                token,
                file,
                mimeType);

        call.enqueue(new Callback<FileUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<FileUploadResponse> call, @NonNull Response<FileUploadResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful() && response.body() != null) {
                    FileUploadResponse response1 = response.body();
                    String imageId = response1.getId();
                    if (selectedPhotoId == R.id.ivInsuranceCardPhoto) {
                        insurance_card_image = imageId;
                        isInsuranceCardPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivInsuranceCardPhotoBack) {
                        insurance_card_back_image = imageId;
                        isInsuranceCardBackPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivOfficialPhoto) {
                        official_id_photo = imageId;
                        isOfficialPhotoPicked = true;
                    } else if (selectedPhotoId == R.id.ivMedPhoto1) {
                        currentMedImages.set(0, imageId);
                        isCurrentMedicationPhoto1Picked = true;
                    } else if (selectedPhotoId == R.id.ivMedPhoto2) {
                        currentMedImages.set(1, imageId);
                        isCurrentMedicationPhoto2Picked = true;
                    } else if (selectedPhotoId == R.id.ivMedPhoto3) {
                        currentMedImages.set(2, imageId);
                        isCurrentMedicationPhoto3Picked = true;
                    } else if (selectedPhotoId == R.id.ivMedPhoto4) {
                        currentMedImages.set(3, imageId);
                        isCurrentMedicationPhoto4Picked = true;
                    }

                    //reset helper fields
                    selectedPhotoId = -1;
                    imageType = "";
                }
            }

            @Override
            public void onFailure(@NonNull Call<FileUploadResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
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
            } else if (selectedPhotoId == R.id.ivOfficialPhoto) {
                binding.ivOfficialPhoto.setImageURI(imageUri);
                imageType = "dl";
            } else if (selectedPhotoId == R.id.ivMedPhoto1) {
                binding.ivMedPhoto1.setImageURI(imageUri);
//                binding.ivMedPhoto1Remove.setVisibility(View.VISIBLE);
                imageType = "current-medication";
            } else if (selectedPhotoId == R.id.ivMedPhoto2) {
                binding.ivMedPhoto2.setImageURI(imageUri);
//                binding.ivMedPhoto2Remove.setVisibility(View.VISIBLE);
                imageType = "current-medication";
            } else if (selectedPhotoId == R.id.ivMedPhoto3) {
                binding.ivMedPhoto3.setImageURI(imageUri);
//                binding.ivMedPhoto3Remove.setVisibility(View.VISIBLE);
                imageType = "current-medication";
            } else if (selectedPhotoId == R.id.ivMedPhoto4) {
                binding.ivMedPhoto4.setImageURI(imageUri);
//                binding.ivMedPhoto4Remove.setVisibility(View.VISIBLE);
                imageType = "current-medication";
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

    private void validateDetailsAndSubmit() {

        /*
            {
    "first_name": "Sailesh",
    "middle_name": "",
    "last_name": "Jaiswal",
    "dob": "08/23/1983",
    "email": "jaiswal@test.com",
    "phone_number": "(934) 667-8561",
    "gender": "Male",
    "ethnicity": "Asian",
    "member_id": 123111,
    "relationship": "Self",
    "pharmacy_id": "11678719",
    "patient_id": 89,
    "dl_image": "2ff5fef02962684453",
    "insurance_card_image": "34b3bfa1e486280252",
    "insurance_card_back_image": "83d19dc09bb4b59d55",
    "current_medication_image_1": "",
    "current_medication_image_2": "",
    "current_medication_image_3": "",
    "current_medication_image_4": ""
    "allergies": [
        {
            "allergy_id": 1,
            "allergy": "Penicillin (Amoxicillin)",
            "severity_id": 1,
            "severity": "Hives/Rash"
        }
    ],
}*/
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivity.this);

        UserFormRequest body = new UserFormRequest();

        body.setFirstName(binding.etFirstName.getText().toString().trim());
        body.setMiddleName(binding.etMiddleName.getText().toString().trim());
        body.setLastName(binding.etLastName.getText().toString().trim());
        body.setDob(binding.etDOB.getText().toString().trim());
        body.setEmail(binding.etEmail.getText().toString().trim());
        body.setPhoneNumber(binding.etMobile.getText().toString().trim());//.replaceAll("\\D",""));
        body.setGender(gender);
        body.setEthnicity(ethnicity);
        body.setMemberId(binding.eetMemberId.getText().toString().trim());
        body.setRelationship(relationShip);
        body.setPharmacyId(pid);

        //TODO: @remove
        /*Member m = patientInfoNew.getPatient().getMembersMap().get("Self");
        body.put("profile_image", m.getProfileImage());*/

        body.setInsuranceCardImage(insurance_card_image);
        body.setInsuranceCardBackImage(insurance_card_back_image);
        body.setDlImage(official_id_photo);
//        body.put("current_medication_image", current_medication_image);
        body.setCurrentMedicationImage1(currentMedImages.get(0));
        body.setCurrentMedicationImage2(currentMedImages.get(1));
        body.setCurrentMedicationImage3(currentMedImages.get(2));
        body.setCurrentMedicationImage4(currentMedImages.get(3));
        body.setPatientId(patient_id);
        body.setAllergies(getSelectedAllergies());
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));

        Call<CommonResponse> call = new ServiceHelper().updatePatientProfile(body, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
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
                DialogUtil.hideProgressDialog();
                Utils.toast("Network error", CONTEXT);
            }
        });
    }

    private void moveToHome() {
        Intent intent = new Intent(UserFormActivity.this, DashboardActivity.class);
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
        if (isOfficialPhotoRequired) {
            isValid = isValid && isOfficialPhotoPicked;
        }
        if (isMemberIdRequired) {
            isValid = isValid && !TextUtils.isEmpty(binding.eetMemberId.getText().toString().trim());
        }
        if (isRelationRequired) {
            isValid = isValid && !TextUtils.isEmpty(relationShip);
        }
        /*if (isCurrentMedicationRequired) { //Non-Mandatory
            isValid = isValid && isCurrentMedicationPhotoPicked;
        }*/
        return isValid && isHipaaTermsAccepted && isLegalTermsAccepted && isPharmacySelected;
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
                        UserFormActivity.this);
                finish();
            }

            // Here we continue only if all permissions are granted.
            // The permissions can also be granted in the system settings manually.
        }
    }

    //21Feb2021 New service flow
    //Not using as of 24Mar2021
    private void checkUserCredentialsServiceCall() {
        String phoneNumber = PreferenceUtil.getData(PrefConstants.PREF_PHONE_NUMBER, UserFormActivity.this);//.replaceAll("\\D","");
        String dob = PreferenceUtil.getData(PrefConstants.PREF_DOB, UserFormActivity.this);
        String firstName = PreferenceUtil.getData(PrefConstants.PREF_FIRST_NAME, UserFormActivity.this);
        String lastName = PreferenceUtil.getData(PrefConstants.PREF_LAST_NAME, UserFormActivity.this);
        String deviceToken = PreferenceUtil.getData(PrefConstants.PREF_FCM_TOKEN, UserFormActivity.this);

        ServiceHelper helper = new ServiceHelper();
        Call<UserFormData> call = null; //helper.checkCredentials2(firstName, lastName, dob, phoneNumber, deviceToken);
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
                        Utils.toast("Server error", UserFormActivity.this);
                        savePrefs(null);
                    }
                } else {
                    if (response.code() == 404) {
                        String errorMsg = "Your member verification has failed. Our Care Coordinator will get in touch with you shortly.";
                        Utils.toast(errorMsg, UserFormActivity.this);
                    } else {
                        Utils.toast("Server error", UserFormActivity.this);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserFormData> call, Throwable t) {
                Utils.toast("Network error", UserFormActivity.this);
            }
        });
    }

    private void analyzeTheUserCredentials(UserFormData result) {
        //TODO: @demo
        AppConstants.userFormData = result;
        if (result.getEmptyFields() == 0) {
            PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, false, UserFormActivity.this);
        } else {
            AppConstants.userFormData = result;
            PreferenceUtil.saveBoolean(PrefConstants.PREF_IS_EMPTY_FIELD_AVAILABLE, true, UserFormActivity.this);
        }
    }

    private void savePrefs(UserFormData result) {
        if (result != null) {//update prefs
            PreferenceUtil.saveData(PrefConstants.PREF_APP_TOKEN, result.getToken(), UserFormActivity.this);
            PreferenceUtil.saveData(PrefConstants.PREF_PATIENT_ID, result.getPatientId(), UserFormActivity.this);
        }
    }

    private void savePrefsAfterUpdateService() {
        PreferenceUtil.saveData(PrefConstants.PREF_DOB, binding.etDOB.getText().toString().trim(), UserFormActivity.this);
        PreferenceUtil.saveData(PrefConstants.PREF_FIRST_NAME, binding.etFirstName.getText().toString().trim(), UserFormActivity.this);
        PreferenceUtil.saveData(PrefConstants.PREF_LAST_NAME, binding.etLastName.getText().toString().trim(), UserFormActivity.this);
        PreferenceUtil.saveData(PrefConstants.PREF_PHONE_NUMBER, binding.etMobile.getText().toString().trim(), UserFormActivity.this);
    }

    //new UI related calls
    private void getAllergies() {
        Call<AllergyData> call = new ServiceHelper().getAllergies();
        call.enqueue(new Callback<AllergyData>() {
            @Override
            public void onResponse(@NonNull Call<AllergyData> call, @NonNull Response<AllergyData> response) {
                if (response.isSuccessful()) {
                    allergyData = response.body();
                    isAllergyLoaded = true;
                    /*if (isAllergyLoaded && isSeverityLoaded)
                        updateAllergyUI();*/
                } else {
                    Log.e("onResponse", "Failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllergyData> call, @NonNull Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    private void getSeverities() {
        Call<SeverityData> call = new ServiceHelper().getSeverities();
        call.enqueue(new Callback<SeverityData>() {
            @Override
            public void onResponse(@NonNull Call<SeverityData> call, @NonNull Response<SeverityData> response) {
                if (response.isSuccessful()) {
                    severityData = response.body();
                    isSeverityLoaded = true;
                    /*if (isAllergyLoaded && isSeverityLoaded)
                        updateAllergyUI();*/
                } else {
                    Log.e("onResponse", "Failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SeverityData> call, @NonNull Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    private void getEthnicities() {
        Call<EthinicityResponse> call = new ServiceHelper().getEthnicities();
        call.enqueue(new Callback<EthinicityResponse>() {
            @Override
            public void onResponse(@NonNull Call<EthinicityResponse> call, @NonNull Response<EthinicityResponse> response) {
                if (response.isSuccessful()) {
                    ethinicityResponse = response.body();
                    isEthnicityLoaded = true;
                } else {
                    Log.e("onResponse", "Failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<EthinicityResponse> call, @NonNull Throwable t) {
                Log.e("onFailure", t.getMessage());
            }
        });
    }

    private int getSeveritySelectedPosition(String id) {
        int selectedPosition = 0;
        for (int i = 0; i < severityData.getSeverityList().size(); i++) {
            SeverityData.Severity s = severityData.getSeverityList().get(i);
            if (s.getId().equalsIgnoreCase(id)) {
                selectedPosition = i;
                break;
            }
        }
        return selectedPosition;
    }

    private void updateAllergyUI() {
        if (severityData == null) {
            severityData = getLocalSeverityData();
        }
        List<SeverityData.Severity> severities = severityData.getSeverityList();
        List<String> sevNames = new ArrayList<>();
        sevNames.add(getString(R.string.select_severity));
        for (int i = 0; i < severities.size(); i++) {
            sevNames.add(severities.get(i).getSeverityName());
        }
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sevNames);
//        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sevNames);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(UserFormActivity.this, R.layout.custom_simple_spinner_item, sevNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //new design 01-Feb-2021
        boolean[] selectedPositions = new boolean[10];
        Arrays.fill(selectedPositions, false);

        if (allergyData == null) {
            allergyData = getLocalAllergyData();
        }

        if (allergyData != null) {
            List<AllergyData.Allergy> allergies = allergyData.getAllergyList();
            int numCheckBoxes = allergies.size();
            selectedPositions = new boolean[numCheckBoxes];
            //Arrays.fill(selectedPositions, false);
            if (binding.llCheckBoxes.getChildCount() > 0)
                binding.llCheckBoxes.removeAllViews();

            allergyWithSeverityList = prepareAllergyListWithSeverityList(allergyData);

            if (previouslySelectedAllergies != null && previouslySelectedAllergies.size() > 0) {
                for (Allergy selectedAllergy : previouslySelectedAllergies) {
                    for (int i = 0; i < allergyWithSeverityList.size(); i++) {
                        Allergy allergy = allergyWithSeverityList.get(i);
                        if (selectedAllergy.getAllergyId().equals(allergy.getAllergyId())) {
                            allergyWithSeverityList.get(i).setSeverity(selectedAllergy.getSeverity());
                            allergyWithSeverityList.get(i).setSeverityId(selectedAllergy.getSeverityId());

                            allergyWithSeverityList.get(i).setSelected(true);
                            allergyWithSeverityList.get(i).setSeveritySelectedPosition(
                                    getSeveritySelectedPosition(selectedAllergy.getSeverityId()) + 1);
                        }
                    }
                }
                for (int i = 0; i < allergyWithSeverityList.size(); i++) {
                    selectedPositions[i] = allergyWithSeverityList.get(i).isSelected();
                }
            }

            for (int i = 0; i < numCheckBoxes; i++) {
                final LinearLayout llAllergy = (LinearLayout) LayoutInflater.from(UserFormActivity.this).inflate(R.layout.cb_item_allergy, null);
                final CheckBox checkBox = llAllergy.findViewById(R.id.checkbox);
                final AppCompatSpinner spinner = llAllergy.findViewById(R.id.severitySpinner);
                spinner.setAdapter(spinnerAdapter);
                spinner.setVisibility(selectedPositions[i] ? View.VISIBLE : View.GONE);
                checkBox.setText(allergies.get(i).getAllergyName());
                checkBox.setChecked(selectedPositions[i]);
                checkBox.setTag(i);
                llAllergy.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
                binding.llCheckBoxes.addView(llAllergy);
                boolean[] finalSelectedPositions = selectedPositions;
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        spinner.setVisibility(View.VISIBLE);
                    } else {
                        spinner.setSelection(0);
                        spinner.setVisibility(View.GONE);
                        allergyWithSeverityList.get((int) checkBox.getTag()).setSeveritySelectedPosition(0);
                    }
                    finalSelectedPositions[(int) checkBox.getTag()] = isChecked;
                    allergyWithSeverityList.get((int) checkBox.getTag()).setSelected(isChecked);

                });
                spinner.setSelection(allergyWithSeverityList.get(i).getSeveritySelectedPosition());
                int finalI = i;
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position != 0) {
                            allergyWithSeverityList.get(finalI).setSeveritySelectedPosition(position);
                            allergyWithSeverityList.get(finalI).setSeverity(severityData.getSeverityList().get(position - 1).getSeverityName());
                            allergyWithSeverityList.get(finalI).setSeverityId(severityData.getSeverityList().get(position - 1).getId());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        } else {
            finish(); //Test
        }
    }

    private List<Allergy> prepareAllergyListWithSeverityList(AllergyData allergyData) {
        List<Allergy> allergyWithSeverityList = new ArrayList<>();
        List<AllergyData.Allergy> allergies = allergyData.getAllergyList();
        int count = allergies.size();
        for (int i = 0; i < count; i++) {
            Allergy a = new Allergy();
            a.setAllergy(allergies.get(i).getAllergyName());
            a.setAllergyId(allergies.get(i).getId());
            allergyWithSeverityList.add(a);
        }
        return allergyWithSeverityList;
    }

    private List<Allergy> getSelectedAllergies() {
        List<Allergy> selectedAllergies = new ArrayList<>();
        for (Allergy a : allergyWithSeverityList) {
            if (a.isSelected())
                selectedAllergies.add(a);
        }
        return selectedAllergies;
    }

    private void getPatientInfoNew() { //03Jan
        Log.e("userform", "getPatientInfoNew()");
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivity.this);
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        Call<PatientInfoNew> call = new ServiceHelper().getPatientInfo(patient_id, token);
        call.enqueue(new Callback<PatientInfoNew>() {
            @Override
            public void onResponse(@NonNull Call<PatientInfoNew> call, @NonNull Response<PatientInfoNew> response) {
                Log.e("userform", response.raw().toString());

                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    patientInfoNew = response.body();
                    updateUI();
                } else {
                    Log.e("userform", "onResponse: unsuccess");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientInfoNew> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                Log.e("userform", "onFailure: " + t.getMessage());
            }
        });
    }

    private SeverityData getLocalSeverityData() {
        String json = "{\"severities\":[{\"severity_id\":5,\"severity\":\"Bloating\"},{\"severity_id\":1,\"severity\":\"Hives\\/Rash\"},{\"severity_id\":2,\"severity\":\"Itchiness\"},{\"severity_id\":3,\"severity\":\"Shortness of breath\"},{\"severity_id\":4,\"severity\":\"Swelling\"}],\"status\":\"success\",\"message\":\"Severities found\"}";
        return new Gson().fromJson(json, SeverityData.class);
    }

    private AllergyData getLocalAllergyData() {
        String json = "{\"allergies\":[{\"allergy_id\":4,\"allergy\":\"Antiseizures (Dilantin)\"},{\"allergy_id\":6,\"allergy\":\"Eggs\"},{\"allergy_id\":3,\"allergy\":\"Narcotics (Morphine\\/Codeine)\"},{\"allergy_id\":5,\"allergy\":\"NSAIDs (Aspirin\\/Ibuprofen)\"},{\"allergy_id\":7,\"allergy\":\"Peanut\"},{\"allergy_id\":1,\"allergy\":\"Penicillin (Amoxicillin)\"},{\"allergy_id\":13,\"allergy\":\"Shellfish\"},{\"allergy_id\":2,\"allergy\":\"Sulfas (Bactrim)\"}],\"status\":\"success\",\"message\":\"Allergies found\"}";
        return new Gson().fromJson(json, AllergyData.class);
    }

    //remove image service call 20Apr2021
    private void removeImageServiceCall(String image_id) {
        DialogUtil.showProgressDialog(this, getResources().getString(R.string.common_loader_title), getString(R.string.please_wait));
        String patient_id = PreferenceUtil.getData(PrefConstants.PREF_PATIENT_ID, UserFormActivity.this);
        String token = PreferenceUtil.getData(PrefConstants.PREF_APP_TOKEN, UserFormActivity.this);
        Call<CommonResponse> call = new ServiceHelper().removeImage(patient_id, image_id, token);
        call.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(@NonNull Call<CommonResponse> call, @NonNull Response<CommonResponse> response) {
                DialogUtil.hideProgressDialog();
                if (response.isSuccessful()) {
                    updateMedicationImageUI(response.body());
                } else {
                    updateMedicationImageUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CommonResponse> call, @NonNull Throwable t) {
                DialogUtil.hideProgressDialog();
                updateMedicationImageUI(null);
            }
        });
    }

    private void updateMedicationImageUI(CommonResponse body) {
        if (body == null || body.getStatus().equalsIgnoreCase("failure")) {
            Utils.toastLong("Server error", UserFormActivity.this);
        } else {
            Utils.toast( body.getMessage(), UserFormActivity.this);
            if (selectedPhotoId == R.id.ivMedPhoto1) {
                currentMedImages.set(0, "");
                isCurrentMedicationPhoto1Picked = false;
                binding.ivMedPhoto1.setImageResource(R.drawable.ic_cam2);
            } else if (selectedPhotoId == R.id.ivMedPhoto2) {
                currentMedImages.set(1, "");
                isCurrentMedicationPhoto2Picked = false;
                binding.ivMedPhoto2.setImageResource(R.drawable.ic_cam2);
            } else if (selectedPhotoId == R.id.ivMedPhoto3) {
                currentMedImages.set(2, "");
                isCurrentMedicationPhoto3Picked = false;
                binding.ivMedPhoto3.setImageResource(R.drawable.ic_cam2);
            } else if (selectedPhotoId == R.id.ivMedPhoto4) {
                currentMedImages.set(3, "");
                isCurrentMedicationPhoto4Picked = false;
                binding.ivMedPhoto4.setImageResource(R.drawable.ic_cam2);
            }
        }
    }

    private boolean showDeletePhotoOption() {
        if (selectedPhotoId == R.id.ivMedPhoto1) {
            return !TextUtils.isEmpty(currentMedImages.get(0));
        } else if (selectedPhotoId == R.id.ivMedPhoto2) {
            return !TextUtils.isEmpty(currentMedImages.get(1));
        } else if (selectedPhotoId == R.id.ivMedPhoto3) {
            return !TextUtils.isEmpty(currentMedImages.get(2));
        } else if (selectedPhotoId == R.id.ivMedPhoto4) {
            return !TextUtils.isEmpty(currentMedImages.get(3));
        } else {
            return false;
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
}