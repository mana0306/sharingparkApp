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

public class AddSharingActivity extends BaseActivity {

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;

    // SuperTextView stv_city;
    SuperTextView stv_begin_time;
    SuperTextView stv_end_time;
    SuperTextView stv_cost;
    SuperTextView stv_parkinglot;
    SuperTextView stv_act_position;
    SuperTextView stv_number;
    // AutoCompleteTextView act_position;
    private ArrayAdapter<String> sugAdapter;
//    TextView mResultTv;

    SuperButton sbnt_add_sharing;

    private int loadIndex = 0;
    String parkingspace_number;
    String act_position;
    String parkinglot_name;
    String positionA;
    String positionB;
    String more;

    PoiInfo poiInfo = new PoiInfo();

    // @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sharing);
        findView();
        initParkingspace();

        // // 初始化搜索模块，注册搜索事件监听
        // mPoiSearch = PoiSearch.newInstance();
        // mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        // mSuggestionSearch = SuggestionSearch.newInstance();
        // mSuggestionSearch.setOnGetSuggestionResultListener(this);
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
                    parkinglot_name = jsonObject.getJSONObject("parkingspace").getString("parkinglot");
                    parkingspace_number = jsonObject.getJSONObject("parkingspace").getString("number");
                    act_position = jsonObject.getJSONObject("parkingspace").getString("position");
                    more = jsonObject.getJSONObject("parkingspace").getString("more");
                    positionA = jsonObject.getJSONObject("parkingspace").getString("positionA");
                    positionB = jsonObject.getJSONObject("parkingspace").getString("positionB");
                    runOnUiThread(() -> {
                        //更新UI
                        stv_parkinglot.setLeftString(parkinglot_name);
                        stv_number.setLeftString(parkingspace_number);
                        stv_act_position.setLeftString(act_position);
                    });
                } else if (success == 2) {
                    showToast("无车位信息！");
                    Intent intent = new Intent(AddSharingActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{  Intent intent = new Intent(AddSharingActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void inputTime(SuperTextView stv) {
        TimePickerView pvTime = new TimePickerBuilder(this, (date, v) -> {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.CHINA);
            stv.setLeftString(format.format(date));
        }).setType(new boolean[]{true, true, true, true, true, true}).isDialog(true).build();

        Dialog mDialog = pvTime.getDialog();
        if (mDialog != null) {

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM);

            params.leftMargin = 0;
            params.rightMargin = 0;
            pvTime.getDialogContainerLayout().setLayoutParams(params);

            Window dialogWindow = mDialog.getWindow();
            if (dialogWindow != null) {
                dialogWindow.setWindowAnimations(com.bigkoo.pickerview.R.style.picker_view_slide_anim);//修改动画样式
                dialogWindow.setGravity(Gravity.BOTTOM);//改成Bottom,底部显示
            }
        }
        pvTime.show();
    }

    private void inputCost(SuperTextView stv) {
        final Dialog d = new Dialog(AddSharingActivity.this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.number_picker_dialog);
        final NumberPicker np = d.findViewById(R.id.numberPicker1);
        int min = 0;
        int max = 100;

//        String[] myValues = getArrayWithSteps(min, max, step); //get the values with steps... Normally
        np.setMinValue(min);
        np.setMaxValue(max); //Like iStepsArray in the function
//        np.setDisplayedValues(myValues);//put on NumberPicker
        np.setValue(min + (max-min)/5);
        np.setWrapSelectorWheel(false);
//        np.onTouchEvent(MotionEvent mon)
        stv_cost.setLeftString(String.valueOf(np.getValue()));
        np.setOnValueChangedListener((picker, oldVal, newVal) -> {
            stv.setLeftString(String.valueOf(newVal));
        });
        d.show();
    }

    private void submitAddSharing(String position, String positionA, String positionB, String begin_time, String end_time, int cost, String more){
        Thread addSharingRunnable = new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("position", position)
                        .add("positionA", positionA)
                        .add("positionB", positionB)
                        .add("beginTime", begin_time)
                        .add("endTime", end_time)
                        .add("cost", String.valueOf(cost))
                        .add("more", more)
                        .build();
                //向服务器发送请求
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        // 指定访问的服务器地址是电脑本机
                        .url(getString(R.string.api_ip_port) + "/createShare")
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
                    Intent intent = new Intent(AddSharingActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(AddSharingActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addSharingRunnable.start();
    }

    private void addSharing(){
        String begin_time = stv_begin_time.getLeftString();
        String end_time = stv_end_time.getLeftString();
        String cost_str = stv_cost.getLeftString();
        int cost = Integer.MIN_VALUE;
        if (!cost_str.equals("请设置花费..."))
            cost = Integer.valueOf(cost_str);
        else if(begin_time.equals("请选择时间..."))
            showToast("请选择开始时间！");
        else if (end_time.equals("请选择时间..."))
            showToast("请选择结束时间！");
        else if (cost < 0 )
            showToast("请设置花费！");
        else{
            showToast(act_position+positionA+positionB);
            submitAddSharing(act_position, positionA, positionB, begin_time, end_time, cost, more);
        }
    }

    private void findView() {
        stv_parkinglot = findViewById(R.id.stv_parkinglot);
        stv_act_position = findViewById(R.id.stv_act_position);
        stv_number = findViewById(R.id.stv_number);
        stv_begin_time = findViewById(R.id.stv_begin_time);
        stv_end_time = findViewById(R.id.stv_end_time);
        stv_cost = findViewById(R.id.stv_cost);

        sbnt_add_sharing = findViewById(R.id.sbt_sharing);

        sbnt_add_sharing.setOnClickListener(v -> addSharing());

        stv_begin_time.setOnClickListener(v -> inputTime(stv_begin_time));
        stv_end_time.setOnClickListener(v -> inputTime(stv_end_time));
        stv_cost.setOnClickListener(v -> inputCost(stv_cost));

        // act_position = findViewById(R.id.act_position);
        // sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        // act_position.setAdapter(sugAdapter);
        // act_position.setThreshold(1);
        // /* 当输入关键字变化时，动态更新建议列表 */
        // act_position.addTextChangedListener(new TextWatcher() {
        //     @Override
        //     public void afterTextChanged(Editable arg0) {

        //         String city_str = stv_city.getLeftString();
        //         String key_str = act_position.getText().toString();

        //         mPoiSearch.searchInCity((new PoiCitySearchOption())
        //                 .city(city_str)
        //                 .keyword(key_str)
        //                 .pageNum(loadIndex)
        //                 .scope(1));
        //     }

        //     @Override
        //     public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        //     }

        //     @Override
        //     public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
        //         if (cs.length() <= 0) {
        //             return;
        //         }

        //         /* 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新 */
        //         mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
        //                 .keyword(cs.toString())
        //                 .city(stv_city.getLeftString()));
        //     }
        // });

        // stv_city.setOnClickListener((View v) -> list());
    }

    // /**
    //  * 获取POI搜索结果，包括searchInCity，searchNearby，searchInBound返回的搜索结果
    //  *
    //  * @param result Poi检索结果，包括城市检索，周边检索，区域检索
    //  */
    // public void onGetPoiResult(PoiResult result) {

    //     if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
    //         showToast("未找到结果");
    //         return;
    //     }

    //     if (result.error == SearchResult.ERRORNO.NO_ERROR) {
    //         poiInfo = result.getAllPoi().get(0);
    //         return;
    //     }

    //     if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
    //         // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
    //         String strInfo = "在";

    //         for (CityInfo cityInfo : result.getSuggestCityList()) {
    //             strInfo += cityInfo.city;
    //             strInfo += ",";
    //         }
    //         strInfo += "找到结果,请切换相应城市后重试！";
    //         showToast(strInfo);
    //     }
    // }

    // /**
    //  * 获取POI详情搜索结果，得到searchPoiDetail返回的搜索结果
    //  * V5.2.0版本之后，还方法废弃，使用{@link #onGetPoiDetailResult(PoiDetailSearchResult)}代替
    //  *
    //  * @param result POI详情检索结果
    //  */
    // public void onGetPoiDetailResult(PoiDetailResult result) {
    //     if (result.error != SearchResult.ERRORNO.NO_ERROR) {
    //         showToast("抱歉，未找到结果");
    //     } else {
    //         showToast(result.getName() + ": " + result.getAddress());
    //     }
    // }

    // @Override
    // public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
    //     if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
    //         showToast("抱歉，未找到结果");
    //     } else {
    //         List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult.getPoiDetailInfoList();
    //         if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
    //             showToast("抱歉，检索结果为空");
    //             return;
    //         }

    //         for (int i = 0; i < poiDetailInfoList.size(); i++) {
    //             PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
    //             if (null != poiDetailInfo) {
    //                 showToast(poiDetailInfo.getName() + ": " + poiDetailInfo.getAddress());
    //             }
    //         }
    //     }
    // }

    
    // /**
    //  * 获取在线建议搜索结果，得到requestSuggestion返回的搜索结果
    //  *
    //  * @param res Sug检索结果
    //  */
    // @Override
    // public void onGetSuggestionResult(SuggestionResult res) {
    //     if (res == null || res.getAllSuggestions() == null) {
    //         return;
    //     }

    //     List<String> suggest = new ArrayList<>();
    //     for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
    //         if (info.key != null) {
    //             suggest.add(info.key);
    //         }
    //     }

    //     sugAdapter = new ArrayAdapter<>(AddSharingActivity.this, android.R.layout.simple_dropdown_item_1line,
    //             suggest);
    //     act_position.setAdapter(sugAdapter);
    //     sugAdapter.notifyDataSetChanged();
    // }

    // public void list() {
    //     Intent intent = new Intent(AddSharingActivity.this, CityListSelectActivity.class);
    //     startActivityForResult(intent, CityListSelectActivity.CITY_SELECT_RESULT_FRAG);
    // }

    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //     super.onActivityResult(requestCode, resultCode, data);
    //     if (requestCode == CityListSelectActivity.CITY_SELECT_RESULT_FRAG) {
    //         if (resultCode == RESULT_OK) {
    //             if (data == null) {
    //                 return;
    //             }
    //             Bundle bundle = data.getExtras();

    //             CityInfoBean cityInfoBean = bundle.getParcelable("cityinfo");

    //             if (null == cityInfoBean) {
    //                 return;
    //             }

    //             stv_city.setLeftString(cityInfoBean.getName());
    //         }
    //     }
    // }

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