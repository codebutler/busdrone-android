/*
 * Copyright (C) 2013 Eric Butler
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

package com.busdrone.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.LruCache;
import com.busdrone.android.R;
import com.busdrone.android.Utils;

public class VehicleMarkerRenderer {
    private final LruCache<String, Bitmap> mCache;
    private final int mCornerRadius;
    private final int mTextSize;
    private final int mPadding;

    public VehicleMarkerRenderer(Context context) {
        mCache        = new LruCache<String, Bitmap>(Utils.calculateMemoryCacheSize(context));
        mCornerRadius = context.getResources().getDimensionPixelOffset(R.dimen.marker_radius   );
        mTextSize     = context.getResources().getDimensionPixelSize  (R.dimen.marker_text_size);
        mPadding      = context.getResources().getDimensionPixelOffset(R.dimen.marker_padding  );
    }

    public Bitmap get(String color, String route) {
        String key = color+"_"+route;
        Bitmap bitmap = mCache.get(key);
        if (bitmap != null) {
            return bitmap;
        }
        bitmap = render(Color.parseColor(color), route);
        mCache.put(key, bitmap);
        return bitmap;
    }

    private Bitmap render(int color, String text) {
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(mTextSize);

        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);

        int width  = mPadding + textBounds.width() + mPadding;
        int height = mPadding + textBounds.height() + mPadding;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(0, 0, width, height), mCornerRadius, mCornerRadius, paint);

        canvas.drawText(
            text,
            (width / 2f) - (textBounds.width() / 2f),
            (height / 2f) + (textBounds.height() / 2f),
            textPaint
        );

        return bitmap;
    }
}
