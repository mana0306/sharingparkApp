package com.lzj.its.sharingpark.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lzj.its.sharingpark.MyApplication;
import com.lzj.its.sharingpark.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by bruce on 2016/11/1.
 * BaseFragment
 */

public class BaseFragment extends Fragment {

    protected OkHttpClient client;
    protected MyApplication app;
    protected String session;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        app = (MyApplication) getActivity().getApplication(); //获得我们的应用程序MyApplication;
        OkHttpClient client0 = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        app.setOkHttpClient(client0);
        client = app.getOkHttpClient();
        session = app.getS();
    }

    public void showToast(String msg) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show());
    }
}
