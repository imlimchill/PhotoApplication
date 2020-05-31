package com.MrPhoto.photoapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /** 권한 허용을 위한 메소드가 있는 클래스 선언 */
    PermissionSupport permission;

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

    private TextureView textureView;

    //백버튼이 눌린 마지막 시간을 저장함.
    private long backKeyPressedTime = 0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    /** 물리 카메라 표현, 캡쳐세션, 캡쳐리퀘스 생성 */
    private CameraDevice cameraDevice;
    /** 카메라로 얻은 이미지를 Surface에 출력요청 */
    private CameraCaptureSession cameraCaptureSessions;
    /** 카메라로 어떻게 이미지를 얻을까 결정 */
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;

    // 사진 저장을 위해 선언
    private File file;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreView();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한을 확인하기 위한 코드 실행
        checkPermission();

        settingBtn = (Button) findViewById(R.id.settingBtn);
        rationBtn = (Button) findViewById(R.id.ratioBtn);
        reverseBtn = (Button) findViewById(R.id.reverseBtn);
        stikerBtn = (Button) findViewById(R.id.stikerBtn);
        photoBtn = (Button) findViewById(R.id.photoBtn);
        filterBtn = (Button) findViewById(R.id.filterBtn);

        // 사진 프리뷰를 위한 선언
        textureView = (TextureView)findViewById(R.id.textureView);
        // 테스트를 위해 사용할 수 있다.
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureLisener);

        /** 사진 버튼을 누르면 사진 찍는 기능 구현 */
        photoBtn.setOnClickListener(new View.OnClickListener() {
            boolean cliked = true;
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

    }

    /**현재 시간이 마지막으로 백버튼을 누른 시간에서 2초 지났으면 마지막으로 백버튼이 눌린 시간으로 현재 시간을 생긴한다.*/
    public void onBackPressed(){
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            return;//백버튼이 한 번 눌리면 종료 안내 메시지가 뜬다.
        }

        /** 마지막으로 백버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후,
         마지막으로 백버튼을 눌렀던 시간이 2초가 지나지 않았으면 앱을 종료시킴.
         (메시지가 유지되는 2초동안 백버튼을 한 번 더 누르면 앱 종료)*/
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish();

        }
    }

    /**
     * 사진을 찍기 위한 함수
     */
    private void takePicture() {
        if(cameraDevice == null) {
            return;
        }
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if(characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

                // 이미지의 사이즈 결정
                int width = 640;
                int height = 480;
                if(jpegSizes != null && jpegSizes.length > 0) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                // ImageReader 를 통해 생성할 이미지의 사이즈와 저장형태를 지정한다.
                final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurface = new ArrayList<>(2);
                outputSurface.add(reader.getSurface());
                outputSurface.add(new Surface(textureView.getSurfaceTexture()));

                // perview 를 위한 캡쳐 리퀘스트 생성
                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
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
                                if(image != null) {
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
                            if(outputStream != null) {
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
                        createCameraPreView();
                    }
                };

                // 카메라의 캡쳐 세션을 생성하기 위한 메소드
                cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
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
     * 카메라의 프리뷰를 만드는 함수
     * 해당 함수 안에서 updatePreview를 실행한다.
     */
    private void createCameraPreView() {
        try {
            // 캑쳐 세션을 만들기 전에 프리뷰를 위한 Subface를 준비
            SurfaceTexture texture =  textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            // 브리뷰를 위한 Subface 생성 > 이미지를 찍기 위해 사용
            Surface surface = new Surface(texture);

            // 캡쳐 세션을 생성하기 위해 아래의 메소드 호출
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    // perview의 계속적인 업데이트를 해주는 함수실행
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /**
     * 카메라 perview에 대한 계속적인 변경을 시켜주는 thread 를 실행하는 함수
     * createCameraPreview 함수 안에서 실행된다.
     */
    private void updatePreview() {
        if(cameraDevice == null) {
            Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 카메라를 열기 위한 함수
     */
    private void openCamera() {
        // 카메라 목록, 특성, 모니터링 > 사용가능한 카메라를 관리하고 제공
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            // 카메라 특성 정보 > CameraDevice에 대한 메타데이터 제공
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension= map.getOutputSizes(SurfaceTexture.class)[0];
            // check realtime permission if run higher API 23
            checkPermission();
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    TextureView.SurfaceTextureListener textureLisener = new TextureView.SurfaceTextureListener() {
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

    /**
     * 애플리케이션을 다시 재개하는 기능의 함수
     * 예를들면 애플리케이션의 최소화 되었다가 다시 킬 때 재개한다.
     * startBackgroundThread 함수에서 thread를 다시 시작한다.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            // 만약 textureView(카메라 화면)이 정상적으로 동작한다면 카메라를 정상 실행한다.
            openCamera();
        } else {
            // 아니라면 카메라에 textureLisener를 통해 제대로 동작할 수 있도록 만들어 준다.
            textureView.setSurfaceTextureListener(textureLisener);
        }
    }

    /**
     * 애플리케이션을 최소화하는 경우 일시정지하는 함수
     * stopBackground 에서 처리한다.
     */
    @Override
    protected void onPause() {
        stopBackground();
        super.onPause();
    }

    /**
     * onPause 함수가 필요한 상황이 나왔을 때 실행된다.
     * 스레드의 loop를 멈춰 일시정지하는 역할을 한다.
     */
    private void stopBackground() {
        // message queue에 쌓인 메시지 처리한 후 스레드의 loop를 중단한다.
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * onResume 함수의 안에서 실행된다.
     * 새로운 thread를 시작하여 카메라가 다시 켜지도록 한다.
     */
    private void startBackgroundThread() {
        // 새로운 thread를 만들고 loop를 시작한다.
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    //------------------------//------------------------//------------------------//------------------------//------------------------//

    /**
     * 권한 허용을 해야 하는지 체크하는 함수
     */
    public void checkPermission() {
        // SDK 23이하(안드로이드 6.0이하) 버전에서는 사용자 권한 허용이 필요하지 않음
        if (Build.VERSION.SDK_INT >= 23) {
            permission = new PermissionSupport(this, this);

            // 권한 요청이 false값을 리턴한다면 권한 허용을 요청한다.
            if (!permission.checkPermssion()) {
                permission.requsetPermission();
            }
        }
    } // end checkPermission

    /**
     * 카메라 사용을 위한 사용자의 권한 허용받기위한 함수
     * 만약 허용이 되지 않았다면 권한 허용에 대해 다시 물어본다.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 리턴이 false라면 허용을 하지 않은 것. 그러면 여기서 권한을 허용하지 않을건지 다시 물어봄
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            permission.requsetPermission();
        }
    } // end permisson


} // end class
