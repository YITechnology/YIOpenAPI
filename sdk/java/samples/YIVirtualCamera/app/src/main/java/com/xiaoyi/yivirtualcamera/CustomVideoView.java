package com.xiaoyi.yivirtualcamera;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by xyb on 12/4/2016.
 */

public class CustomVideoView extends VideoView {
    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void play(String uri) {
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                start();
            }
        });
        setVideoURI(Uri.parse(uri));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
