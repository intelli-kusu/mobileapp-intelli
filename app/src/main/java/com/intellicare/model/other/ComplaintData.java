package com.intellicare.model.other;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ComplaintData {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("complaints")
    private List<Complaint> complaintList;

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

    public List<Complaint> getComplaintList() {
        return complaintList;
    }

    public void setComplaintList(List<Complaint> complaintList) {
        this.complaintList = complaintList;
    }

    public class Complaint {
        @SerializedName("complaint_id")
        private String id;
        @SerializedName("complaint")
        private String complaintName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getComplaintName() {
            return complaintName;
        }

        public void setComplaintName(String complaintName) {
            this.complaintName = complaintName;
        }
    }
}
