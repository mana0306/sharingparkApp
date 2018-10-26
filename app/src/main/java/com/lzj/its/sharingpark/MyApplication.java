package com.lzj.its.sharingpark;

import android.app.Application;

import okhttp3.OkHttpClient;


public class MyApplication extends Application {

    private String NAME;
    private String S;
    private OkHttpClient client ;

    @Override
    public void onCreate() {
        super.onCreate();
        setName("未登录"); //初始化全局变量
        setS("0");
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
