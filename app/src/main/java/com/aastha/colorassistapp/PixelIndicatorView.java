package com.aastha.colorassistapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class PixelIndicatorView extends View {
    private int tapX = -1;
    private int tapY = -1;
    private int hexColor = Color.BLACK;
    private String hexString = "#000000";
    private static final int RADIUS = 12; // 3-pixel radius = 6px diameter, scaled for visibility
    private static final int INDICATOR_SIZE = 25; // Size of indicator square
    
    private Paint circlePaint;
    private Paint invertedSquarePaint;
    private Paint textPaint;
    private Paint borderPaint;

    public PixelIndicatorView(Context context) {
        super(context);
        init();
    }

    public PixelIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Circle paint for pixel radius indicator
        circlePaint = new Paint();
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAlpha(200);

        // Inverted square paint
        invertedSquarePaint = new Paint();
        invertedSquarePaint.setStyle(Paint.Style.FILL);

        // Text paint for HEX value
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(8, 2, 2, Color.BLACK);

        // Border paint
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
    }

    public void updateIndicator(int x, int y, int color, String hex) {
        this.tapX = x;
        this.tapY = y;
        this.hexColor = color;
        this.hexString = hex;
        invalidate();
    }

    public void clearIndicator() {
        this.tapX = -1;
        this.tapY = -1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (tapX < 0 || tapY < 0) {
            return; // No tap recorded yet
        }

        // Draw circle for pixel radius (3-pixel radius)
        canvas.drawCircle(tapX, tapY, RADIUS, circlePaint);
        canvas.drawCircle(tapX, tapY, RADIUS, borderPaint);

        // Draw inverted colored square
        int invertedColor = 0xFFFFFF ^ hexColor; // Bitwise XOR for inversion
        invertedSquarePaint.setColor(invertedColor);


        int squareLeft = tapX - (INDICATOR_SIZE / 2);
        int squareTop = tapY - (INDICATOR_SIZE / 2);
        canvas.drawRect(squareLeft, squareTop, squareLeft + INDICATOR_SIZE, squareTop + INDICATOR_SIZE, invertedSquarePaint);
        canvas.drawRect(squareLeft, squareTop, squareLeft + INDICATOR_SIZE, squareTop + INDICATOR_SIZE, borderPaint);

        // Draw HEX value text near the indicator
        int textX = tapX;
        int textY = tapY - (INDICATOR_SIZE / 2) - 20;
        
        // Ensure text stays within bounds
        if (textY < 80) {
            textY = tapY + (INDICATOR_SIZE / 2) + 40;
        }

        canvas.drawText(hexString, textX, textY, textPaint);
    }
}
