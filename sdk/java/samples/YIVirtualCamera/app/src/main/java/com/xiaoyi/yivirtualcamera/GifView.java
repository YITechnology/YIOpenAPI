package com.xiaoyi.yivirtualcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GifView extends ImageView {
    private Movie mMovie;
    private long mStart;

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int src = attrs.getAttributeResourceValue( "http://schemas.android.com/apk/res/android", "src", 0);
        mMovie = Movie.decodeStream(getResources().openRawResource(src));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie != null) {
            long now = android.os.SystemClock.uptimeMillis();
            if (mStart == 0) {
                mStart = now;
            }

            int duration = mMovie.duration();
            if (duration == 0) {
                duration = 100;
            }
            mMovie.setTime((int) ((now - mStart) % duration));
            canvas.scale(getWidth() / mMovie.width(), getHeight() / mMovie.height());
            mMovie.draw(canvas, 0, 0);
            invalidate();
        }
    }
}