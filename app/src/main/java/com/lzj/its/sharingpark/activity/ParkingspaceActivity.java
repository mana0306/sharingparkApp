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

public class ParkingspaceActivity extends BaseActivity {
    private SuperTextView stv_parkinglot;
    private SuperTextView stv_num;
    private SuperTextView stv_address;
    private SuperTextView stv_more;
    private FloatingActionButton fab_account;

    private void initViews() {
        stv_parkinglot = findViewById(R.id.stv_parkinglot);
        stv_num = findViewById(R.id.stv_num);
        stv_address = findViewById(R.id.stv_address);
        stv_more= findViewById(R.id.stv_more);
    }

    private void initParkingspace() {
        new Thread(() -> {
            try {
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/MyParkingspace")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");

                if (success == 1) {
                    String parkinglot = jsonObject.getJSONObject("parkingspace").getString("parkinglot");
                    String num = jsonObject.getJSONObject("parkingspace").getString("number");
                    String address = jsonObject.getJSONObject("parkingspace").getString("position");
                    String more = jsonObject.getJSONObject("parkingspace").getString("more");
                    runOnUiThread(() -> {
                        //更新UI
                        stv_parkinglot.setCenterString(parkinglot);
                        stv_num.setCenterString(num);
                        stv_address.setCenterString(address);
                        stv_more.setCenterString(more);
                    });

                } if (success == 3) {
                    Intent intent = new Intent(ParkingspaceActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
        setContentView(R.layout.activity_myparkingspace);
        initViews();
        initParkingspace();
    }
}
