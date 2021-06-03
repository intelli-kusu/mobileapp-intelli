package com.intellicare.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IntelliClient {
    private final static String BASE_URL = "https://staging.intellicare.health/api/v1/";
    private static IntelliClient client;
    private String REQUEST_TAG = "INTELLI";

    private IntelliClient() {

    }

    public static IntelliClient getInstance() {
        if (null == client) {
            client = new IntelliClient();
        }
        return client;
    }

    private Retrofit buildServiceHandler() {
        OkHttpClient client = getLoggingClient();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .callFactory(request -> {
                    request = request.newBuilder().tag(new String[]{REQUEST_TAG}).build();
                    return client.newCall(request);
                })
                .build();
    }

    public IntelliAPI getService() {
        return buildServiceHandler().create(IntelliAPI.class);
    }

    public IntelliAPI getService(String requestTag) {
        this.REQUEST_TAG = requestTag;
        return buildServiceHandler().create(IntelliAPI.class);
    }

    private OkHttpClient getLoggingClient() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor);
        return httpBuilder.build();
    }
}
