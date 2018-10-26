package com.lzj.its.sharingpark.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzj.its.sharingpark.MyApplication;
import com.lzj.its.sharingpark.R;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
                try {
                    register();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
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
    private void register() throws IOException, JSONException {
        
        RequestBody requestBody=new FormBody.Builder()
                .add("userName", getUserName())
                .add("password", getPassword())
                .add("phone", getPhone())
                .build();
        Request request = new Request.Builder()
                // 指定访问的服务器地址是电脑本机
                .url( getString(R.string.api_ip_port) + "/register")
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
        if(success==1){
            runOnUiThread(() -> {
                //更新UI
                Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
            });

            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }
        else{
            runOnUiThread(() -> {
                //更新UI
                Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_SHORT).show();
            });
        }
    }


    public String getUserName() {
        return et_username.getText().toString().trim();//去掉空格
    }

    /**
     * 获取密码
     */
    public String getPassword() {
        return et_password.getText().toString().trim();//去掉空格
    }

    public String getPasswordConfirm() {
        return et_password_confirm.getText().toString().trim();//去掉空格
    }

    public String getPhone() {
        return et_phone.getText().toString().trim();//去掉空格
    }
}
