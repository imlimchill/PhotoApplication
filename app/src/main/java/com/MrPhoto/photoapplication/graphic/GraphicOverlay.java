package com.MrPhoto.photoapplication.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import androidx.annotation.Nullable;

import com.MrPhoto.photoapplication.util.MatrixTransformation;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {

    private final Object lock = new Object();
    private final List<Graphic> graphics = new ArrayList<>();

    private MatrixTransformation matrixTransformation;

    public GraphicOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMatrixTransformation(MatrixTransformation matrixTransformation) {
        synchronized (lock) {
            this.matrixTransformation = matrixTransformation;
        }
        postInvalidate();
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

    public List<Graphic> getChildView() {
        synchronized (lock) {
            List<Graphic> children = new ArrayList<>();

            for (Graphic graphic : graphics) {
                if (graphic instanceof FaceGraphic) {
                    children.add(new FaceGraphic((FaceGraphic) graphic));
                }
            }

            return children;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (lock) {

            for (Graphic graphic : graphics) {
                graphic.draw(canvas, matrixTransformation);
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

        public abstract void draw(Canvas canvas, @Nullable MatrixTransformation matrixTransformation);

        public abstract Size getSize();

        public abstract Rect getBoundingBox();

        public void postInvalidate() {
            overlay.postInvalidate();
        }
    }
}

