package com.MrPhoto.photoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;

//설정창 작업은 여기서 하게 됨.

public class PopActivity extends AppCompatActivity {

    ImageView flashBtn;
    ImageView timerBtn;
    ImageView muteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        //모든 스마트폰에서 동일하게 나오도록 사이즈를 지정해논다. 가로:세로 9:2 (버튼 아이콘 바꾸면 그때 맞춰서 수정 가능)
        int width = (int) (display.getWidth() * 0.9);
        int height = (int) (display.getHeight() * 0.2);
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        timerBtn = (ImageView) findViewById(R.id.timerBtn);
        muteBtn = (ImageView) findViewById(R.id.muteBtn);

        // 음소거의 아이콘 설정
        switch (MainActivity.isMute) {
            case 1:
                muteBtn.setImageResource(R.drawable.mute_on);
                break;
            case 0:
                muteBtn.setImageResource(R.drawable.mute_off);
                break;
        }

        // 플래시의 아이콘 설정
        switch (MainActivity.flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashBtn.setImageResource((R.drawable.flash_off));
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashBtn.setImageResource((R.drawable.flash_on));
                break;
        }

        // 타이머의 아이콘 설정
        switch (MainActivity.time) {
            case 0:
                timerBtn.setImageResource(R.drawable.timer_off);
                break;
            case 3000:
                timerBtn.setImageResource(R.drawable.timer_on3);
                break;
            case 5000:
                timerBtn.setImageResource(R.drawable.timer_on5);
                break;
            case 7000:
                timerBtn.setImageResource(R.drawable.timer_on7);
                break;
        }


        // 음소거 버튼 클릭시 음소거 기능을 끄고 킬 수 있는 기능 구현
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.isMute == 1) {
                    MainActivity.isMute = 0;
                    muteBtn.setImageResource(R.drawable.mute_off);
                } else {
                    MainActivity.isMute = 1;
                    muteBtn.setImageResource(R.drawable.mute_on);
                }
            }
        });

        // 플래시 버튼 클릭시 플래시 기능을 끄고 킬 수 있는 기능 구현
        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.flashMode == ImageCapture.FLASH_MODE_OFF) {
                    flashBtn.setImageResource(R.drawable.flash_on);
                    MainActivity.flashMode = ImageCapture.FLASH_MODE_ON;
                } else {
                    MainActivity.flashMode = ImageCapture.FLASH_MODE_OFF;
                    flashBtn.setImageResource(R.drawable.flash_off);
                }
            }
        });

        // 타이머 클릭 시 time의 시간 후에 사진 촬영 기능을 수행한다.
        timerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.time == 0) {
                    MainActivity.time = 3000;
                    timerBtn.setImageResource(R.drawable.timer_on3);
                } else if (MainActivity.time == 3000) {
                    MainActivity.time = 5000;
                    timerBtn.setImageResource(R.drawable.timer_on5);
                } else if (MainActivity.time == 5000) {
                    MainActivity.time = 7000;
                    timerBtn.setImageResource(R.drawable.timer_on7);
                } else {
                    MainActivity.time = 0;
                    timerBtn.setImageResource(R.drawable.timer_off);
                }
            }
        });
    }
}
