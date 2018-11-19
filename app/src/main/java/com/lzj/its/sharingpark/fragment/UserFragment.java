package com.lzj.its.sharingpark.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.allen.library.SuperTextView;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.activity.AccountActivity;
import com.lzj.its.sharingpark.activity.LoginActivity;
import com.lzj.its.sharingpark.activity.ParkingActivity;
import com.lzj.its.sharingpark.activity.SharingActivity;
import com.lzj.its.sharingpark.util.SharedPreferencesUtils;

import org.json.JSONObject;

import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserFragment extends BaseFragment {

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void logout() {
        Thread loginRunnable = new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        // 指定访问的服务器地址是电脑本机
                        .addHeader("cookie", session)
                        .url(getString(R.string.api_ip_port) + "/logout")
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Headers headers = response.headers();
                List<String> cookies = headers.values("Set-Cookie");
                String session = cookies.get(0);
                String s = session.substring(0, session.indexOf(";"));
                app.setS(s);

                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
//                final String message = jsonObject.getString("message");
                //判断账号和密码
                if (success == 1) {
                    showToast("注销成功，请重新登陆！");

                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();//关闭页面
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loginRunnable.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);
//        TextView textView = view.findViewById(R.id.textView);
//        textView.setText(mParam1);
        SuperTextView stv_logout = view.findViewById(R.id.stv_logout);
        stv_logout.setOnSuperTextViewClickListener(superTextView1 -> {
            logout();
            //置空密码即可
            //获取SharedPreferences对象，使用自定义类的方法来获取对象
            SharedPreferencesUtils helper = new SharedPreferencesUtils(getActivity(), "setting");
            //创建记住密码和自动登录是默认不选,密码为空
            helper.putValues(
                    new SharedPreferencesUtils.ContentValue("autoLogin", false));
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        });

        SuperTextView stv_account = view.findViewById(R.id.stv_my_account);
        stv_account.setOnSuperTextViewClickListener(superTextView -> {
            startActivity(new Intent(getActivity(), AccountActivity.class));
        });
        SuperTextView stv_my_sharing = view.findViewById(R.id.stv_my_sharing);
        stv_my_sharing.setOnSuperTextViewClickListener(superTextView -> {
            startActivity(new Intent(getActivity(), SharingActivity.class));
        });
        SuperTextView stv_my_parking = view.findViewById(R.id.stv_my_parking);
        stv_my_parking.setOnSuperTextViewClickListener(superTextView -> {
            startActivity(new Intent(getActivity(), ParkingActivity.class));
        });
        return view;
    }

}
