package com.intellicare.model.other;

import com.intellicare.model.createvisitmodel.CreateVisitRequest;
import com.intellicare.model.patientinfo.PatientInfo;
import com.intellicare.model.patientinfo.PatientModifiedData;
import com.intellicare.model.patientinfonew.PatientInfoNew;
import com.intellicare.model.response.UserFormData;

public class AppConstants {
    public static PatientInfo PatientInfo;
    public static PatientInfoNew patientInfoNew;
    public static PatientModifiedData patientModifiedData;
    public static UserFormData userFormData;
    public static CreateVisitRequest createVisitRequest = new CreateVisitRequest();
    public static String agora_access_token;
}
