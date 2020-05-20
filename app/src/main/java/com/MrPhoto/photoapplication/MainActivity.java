package com.MrPhoto.photoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    /** 설정 창 버튼 */
    Button settingBtn;
    /** 화면 비율 변환 버튼 */
    Button rationBtn;
    /** 정방향, 후방향 화면 전환 버튼 */
    Button reverseBtn;
    /** 스티커 버튼 */
    Button stikerBtn;
    /** 촬영 버튼 */
    Button photoBtn;
    /** 필터 버튼 */
    Button filterBtn;
    /** 촬영 화면 */
    TextView caScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingBtn = (Button)findViewById(R.id.settingBtn);
        rationBtn = (Button)findViewById(R.id.ratioBtn);
        reverseBtn = (Button)findViewById(R.id.reverseBtn);
        stikerBtn = (Button)findViewById(R.id.stikerBtn);
        photoBtn = (Button)findViewById(R.id.photoBtn);
        filterBtn = (Button)findViewById(R.id.filterBtn);
        caScreen = (TextView)findViewById(R.id.caScreen);

        stikerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout linear=(LinearLayout)inflater.inflate(R.layout.activity_sticker_main,null);
                LinearLayout.LayoutParams paramlinear=new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                getWindow().addContentView(linear,paramlinear);
            }

        });

    }


}
