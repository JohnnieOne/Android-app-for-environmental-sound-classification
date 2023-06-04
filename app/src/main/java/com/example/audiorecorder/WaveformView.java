// NOTE: This is for testing purposes

package com.example.audiorecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private final int SCALE = 5;
    private static final int DEFAULT_COLOR = Color.RED;
    private static final float DEFAULT_STROKE_WIDTH = 2f;

    private float[] waveform;
    private Paint waveformPaint;

    private int waveformColor = DEFAULT_COLOR;
    private boolean redrawWaveform = false; // Flag to indicate if waveform needs to be redrawn

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        waveformPaint = new Paint();
        waveformPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);
    }

    public void setWaveformData(float[] waveform) {

//        float[] scaled = new float[waveform.length];
//        for (short i = 0; i <= waveform.length; i++){
//            int SCALE = 3;
//            scaled[i] = (float) (waveform[i] * SCALE);
//        }
//
//        this.waveform = scaled;
        this.waveform = waveform;
        redrawWaveform = true; // Set flag to indicate waveform needs to be redrawn
        invalidate(); // Trigger redraw
    }

    public void setWaveformColor(int color) {
        waveformColor = color;
        redrawWaveform = true; // Set flag to indicate waveform needs to be redrawn
        invalidate(); // Trigger redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (waveform == null || waveform.length == 0) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;

        if (redrawWaveform) {
            waveformPaint.setColor(waveformColor);
            redrawWaveform = false; // Reset the flag

            float xIncrement = (float) width / waveform.length;
            float currentX = 0f;

            for (float amplitude : waveform) {
                float startY = centerY - amplitude * centerY / Short.MAX_VALUE;
                float endY = centerY + amplitude * centerY / Short.MAX_VALUE;

                canvas.drawLine(currentX, startY, currentX, endY, waveformPaint);
                currentX += xIncrement;
            }
        }
    }
}

