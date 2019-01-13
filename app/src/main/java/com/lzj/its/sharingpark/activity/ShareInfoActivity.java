package com.lzj.its.sharingpark.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

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

    private TextView tv_share_id;
    private TextView tv_position;
    private TextView tv_begin_time;
    private TextView tv_end_time;
    private TextView tv_borrower_id;
    private TextView tv_borrower_name;
    private TextView tv_borrower_phone;
    private TextView tv_borrower_credit;
    private TextView tv_cost;
    private TextView tv_more;
    private TextView tv_state;

    private SharingBean sharingBean;

    public void getShareInfo(){
        new Thread(() -> {
            try {

                RequestBody requestBody = new FormBody.Builder()
                        .build();
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
                        tv_share_id.setText(String.format(getString(R.string.info_share_id), String.valueOf(sharingBean.getShareID())));
                        tv_position.setText(String.format(getString(R.string.info_position), sharingBean.getPosition()));
                        tv_begin_time.setText(String.format(getString(R.string.info_begin_time), sharingBean.getBeginTime()));
                        tv_end_time.setText(String.format(getString(R.string.info_end_time), sharingBean.getEndTime()));

                        tv_borrower_id.setText(String.format(getString(R.string.info_borrower_id), String.valueOf(sharingBean.getBorrower().getUserID())));
                        tv_borrower_name.setText(String.format(getString(R.string.info_borrower_name), sharingBean.getBorrower().getUserName()));
                        tv_borrower_phone.setText(String.format(getString(R.string.info_borrower_phone), sharingBean.getBorrower().getPhone()));
                        tv_borrower_credit.setText(String.format(getString(R.string.info_borrower_credit), String.valueOf(sharingBean.getBorrower().getCredit())));

                        tv_cost.setText(String.format(getString(R.string.info_cost), String.valueOf(sharingBean.getCost())));
                        tv_more.setText(String.format(getString(R.string.info_more), sharingBean.getMore()));

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
                        }
                        tv_state.setText(String.format(getString(R.string.info_state), state));
                        tv_state.setTextColor(color);

                    });

                } else {
                    showToast(jsonObject.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void initView(){
        tv_share_id = findViewById(R.id.tv_share_id);
        tv_position = findViewById(R.id.tv_position);
        tv_begin_time = findViewById(R.id.tv_begin_time);
        tv_end_time = findViewById(R.id.tv_end_time);
        tv_borrower_id = findViewById(R.id.tv_borrower_id);
        tv_borrower_name = findViewById(R.id.tv_borrower_name);
        tv_borrower_phone = findViewById(R.id.tv_borrower_phone);
        tv_borrower_credit = findViewById(R.id.tv_borrower_credit);
        tv_cost = findViewById(R.id.tv_cost);
        tv_more = findViewById(R.id.tv_more);
        tv_state = findViewById(R.id.tv_state);

        tb_share_info = findViewById(R.id.tb_share_info);
        setSupportActionBar(tb_share_info);

        fab_use_share = findViewById(R.id.fab_use_share);
        fab_use_share.setOnClickListener((View view) -> showToast(Integer.toString(share_id)));
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
