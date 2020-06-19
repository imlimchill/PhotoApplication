package com.MrPhoto.photoapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    /** 시간초를 선언해 자동으로 넘어갈 수 있도록 해주는 클래스 */
    Handler handler = new Handler();
    Runnable runnable = () -> {
        // handler로 설정해둔 시간이 끝났을 때 MainActivity로 넘어갈 수 있게해준다.
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        // splash 화면은 필요가 없기때문에 finish해준다.
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    /**
     * 애플리케이션이 최소화되었을 때 실행하는 함수
     */
    @Override
    protected void onPause() {
        super.onPause();
        // handler가 예약해둔 작업을 취소한다.
        handler.removeCallbacks(runnable);
    }

    /**
     * 화면이 최대화가 됐을 때 실행하는 함수
     * 1. 애플리케이션이 켜졌을 때 실행한다.
     * 2. 애플리케이션이 최소화 되었다가 최대화 되면 실행한다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // handler의 작업을 실행한다. > spalsh의 0.8초 후에 화면을 전환한다.
        handler.postDelayed(runnable, 800);
    }
}
