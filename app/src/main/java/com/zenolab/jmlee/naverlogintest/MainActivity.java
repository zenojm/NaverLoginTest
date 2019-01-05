package com.zenolab.jmlee.naverlogintest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnStartNaverLoginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setView();
        setOnCustomClickListener();
    }

    private void setView() {

        btnStartNaverLoginActivity = (Button) findViewById(R.id.btnStartNaverLoginActivity);
    }

    private void setOnCustomClickListener() {

        btnStartNaverLoginActivity.setOnClickListener(onClickListenerForbtnStartNaverLoginActivity);
    }


    View.OnClickListener onClickListenerForbtnStartNaverLoginActivity = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, OAuthSampleActivity.class));
        }
    };
}
