package com.lzj.its.sharingpark;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import okhttp3.OkHttpClient;


public class MyApplication extends Application {

    private String NAME;
    private String S;
    private OkHttpClient client ;

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }

    public String getName() {
        return NAME;
    }

    public void setName(String name) {
        this.NAME = name;
    }

    public String getS() {
        return S;
    }

    public void setS(String s) {
        this.S = s;
    }
    public OkHttpClient getOkHttpClient() {
        return client;
    }
    public void setOkHttpClient(OkHttpClient client) {
        this.client =client;
    }
}
