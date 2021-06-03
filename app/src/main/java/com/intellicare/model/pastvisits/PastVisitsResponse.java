package com.intellicare.model.pastvisits;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PastVisitsResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("messages")
    @Expose
    private String messages;

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("total_visits")
    @Expose
    private Integer totalVisits;
    @SerializedName("total_pages")
    @Expose
    private Integer totalPages;
    @SerializedName("current_page")
    @Expose
    private Integer currentPage;
    @SerializedName("consults")
    @Expose
    private List<Consult> consults = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public Integer getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(Integer totalVisits) {
        this.totalVisits = totalVisits;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<Consult> getConsults() {
        return consults;
    }

    public void setConsults(List<Consult> consults) {
        this.consults = consults;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}