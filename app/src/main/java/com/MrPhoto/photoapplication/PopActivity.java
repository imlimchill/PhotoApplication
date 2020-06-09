package com.MrPhoto.photoapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class PopActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        int width = (int) (display.getWidth() * 0.9); //Display 사이즈의 90% 각자 원하는 사이즈로 설정하여 사용

        int height = (int) (display.getHeight() * 0.1);  //Display 사이즈의 90% 각자 원하는 사이즈로 설정하여 사용

        getWindow().getAttributes().width = width;

        getWindow().getAttributes().height = height;
    }
}
