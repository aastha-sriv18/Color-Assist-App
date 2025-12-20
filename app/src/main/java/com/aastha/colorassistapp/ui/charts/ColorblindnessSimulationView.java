package com.aastha.colorassistapp.ui.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

public class ColorblindnessSimulationView extends AppCompatImageView {

    private Bitmap originalBitmap;
    private Bitmap transformedBitmap;
    private ColorblindnessMode colorblindnessMode = ColorblindnessMode.NONE;
    private float currentScale = 1.0f;
    private float minScale = 1.0f;
    private float maxScale = 5.0f;

    private float translateX = 0f;
    private float translateY = 0f;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Paint paint;

    private static final String TAG = "ColorblindnessView";

    public enum ColorblindnessMode {
        NONE,
        PROTANOPIA,
        DEUTERANOPIA,
        TRITANOPIA
    }

    public ColorblindnessSimulationView(Context context) {
        super(context);
        init();
    }

    public ColorblindnessSimulationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorblindnessSimulationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.MATRIX);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            this.originalBitmap = bitmap;
            transformBitmap();
            updateImageViewMatrix();
            invalidate();
        }
    }

    public void setColorblindnessMode(ColorblindnessMode mode) {
        this.colorblindnessMode = mode;
        if (originalBitmap != null) {
            transformBitmap();
            updateImageViewMatrix();
            invalidate();
        }
    }

    private void transformBitmap() {
        if (originalBitmap == null) {
            return;
        }

        // Create a mutable copy of the original bitmap
        transformedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);

        // Apply colorblindness transformation pixel by pixel
        int width = transformedBitmap.getWidth();
        int height = transformedBitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = transformedBitmap.getPixel(x, y);
                int transformedPixel = ColorTransformer.transformColor(pixel, convertMode(colorblindnessMode));
                transformedBitmap.setPixel(x, y, transformedPixel);
            }
        }

        setImageBitmap(transformedBitmap);
    }

    private ColorTransformer.Mode convertMode(ColorblindnessMode mode) {
        switch (mode) {
            case PROTANOPIA:
                return ColorTransformer.Mode.PROTANOPIA;
            case DEUTERANOPIA:
                return ColorTransformer.Mode.DEUTERANOPIA;
            case TRITANOPIA:
                return ColorTransformer.Mode.TRITANOPIA;
            case NONE:
            default:
                return ColorTransformer.Mode.NONE;
        }
    }

    private void updateImageViewMatrix() {
        Matrix matrix = new Matrix();
        matrix.postScale(currentScale, currentScale);
        matrix.postTranslate(translateX, translateY);
        setImageMatrix(matrix);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            currentScale *= scaleFactor;

            // Clamp the scale value
            currentScale = Math.max(minScale, Math.min(currentScale, maxScale));

            updateImageViewMatrix();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (currentScale > minScale) { // Allow scrolling only when zoomed in
                translateX -= distanceX;
                translateY -= distanceY;

                // Clamp translation values to prevent scrolling too far
                if (transformedBitmap != null) {
                    float maxTranslateX = (currentScale - 1) * getWidth() / 2;
                    float maxTranslateY = (currentScale - 1) * getHeight() / 2;

                    translateX = Math.max(-maxTranslateX, Math.min(translateX, maxTranslateX));
                    translateY = Math.max(-maxTranslateY, Math.min(translateY, maxTranslateY));
                }

                updateImageViewMatrix();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Double tap to reset zoom
            if (currentScale > minScale) {
                currentScale = minScale;
                translateX = 0f;
                translateY = 0f;
            } else {
                currentScale = minScale * 2;
            }
            updateImageViewMatrix();
            return true;
        }
    }

    public void resetZoom() {
        currentScale = minScale;
        translateX = 0f;
        translateY = 0f;
        updateImageViewMatrix();
    }

    public ColorblindnessMode getColorblindnessMode() {
        return colorblindnessMode;
    }
}