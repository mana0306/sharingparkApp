package com.lzj.its.sharingpark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.activity.AddSharingActivity;

/**
 * Created by lzj on 18-10-30.
 */

public class IndexFragment extends BaseFragment {
    private static final String LTAG = IndexFragment.class.getSimpleName();
    SupportMapFragment map;
    BaiduMap mBaiduMap;
    private LocationClient mLocationClient = null;
    private FloatingActionButton fab_add_sharing;
    private FloatingActionButton fab_my_location;
    public IndexFragment() {
        // Required empty public constructor
    }

    private void initView(View view){
        fab_add_sharing = view.findViewById(R.id.fab_add_sharing);
        fab_my_location = view.findViewById(R.id.fab_my_location);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fragment);
        Intent intent = getActivity().getIntent();
        MapStatus.Builder builder = new MapStatus.Builder();
        if (intent.hasExtra("x") && intent.hasExtra("y")) {
            // 当用intent参数时，设置中心点为指定点
            Bundle b = intent.getExtras();
            LatLng p = new LatLng(b.getDouble("y"), b.getDouble("x"));
            builder.target(p);
        }
        builder.overlook(-20).zoom(15);
        BaiduMapOptions bo = new BaiduMapOptions().mapStatus(builder.build())
                .compassEnabled(false).zoomControlsEnabled(false);
        map = SupportMapFragment.newInstance(bo);
        FragmentManager manager = getActivity().getSupportFragmentManager();

        manager.beginTransaction().add(R.id.fg_index, map, "map_fragment").commit();

//        mBaiduMap = map.getBaiduMap();
        // 开启定位图层
//        mBaiduMap.setMyLocationEnabled(true);

// 构造定位数据
//        MyLocationData locData = new MyLocationData.Builder()
//                .accuracy(location.getRadius())
//                // 此处设置开发者获取到的方向信息，顺时针0-360
//                .direction(100).latitude(location.getLatitude())
//                .longitude(location.getLongitude()).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_index, container, false);
        initView(view);
        fab_add_sharing.setOnClickListener(view1 -> {
            startActivity(new Intent(getActivity(), AddSharingActivity.class));
        });
        fab_my_location.setOnClickListener(v -> {
            /**
             * @TODO
             * GPS定位
             * */

        });
        return view;
    }
}
