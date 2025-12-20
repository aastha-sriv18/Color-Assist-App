package com.aastha.colorassistapp.ui.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class ColorblindnessSimulationView extends AppCompatImageView {

    private Bitmap originalBitmap;
    private Bitmap transformedBitmap;
    private ColorblindnessMode colorblindnessMode = ColorblindnessMode.NONE;

    private final Matrix imageMatrix = new Matrix();
    private final float[] matrixValues = new float[9];

    private float minScale = 1.0f;
    private float maxScale = 5.0f;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

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

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let our detectors handle the event
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        // Ask parent (e.g. ScrollView) not to intercept while interacting
        getParent().requestDisallowInterceptTouchEvent(true);

        return true;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            originalBitmap = bitmap;
            transformBitmap();
            configureInitialMatrix();
            invalidate();
        }
    }

    public void setColorblindnessMode(ColorblindnessMode mode) {
        colorblindnessMode = mode;
        if (originalBitmap != null) {
            transformBitmap();
            configureInitialMatrix();
            invalidate();
        }
    }

    private void transformBitmap() {
        if (originalBitmap == null) return;

        transformedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);

        int width = transformedBitmap.getWidth();
        int height = transformedBitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = transformedBitmap.getPixel(x, y);
                int transformedPixel =
                        ColorTransformer.transformColor(pixel, convertMode(colorblindnessMode));
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

    /**
     * Called to fit the image initially (or after bitmap change) similar to CENTER_CROP / FIT_CENTER.
     */
    private void configureInitialMatrix() {
        if (transformedBitmap == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        imageMatrix.reset();

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bmWidth = transformedBitmap.getWidth();
        float bmHeight = transformedBitmap.getHeight();

        // Fit center: scale so the image fully fits inside the view
        float scale = Math.min(viewWidth / bmWidth, viewHeight / bmHeight);
        minScale = scale;

        float dx = (viewWidth - bmWidth * scale) / 2f;
        float dy = (viewHeight - bmHeight * scale) / 2f;

        imageMatrix.postScale(scale, scale);
        imageMatrix.postTranslate(dx, dy);

        setImageMatrix(imageMatrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Recalculate initial placement when view size changes
        configureInitialMatrix();
    }

    private float getCurrentScale() {
        imageMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X]; // assuming uniform scaling
    }

    private void clampTranslation() {
        if (transformedBitmap == null) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bmWidth = transformedBitmap.getWidth();
        float bmHeight = transformedBitmap.getHeight();

        imageMatrix.getValues(matrixValues);
        float scale = matrixValues[Matrix.MSCALE_X];
        float transX = matrixValues[Matrix.MTRANS_X];
        float transY = matrixValues[Matrix.MTRANS_Y];

        float scaledWidth = bmWidth * scale;
        float scaledHeight = bmHeight * scale;

        float minX, maxX, minY, maxY;

        if (scaledWidth <= viewWidth) {
            // Center horizontally
            minX = maxX = (viewWidth - scaledWidth) / 2f;
        } else {
            // Allow pan within bounds
            minX = viewWidth - scaledWidth;
            maxX = 0f;
        }

        if (scaledHeight <= viewHeight) {
            // Center vertically
            minY = maxY = (viewHeight - scaledHeight) / 2f;
        } else {
            minY = viewHeight - scaledHeight;
            maxY = 0f;
        }

        float clampedX = Math.min(Math.max(transX, minX), maxX);
        float clampedY = Math.min(Math.max(transY, minY), maxY);

        matrixValues[Matrix.MTRANS_X] = clampedX;
        matrixValues[Matrix.MTRANS_Y] = clampedY;
        imageMatrix.setValues(matrixValues);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (transformedBitmap == null) return false;

            float scaleFactor = detector.getScaleFactor();
            float currentScale = getCurrentScale();
            float newScale = currentScale * scaleFactor;

            // Clamp scale
            newScale = Math.max(minScale, Math.min(newScale, maxScale));
            scaleFactor = newScale / currentScale;

            // Zoom around the gesture's focus point (pinch center)
            imageMatrix.postScale(scaleFactor, scaleFactor,
                    detector.getFocusX(),
                    detector.getFocusY());

            clampTranslation();
            setImageMatrix(imageMatrix);
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (transformedBitmap == null) return false;

            // Only pan when zoomed in beyond minScale
            if (getCurrentScale() > minScale) {
                imageMatrix.postTranslate(-distanceX, -distanceY);
                clampTranslation();
                setImageMatrix(imageMatrix);
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float currentScale = getCurrentScale();
            if (currentScale > minScale) {
                // Reset to initial fit
                configureInitialMatrix();
            } else {
                // Zoom in around double-tap point
                float targetScale = Math.min(minScale * 2f, maxScale);
                float factor = targetScale / currentScale;

                imageMatrix.postScale(factor, factor, e.getX(), e.getY());
                clampTranslation();
                setImageMatrix(imageMatrix);
                invalidate();
            }
            return true;
        }
    }

    public void resetZoom() {
        configureInitialMatrix();
        invalidate();
    }

    public ColorblindnessMode getColorblindnessMode() {
        return colorblindnessMode;
    }
}
