package com.MrPhoto.photoapplication.graphic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Size;

import androidx.annotation.Nullable;

import com.MrPhoto.photoapplication.util.MatrixTransformation;
import com.google.mlkit.vision.face.Face;

public class FaceGraphic extends GraphicOverlay.Graphic {

    private GraphicOverlay overlay;
    private volatile Face face;

    private final Paint facePositionPaint;
    private final Bitmap faceSticker;

    public FaceGraphic(GraphicOverlay overlay, Face face, Bitmap sticker) {
        this.face = face;
        this.overlay = overlay;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(Color.WHITE);

        faceSticker = sticker;
    }

    public FaceGraphic(FaceGraphic faceGraphic) {
        this(faceGraphic.overlay, faceGraphic.face, faceGraphic.faceSticker);
    }

    @Override
    public void draw(Canvas canvas, @Nullable MatrixTransformation matrixTransformation) {
        if (face == null) return;
        if (matrixTransformation == null) return;

        drawSticker(canvas, matrixTransformation);
    }

    public void drawSticker(Canvas canvas, MatrixTransformation matrixTransformation) {
        if (faceSticker != null) {
            Bitmap resizedFaceSticker = Bitmap.createScaledBitmap(faceSticker,
                    (int) (face.getBoundingBox().width() * matrixTransformation.getXRatio()),
                    (int) (face.getBoundingBox().height() * matrixTransformation.getYRatio()),
                    true
            );

            canvas.drawBitmap(resizedFaceSticker,
                    (int) (face.getBoundingBox().left * matrixTransformation.getXRatio()),
                    (int) (face.getBoundingBox().top * matrixTransformation.getYRatio()),
                    null);
        }
    }

    @Override
    public String toString() {
        return "FaceGraphic {" + face.toString() + '}';
    }

}