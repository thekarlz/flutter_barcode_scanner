/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amolg.flutterbarcodescanner.camera;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.amolg.flutterbarcodescanner.BarcodeCaptureActivity;
import com.amolg.flutterbarcodescanner.FlutterBarcodeScannerPlugin;
import com.amolg.flutterbarcodescanner.R;
import com.amolg.flutterbarcodescanner.constants.AppConstants;
import com.amolg.flutterbarcodescanner.utils.AppUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;


public class GraphicOverlay<T extends GraphicOverlay.Graphic> extends View {
    private final Object mLock = new Object();
    private final float mWidthScaleFactor = 1.0f;
    private final float mHeightScaleFactor = 1.0f;

    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    private final Set<T> mGraphics = new HashSet<>();

    /**
     * Custom added values for overlay
     */
    private float left, top, endY;
    private final int rectWidth;
    private final int rectHeight;
    private final int frames;
    private final int lineColor;
    private final int lineWidth;
    private boolean revAnimation;

    private final String alertText;
    private final boolean isUserPremium;


    public static abstract class Graphic {
        private final GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        public float scaleY(float vertical) {
            return vertical * mOverlay.mHeightScaleFactor;
        }

        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        public float translateY(float y) {
            return scaleY(y);
        }

        public void postInvalidate() {
            mOverlay.postInvalidate();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        rectWidth = AppConstants.BARCODE_RECT_WIDTH;
        rectHeight = BarcodeCaptureActivity.SCAN_MODE == BarcodeCaptureActivity.SCAN_MODE_ENUM.QR.ordinal()
                ? AppConstants.BARCODE_RECT_HEIGHT : (int) (AppConstants.BARCODE_RECT_HEIGHT / 1.5);

        lineColor = Color.parseColor(FlutterBarcodeScannerPlugin.lineColor);
        alertText = FlutterBarcodeScannerPlugin.alertText;
        isUserPremium = FlutterBarcodeScannerPlugin.isUserPremium;


        lineWidth = AppConstants.BARCODE_LINE_WIDTH;
        frames = AppConstants.BARCODE_FRAMES;
    }


    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }


    public void add(T graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - AppUtil.dpToPx(getContext(), rectWidth)) / 2;
        top = (h - AppUtil.dpToPx(getContext(), rectHeight)) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }


    public void remove(T graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    public List<T> getGraphics() {
        synchronized (mLock) {
            return new Vector<>(mGraphics);
        }
    }

    public float getWidthScaleFactor() {
        return mWidthScaleFactor;
    }

    public float getHeightScaleFactor() {
        return mHeightScaleFactor;
    }

    public void setCameraInfo(int facing) {
        synchronized (mLock) {
            mFacing = facing;
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();

        if (!isUserPremium) {
            prepareAlertText(canvas);
        }

        drawRectangleAndLine(canvas, paint);
    }


    private void prepareAlertText(Canvas canvas) {
        int pageWidth = (int) (getWidth() * 0.25);
        int leftPadding = (int) (getWidth() * 0.053);
        int topPadding = (int) (getHeight() * 0.053);

        TextView alertTextView = new TextView(getContext());
        FrameLayout alertLayout = new FrameLayout(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(leftPadding, topPadding, 0, 0);
        alertTextView.setLayoutParams(layoutParams);

        alertTextView.setText(alertText);
        alertTextView.setMaxLines(3);
        alertTextView.setTextSize(14);
        alertTextView.setMaxEms((int) (pageWidth * 0.085));
        alertTextView.setTextColor(Color.BLACK);

        alertTextView.setBackgroundResource(R.drawable.rounded_corner);
        alertTextView.setPadding(10, 10, 10, 10);
        alertTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_danger, 0, 0, 0);
        alertTextView.setCompoundDrawablePadding(10);

        alertLayout.setLayoutParams(layoutParams);
        alertLayout.addView(alertTextView);


        alertLayout.measure(getWidth(), getHeight());
        alertLayout.layout(leftPadding, topPadding, leftPadding, topPadding);

        alertLayout.draw(canvas);
    }

    private void drawRectangleAndLine(Canvas canvas, Paint paint) {
        // draw transparent rect
        int cornerRadius = 10;

        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        RectF rect = new RectF(left, top, AppUtil.dpToPx(getContext(), rectWidth) + left, AppUtil.dpToPx(getContext(), rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, paint);

        // draw horizontal line
        Paint line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth(Float.valueOf(lineWidth));

        // draw the line to product animation
        if (endY >= top + AppUtil.dpToPx(getContext(), rectHeight) + frames) {
            revAnimation = true;
        } else if (endY == top + frames) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
        } else {
            endY += frames;
        }
        canvas.drawLine(left, endY, left + AppUtil.dpToPx(getContext(), rectWidth), endY, line);
        invalidate();
    }
}