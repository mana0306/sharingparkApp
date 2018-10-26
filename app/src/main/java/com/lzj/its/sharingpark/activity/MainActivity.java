package com.lzj.its.sharingpark.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.lzj.its.sharingpark.R;

/**
 * 主页面
 */
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }


    public void login(View view) {
        startActivity(new Intent(this,LoginActivity.class));

    }

    public void exit(View view) {
        finish();
    }

}
