package com.lzj.its.sharingpark.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzj.its.sharingpark.MyApplication;
import com.lzj.its.sharingpark.R;
import com.lzj.its.sharingpark.wedget.LoadingDialog;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends Activity implements View.OnClickListener {

    private OkHttpClient client;
    private MyApplication app;

    private Button mRegisterBtn;
    private EditText et_username;
    private EditText et_password;
    private EditText et_password_confirm;
    private EditText et_phone;
    private ImageView iv_see_password;
    private ImageView iv_see_password_confirm;

    private LoadingDialog mLoadingDialog; //显示正在加载的对话框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApplication) getApplication(); //获得我们的应用程序MyApplication;
        OkHttpClient client0 = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        app.setOkHttpClient(client0);
        client = app.getOkHttpClient();

        setContentView(R.layout.activity_register);
        initViews();
        setupEvents();
    }

    private void initViews() {
        mRegisterBtn = findViewById(R.id.btn_register);
        et_username = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_phone = findViewById(R.id.et_phone);
        iv_see_password = findViewById(R.id.iv_see_password);
        iv_see_password_confirm = findViewById(R.id.iv_see_password_confirm);
    }

    private void setupEvents() {
        mRegisterBtn.setOnClickListener(this);
        iv_see_password.setOnClickListener(this);
        iv_see_password_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_see_password:
                setPasswordVisibility(this.iv_see_password, this.et_password);    //改变图片并设置输入框的文本可见或不可见
                break;
            case R.id.iv_see_password_confirm:
                setPasswordVisibility(this.iv_see_password_confirm, this.et_password_confirm);    //改变图片并设置输入框的文本可见或不可见
                break;
            case R.id.btn_register:
                register();
                break;

        }
    }

    /**
     * 设置密码可见和不可见的相互转换
     */
    private void setPasswordVisibility(ImageView iv_see_password, EditText et_password) {
        if (iv_see_password.isSelected()) {
            iv_see_password.setSelected(false);
            //密码不可见
            et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        } else {
            iv_see_password.setSelected(true);
            //密码可见
            et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }

    }
    /**
     * 大陆号码或香港号码均可
     */
    public static boolean isPhoneLegal(String str) throws PatternSyntaxException {
        return isChinaPhoneLegal(str);
    }

    /**
     * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数
     * 此方法中前三位格式有：
     * 13+任意数
     * 15+除4的任意数
     * 18+除1和4的任意数
     * 17+除9的任意数
     * 147
     */
    public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
        // ^ 匹配输入字符串开始的位置
        // \d 匹配一个或多个数字，其中 \ 要转义，所以是 \\d
        // $ 匹配输入字符串结尾的位置
        String regExp = "^((13[0-9])|(15[0-3, 5-9])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }
    private void register() {
        if (getUserName().isEmpty()){
            showToast("你输入的账号为空！");
            return;
        }

        if (getPassword().isEmpty()){
            showToast("你输入的密码为空！");
            return;
        }
        if (getPasswordConfirm().isEmpty()){
            showToast("请确认密码！");
            return;
        }
        if (getPhone().isEmpty()){
            showToast("你输入的手机号为空！");
            return;
        }else if (!isPhoneLegal(getPhone())){
            showToast("请输入正确的手机号！");
            return;
        }
        if(!getPassword().equals(getPasswordConfirm())){
            showToast("两次输入密码不一致！");
            return;
        }
        //登录一般都是请求服务器来判断密码是否正确，要请求网络，要子线程
        showLoading();//显示加载框
        Thread registerRunnable = new Thread(() -> {
            try {
                RequestBody requestBody = new FormBody.Builder()
                        .add("userName", getUserName())
                        .add("password", getPassword())
                        .add("phone", getPhone())
                        .build();
                //向服务器发送注册请求
                Request request = new Request.Builder()
                        // 指定访问的服务器地址是电脑本机
                        .url(getString(R.string.api_ip_port) + "/register")
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String responseData = response.body().string();
                Headers headers = response.headers();
                List<String> cookies = headers.values("Set-Cookie");
                String session = cookies.get(0);
                String s = session.substring(0, session.indexOf(";"));
                app = (MyApplication) getApplication(); //获得我们的应用程序MyApplication
                app.setS(s);

                JSONObject jsonObject = new JSONObject(responseData);
                Integer success = jsonObject.getInt("success");
                final String message = jsonObject.getString("message");
                //判断账号和密码
                if (success == 1) {
                    showToast("注册成功");
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();//关闭页面
                } else {
                    showToast(message);
                }
                hideLoading();//隐藏加载框

            } catch (Exception e) {
                e.printStackTrace();
                hideLoading();//隐藏加载框
            }
        });
        registerRunnable.start();
    }

    //获取用户名
    public String getUserName() {
        return et_username.getText().toString().trim();//去掉空格
    }

    /**
     * 获取密码
     */
    public String getPassword() {
        return et_password.getText().toString().trim();//去掉空格
    }

    //获取确认密码
    public String getPasswordConfirm() {
        return et_password_confirm.getText().toString().trim();//去掉空格
    }

    //获取手机号
    public String getPhone() {
        return et_phone.getText().toString().trim();//去掉空格
    }

    /**
     * 显示加载的进度款
     */
    public void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, getString(R.string.loading), false);
        }
        mLoadingDialog.show();
    }


    /**
     * 隐藏加载的进度框
     */
    public void hideLoading() {
        if (mLoadingDialog != null) {
            runOnUiThread(() -> mLoadingDialog.hide());

        }
    }


    /**
     * 监听回退键
     */
    @Override
    public void onBackPressed() {
        if (mLoadingDialog != null) {
            if (mLoadingDialog.isShowing()) {
                mLoadingDialog.cancel();
            } else {
                finish();
            }
        } else {
            finish();
        }

    }

    /**
     * 页面销毁前回调的方法
     */
    protected void onDestroy() {
        if (mLoadingDialog != null) {
            mLoadingDialog.cancel();
            mLoadingDialog = null;
        }
        super.onDestroy();
    }


    public void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show());
    }
}
