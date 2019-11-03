package com.lzj.its.sharingpark.activity;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import com.allen.library.SuperButton;
import com.allen.library.SuperTextView;

import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.lljjcoder.style.citylist.CityListSelectActivity;
import com.lljjcoder.style.citylist.bean.CityInfoBean;
import com.lzj.its.sharingpark.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity1 extends BaseActivity {

    EditText car_id;
    EditText car_model;
    EditText car_brand;
    private ArrayAdapter<String> sugAdapter;
//    TextView mResultTv;

    SuperButton sbnt_add_sharing;

    private int loadIndex = 0;
    private Boolean carinfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCar();
        setContentView(R.layout.activity_auth1);
        
        findView();

    }
    private void initCar() {
        new Thread(() -> {
            try {
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/myCar")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");

                if (success == 1) {
                    carinfo = true;
                    startActivity(new Intent(AuthActivity1.this, CarActivity.class));
                } else if (success == 3) { 
                    Intent intent = new Intent(AuthActivity1.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }else{    
                    carinfo = false;               
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void submitAddSharing(String car_model,String car_id,String car_brand){
        Thread addSharingRunnable = new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("car_model", car_model)
                        .add("car_id", car_id)
                        .add("car_brand", car_brand)
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        // 指定访问的服务器地址是电脑本机
                        .url(getString(R.string.api_ip_port) + "/addCar")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();

                assert response.body() != null;
                String responseData = response.body().string();

                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                final String message = jsonObject.getString("message");
                //判断账号和密码
                if (success == 1) {
                    showToast("添加成功");
                    Intent intent = new Intent(AuthActivity1.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if(success == 3){
                    Intent intent = new Intent(AuthActivity1.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addSharingRunnable.start();
    }

    private void addSharing(){
        if(carinfo){
            showToast("不能重复添加！");
        }
        String carid = car_id.getText().toString();
        String carbrand = car_brand.getText().toString();
        String carmodel = car_model.getText().toString();
        if(carid == null)
            showToast("请输入车牌号！");
        else if(carmodel == null)
            showToast("请输入车辆型号");
        else if(carbrand == null)
            showToast("请输入车辆品牌");
        else
            submitAddSharing(carmodel,carid,carbrand);
    }

    private void findView() {
        car_id = findViewById(R.id.car_id);
        car_brand = findViewById(R.id.car_brand);
        car_model = findViewById(R.id.car_model);

        sbnt_add_sharing = findViewById(R.id.sbt_sharing);
        sbnt_add_sharing.setOnClickListener(v -> addSharing());

        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

}