package com.MrPhoto.photoapplication.graphic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.mlkit.vision.face.Face;

public class FaceGraphic extends GraphicOverlay.Graphic {

    private volatile Face face;

    private final Paint facePositionPaint;
    private final Bitmap faceSticker;

    public FaceGraphic(GraphicOverlay overlay, Face face, Bitmap sticker) {
        super(overlay);
        this.face = face;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(Color.WHITE);

        faceSticker = sticker;
    }

    @Override
    public void draw(Canvas canvas) {
        if (face == null) return;

        float x = xScale(face.getBoundingBox().centerX());
        float y = yScale(face.getBoundingBox().centerY());

        Log.d("FaceGraphic", String.format("X = %.2f, Y = %.2f", x, y));

        canvas.drawCircle(x, y, 4.0f, facePositionPaint);

        if (faceSticker != null) {
            Bitmap resizedFaceSticker = Bitmap.createScaledBitmap(faceSticker,
                    (int) xScale(face.getBoundingBox().width()),
                    (int) yScale(face.getBoundingBox().height()),
                    true
            );
            canvas.drawBitmap(resizedFaceSticker, xScale(face.getBoundingBox().left), yScale(face.getBoundingBox().top), null);
        }
    }
}
