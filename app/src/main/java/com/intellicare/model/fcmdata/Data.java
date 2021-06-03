package com.intellicare.model.fcmdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("is_background")
    @Expose
    private Boolean isBackground;
    @SerializedName("content_available")
    @Expose
    private Boolean contentAvailable;
    @SerializedName("payload")
    @Expose
    private List<Object> payload = null;
    @SerializedName("sound")
    @Expose
    private String sound;
    @SerializedName("icon")
    @Expose
    private Boolean icon;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    public Boolean getIsBackground() {
        return isBackground;
    }

    public void setIsBackground(Boolean isBackground) {
        this.isBackground = isBackground;
    }

    public Boolean getContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(Boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
    }

    public List<Object> getPayload() {
        return payload;
    }

    public void setPayload(List<Object> payload) {
        this.payload = payload;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Boolean getIcon() {
        return icon;
    }

    public void setIcon(Boolean icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}