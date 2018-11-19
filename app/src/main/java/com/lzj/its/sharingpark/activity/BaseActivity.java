package com.lzj.its.sharingpark.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.lzj.its.sharingpark.MyApplication;
import com.lzj.its.sharingpark.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class BaseActivity extends AppCompatActivity {

    protected OkHttpClient client;
    private MyApplication app;
    protected String session;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_base);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show());

        app = (MyApplication) getApplication(); //获得我们的应用程序MyApplication;
        OkHttpClient client0 = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        app.setOkHttpClient(client0);
        client = app.getOkHttpClient();
        session = app.getS();
    }

    public void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
