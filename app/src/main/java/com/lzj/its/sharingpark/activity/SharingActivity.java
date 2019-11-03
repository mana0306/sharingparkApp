package com.lzj.its.sharingpark.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.adapter.SharingAdapter;
import com.lzj.its.sharingpark.bean.SharingBean;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SharingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SharingAdapter adapter;
    private List<SharingBean> sharingBeanList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharing);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view);
        getData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void cancleShare(String shareID){
        new Thread(()->{
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .post(requestBody)
                        .url(getString(R.string.api_ip_port) + "/share/"+shareID+"/cancle")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1){
                    showToast("撤销成功");
                    getRefreshData();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void submitAppeal(String shareID, String apContent){
        new Thread(()->{
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("apContent", apContent)
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .post(requestBody)
                        .url(getString(R.string.api_ip_port ) + "/share/" + shareID + "/appeal")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1){
                    showToast("提交申诉成功！");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    private void getData() {
        new Thread(() -> {
            try {
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/borrowed")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1) {
                    JSONArray shares = jsonObject.getJSONArray("shares");
                    for (int i=0;i<shares.length();i++){
                        SharingBean sharingBean = new SharingBean();
                        sharingBean.setShareID(((JSONObject)shares.get(i)).getInt("shareID"));
                        sharingBean.setPosition(((JSONObject)shares.get(i)).getString("position"));
                        sharingBean.setBeginTime(((JSONObject)shares.get(i)).getString("beginTimeString"));
                        sharingBean.setEndTime(((JSONObject)shares.get(i)).getString("endTimeString"));
                        sharingBean.setMore(((JSONObject)shares.get(i)).getString("more"));
                        sharingBean.setState(((JSONObject)shares.get(i)).getInt("state"));
                        sharingBean.setCost(((JSONObject)shares.get(i)).getInt("cost"));
                        sharingBean.setStars(((JSONObject)shares.get(i)).getInt("stars"));
                        sharingBeanList.add(sharingBean);
                    }
                    runOnUiThread(() -> {
                        //更新UI
                        adapter = new SharingAdapter(this, sharingBeanList);
                        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                                SuperTextView cost_state = view.findViewById(R.id.cost_state);
                                if (cost_state.getRightString().equals("已使用")){
                                    SuperTextView stv_share_id = view.findViewById(R.id.share_id);
                                    String share_id = stv_share_id.getCenterTopString();
                                    AlertDialog.Builder bad_windup_dialog =
                                            new AlertDialog.Builder(SharingActivity.this);
                                    final LayoutInflater inflater = SharingActivity.this.getLayoutInflater();
                                    View view_custom = inflater.inflate(R.layout.dialog_bed_windup, null,false);
                                    bad_windup_dialog.setView(view_custom);
                                    AlertDialog alert = bad_windup_dialog.create();
                                    view_custom.findViewById(R.id.bt_reason_ok).setOnClickListener((View v) -> {
                                        String reason = ((EditText)view_custom.findViewById(R.id.et_reason_content)).getText().toString();
                                        if (reason.equals("")){
                                            showToast("请申诉理由。");
                                        }else {
                                            submitAppeal(share_id, reason);
                                            alert.dismiss();
                                        }
                                    });
                                    alert.show();
                                }
                            }
                            @Override
                            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                                SuperTextView cost_state = view.findViewById(R.id.cost_state);
                                if (cost_state.getRightString().equals("待使用")){
                                    final AlertDialog.Builder normalDialog =
                                            new AlertDialog.Builder(SharingActivity.this);
                                    normalDialog.setTitle("撤销确认");
                                    normalDialog.setMessage("你确定要撤回该车位的共享吗？");

                                    normalDialog.setPositiveButton("确定",
                                            (DialogInterface dialog, int which) -> {
                                                SuperTextView stv_share_id = view.findViewById(R.id.share_id);
                                                cancleShare(stv_share_id.getCenterTopString());
                                            });
                                    normalDialog.setNegativeButton("取消",
                                            (DialogInterface dialog, int which) -> {});
                                    normalDialog.show();
                                }
                                return false;
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    });

                } else {
                    Intent intent = new Intent(SharingActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getRefreshData() {
        sharingBeanList.clear();
        getData();
    }

    @Override
    public void onRefresh() {

        getRefreshData();
        adapter.notifyDataSetChanged();

        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
    }
}
