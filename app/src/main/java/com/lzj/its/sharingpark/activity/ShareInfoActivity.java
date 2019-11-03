package com.lzj.its.sharingpark.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.bean.SharingBean;
import com.lzj.its.sharingpark.bean.UserBean;

import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.Request;

import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareInfoActivity extends BaseActivity {

    private int share_id;
    private Toolbar tb_share_info;
    private FloatingActionButton fab_use_share;

    private SuperTextView stv_share_id;
    private SuperTextView stv_position;
    private SuperTextView stv_begin_time;
    private SuperTextView stv_end_time;
    private SuperTextView stv_borrower_id;
    private SuperTextView stv_borrower_name;
    private SuperTextView stv_borrower_phone;
    private SuperTextView stv_borrower_credit;
    private SuperTextView stv_cost;
    private SuperTextView stv_more;
    private SuperTextView stv_state;

    private SharingBean sharingBean;

    public void getShareInfo(){
        new Thread(() -> {
            try {

                RequestBody requestBody = new FormBody.Builder()
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .post(requestBody)
                        .url(getString(R.string.api_ip_port) + "/share/"+share_id)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1) {
                    JSONObject share = (JSONObject) jsonObject.get("share");
                    sharingBean = new SharingBean();
                    sharingBean.setShareID(share.getInt("shareID"));
                    sharingBean.setPosition(share.getString("position"));
                    sharingBean.setBeginTime(share.getString("beginTimeString"));
                    sharingBean.setEndTime(share.getString("endTimeString"));

                    UserBean borrower = new UserBean();
                    borrower.setUserID(share.getInt("borrowerID"));
                    borrower.setUserName(share.getString("borrowerName"));
                    borrower.setPhone(share.getString("borrowerPhone"));
                    borrower.setCredit(share.getInt("credit"));
                    sharingBean.setBorrower(borrower);

                    sharingBean.setCost(share.getInt("cost"));
                    sharingBean.setState(share.getInt("state"));
                    sharingBean.setMore(share.getString("more"));

                    runOnUiThread(() -> {
                        //更新UI
                        stv_share_id.setCenterString(String.valueOf(sharingBean.getShareID()));
                        stv_position.setCenterString(sharingBean.getPosition());
                        stv_begin_time.setCenterString(sharingBean.getBeginTime());
                        stv_end_time.setCenterString(sharingBean.getEndTime());

                        stv_borrower_id.setCenterString(String.valueOf(sharingBean.getBorrower().getUserID()));
                        stv_borrower_name.setCenterString(sharingBean.getBorrower().getUserName());
                        stv_borrower_phone.setCenterString(sharingBean.getBorrower().getPhone());
                        stv_borrower_credit.setCenterString(String.valueOf(sharingBean.getBorrower().getCredit()));

                        stv_cost.setCenterString(String.valueOf(sharingBean.getCost()));
                        stv_more.setCenterString(sharingBean.getMore());

                        int color = 0;
                        String state = "";
                        switch (sharingBean.getState()){
                            case 0:
                                state = "待使用";
                                color = Color.GREEN;
                                break;
                            case 1:
                                state = "使用中";
                                color = Color.RED;
                                break;
                            case 2:
                                state = "已使用";
                                color = Color.BLUE;
                            case 3:
                                state = "已撤销";
                                color = Color.GRAY;
                                break;
                        }
                        stv_state.setCenterString(state);
                        stv_state.setCenterTextColor(color);

                    });

                } else {
                    Intent intent = new Intent(ShareInfoActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void use_share(){
        new Thread(()->{
            try {

                RequestBody requestBody = new FormBody.Builder()
                        .add("shareID", String.valueOf(sharingBean.getShareID()))
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .post(requestBody)
                        .url(getString(R.string.api_ip_port) + "/share/"+share_id+"/accept")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1) {
                    showToast(String.format("使用停车位%d成功，停车位地址：%s", sharingBean.getShareID(), sharingBean.getPosition()));
                    startActivity(new Intent(ShareInfoActivity.this, ParkingActivity.class));
                } else {
                    showToast(jsonObject.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    public void initView(){
        stv_share_id = findViewById(R.id.stv_share_id);
        stv_position = findViewById(R.id.stv_position);
        stv_begin_time = findViewById(R.id.stv_begin_time);
        stv_end_time = findViewById(R.id.stv_end_time);
        stv_borrower_id = findViewById(R.id.stv_borrower_id);
        stv_borrower_name = findViewById(R.id.stv_borrower_name);
        stv_borrower_phone = findViewById(R.id.stv_borrower_phone);
        stv_borrower_credit = findViewById(R.id.stv_borrower_credit);
        stv_cost = findViewById(R.id.stv_cost);
        stv_more = findViewById(R.id.stv_more);
        stv_state = findViewById(R.id.stv_state);

        tb_share_info = findViewById(R.id.tb_share_info);
        setSupportActionBar(tb_share_info);

        fab_use_share = findViewById(R.id.fab_use_share);
        fab_use_share.setOnClickListener((View view) -> {
            use_share();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_info);
        initView();

        Bundle bundle = getIntent().getExtras();
        share_id = bundle.getInt("share_id");

        getShareInfo();

    }

}
