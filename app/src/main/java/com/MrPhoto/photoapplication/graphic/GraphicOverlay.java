package com.MrPhoto.photoapplication.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {

    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();

    private float xRatio;
    private float yRatio;

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRatio(float xRatio, float yRatio) {
        synchronized (lock) {
            this.xRatio = xRatio;
            this.yRatio = yRatio;
        }
        postInvalidate();
    }

    public float getXRatio() {
        return xRatio;
    }

    public float getYRatio() {
        return yRatio;
    }

    public void clear() {
        synchronized (lock) {
            graphics.clear();
        }
        postInvalidate();
    }

    public void add(Graphic graphic) {
        synchronized (lock) {
            graphics.add(graphic);
        }
    }

    public void remove(Graphic graphic) {
        synchronized (lock) {
            graphics.remove(graphic);
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {

            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        }
    }

    public abstract static class Graphic {
        private GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public Context getApplicationContext() {
            return overlay.getContext().getApplicationContext();
        }

        public abstract void draw(Canvas canvas);

        public float xScale(float pixel) {
            return pixel * overlay.getXRatio();
        }

        public float yScale(float pixel) {
            return pixel * overlay.getYRatio();
        }

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }
}

