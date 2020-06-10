package com.MrPhoto.photoapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.MrPhoto.photoapplication.graphic.FaceGraphic;
import com.MrPhoto.photoapplication.graphic.GraphicOverlay;
import com.MrPhoto.photoapplication.util.Utils;
import com.google.android.flexbox.FlexboxLayout;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity.class";

    private static final String STATE_LENS_FACING = "lens_facing";

    // 화면 비율 정의
    private enum ScreenRatio {S1_1, S4_3, S16_9}

    // 상수 값 선언
    private int dp4;
    private int dp40;
    private int dp12;

    private List<ScreenRatio> mScreenRatios;
    private ScreenRatio mScreenRatio = ScreenRatio.S4_3;

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
    private View settingBtn;
    /**
     * 화면 비율 변환 버튼
     */
    private ImageView rationBtn;
    /**
     * 정방향, 후방향 화면 전환 버튼
     */
    private View reverseBtn;
    /**
     * 스티커 버튼
     */
    private View stikerBtn;
    /**
     * 촬영 버튼
     */
    private View photoBtn;
    /**
     * 음소거 판단 필드
     */
    boolean isMute = true;
    /**
     * 플래시 판단 필드
     */
    int flashMode = ImageCapture.FLASH_MODE_OFF;
    /** 타이머 시간 설정 필드 */
    int time = 0;
    /**
     * 필터 버튼
     */
    private View filterBtn;

    private GraphicOverlay graphicOverlay;

    // 권한 허용을 위한 메소드가 있는 클래스 선언
    private PermissionSupport mPermissionHelper;

    // 카메라 화면 View
    private PreviewView mCameraPreviewView;

    // 백버튼이 눌린 마지막 시간을 저장함.
    private long backKeyPressedTime = 0;

    // 카메라 API
    private Rational mAspectRatio;
    private Size mPreviewSize;
    private int mLensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector mCameraSelector;
    private ProcessCameraProvider mCameraProvider;
    private Preview mPreviewUseCase;
    private ImageAnalysis mImageAnalysisUseCase;
    private ImageCapture mImageCaptureUseCase;

    private int mCurrentStickerResourceId;
    private FaceProcessing mFaceProcessing;
    private PreviewTransformation mPreviewTransformation;

    private File mOutputBaseDir;

    private int mOrientation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate()");

        // region [ 변수 초기화 ]

        dp4 = Utils.dp2px(this, 4);
        dp12 = Utils.dp2px(this, 12);
        dp40 = Utils.dp2px(this, 40);

        mOutputBaseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mCurrentStickerResourceId = 0;

        mScreenRatios = new ArrayList<>();
        mScreenRatios.add(ScreenRatio.S1_1);
        mScreenRatios.add(ScreenRatio.S4_3);
        mScreenRatios.add(ScreenRatio.S16_9);

        if (savedInstanceState != null)
            mLensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK);

        pnlMain = findViewById(R.id.pnlMain);
        pnlTop = findViewById(R.id.pnlTop);
        pnlBottom = findViewById(R.id.pnlBottom);
        pnlCamera = findViewById(R.id.textureView);
        pnlTopBtn = findViewById(R.id.pnlTopBtn);
        pnlBottomBtn = findViewById(R.id.pnlBottomBtn);

        settingBtn = findViewById(R.id.settingBtn);
        rationBtn = findViewById(R.id.ratioBtn);
        reverseBtn = findViewById(R.id.reverseBtn);
        stikerBtn = findViewById(R.id.stikerBtn);
        photoBtn = findViewById(R.id.photoBtn);
        filterBtn = findViewById(R.id.filterBtn);

        graphicOverlay = findViewById(R.id.graphicOverlay);
        // endregion

        // region [ 이벤트 리스너 등록 ]

        // region [ 설정 버튼 클릭 시 ]
        settingBtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), PopActivity.class);
            startActivity(intent);
        });
        // endregion

        // region [ 비율 버튼 클릭 시 ]
        rationBtn.setOnClickListener(v -> {
            ScreenRatio curScreenRatio = mScreenRatio;

            int index = mScreenRatios.indexOf(curScreenRatio);
            int nextIndex = index + 1;
            if (nextIndex == mScreenRatios.size()) nextIndex = 0;

            switch (nextIndex) {
                case 0:
                    rationBtn.setImageResource(R.drawable.s11_2);
                    break;
                case 1:
                    rationBtn.setImageResource(R.drawable.s43_2);
                    break;
                case 2:
                    rationBtn.setImageResource(R.drawable.s169_2);
                    break;
                default:
                    break;
            }

            mScreenRatio = mScreenRatios.get(nextIndex);
            mPreviewTransformation = null;

            onWindowFocusChanged(true);
        });
        // endregion

        // region [ 화면 전환 버튼 클릭 시 ]
        reverseBtn.setOnClickListener(view -> {
            // 클릭 시 화면 FRONT > BACK / BACK > FRONT
            if (mLensFacing == CameraSelector.LENS_FACING_FRONT) {
                mLensFacing = CameraSelector.LENS_FACING_BACK;
            } else {
                mLensFacing = CameraSelector.LENS_FACING_FRONT;
            }
            // 새로운 mLensFacing 값을 mCameraSelector 에 넣는다.
            mCameraSelector = new CameraSelector.Builder().requireLensFacing(mLensFacing).build();

            // UseCases reload
            bindAllCameraUseCases();

            // GraphicOverlay clear
            if (graphicOverlay != null) graphicOverlay.clear();
        });
        // endregion

        // region [ 스티커 버튼 클릭 시 ]
        stikerBtn.setOnClickListener(v -> {
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
            stikerBack.setOnClickListener(v1 -> MainActivity.this.onBackPressed());

            // 스티커 버튼의 리스트
            LinearLayout listStikerItems = pnlStiker.findViewById(R.id.list_sticker_items);

            // 실제 스티커 리스트
            final FlexboxLayout listStiker = pnlStiker.findViewById(R.id.list_sticker);

            // 스티커 버튼의 레이아웃 정의
            LinearLayout.LayoutParams btnStickerButtonLayoutParams = new LinearLayout.LayoutParams(dp40, dp40);
            btnStickerButtonLayoutParams.setMarginEnd(dp12);

            // 스티커 해제 버튼
            View btnStickerNone = pnlStiker.findViewById(R.id.btnStickerNone);
            btnStickerNone.setOnClickListener(v17 -> {
                unBindAnalysisUseCase();
                mCurrentStickerResourceId = 0;
                graphicOverlay.clear();
            });

            // 즐겨찾기 스티커 버튼
            ImageView btnFav = new ImageView(MainActivity.this);
            btnFav.setLayoutParams(btnStickerButtonLayoutParams);
            btnFav.setPadding(dp4, dp4, dp4, dp4);
            btnFav.setAdjustViewBounds(true);
            btnFav.setImageResource(R.drawable.favorite);
            btnFav.setOnClickListener(v12 -> {
                listStiker.removeAllViews();

                // 스티커의 레이아웃 정의
                FlexboxLayout.LayoutParams stikerLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                stikerLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                //즐겨찾기,전체 스티커 아이콘 계속 누르면 무한 생성됨. 수정할 부분
            });

            listStikerItems.addView(btnFav);

            // 첫번째 스티커 리스트 추가
            ImageView btnSticker1 = new ImageView(MainActivity.this);
            btnSticker1.setLayoutParams(btnStickerButtonLayoutParams);
            btnSticker1.setPadding(dp4, dp4, dp4, dp4);
            btnSticker1.setAdjustViewBounds(true);
            btnSticker1.setImageResource(R.drawable.slist);
            btnSticker1.setOnClickListener(v13 -> {
                listStiker.removeAllViews();

                FlexboxLayout.LayoutParams stikerLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                stikerLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
                addSticker(stikerLayoutParams, listStiker, R.drawable.filter_list);
                addSticker(stikerLayoutParams, listStiker, R.drawable.photo);
                addSticker(stikerLayoutParams, listStiker, R.drawable.stiker);
            });

            listStikerItems.addView(btnSticker1);

            // 첫번째 스티커 리스트 추가
            ImageView sDoraemong = new ImageView(MainActivity.this);
            sDoraemong.setLayoutParams(btnStickerButtonLayoutParams);
            sDoraemong.setPadding(dp4, dp4, dp4, dp4);
            sDoraemong.setAdjustViewBounds(true);
            sDoraemong.setImageResource(R.drawable.s_do);
            sDoraemong.setOnClickListener(v15 -> {
                listStiker.removeAllViews();

                FlexboxLayout.LayoutParams stikerLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                stikerLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_1);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_2);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_3);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_4);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_5);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_6);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_7);
                addSticker(stikerLayoutParams, listStiker, R.drawable.s_do_8);
            });

            listStikerItems.addView(sDoraemong);

            // 기본으로 첫번째 스티커 를 먼저 자동으로 클릭
            btnSticker1.performClick();
        });
        // endregion

        // region [ 필터 버튼 클릭 시 ]
        filterBtn.setOnClickListener(v -> {
            getLayoutInflater().inflate(R.layout.view_filter_list, pnlMain, true);
            pnlFilter = pnlMain.getViewById(R.id.filterList);
            if (pnlFilter == null) return; //제대로 가져왔는지 확인

            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            pnlFilter.setLayoutParams(layoutParams);

            pnlFilter.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_in_up));
            pnlFilter.setBackgroundColor(Color.TRANSPARENT);

            View filterBack = pnlFilter.findViewById(R.id.filterBack);
            filterBack.setOnClickListener(v14 -> MainActivity.this.onBackPressed());

            // 필터 버튼의 리스트
            LinearLayout listFilterItems = pnlFilter.findViewById(R.id.list_filter_items);

            // 실제 스티커 리스트
            final FlexboxLayout listFilter = pnlFilter.findViewById(R.id.list_filter);

            //스티커 버튼의 레이아웃 정의
            LinearLayout.LayoutParams btnFilterButtonLayoutParams = new LinearLayout.LayoutParams(dp40, dp40);
            btnFilterButtonLayoutParams.setMarginEnd(dp12);

            //즐겨찾기 필터 버튼
            ImageView btnFav = new ImageView(MainActivity.this);
            btnFav.setLayoutParams(btnFilterButtonLayoutParams);
            btnFav.setPadding(dp4, dp4, dp4, dp4);
            btnFav.setAdjustViewBounds(true);
            btnFav.setImageResource(R.drawable.favorite);
            btnFav.setOnClickListener(v15 -> {
                // 스티커의 레이아웃 정의
                FlexboxLayout.LayoutParams filterLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                filterLayoutParams.setMargins(dp4, dp4, dp4, dp4);


                addFilter(filterLayoutParams, listFilter, R.drawable.filter_list);
                addFilter(filterLayoutParams, listFilter, R.drawable.photo);
                //즐겨찾기,전체 스티커 아이콘 계속 누르면 무한 생성됨. 수정할 부분
            });
            listFilterItems.addView(btnFav);

            // 전체 필터 추가
            ImageView btnAll = new ImageView(MainActivity.this);
            btnAll.setLayoutParams(btnFilterButtonLayoutParams);
            btnAll.setPadding(dp4, dp4, dp4, dp4);
            btnAll.setAdjustViewBounds(true);
            btnAll.setImageResource(R.drawable.filter_list);
            btnAll.setOnClickListener(v16 -> {
                FlexboxLayout.LayoutParams filterLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                filterLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                addFilter(filterLayoutParams, listFilter, R.drawable.temp);
            });
            listFilterItems.addView(btnAll);
            // 기본으로 전체를 먼저 자동으로 클릭
            btnAll.performClick();
        });
        // endregion

        // region [ 사진 버튼 클릭 시 ]
        photoBtn.setOnClickListener(v -> {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    takePicture();
                    if (isMute) {
                        MediaActionSound sound = new MediaActionSound();
                        sound.play(MediaActionSound.SHUTTER_CLICK);
                    }
                }
            };
            timer.schedule(timerTask, time);
        });

        //        (음소거 버튼).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                if (isMute == true) {
        //                    isMute = false;
        //                } else {
        //                    isMute = true;
        //                }
        //            }
        //        });

        //        // 플래시 버튼 클릭시 플래시 기능을 끄고 킬 수 있는 기능 구현
        //        (플래시버튼).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                if (flashMode == ImageCapture.FLASH_MODE_OFF) {
        //                    flashMode = ImageCapture.FLASH_MODE_ON;
        //                } else {
        //                    flashMode = ImageCapture.FLASH_MODE_OFF;
        //                }
        //                openCamera();
        //            }
        //        });

        //        // 타이머 클릭 시 time의 시간 후에 사진 촬영 기능을 수행한다.
        //        (타이머버튼).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                if (time == 0) {
        //                    time = 3000;
        //                } else if (time == 3000) {
        //                    time = 5000;
        //                } else if (time == 5000) {
        //                    time = 7000;
        //                } else {
        //                    time = 0;
        //                }
        //            }
        //        });

        // endregion

        // endregion

        // 카메라 권한을 확인하기 위한 코드 실행
        checkPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mFaceProcessing != null) mFaceProcessing.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mFaceProcessing != null) mFaceProcessing.stop();
    }

    /**
     * Window 가 Focus 되었을 경우 (화면이 디스플레이에 표출 되는 경우) 실행
     * 카메라 프리뷰의 비율을 계산하여 표시
     * 1:1, 4:3, 16:9 이용 가능
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG, "onWindowFocusChanged(" + hasFocus + ")");
        if (!hasFocus) return;

        // 메인 패널 사이즈 가져오기
        int mainWidth = pnlMain.getMeasuredWidth();
        int mainHeight = pnlMain.getMeasuredHeight();
        Log.d(TAG, String.format("MainWidth: %dpx, Main Height: %dpx", mainWidth, mainHeight));

        // 비율별 카메라 높이 계산
        int cameraHeight;
        if (mScreenRatio == ScreenRatio.S4_3) cameraHeight = (int) (mainWidth / 3.0 * 4);
        else if (mScreenRatio == ScreenRatio.S1_1) cameraHeight = mainWidth;
        else cameraHeight = (int) (mainWidth / 9.0 * 16);
        Log.d(TAG, String.format("Camera Width: %dpx, Camera Height: %dpx", mainWidth, cameraHeight));

        // 카메라 프리뷰를 제외한 남은 공간
        int topBottomFrameSize = mainHeight - cameraHeight;
        Log.d(TAG, String.format("Remain Height: %dpx", topBottomFrameSize));

        // 상, 하단 버튼 높이 측정
        int pnlTopBtnHeight = pnlTopBtn.getMeasuredHeight();
        int pnlBottomBtnHeight = pnlBottomBtn.getMeasuredHeight();
        Log.d(TAG, String.format("Top Btn Panel Height: %dpx, Bottom Btn Panel Height: %dpx", pnlTopBtnHeight, pnlBottomBtnHeight));

        // 남은 공간 2:3 비율로 나눈 결과 측정
        int topHeight = (int) (topBottomFrameSize * (2.0 / 5.0));
        int bottomHeight = topBottomFrameSize - topHeight;
        Log.d(TAG, String.format("Top Height: %dpx, Bottom Height: %dpx", topHeight, bottomHeight));

        // 여백 공간이 안맞는 경우 상단 빈 여백 제거
        if (bottomHeight < pnlBottomBtnHeight) {
            bottomHeight = topBottomFrameSize;
            topHeight = 0;
        }

        if (topHeight == 0 && bottomHeight < pnlBottomBtnHeight) {
            bottomHeight = 0;
            cameraHeight = mainHeight;
        }

        // 상단 여백 높이 및 색상 처리
        pnlTop.getLayoutParams().height = topHeight;
        if (topHeight == 0) pnlTop.setBackgroundColor(Color.TRANSPARENT);

        // 하단 여백 높이 및 색상 처리
        pnlBottom.getLayoutParams().height = bottomHeight;
        pnlBottom.setBackgroundColor(Color.TRANSPARENT);

        // 계산 완료 후 카메라 레이아웃의 여백 재 설정
        ConstraintLayout.LayoutParams pnlCameraLayoutParams = (ConstraintLayout.LayoutParams) pnlCamera.getLayoutParams();
        pnlCameraLayoutParams.topMargin = topHeight;
        pnlCameraLayoutParams.bottomMargin = bottomHeight;
        pnlCamera.setLayoutParams(pnlCameraLayoutParams);

        // 그래픽 레이아웃의 여백 재 설정
        ConstraintLayout.LayoutParams graphicLayoutParams = (ConstraintLayout.LayoutParams) graphicOverlay.getLayoutParams();
        graphicLayoutParams.topMargin = topHeight;
        graphicLayoutParams.bottomMargin = bottomHeight;
        graphicOverlay.setLayoutParams(graphicLayoutParams);

        // 여백공간이 조금 남는 경우 가운데 정렬
        if (topHeight - pnlTopBtnHeight > 0)
            ((ConstraintLayout.LayoutParams) pnlTopBtn.getLayoutParams()).topMargin = (topHeight - pnlTopBtnHeight) / 2;
        if (bottomHeight - pnlBottomBtnHeight > 0)
            ((ConstraintLayout.LayoutParams) pnlBottomBtn.getLayoutParams()).bottomMargin = (bottomHeight - pnlBottomBtnHeight) / 2;

        // 카메라 프리뷰 크기 가져오기
        mCameraPreviewView = (PreviewView) pnlCamera;

        // 프리뷰의 크기 측정
        mPreviewSize = new Size(mainWidth, cameraHeight);
        mAspectRatio = new Rational(mainWidth, cameraHeight);

        openCamera();
    }

    /**
     * 스티커 리스트를 클릭 한 경우 동적으로 스티커 추가
     */
    public void addSticker(FlexboxLayout.LayoutParams stickerLayoutParams, FlexboxLayout listSticker, @DrawableRes int resId) {
        int width = pnlMain.getMeasuredWidth() - Utils.dp2px(this, 24);
        int imgSize = width / 4 - Utils.dp2px(this, 8);

        stickerLayoutParams.width = imgSize;
        stickerLayoutParams.height = imgSize;

        ImageView sticker = new ImageView(MainActivity.this);
        sticker.setLayoutParams(stickerLayoutParams);
        sticker.setPadding(dp4, dp4, dp4, dp4);
        sticker.setAdjustViewBounds(true);
        sticker.setImageResource(resId);
        sticker.setOnClickListener(v -> {
            if (mImageAnalysisUseCase == null) bindAnalysisUseCase();
            mCurrentStickerResourceId = resId;
        });
        listSticker.addView(sticker);
    }

    /**
     * 필터 리스트를 클릭 한 경우 동적으로 필터 추가
     */
    public void addFilter(FlexboxLayout.LayoutParams filterLayoutParams, FlexboxLayout listFilter, @DrawableRes int resId) {
        ImageView filter = new ImageView(MainActivity.this);
        filter.setLayoutParams(filterLayoutParams);
        filter.setPadding(dp4, dp4, dp4, dp4);
        filter.setAdjustViewBounds(true);
        filter.setImageResource(resId);
        filter.setOnClickListener(v -> Toast.makeText(MainActivity.this, "필터 클릭 되었어요.", Toast.LENGTH_SHORT).show());

        listFilter.addView(filter);
    }

    /**
     * 카메라 프리뷰 권한을 가지고 있는지 체크하는 함수
     */
    public void checkPermission() {
        Log.d(TAG, "checkPermission()");
        // SDK 23이하(안드로이드 6.0이하) 버전에서는 사용자 권한 허용이 필요하지 않음
        if (Build.VERSION.SDK_INT >= 23) {
            mPermissionHelper = new PermissionSupport(this, this);

            // 권한이 없으면 퍼미션을 요청한다.
            if (!mPermissionHelper.checkPermission()) mPermissionHelper.requestPermissions();
        }
    }

    /**
     * 뒤로가기 버튼 클릭 되었을 경우 실행 되는 함수
     */
    @Override
    public void onBackPressed() {
        // 스티커 패널 열려 있을 시 닫아주기
        if (pnlStiker != null) {
            pnlStiker.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_down));
            pnlMain.removeView(pnlStiker);
            pnlStiker = null;
            return;
        }

        // 필터 패널 열려 있을 시 닫아주기
        if (pnlFilter != null) {
            pnlFilter.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_out_down));
            pnlMain.removeView(pnlFilter);
            pnlFilter = null;
            return;
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return; //백버튼이 한 번만 눌리면 종료 안내 메시지가 뜬다.
        }

        /* 마지막으로 백버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후,
           마지막으로 백버튼을 눌렀던 시간이 2초가 지나지 않았으면 앱을 종료시킴.
           (메시지가 유지되는 2초동안 백버튼을 한 번 더 누르면 앱 종료) */
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) finish();
    }

    /**
     * 카메라를 열기 위한 함수
     */
    private void openCamera() {
        Log.d(TAG, "openCamera()");
        if (!mPermissionHelper.checkPermission()) return;
        else if (mCameraProvider != null) {
            // 이미 카메라가 열려 있으면 UseCases만 바인딩
            bindAllCameraUseCases();
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                mCameraProvider = cameraProviderFuture.get();
                bindAllCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 카메라의 UseCases 바인딩
     */
    private void bindAllCameraUseCases() {
        Log.d(TAG, "bindAllCameraUseCases()");
        unBindAllUseCases();

        bindPreviewUseCase();
        bindImageCaptureUseCase();
    }

    /**
     * 카메라의 프리뷰를 위한 UseCase
     */
    private void bindPreviewUseCase() {
        if (mCameraProvider == null) return;

        if (mPreviewUseCase != null) mCameraProvider.unbind(mPreviewUseCase);

        // 프리뷰 UseCase 작성
        mPreviewUseCase = new Preview.Builder()
                .setTargetAspectRatioCustom(mAspectRatio)
                .setTargetResolution(mPreviewSize)
                .build();

        // 프리뷰에 서페이스 적용
        mPreviewUseCase.setSurfaceProvider(mCameraPreviewView.createSurfaceProvider());

        mCameraProvider.bindToLifecycle(this, mCameraSelector, mPreviewUseCase);
    }

    /**
     * 카메라 프리뷰의 이미지 분석을 위한 UseCase
     */
    private void bindAnalysisUseCase() {
        if (mCameraProvider == null) return;

        if (mImageAnalysisUseCase != null) mCameraProvider.unbind(mImageAnalysisUseCase);

        if (mFaceProcessing != null) {
            mFaceProcessing.stop();
            mFaceProcessing = null;
        }

        mFaceProcessing = new FaceProcessing(this, this::drawFaces);

        mImageAnalysisUseCase = new ImageAnalysis.Builder()
                .setTargetResolution(mPreviewSize)
                .build();

        mImageAnalysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(this),
                imageProxy -> mFaceProcessing.processImageProxy(imageProxy));

        mCameraProvider.bindToLifecycle(this, mCameraSelector, mImageAnalysisUseCase);
    }

    /**
     * 카메라 프리뷰의 이미지 분석 UseCase 를 Unbinding
     */
    private void unBindAnalysisUseCase() {
        if (mImageAnalysisUseCase != null) {
            mCameraProvider.unbind(mImageAnalysisUseCase);
            mImageAnalysisUseCase = null;
        }

        if (mFaceProcessing != null) {
            mFaceProcessing.stop();
            mFaceProcessing = null;
        }
    }

    /**
     * 카메라 캡처를 위한 UseCase
     */
    private void bindImageCaptureUseCase() {
        if (mCameraProvider == null) return;

        if (mImageCaptureUseCase != null) mCameraProvider.unbind(mImageCaptureUseCase);

        mImageCaptureUseCase = new ImageCapture.Builder()
                .setTargetResolution(mPreviewSize)
                .setTargetAspectRatioCustom(mAspectRatio)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(flashMode)
                .build();

        mCameraProvider.bindToLifecycle(this, mCameraSelector, mImageCaptureUseCase);
    }

    /**
     * 카메라의 UseCases 바인딩
     */
    private void unBindAllUseCases() {
        Log.d(TAG, "unBindAllUseCases()");
        if (mCameraProvider == null) return;
        mCameraProvider.unbindAll();

        if (mPreviewUseCase != null) mPreviewUseCase = null;
        if (mImageAnalysisUseCase != null) mImageAnalysisUseCase = null;
        if (mImageCaptureUseCase != null) mImageCaptureUseCase = null;

        if (mFaceProcessing != null) {
            mFaceProcessing.stop();
            mFaceProcessing = null;
        }

        if (graphicOverlay != null) {
            graphicOverlay.clear();
        }
    }

    /**
     * 사진 촬영 함수
     */
    private void takePicture() {
        if (mImageCaptureUseCase == null) return;

        String imageFileName = "MrPhoto_" + new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.KOREA).format(new Date()) + ".jpg";

        // region [ Android Q 이상 API 에서의 사진 처리 ]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            takePictureOverAndroidQ(imageFileName);
        }
        // endregion

        // region [ Android Q 미만 API 에서의 사진 처리 ]
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            takePictureUnderAndroidQ(imageFileName);
        }
        // endregion
    }

    /**
     * Android Q 이상일 때의 사진 촬영
     */
    private void takePictureOverAndroidQ(String imageFileName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MrPhoto");

        ImageCapture.Metadata metaData = new ImageCapture.Metadata();

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .setMetadata(metaData)
                .build();

        mImageCaptureUseCase.takePicture(outputFileOptions, Executors.newSingleThreadExecutor(), new ImageCapture.OnImageSavedCallback() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Uri savedUri = outputFileResults.getSavedUri();
                if (savedUri == null) return;

                // 사진 저장이 완료되었다고 resolver 에게 통보
                contentValues.clear();
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(savedUri, contentValues, null, null);

                runOnUiThread(() -> Toast.makeText(MainActivity.this, "사진을 찍었습니다.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.w(TAG, exception.getMessage(), exception);
            }
        });
    }

    /**
     * Android Q 미만일 때의 사진 촬영
     */
    private void takePictureUnderAndroidQ(String imageFileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return;

        File imageFile = new File(getOutputDirectory(), imageFileName);

        try {
            if (!imageFile.createNewFile()) throw new IOException("파일 생성에 실패 했습니다.");
        } catch (IOException e) {
            Log.w(TAG, e.getMessage(), e);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mImageCaptureUseCase.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            @SuppressLint("UnsafeExperimentalUsageError")
            public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                Image image = imageProxy.getImage();
                if (image == null) throw new RuntimeException("이미지가 없습니다.");

                // 이미지 프록시에서 바이트 버퍼 가져오기
                ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[byteBuffer.capacity()];
                byteBuffer.get(bytes);

                // 가져온 바이트 배열 이미지 파일에 저장
                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    fos.write(bytes);
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "사진을 저장하는데 실패 했습니다.", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 이미지 사용을 다 한 후 종료
                imageProxy.close();

                // 새로운 이미지가 저장 되었다는 broadcast 전송
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

                // 새로운 이미지 촬영 완료 Toast 전송
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "사진 촬영을 완료 했습니다.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });
    }

    /**
     * 인식한 얼굴을 그릴 때 사용하는 함수
     */
    private void drawFaces(List<Face> faces, InputImage inputImage, Bitmap originalCameraImage) {
        graphicOverlay.clear();

        if (faces == null || faces.size() == 0 || inputImage == null || originalCameraImage == null)
            return;

        // Log.d(TAG, "Graphic Overlay Width: " + graphicOverlay.getWidth() + ", Graphic Overlay Height: " + graphicOverlay.getHeight());
        // Log.d(TAG, "Original Image Width: " + originalCameraImage.getWidth() + ", Original Image Height: " + originalCameraImage.getHeight());
        // Log.d(TAG, "Image Width: " + inputImage.getWidth() + ", Image Height: " + inputImage.getHeight() + ", Rotation: " + inputImage.getRotationDegrees());

        if (mPreviewTransformation == null) {
            mPreviewTransformation = new PreviewTransformation(
                    /* 뷰 사이즈 = */ new Size(graphicOverlay.getMeasuredWidth(), graphicOverlay.getMeasuredHeight()),
                    /* 이미지 사이즈 = */ new Size(inputImage.getWidth(), inputImage.getHeight()),
                    /* 이미지 회전 체크 = */ inputImage.getRotationDegrees()
            );

            graphicOverlay.setRatio(mPreviewTransformation.getXRatio(), mPreviewTransformation.getYRatio());
        }

        if (mCurrentStickerResourceId == 0) return;

        for (Face face : faces) {
            Bitmap sticker = BitmapFactory.decodeResource(getResources(), mCurrentStickerResourceId);
            graphicOverlay.add(new FaceGraphic(graphicOverlay, face, sticker));
        }
    }

    /**
     * 프리뷰의 비율과 이미지의 비율을 계산하는 클래스
     */
    public static class PreviewTransformation {
        float xRatio;
        float yRatio;

        public PreviewTransformation(Size viewSize, Size imageSize, int rotationDegree) {
            if (rotationDegree == 0 || rotationDegree == 180) {
                xRatio = (float) viewSize.getWidth() / imageSize.getWidth();
                yRatio = (float) viewSize.getHeight() / imageSize.getHeight();
            } else {
                xRatio = (float) viewSize.getWidth() / imageSize.getHeight();
                yRatio = (float) viewSize.getHeight() / imageSize.getWidth();
            }
        }

        public float getXRatio() {
            return xRatio;
        }

        public float getYRatio() {
            return yRatio;
        }
    }

    /**
     * 사진 저장 폴더 설정
     */
    private File getOutputDirectory() {
        File outputDir = new File(mOutputBaseDir, "MrPhoto");
        if (!outputDir.exists()) if (!outputDir.mkdirs()) return getFilesDir();
        return outputDir;
    }

    /**
     * 카메라 사용을 위한 사용자의 권한 허용받기위한 함수
     * 만약 허용이 되지 않았다면 권한 허용에 대해 다시 물어본다.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 퍼미션이 있으면 카메라를 실행하고, 퍼미션이 없으면 다시 퍼미션을 요청한다.
        if (mPermissionHelper.permissionResult(requestCode, permissions, grantResults)) {
            openCamera();
        } else {
            mPermissionHelper.requestPermissions();
        }
    }
}