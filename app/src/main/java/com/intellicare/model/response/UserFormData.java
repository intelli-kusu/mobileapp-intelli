package com.intellicare.model.response;

import com.google.gson.annotations.SerializedName;
import com.intellicare.model.patientinfo.Field;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFormData {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("token") //new
    private String token;
    @SerializedName("patient_id")
    private String patientId;
    @SerializedName("fields")
    private List<Field> fields = null;
    @SerializedName("empty_fields")
    private Integer emptyFields;

    public Map<String, Field> getFieldMap() {
        Map<String, Field> fieldMap = new HashMap<>();
        if (fields != null) {
            for (Field f : fields) {
                fieldMap.put(f.getLabel(), f);
            }
        }
        return fieldMap;
    }

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

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Integer getEmptyFields() {
        return emptyFields;
    }

    public void setEmptyFields(Integer emptyFields) {
        this.emptyFields = emptyFields;
    }
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}