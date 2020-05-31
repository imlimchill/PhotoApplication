package com.MrPhoto.photoapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.MrPhoto.photoapplication.util.Utils;
import com.google.android.flexbox.FlexboxLayout;

public class MainActivity extends AppCompatActivity {

    private enum ScreenRatio {S1_1, S3_4, S16_9}

    /**
     * 메인 패널
     */
    private ConstraintLayout pnlMain;
    /**
     * 상단 여백 패널
     */
    private View pnlTop;
    /**
     * 하단 여백 패널
     */
    private View pnlBottom;
    /**
     * 카메라 패널
     */
    private View pnlCamera;
    /**
     * 상단 버튼 패널
     */
    private View pnlTopBtn;
    /**
     * 하단 버튼 패널
     */
    private View pnlBottomBtn;
    /**
     * 스티커 패널
     */
    private View pnlStiker;
    /**
     * 필터 패널
     */
    private View pnlFilter;
    /**
     * 설정 창 버튼
     */
    private Button settingBtn;
    /**
     * 화면 비율 변환 버튼
     */
    private Button rationBtn;
    /**
     * 정방향, 후방향 화면 전환 버튼
     */
    private Button reverseBtn;
    /**
     * 스티커 버튼
     */
    private View stikerBtn;
    /**
     * 촬영 버튼
     */
    private View photoBtn;
    /**
     * 필터 버튼
     */
    private View filterBtn;

    private int dp4;
    private int dp40;
    private int dp12;
    private int dp100;

    private ScreenRatio mScreenRatio = ScreenRatio.S3_4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // region [ 변수 초기화 ]

        dp4 = Utils.dp2px(this, 4);
        dp12 = Utils.dp2px(this, 12);
        dp40 = Utils.dp2px(this, 40);
        dp100 = Utils.dp2px(this, 100);

        pnlMain = findViewById(R.id.pnlMain);
        pnlTop = findViewById(R.id.pnlTop);
        pnlBottom = findViewById(R.id.pnlBottom);
        pnlCamera = findViewById(R.id.pnlCamera);
        pnlTopBtn = findViewById(R.id.pnlTopBtn);
        pnlBottomBtn = findViewById(R.id.pnlBottomBtn);

        settingBtn = findViewById(R.id.settingBtn);
        rationBtn = findViewById(R.id.ratioBtn);
        reverseBtn = findViewById(R.id.reverseBtn);
        stikerBtn = findViewById(R.id.stikerBtn);
        photoBtn = findViewById(R.id.photoBtn);
        filterBtn = findViewById(R.id.filterBtn);

        // endregion

        // region [ 이벤트 리스너 등록 ]

        stikerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 동적으로 패널 추가
                getLayoutInflater().inflate(R.layout.view_sticker_list, pnlMain, true);
                pnlStiker = pnlMain.getViewById(R.id.stikerList);
                if (pnlStiker == null) return;

                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                pnlStiker.setLayoutParams(layoutParams);

                pnlStiker.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_up));
                pnlStiker.setBackgroundColor(Color.TRANSPARENT);

                View stikerBack = pnlStiker.findViewById(R.id.stikerBack);
                stikerBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.this.onBackPressed();
                    }
                });

                // 스티커 버튼의 리스트
                LinearLayout listStikerItems = pnlStiker.findViewById(R.id.list_sticker_items);

                // 실제 스티커 리스트
                final FlexboxLayout listStiker = pnlStiker.findViewById(R.id.list_sticker);

                // 스티커 버튼의 레이아웃 정의
                LinearLayout.LayoutParams btnStickerButtonLayoutParams = new LinearLayout.LayoutParams(dp40, dp40);
                btnStickerButtonLayoutParams.setMarginEnd(dp12);

                // 즐겨찾기 버튼 추가
                ImageView btnFav = new ImageView(MainActivity.this);
                btnFav.setLayoutParams(btnStickerButtonLayoutParams);
                btnFav.setPadding(dp4, dp4, dp4, dp4);
                btnFav.setAdjustViewBounds(true);
                btnFav.setImageResource(R.drawable.favorite);
                btnFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 스티커의 레이아웃 정의
                        FlexboxLayout.LayoutParams stikerLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                        stikerLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                        addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                        addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                        addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                        addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                        addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                        addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                    }
                });

                listStikerItems.addView(btnFav);

                // ??? 스티커 추가
                ImageView btnStiker1 = new ImageView(MainActivity.this);
                btnStiker1.setLayoutParams(btnStickerButtonLayoutParams);
                btnStiker1.setPadding(dp4, dp4, dp4, dp4);
                btnStiker1.setAdjustViewBounds(true);
                btnStiker1.setImageResource(R.drawable.slist);
                btnStiker1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 첫번째 스티커 버튼이 추가 되었을 때 어떤 행동을 해야 할지 정의
                        Toast.makeText(MainActivity.this, "스티커1 버튼 클릭", Toast.LENGTH_SHORT).show();
                    }
                });

                listStikerItems.addView(btnStiker1);

                // 기본으로 즐겨찾기를 먼저 자동으로 클릭
                btnFav.performClick();
            }
        });

        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLayoutInflater().inflate(R.layout.view_filter_list, pnlMain, true);
                pnlFilter = pnlMain.getViewById(R.id.filterList);
                if (pnlFilter == null) return; //제대로 가져왔는지 확인

                ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                pnlFilter.setLayoutParams(layoutParams);

                pnlFilter.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_up));
                pnlFilter.setBackgroundColor(Color.TRANSPARENT);

                View filterBack = pnlFilter.findViewById(R.id.filterBack);
                filterBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MainActivity.this.onBackPressed();
                    }
                });
            }
        });

        // endregion

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) return;

        // 메인 패널 사이즈 가져오기
        int mainWidth = pnlMain.getMeasuredWidth();
        int mainHeight = pnlMain.getMeasuredHeight();

        // 비율별 카메라 높이 계산
        int cameraHeight;
        if (mScreenRatio == ScreenRatio.S3_4) cameraHeight = (int) (mainWidth / 3.0 * 4);
        else if (mScreenRatio == ScreenRatio.S1_1) cameraHeight = mainWidth;
        else cameraHeight = (int) (mainWidth / 9.0 * 16);

        int topBottomFrameSize = mainHeight - cameraHeight;

        // 상, 하단 여백 계산
        int topHeight = (int) (topBottomFrameSize * (2.0 / 5.0));
        int bottomHeight = topBottomFrameSize - topHeight;

        pnlTop.getLayoutParams().height = topHeight;
        pnlBottom.getLayoutParams().height = bottomHeight;

        ConstraintLayout.LayoutParams pnlCameraLayoutParams = (ConstraintLayout.LayoutParams) pnlCamera.getLayoutParams();
        pnlCameraLayoutParams.topMargin = topHeight;
        pnlCameraLayoutParams.bottomMargin = bottomHeight;

        pnlCamera.setLayoutParams(pnlCameraLayoutParams);

        // 상, 하단 버튼 여백 계산
        int pnlTopHeight = pnlTopBtn.getMeasuredHeight();
        int pnlBottomHeight = pnlBottomBtn.getMeasuredHeight();

        if (topHeight - pnlTopHeight > 0)
            ((ConstraintLayout.LayoutParams) pnlTopBtn.getLayoutParams()).topMargin = (topHeight - pnlTopHeight) / 2;
        if (bottomHeight - pnlBottomHeight > 0)
            ((ConstraintLayout.LayoutParams) pnlBottomBtn.getLayoutParams()).bottomMargin = (bottomHeight - pnlBottomHeight) / 2;
    }

    public void addSticker(FlexboxLayout.LayoutParams stickerLayoutParams, FlexboxLayout listSticker, @DrawableRes int resId) {
        ImageView sticker = new ImageView(MainActivity.this);
        sticker.setLayoutParams(stickerLayoutParams);
        sticker.setPadding(dp4, dp4, dp4, dp4);
        sticker.setAdjustViewBounds(true);
        sticker.setImageResource(resId);
        sticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "스티커 클릭 되었어요.", Toast.LENGTH_SHORT).show();
            }
        });

        listSticker.addView(sticker);
    }

    @Override
    public void onBackPressed() {
        // 스티커 패널 열려 있을 시 닫아주기
        if (pnlStiker != null) {
            pnlStiker.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_down));
            pnlMain.removeView(pnlStiker);
            pnlStiker = null;
            return;
        }
        if (pnlFilter != null) {
            pnlFilter.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_down));
            pnlMain.removeView(pnlFilter);
            pnlFilter = null;
            return;
        }
        super.onBackPressed();

    }


}
