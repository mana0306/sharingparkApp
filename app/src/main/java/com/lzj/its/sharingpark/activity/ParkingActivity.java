package com.lzj.its.sharingpark.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.adapter.ParkingAdapter;
import com.lzj.its.sharingpark.adapter.SharingAdapter;
import com.lzj.its.sharingpark.bean.ParkingBean;
import com.lzj.its.sharingpark.bean.SharingBean;
import com.orhanobut.logger.Logger;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

public class ParkingActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ParkingAdapter adapter;
    private List<ParkingBean> parkingBeanList = new ArrayList<>();

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
                                Toast.makeText(ParkingActivity.this, position + "", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                                return false;
                            }
                        });
                        recyclerView.setAdapter(adapter);
                    });

                } else {
                    showToast(jsonObject.getString("message"));
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
