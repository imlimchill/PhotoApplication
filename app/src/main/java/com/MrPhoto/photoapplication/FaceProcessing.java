package com.MrPhoto.photoapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageProxy;

import com.MrPhoto.photoapplication.util.BitmapUtils;
import com.MrPhoto.photoapplication.util.ScopedExecutor;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;
import java.util.Objects;

public class FaceProcessing {

    private final static String TAG = "FaceProcessing";

    private final ScopedExecutor executor;

    private boolean isShutdown;

    private final FaceDetector detector;
    private final FaceProcessingListener faceProcessingListener;

    public FaceProcessing(Context context, FaceProcessingListener listener) {
        executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);

        FaceDetectorOptions realTimeOpts = new FaceDetectorOptions.Builder()
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .enableTracking()
                .build();

        detector = FaceDetection.getClient(realTimeOpts);
        faceProcessingListener = listener;
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    public void processImageProxy(ImageProxy imageProxy) {
        if (imageProxy == null) return;

        if (isShutdown) {
            imageProxy.close();
            return;
        }

        Bitmap bitmap = BitmapUtils.getBitmap(imageProxy);

        requestDetectInImage(
                InputImage.fromMediaImage(Objects.requireNonNull(imageProxy.getImage()), imageProxy.getImageInfo().getRotationDegrees()), bitmap)
                .addOnCompleteListener(results -> imageProxy.close());
    }

    private Task<List<Face>> requestDetectInImage(final InputImage image, final Bitmap originalCameraImage) {
        return detector.process(image)
                .addOnSuccessListener(executor, faces -> {
                    if (faceProcessingListener == null) return;
                    faceProcessingListener.onSuccess(faces, image, originalCameraImage);
                })
                .addOnFailureListener(executor, e -> {
                    String error = "Failed to process. Error: " + e.getLocalizedMessage();
                    Log.e(TAG, error, e);
                    onFailure(e);
                });
    }

    private void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e, e);
    }

    public void stop() {
        executor.shutdown();
        isShutdown = true;
        detector.close();
    }

}
