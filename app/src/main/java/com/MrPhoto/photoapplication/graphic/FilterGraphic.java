package com.MrPhoto.photoapplication.graphic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.MrPhoto.photoapplication.util.MatrixTransformation;

public class FilterGraphic extends GraphicOverlay.Graphic {

    @DrawableRes
    private int resId;
    private Bitmap filterBitmap;

    public FilterGraphic(Context context, int resId) {
        this.resId = resId;
        filterBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
    }

    @Override
    public void draw(Canvas canvas, @Nullable MatrixTransformation matrixTransformation) {
        if (filterBitmap == null) return;
        //크기 맞춰주고, 캔버스에 필터 그려줌
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(filterBitmap, canvas.getWidth(), canvas.getHeight(), true);
        canvas.drawBitmap(resizedBitmap, 0, 0, null);
    }

    @Override
    public String toString() {
        return "FilterGraphic{" + "filterResourceId=" + resId + '}';
    }

}