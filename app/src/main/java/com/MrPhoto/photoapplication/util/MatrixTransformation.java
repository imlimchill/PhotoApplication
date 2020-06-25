package com.MrPhoto.photoapplication.util;

import android.util.Size;

public class MatrixTransformation {
    private float xRatio;
    private float yRatio;

    private Size original;
    private Size destination;

    public MatrixTransformation(Size ori, Size dest, int rotationDegree) {
        if (rotationDegree == 0 || rotationDegree == 180) {
            xRatio = (float) ori.getWidth() / dest.getWidth();
            yRatio = (float) ori.getHeight() / dest.getHeight();
        } else {
            xRatio = (float) ori.getWidth() / dest.getHeight();
            yRatio = (float) ori.getHeight() / dest.getWidth();

            dest = new Size(dest.getHeight(), dest.getWidth());
        }

        this.original = ori;
        this.destination = dest;
    }

    public float getXRatio() {
        return xRatio;
    }

    public float getYRatio() {
        return yRatio;
    }

    public Size getOriginal() {
        return original;
    }

    public Size getDestination() {
        return destination;
    }
}