package com.MrPhoto.photoapplication;

import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.util.List;

public interface FaceProcessingListener {

    void onSuccess(List<Face> faces, InputImage inputImage, Bitmap originalCameraImage);

}
