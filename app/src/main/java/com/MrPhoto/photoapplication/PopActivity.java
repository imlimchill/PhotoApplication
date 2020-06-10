package com.MrPhoto.photoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

//설정창 작업은 여기서 하게 됨.

public class PopActivity extends AppCompatActivity {
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
    }
}
