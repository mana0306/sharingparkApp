package com.lzj.its.sharingpark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.lljjcoder.style.citylist.CityListSelectActivity;
import com.lzj.its.sharingpark.InfoWindowHolder;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.activity.AddSharingActivity;
import com.lzj.its.sharingpark.activity.ChoosePositionActivity;
import com.lzj.its.sharingpark.activity.ShareInfoActivity;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.constraint.Constraints.TAG;

/**
 * Created by lzj on 18-10-30.
 */

public class IndexFragment extends BaseFragment {
    private static final String LTAG = IndexFragment.class.getSimpleName();
    MapView mMapView;
    SupportMapFragment map;
    BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private FloatingActionButton fab_add_sharing;
    private FloatingActionButton fab_my_location;
    private TextView et_position;
    //    private TextView mTvAddress;
    private BDLocation mLocation = null;

    private LatLng latLng;
    private boolean isFirstLoc = true; // 是否首次定位
    public BDAbstractLocationListener myListener = new MyLocationListener();

    /**
     * @Fields mInfoWindow : 弹出的窗口
     */
    private InfoWindow mInfoWindow;
    private RelativeLayout share_info;

    public IndexFragment() {
        // Required empty public constructor
    }

    private void initView(View view) {
        mMapView = view.findViewById(R.id.map);
        fab_add_sharing = view.findViewById(R.id.fab_add_sharing);
        fab_my_location = view.findViewById(R.id.fab_my_location);
//        mTvAddress = view.findViewById(R.id.tvAddress);
        et_position = view.findViewById(R.id.et_position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.i("onCreate");
        super.onCreate(savedInstanceState);
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag(TAG) // 全局tag
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
//        setContentView(R.layout.fragment_index);
//        MapStatus.Builder builder = new MapStatus.Builder();
//        builder.overlook(-20).zoom(15);
//        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(builder.build())
//                .compassEnabled(false).zoomControlsEnabled(false);
        map = SupportMapFragment.newInstance();
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction().add(R.id.map, map, "map_fragment").commit();
    }


    private void initMap() {
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
//        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//        mBaiduMap.setMyLocationEnabled(true);

        //默认显示普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        //mBaiduMap.setTrafficEnabled(true);
        //开启热力图
        //mBaiduMap.setBaiduHeatMapEnabled(true);
        // 开启定位图层
//        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getActivity().getApplicationContext());  //声明LocationClient类
        //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        //开启定位
        mLocationClient.start();
        //图片点击事件，回到定位点
        mLocationClient.requestLocation();
    }

    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = 1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
//        option.setOpenGps(true); // 打开gps

        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            mLocation = location;
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);
            // 当不需要定位图层时关闭定位图层
            //mBaiduMap.setMyLocationEnabled(false);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    et_position.setText(location.getAddrStr());
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    et_position.setText(location.getAddrStr());
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                    et_position.setText(location.getAddrStr());
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(getActivity(), "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(getActivity(), "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(getActivity(), "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
                getAvaSharingByPostion();
            }

        }
    }

    /**
     * @param share_info
     * @param bundle
     * @Description: 创建 弹出窗口
     * @Author:杨攀
     * @Since: 2016年1月20日上午11:18:33
     */
    private View createInfoWindow(RelativeLayout share_info, Bundle bundle) {

        InfoWindowHolder holder;
        if ( null == share_info ) {
            share_info = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(R.layout.share_info, null);
            holder = new InfoWindowHolder();

            holder.tv_position = share_info.findViewById(R.id.tv_position);
            holder.tv_begin_time = share_info.findViewById(R.id.tv_begin_time);
            holder.tv_end_time = share_info.findViewById(R.id.tv_end_time);
            holder.btn_use = share_info.findViewById(R.id.btn_use);
            holder.btn_cancel = share_info.findViewById(R.id.btn_cancel);
            share_info.setTag(holder);
        }

        holder = (InfoWindowHolder) share_info.getTag();

        holder.share_id = bundle.getInt("shareID");
        holder.tv_position.setText(String.format(getString(R.string.info_position), bundle.getString("position")));
        holder.tv_begin_time.setText(String.format(getString(R.string.info_begin_time), bundle.getString("beginTime")));
        holder.tv_end_time.setText(String.format(getString(R.string.info_end_time), bundle.getString("endTime")));

        holder.btn_use.setTag(bundle.getInt("shareID"));
        holder.btn_use.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShareInfoActivity.class);
            intent.putExtra("share_id", (Integer)v.getTag());
            startActivity(intent);
        });
        holder.btn_cancel.setOnClickListener(v -> mBaiduMap.hideInfoWindow());

        return share_info;
    }

    public void getAvaSharingByPostion() {
        new Thread(() -> {
            double lat = mLocation.getLatitude();
            double lon = mLocation.getLongitude();
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("positionA", String.valueOf(lat))
                        .add("positionB", String.valueOf(lon))
                        .build();
                Request request = new Request.Builder()
                        .addHeader("cookie", session)
                        .post(requestBody)
                        .url(getString(R.string.api_ip_port) + "/search")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                if (success == 1) {
                    JSONArray shares = jsonObject.getJSONArray("shares");
                    for (int i = 0; i < shares.length(); i++) {
                        JSONObject the_share = (JSONObject) shares.get(i);
                        LatLng latLng = new LatLng(
                                the_share.getDouble("positionA"),
                                the_share.getDouble("positionB")
                        );
                        //准备 marker 的图片
                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marker);
                        Bundle mBundle = new Bundle();
                        mBundle.putString("position", the_share.getString("position"));
                        mBundle.putString("beginTime", the_share.getString("beginTime"));
                        mBundle.putString("endTime", the_share.getString("endTime"));
                        mBundle.putInt("shareID", the_share.getInt("shareID"));
//                        mBundle.putInt();
                        //准备 marker option 添加 marker 使用
                        MarkerOptions markerOptions = new MarkerOptions()
                                .icon(bitmap)
                                .position(latLng)
                                .title(String.valueOf(the_share.getInt("shareID")))
                                .extraInfo(mBundle);
                        //获取添加的 marker 这样便于后续的操作
                        mBaiduMap.addOverlay(markerOptions);
                    }

                } else {
                    showToast("获取可用车位出错");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("服务器错误!");
            }
        }).start();

        //对 marker 添加点击相应事件
        mBaiduMap.setOnMarkerClickListener((Marker arg0) -> {
            // TODO Auto-generated method stub
//            Toast.makeText(getActivity(), "Marker被点击了！", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), arg0.getTitle(), Toast.LENGTH_SHORT).show();
            final LatLng ll = arg0.getPosition();
            share_info = (RelativeLayout) createInfoWindow(null, arg0.getExtraInfo());
            mInfoWindow = new InfoWindow(share_info, ll, -47);
            //显示InfoWindow
            mBaiduMap.showInfoWindow(mInfoWindow);
            return false;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.i("onStart");
//        initMap();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.i("test");
//  map = (SupportMapFragment)(getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
//        mBaiduMap = mMapView.getMap();
        initMap();


//        Logger.i("test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Logger.i("onCreateView");

        View view = inflater.inflate(R.layout.fragment_index, container, false);

        initView(view);
        fab_add_sharing.setOnClickListener(view1 -> {
//            Intent intent = new Intent(getActivity(), CityListSelectActivity.class);
//            startActivityForResult(intent, CityListSelectActivity.CITY_SELECT_RESULT_FRAG);
            startActivity(new Intent(getActivity(), ChoosePositionActivity.class));
        });
        fab_my_location.setOnClickListener(v -> {
            //开启定位
            isFirstLoc = true;
        });

        return view;
    }

    @Override
    public void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}
