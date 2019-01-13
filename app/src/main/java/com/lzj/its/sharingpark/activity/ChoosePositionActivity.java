package com.lzj.its.sharingpark.activity;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.allen.library.SuperButton;
import com.allen.library.SuperTextView;

import com.baidu.mapapi.model.LatLng;
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
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.lljjcoder.style.citylist.CityListSelectActivity;
import com.lljjcoder.style.citylist.bean.CityInfoBean;
import com.lzj.its.sharingpark.MyApplication;
import com.lzj.its.sharingpark.R;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChoosePositionActivity extends BaseActivity implements
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;

    SuperTextView stv_city;
    SuperTextView stv_begin_time;
    SuperTextView stv_end_time;
    SuperTextView stv_cost;
    EditText et_more;
    AutoCompleteTextView act_position;
    private ArrayAdapter<String> sugAdapter;
//    TextView mResultTv;

    SuperButton sbnt_add_sharing;

    private int searchType = 0;  // 搜索的类型，在显示时区分
    private int loadIndex = 0;

    PoiInfo poiInfo = new PoiInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_position);
        findView();

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 初始化建议搜索模块，注册建议搜索事件监听
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);
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
        final Dialog d = new Dialog(ChoosePositionActivity.this);
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

    private void submitAddSharing(String position, double positionA, double positionB, String begin_time, String end_time, int cost, String more){
        Thread addSharingRunnable = new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("position", position)
                        .add("positionA", String.valueOf(positionA))
                        .add("positionB", String.valueOf(positionB))
                        .add("beginTime", begin_time)
                        .add("endTime", end_time)
                        .add("cost", String.valueOf(cost))
                        .add("more", more)
                        .build();
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
                    startActivity(new Intent(ChoosePositionActivity.this, MainActivity.class));
                    finish();//关闭页面
                } else {
                    showToast(message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        addSharingRunnable.start();
    }

    private void addSharing(){
        String position = poiInfo.getName();
        double positionA = poiInfo.getLocation().latitude;
        double positionB = poiInfo.getLocation().longitude;
        String begin_time = stv_begin_time.getLeftString();
        String end_time = stv_end_time.getLeftString();
        int cost = Integer.valueOf(stv_cost.getLeftString());
        String more = et_more.getText().toString();

        if(position.equals(""))
            showToast("请输入地址！");
        else if(positionA <= 0 || positionB <= 0)
            showToast("输入地址无法找到，请确认地址无误！");
        else if(begin_time.equals("请选择时间..."))
            showToast("请选择开始时间！");
        else if (end_time.equals("请选择时间..."))
            showToast("请选择结束时间！");
        else {
            submitAddSharing(position, positionA, positionB, begin_time, end_time, cost, more);
        }
    }

    private void findView() {
        stv_city = findViewById(R.id.stv_city);
        stv_begin_time = findViewById(R.id.stv_begin_time);
        stv_end_time = findViewById(R.id.stv_end_time);
        stv_cost = findViewById(R.id.stv_cost);
        et_more = findViewById(R.id.et_more);

        sbnt_add_sharing = findViewById(R.id.sbt_sharing);

        sbnt_add_sharing.setOnClickListener(v -> addSharing());

        stv_begin_time.setOnClickListener(v -> inputTime(stv_begin_time));
        stv_end_time.setOnClickListener(v -> inputTime(stv_end_time));
        stv_cost.setOnClickListener(v -> inputCost(stv_cost));

        act_position = findViewById(R.id.act_position);
        sugAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        act_position.setAdapter(sugAdapter);
        act_position.setThreshold(1);
        /* 当输入关键字变化时，动态更新建议列表 */
        act_position.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                searchType = 1;

                String citystr = stv_city.getLeftString();
                String keystr = act_position.getText().toString();

                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(citystr)
                        .keyword(keystr)
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

        sugAdapter = new ArrayAdapter<>(ChoosePositionActivity.this, android.R.layout.simple_dropdown_item_1line,
                suggest);
        act_position.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    public void list() {
        Intent intent = new Intent(ChoosePositionActivity.this, CityListSelectActivity.class);
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