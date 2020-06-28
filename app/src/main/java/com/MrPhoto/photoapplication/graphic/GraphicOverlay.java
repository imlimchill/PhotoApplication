package com.MrPhoto.photoapplication.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
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
        graphics.clear();
    }

    public void clearFilterGraphic() {
        synchronized (lock) {
            for (Graphic graphic : graphics)
                if (graphic instanceof FilterGraphic) remove(graphic);
        }
        postInvalidate();
    }

    public void clearFaceGraphic() {
        synchronized (lock) {
            for (Graphic graphic : graphics)
                if (graphic instanceof FaceGraphic) remove(graphic);
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

    public List<Graphic> getFaceGraphicViews() {
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

    public Graphic getFilterGraphicView() {
        synchronized (lock) {
            for (Graphic graphic : graphics) {
                if (graphic instanceof FilterGraphic) {
                    return graphic;
                }
            }
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (lock) {
            for (Graphic graphic : graphics) {
                graphic.draw(canvas, matrixTransformation);
            }
        }
    }

    public abstract static class Graphic {
        public Graphic() {

        }

        public abstract void draw(Canvas canvas, @Nullable MatrixTransformation matrixTransformation);
    }

}

