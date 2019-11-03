package com.lzj.its.sharingpark.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;

import org.json.JSONObject;

import okhttp3.Request;
import okhttp3.Response;

public class CarActivity extends BaseActivity {
    private SuperTextView stv_carbrand;
    private SuperTextView stv_carmodel;
    private SuperTextView stv_carid;
    private FloatingActionButton fab_account;

    private void initViews() {
        stv_carbrand = findViewById(R.id.stv_carbrand);
        stv_carmodel = findViewById(R.id.stv_carmodel);
        stv_carid = findViewById(R.id.stv_carid);
    }

    private void initCar() {
        new Thread(() -> {
            try {
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/Mycar")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");

                if (success == 1) {
                    String carbrand = jsonObject.getJSONObject("car").getString("carbrand");
                    String carmodel = jsonObject.getJSONObject("car").getString("carmodel");
                    String carid = jsonObject.getJSONObject("car").getString("carid");
                    runOnUiThread(() -> {
                        //更新UI
                        stv_carbrand.setCenterString(carbrand);
                        stv_carmodel.setCenterString(carmodel);
                        stv_carid.setCenterString(carid);
                    });

                } else {
                    Intent intent = new Intent(CarActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycar);
        initViews();
        initCar();
    }
}
