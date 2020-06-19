package com.MrPhoto.photoapplication.graphic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.Size;

import com.google.mlkit.vision.face.Face;

public class FaceGraphic extends GraphicOverlay.Graphic {

    private GraphicOverlay overlay;
    private volatile Face face;

    private final Paint facePositionPaint;
    private final Bitmap faceSticker;
    private final Size imageSize;

    public FaceGraphic(GraphicOverlay overlay, Face face, Bitmap sticker, Size imageSize) {
        super(overlay);
        this.face = face;
        this.overlay = overlay;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(Color.WHITE);

        faceSticker = sticker;
        this.imageSize = imageSize;
    }

    public FaceGraphic(FaceGraphic faceGraphic) {
        this(faceGraphic.overlay, faceGraphic.face, faceGraphic.faceSticker, faceGraphic.imageSize);
    }

    @Override
    public void draw(Canvas canvas) {
        if (face == null) return;

        float x = xScale(face.getBoundingBox().centerX());
        float y = yScale(face.getBoundingBox().centerY());

        Log.d("FaceGraphic", String.format("X = %.2f, Y = %.2f", x, y));

        canvas.drawCircle(x, y, 4.0f, facePositionPaint);
        drawSticker(canvas, true);
    }

    public void drawSticker(Canvas canvas, boolean scale) {
        if (faceSticker != null) {
            Bitmap resizedFaceSticker = Bitmap.createScaledBitmap(faceSticker,
                    scale ? (int) xScale(face.getBoundingBox().width()) : face.getBoundingBox().width(),
                    scale ? (int) yScale(face.getBoundingBox().height()) : face.getBoundingBox().height(),
                    true
            );

            canvas.drawBitmap(resizedFaceSticker,
                    scale ? xScale(face.getBoundingBox().left) : face.getBoundingBox().left,
                    scale ? yScale(face.getBoundingBox().top) : face.getBoundingBox().top,
                    null);
        }
    }

    @Override
    public Size getSize() {
        return imageSize;
    }

    @Override
    public Rect getBoundingBox() {
        return face.getBoundingBox();
    }

    @Override
    public String toString() {
        return "FaceGraphic {" + face.toString() + '}';
    }
}
