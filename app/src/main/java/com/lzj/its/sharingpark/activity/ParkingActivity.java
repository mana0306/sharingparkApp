package com.lzj.its.sharingpark.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.widget.SwipeRefreshLayout;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.adapter.ParkingAdapter;

import com.lzj.its.sharingpark.bean.ParkingBean;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ParkingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ParkingAdapter adapter;
    private List<ParkingBean> parkingBeanList = new ArrayList<>();
    private int you_stars = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view);
        getData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void windUpShare(String shareID, int stars, String reason){
        new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("reason", reason)
                        .add("stars", String.valueOf(stars))
                        .build();
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/share/"+shareID+"/windup")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1){
                    showToast("评价成功");
                    getRefreshData();
                }else {
                    showToast(jsonObject.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getData() {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/rented")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1) {
                    JSONArray shares = jsonObject.getJSONArray("shares");
                    for (int i=0;i<shares.length();i++){
                        ParkingBean parkingBean = new ParkingBean();
                        parkingBean.setShareID(((JSONObject)shares.get(i)).getInt("shareID"));
                        parkingBean.setPosition(((JSONObject)shares.get(i)).getString("position"));
                        parkingBean.setBeginTime(((JSONObject)shares.get(i)).getString("beginTimeString"));
                        parkingBean.setEndTime(((JSONObject)shares.get(i)).getString("endTimeString"));
                        parkingBean.setMore(((JSONObject)shares.get(i)).getString("more"));
                        parkingBean.setState(((JSONObject)shares.get(i)).getInt("state"));
                        parkingBean.setCost(((JSONObject)shares.get(i)).getInt("cost"));
                        parkingBean.setStars(((JSONObject)shares.get(i)).getInt("stars"));
                        parkingBeanList.add(parkingBean);
                    }
                    runOnUiThread(() -> {
                        //更新UI
                        adapter = new ParkingAdapter(this, parkingBeanList);
                        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                                if (((SuperTextView)view.findViewById(R.id.cost_state)).getRightString().equals("使用中")){
                                    final String[] items = { "1星","2星","3星","4星","5星" };
                                    final
                                    AlertDialog.Builder singleChoiceDialog =
                                            new AlertDialog.Builder(ParkingActivity.this);
                                    singleChoiceDialog.setTitle("请对本次使用车位体验评分：");
                                    // 第二个参数是默认选项，此处设置为0
                                    singleChoiceDialog.setSingleChoiceItems(items, 4,
                                            (DialogInterface dialog, int which) -> you_stars = which);
                                    singleChoiceDialog.setPositiveButton("确定",
                                            (DialogInterface dialog, int which) -> {
                                                String shareID = ((SuperTextView)view.findViewById(R.id.share_id)).getCenterTopString();
                                                if (you_stars < 2){
                                                    AlertDialog.Builder bad_windup_dialog =
                                                            new AlertDialog.Builder(ParkingActivity.this);
                                                    final LayoutInflater inflater = ParkingActivity.this.getLayoutInflater();
                                                    View view_custom = inflater.inflate(R.layout.dialog_bed_windup, null,false);
                                                    bad_windup_dialog.setView(view_custom);
                                                    AlertDialog alert = bad_windup_dialog.create();
                                                    view_custom.findViewById(R.id.bt_reason_ok).setOnClickListener((View v) -> {
                                                        String reason = ((EditText)view_custom.findViewById(R.id.et_reason_content)).getText().toString();
                                                        if (reason.equals("")){
                                                            showToast("请输入差评理由。");
                                                        }else {
                                                            windUpShare(shareID, you_stars+1, reason);
                                                            showToast("评价提交成功！");
                                                            alert.dismiss();
                                                        }
                                                    });
                                                    alert.show();
                                                }else{
                                                    windUpShare(shareID, you_stars + 1, "");
                                                }

                                            });
                                    singleChoiceDialog.show();
                                }
                            }
                            @Override
                            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                                return false;
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    });

                } else {
                    Intent intent = new Intent(ParkingActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getRefreshData() {
        parkingBeanList.clear();
        getData();
    }

    @Override
    public void onRefresh() {

        getRefreshData();
        adapter.notifyDataSetChanged();

        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
    }
}
