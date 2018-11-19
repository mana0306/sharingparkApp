package com.lzj.its.sharingpark.activity;

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

public class AccountActivity extends BaseActivity {
    private SuperTextView stv_username;
    private SuperTextView stv_nickname;
    private SuperTextView stv_phone;
    private SuperTextView stv_credit;
    private FloatingActionButton fab_account;

    private void initViews() {
        stv_username = findViewById(R.id.stv_username);
        stv_nickname = findViewById(R.id.stv_nickname);
        stv_phone = findViewById(R.id.stv_phone);
        stv_credit = findViewById(R.id.stv_credit);
        fab_account = findViewById(R.id.fab_account);
    }

    private void initUser() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/userCenter")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");

                if (success == 1) {
                    String username = jsonObject.getJSONObject("user").getString("userName");
                    String nickname = jsonObject.getJSONObject("user").getString("nickname");
                    String phone = jsonObject.getJSONObject("user").getString("phone");
                    String credit = jsonObject.getJSONObject("user").getString("credit");
                    runOnUiThread(() -> {
                        //更新UI
                        stv_username.setCenterString(username);
                        stv_nickname.setCenterString(nickname);
                        stv_phone.setCenterString(phone);
                        stv_credit.setCenterString(credit);
                    });

                } else {
                    showToast(jsonObject.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initViews();
        initUser();

        fab_account.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }
}
