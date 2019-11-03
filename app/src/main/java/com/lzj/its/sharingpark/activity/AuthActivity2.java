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
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

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
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity2 extends BaseActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;

    SuperTextView stv_city;
    // SuperTextView stv_city;
    EditText et_more;
    EditText stv_parkingspace_number;
    AutoCompleteTextView act_position;
    Spinner stv_parkinglot_name;
    private ArrayAdapter<CharSequence> adapter ;
    private ArrayAdapter<String> sugAdapter;

    private ArrayAdapter<String> arrayAdapter;

    SuperButton sbnt_add_sharing;

    private int loadIndex = 0;

    String[] ps;

    // private Boolean psinfo = false;

    PoiInfo poiInfo = new PoiInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth2);
        initParkingspace();
        initParkingspaces();
        findView();
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
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
                    // psinfo = true;
                    startActivity(new Intent(AuthActivity2.this, ParkingspaceActivity.class));
                // }else if (success == 2) {
                    // psinfo = false;
                } else  if (success == 3) {
                    Intent intent = new Intent(AuthActivity2.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void initParkingspaces() {
        new Thread(() -> {
            try {
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/allParkinglots")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");

                if (success == 1) {
                    // ps = new String[]{"请选择停车场", "1号停车场", "2号停车场", "3号停车场", "4号停车场"};
                    JSONArray newjson = jsonObject.getJSONArray("parkinglots");
                    ps = new String[newjson.length()];
                    ps[0] = "请选择停车场";
                    for (int i=1;i<newjson.length();i++){
                        ps[i] = ((JSONObject)newjson.get(i)).getString("name");
                    }
                } else {
                    Intent intent = new Intent(AuthActivity2.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void submitAddSharing(String position, double positionA, double positionB,String parkinglot_name,String parkingspace_number,String more){
        Thread addSharingRunnable = new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("position", position)
                        .add("positionA", String.valueOf(positionA))
                        .add("positionB", String.valueOf(positionB))
                        .add("parkinglot_name", parkinglot_name)
                        .add("parkingspace_number",parkingspace_number)
                        .add("more", more)
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        // 指定访问的服务器地址是电脑本机
                        .url(getString(R.string.api_ip_port) + "/addParkingspace")
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
                    Intent intent = new Intent(AuthActivity2.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AuthActivity2.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addSharingRunnable.start();
    }

    private void addSharing(){
        String position = poiInfo.getName();
        double positionA = -1;
        double positionB = -1;
        if (poiInfo.getLocation() != null){
            positionA = poiInfo.getLocation().latitude;
            positionB = poiInfo.getLocation().longitude;
        }
        
        String more = et_more.getText().toString();
        String parkingspace_number = stv_parkingspace_number.getText().toString();
        String parkinglot_name = (String) stv_parkinglot_name.getSelectedItem();
        if(position == null)
            showToast("请输入地址！");
        else if(positionA <= 0 || positionB <= 0)
            showToast("输入地址无法找到，请确认地址无误！");
        else {
            submitAddSharing(position, positionA, positionB, parkinglot_name,parkingspace_number,more);
        }
    }
    
    private void findView() {
        stv_city = findViewById(R.id.stv_city);
        et_more = findViewById(R.id.et_more);
        stv_parkingspace_number = findViewById(R.id.stv_parkingspace_number);
        sbnt_add_sharing = findViewById(R.id.sbt_sharing);

        sbnt_add_sharing.setOnClickListener(v -> addSharing());

        act_position = findViewById(R.id.act_position);
        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        act_position.setAdapter(sugAdapter);
        act_position.setThreshold(1);
        /* 当输入关键字变化时，动态更新建议列表 */
        act_position.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {

                String city_str = stv_city.getLeftString();
                String key_str = act_position.getText().toString();

                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(city_str)
                        .keyword(key_str)
                        .pageNum(loadIndex)
                        .scope(1));
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }

                /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(cs.toString())
                        .city(stv_city.getLeftString()));
            }
        });

        stv_city.setOnClickListener((View v) -> list());
        f1();
    }


    private void f1(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ps);  //创建一个数组适配器
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     //设置下拉列表框的下拉选项样式
 
        stv_parkinglot_name = findViewById(R.id.stv_parkinglot_name);
        stv_parkinglot_name.setAdapter(adapter);
    }
    
    /**
     * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
     *
     * @param result Poi检索结果，包括城市检索，周边检索，区域检索
     */
    public void onGetPoiResult(PoiResult result) {

        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            showToast("未找到结果");
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            poiInfo = result.getAllPoi().get(0);
            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";

            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果,请切换相应城市后重试！";
            showToast(strInfo);
        }
    }

    /**
     * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
     * V5.2.0版本之后，还方法废弃，使用{@link #onGetPoiDetailResult(PoiDetailSearchResult)}代替
     *
     * @param result POI详情检索结果
     */
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast("抱歉，未找到结果");
        } else {
            showToast(result.getName() + ": " + result.getAddress());
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showToast("抱歉，未找到结果");
        } else {
            List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult.getPoiDetailInfoList();
            if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
                showToast("抱歉，检索结果为空");
                return;
            }

            for (int i = 0; i < poiDetailInfoList.size(); i++) {
                PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
                if (null != poiDetailInfo) {
                    showToast(poiDetailInfo.getName() + ": " + poiDetailInfo.getAddress());
                }
            }
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    /**
     * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
     *
     * @param res Sug检索结果
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }

        List<String> suggest = new ArrayList<>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }

        sugAdapter = new ArrayAdapter<>(AuthActivity2.this, android.R.layout.simple_dropdown_item_1line,
                suggest);
        act_position.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    public void list() {
        Intent intent = new Intent(AuthActivity2.this, CityListSelectActivity.class);
        startActivityForResult(intent, CityListSelectActivity.CITY_SELECT_RESULT_FRAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CityListSelectActivity.CITY_SELECT_RESULT_FRAG) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    return;
                }
                Bundle bundle = data.getExtras();

                CityInfoBean cityInfoBean = bundle.getParcelable("cityinfo");

                if (null == cityInfoBean) {
                    return;
                }

                stv_city.setLeftString(cityInfoBean.getName());
            }
        }
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
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
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