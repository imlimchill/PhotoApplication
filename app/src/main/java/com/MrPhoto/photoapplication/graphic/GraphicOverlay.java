package com.MrPhoto.photoapplication.graphic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.MrPhoto.photoapplication.util.MatrixTransformation;

import java.util.ArrayList;
import java.util.List;

//그래픽을 그려주는 공간 즉, 캔버스

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
    //필터 그래픽을 완전 없앰
    //lock을 걸어주는건 임계영역문제를 해결하기 위해
    public void clearFilterGraphic() {
        synchronized (lock) {
            Graphic filterGraphic = null;
            for (Graphic graphic : graphics)
                if (graphic instanceof FilterGraphic) filterGraphic = graphic;
            if (filterGraphic != null) graphics.remove(filterGraphic);
        }
        postInvalidate();
    }
    //페이스그래픽을 완전 없앰
    public void clearFaceGraphic() {
        synchronized (lock) {
            List<Graphic> faceGraphics = new ArrayList<>();
            for (Graphic graphic : graphics)
                if (graphic instanceof FaceGraphic) faceGraphics.add(graphic);
            graphics.removeAll(faceGraphics);
        }
        postInvalidate();
    }
    //그래픽 추가
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
    //캔버스에 그리기 위한 메소드
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

