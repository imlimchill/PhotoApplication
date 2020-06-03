package com.MrPhoto.photoapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.MrPhoto.photoapplication.util.Utils;
import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // 화면 비율 정의
    private enum ScreenRatio {S1_1, S3_4, S16_9}

    // 상수 값 선언
    private int dp4;
    private int dp40;
    private int dp12;
    private int dp100;
    private ScreenRatio mScreenRatio = ScreenRatio.S3_4;

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

    // 권한 허용을 위한 메소드가 있는 클래스 선언
    private PermissionSupport mPermissionHelper;

    // 카메라 화면 View
    private TextureView mCameraPrview;

    // 백버튼이 눌린 마지막 시간을 저장함.
    private long backKeyPressedTime = 0;

    // 카메라 기본 orientation
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String mCameraId;

    /**
     * 물리 카메라 표현, 캡쳐세션, 캡쳐리퀘스 생성
     */
    private CameraDevice mCameraDevice;
    /**
     * 카메라로 얻은 이미지를 Surface에 출력요청
     */
    private CameraCaptureSession mCameraCaptionSession;
    /**
     * 카메라로 어떻게 이미지를 얻을까 결정
     */
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Size mImageDimension;
    private ImageReader imageReader;

    // 사진 저장을 위해 선언
    private File file;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };

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
        pnlCamera = findViewById(R.id.textureView);
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

        // region [ 스티커 버튼 클릭 시 ]
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

                // 즐겨찾기 스티커 버튼
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
                        //즐겨찾기,전체 스티커 아이콘 계속 누르면 무한 생성됨. 수정할 부분
                    }
                });

                listStikerItems.addView(btnFav);

                // 전체 스티커 추가
                ImageView btnAll = new ImageView(MainActivity.this);
                btnAll.setLayoutParams(btnStickerButtonLayoutParams);
                btnAll.setPadding(dp4, dp4, dp4, dp4);
                btnAll.setAdjustViewBounds(true);
                btnAll.setImageResource(R.drawable.slist);
                btnAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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

                listStikerItems.addView(btnAll);

                // 기본으로 전체를 먼저 자동으로 클릭
                btnAll.performClick();
            }
        });
        // endregion

        // region [ 필터 버튼 클릭 시 ]
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

                //필터 버튼의 리스트
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
                btnFav.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 스티커의 레이아웃 정의
                        FlexboxLayout.LayoutParams filterLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                        filterLayoutParams.setMargins(dp4, dp4, dp4, dp4);


                        addFilter(filterLayoutParams, listFilter, R.drawable.filter_list);
                        addFilter(filterLayoutParams, listFilter, R.drawable.photo);
                        //즐겨찾기,전체 스티커 아이콘 계속 누르면 무한 생성됨. 수정할 부분
                    }
                });
                listFilterItems.addView(btnFav);

                // 전체 필터 추가
                ImageView btnAll = new ImageView(MainActivity.this);
                btnAll.setLayoutParams(btnFilterButtonLayoutParams);
                btnAll.setPadding(dp4, dp4, dp4, dp4);
                btnAll.setAdjustViewBounds(true);
                btnAll.setImageResource(R.drawable.filter_list);
                btnAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FlexboxLayout.LayoutParams filterLayoutParams = new FlexboxLayout.LayoutParams(Utils.dp2px(MainActivity.this, 60), Utils.dp2px(MainActivity.this, 60));
                        filterLayoutParams.setMargins(dp4, dp4, dp4, dp4);

                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                        addFilter(filterLayoutParams, listFilter, R.drawable.temp);
                    }
                });
                listFilterItems.addView(btnAll);
                // 기본으로 전체를 먼저 자동으로 클릭
                btnAll.performClick();
            }
        });
        // endregion

        // region [ 사진 버튼 클릭 시 ]
        photoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        // endregion

        // endregion

        // region [ 카메라 정보 등록 ]

        // 카메라 프리뷰를 위한 선언
        mCameraPrview = (TextureView) pnlCamera;
        if (mCameraPrview == null) {
            new AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("카메라 화면을 찾을 수 없습니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    }).show();
            return;
        }
        mCameraPrview.setSurfaceTextureListener(textureListener);

        // endregion

        // 카메라 권한을 확인하기 위한 코드 실행
        checkPermission();
    }

    /**
     * Window 가 Focus 되었을 경우 (화면이 디스플레이에 표출 되는 경우) 실행
     * 카메라 프리뷰의 비율을 계산하여 표시
     * 1:1, 4:3, 16:9 이용 가능
     */
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
        int topHeight = (int) (topBottomFrameSize * (2.0 / 24.0));
        int bottomHeight = topBottomFrameSize - topHeight;

        pnlTop.setBackgroundColor(Color.TRANSPARENT);
        pnlTop.getLayoutParams().height = topHeight;
        pnlBottom.getLayoutParams().height = bottomHeight;
        pnlTopBtn.setBackgroundColor(Color.TRANSPARENT);

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

    /**
     * 스티커 리스트를 클릭 한 경우 동적으로 스티커 추가
     */
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

    /**
     * 필터 리스트를 클릭 한 경우 동적으로 필터 추가
     */
    public void addFilter(FlexboxLayout.LayoutParams filterLayoutParams, FlexboxLayout listFilter, @DrawableRes int resId) {
        ImageView filter = new ImageView(MainActivity.this);
        filter.setLayoutParams(filterLayoutParams);
        filter.setPadding(dp4, dp4, dp4, dp4);
        filter.setAdjustViewBounds(true);
        filter.setImageResource(resId);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "필터 클릭 되었어요.", Toast.LENGTH_SHORT).show();
            }
        });

        listFilter.addView(filter);
    }

    /**
     * 카메라 프리뷰 권한을 가지고 있는지 체크하는 함수
     */
    public void checkPermission() {
        // SDK 23이하(안드로이드 6.0이하) 버전에서는 사용자 권한 허용이 필요하지 않음
        if (Build.VERSION.SDK_INT >= 23) {
            mPermissionHelper = new PermissionSupport(this, this);

            // 권한 요청이 false 값을 리턴한다면 권한 허용을 요청한다.
            if (!mPermissionHelper.checkPermission()) {
                mPermissionHelper.requestPermissions();
            }
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
        if (!mPermissionHelper.checkPermission()) return;

        // 카메라 목록, 특성, 모니터링 > 사용가능한 카메라를 관리하고 제공
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            Toast.makeText(this, "카메라 서비스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mCameraId = manager.getCameraIdList()[0];
            // 카메라 특성 정보 > CameraDevice에 대한 메타데이터 제공
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            mImageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            manager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라 접근에 문제가 발생 했습니다. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 카메라의 프리뷰를 만드는 함수
     * 해당 함수 안에서 updatePreview를 실행한다.
     */
    private void createCameraPreview() {
        try {
            // 캡쳐 세션을 만들기 전에 프리뷰를 위한 SurfaceTexture 준비
            SurfaceTexture surfaceTexture = mCameraPrview.getSurfaceTexture();

            assert surfaceTexture != null;
            assert mImageDimension != null;
            surfaceTexture.setDefaultBufferSize(mImageDimension.getWidth(), mImageDimension.getHeight());

            // 프리뷰를 위한 Surface 생성 > 이미지를 찍기 위해 사용
            Surface surface = new Surface(surfaceTexture);

            // 캡쳐 세션을 생성하기 위해 아래의 메소드 호출
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (mCameraDevice == null) return;
                    mCameraCaptionSession = cameraCaptureSession;

                    // preview 의 계속적인 업데이트를 해주는 함수실행
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라 접근에 문제가 발생 했습니다. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 카메라 perview에 대한 계속적인 변경을 시켜주는 thread 를 실행하는 함수
     * createCameraPreview 함수 안에서 실행된다.
     */
    private void updatePreview() {
        if (mCameraDevice == null) {
            Toast.makeText(MainActivity.this, "카메라 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            mCameraCaptionSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Toast.makeText(this, "카메라 접근에 문제가 발생 했습니다. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 사진을 찍기 위한 함수
     */
    private void takePicture() {
        if (mCameraDevice == null) return;

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

                // 이미지의 사이즈 결정
                int width = 640;
                int height = 480;
                if (jpegSizes != null && jpegSizes.length > 0) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                // ImageReader 를 통해 생성할 이미지의 사이즈와 저장형태를 지정한다.
                final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurface = new ArrayList<>(2);
                outputSurface.add(reader.getSurface());
                outputSurface.add(new Surface(mCameraPrview.getSurfaceTexture()));

                // perview 를 위한 캡쳐 리퀘스트 생성
                final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                //check orientation base on device
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

                // 저장을 위한 파일 생성과 파일 지정
                file = new File(Environment.getExternalStorageDirectory() + "/" + UUID.randomUUID().toString() + ".jpg");
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            save(bytes);

                        } catch (FileNotFoundException e) {
                            e.getStackTrace();
                        } catch (IOException e) {
                            e.getStackTrace();
                        } finally {
                            {
                                if (image != null) {
                                    image.close();
                                }
                            }
                        }
                    }

                    // 직접적으로 저장하는 부분
                    private void save(byte[] bytes) throws IOException {
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }
                };

                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        Toast.makeText(MainActivity.this, "saved" + file, Toast.LENGTH_SHORT).show();
                        createCameraPreview();
                    }
                };

                // 카메라의 캡쳐 세션을 생성하기 위한 메소드
                mCameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                        try {
                            cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                    }
                }, mBackgroundHandler);

            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * onCreate 후에 동작 하는 함수
     * 또한 애플리케이션이 최소화 되었다가 다시 킬 때도 실행된다.
     * startBackgroundThread 함수에서 thread를 다시 시작한다.
     */
    @Override
    protected void onResume() {
        super.onResume();

        createCameraBackgroundThread();
        if (mCameraPrview.isAvailable()) {
            // 만약 textureView(카메라 화면)이 정상적으로 동작한다면 카메라를 정상 실행한다.
            openCamera();
        } else {
            // 아니라면 카메라에 textureListener 를 통해 제대로 동작할 수 있도록 만들어 준다.
            mCameraPrview.setSurfaceTextureListener(textureListener);
        }
    }

    /**
     * 애플리케이션을 최소화하는 경우 일시정지하는 함수
     * stopBackground 에서 처리한다.
     */
    @Override
    protected void onPause() {
        stopCameraBackgroundThread();
        super.onPause();
    }

    /**
     * onResume 함수의 안에서 실행된다.
     * 새로운 thread를 시작하여 카메라가 다시 켜지도록 한다.
     */
    private void createCameraBackgroundThread() {
        // 새로운 thread를 만들고 loop를 시작한다.
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("Camera Background");
            mBackgroundThread.start();

            if (mBackgroundHandler == null) {
                mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            }
        }
    }

    /**
     * onPause 함수가 필요한 상황이 나왔을 때 실행된다.
     * 스레드의 loop를 멈춰 카메라를 일시정지하는 역할을 한다.
     */
    private void stopCameraBackgroundThread() {
        // message queue에 쌓인 메시지 처리한 후 스레드의 loop를 중단한다.
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                // 스레드에 join 하여 Hanlder가 종료되기 까지 기다린다
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException ignored) {

            }
        }
    }

    /**
     * 카메라 사용을 위한 사용자의 권한 허용받기위한 함수
     * 만약 허용이 되지 않았다면 권한 허용에 대해 다시 물어본다.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 리턴이 false라면 허용을 하지 않은 것. 그러면 여기서 권한을 허용하지 않을건지 다시 물어봄
        if (!mPermissionHelper.permissionResult(requestCode, permissions, grantResults)) {
            mPermissionHelper.requestPermissions();
        } else {
            openCamera();
        }
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };
}