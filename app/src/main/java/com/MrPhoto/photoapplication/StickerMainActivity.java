package com.MrPhoto.photoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;

public class StickerMainActivity extends AppCompatActivity {

    ImageButton noneBtn;
    ImageButton favBtn;
    ImageButton sListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_sticker_list);

        noneBtn=(ImageButton)findViewById(R.id.noneBtn);
        favBtn=(ImageButton)findViewById(R.id.favBtn);
        sListBtn=(ImageButton)findViewById(R.id.sListBtn);
    }
}
