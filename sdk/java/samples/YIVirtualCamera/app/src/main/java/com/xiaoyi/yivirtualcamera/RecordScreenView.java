package com.xiaoyi.yivirtualcamera;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.xiaoyi.action.ActionCameraCommandCallback;
import com.xiaoyi.action.ActionCameraCommandCallback1;
import com.xiaoyi.action.ActionCameraSettings;
import com.xiaoyi.action.ToggleState;
import com.xiaoyi.action.VideoResolution;
import com.xiaoyi.action.YICameraSDKError;

/**
 * Created by xyb on 11/16/2016.
 */

public class RecordScreenView implements CameraObserver{
    private VirtualCameraActivity mActivity;
    private ImageView mVideoMuteTopbarIcon;
    private ImageView mBattryTopbarIcon;
    private ImageView mVideoResolution;
    private ImageView mSettingButton;
    private RecordScreenView mSelf;
    private CustomVideoView mRtspView;

    RecordScreenView(VirtualCameraActivity activity) {
        mSelf = this;
        mActivity = activity;
        showContent();
        mActivity.RegisterCameraObserver(this);
    }

    @Override
    public void onCameraSettingsUpdated() {
        ActionCameraSettings settings = mActivity.getCameraSettings();
        mVideoMuteTopbarIcon.setVisibility(settings.videoMuteState == ToggleState.On ? View.VISIBLE : View.INVISIBLE);
        updateVideoResolutionIcon(settings.videoResolution);
    }

    @Override
    public void onBatteryLifeChanged(int restBatteryLife) {
        int icon = R.drawable.battery_0_topbar_icon;
        if (restBatteryLife >= 95) {
            icon = R.drawable.battery_5_topbar_icon;
        } else if (restBatteryLife >= 70) {
            icon = R.drawable.battery_4_topbar_icon;
        } else if (restBatteryLife >= 50) {
            icon = R.drawable.battery_3_topbar_icon;
        } else if (restBatteryLife >= 30) {
            icon = R.drawable.battery_2_topbar_icon;
        } else if (restBatteryLife >= 10) {
            icon = R.drawable.battery_1_topbar_icon;
        }
        mBattryTopbarIcon.setImageDrawable(mActivity.getResources().getDrawable(icon));
    }

    @Override
    public void onVideoFinderChanged(boolean started) {
        if (started) {
            // if we received the started event, we should re-start the video view, otherwise
            // the video will be frozen (stopped?)
            mRtspView.play(mActivity.getCamera().getRtspURL());
        }
    }

    private void showContent() {
        mActivity.getScreenPanel().removeAllViews();
        mActivity.getScreenPanel().addView(LayoutInflater.from(mActivity).inflate(R.layout.record_screen, null));
        mVideoMuteTopbarIcon = (ImageView)mActivity.findViewById(R.id.video_mute_topbar_icon);
        mBattryTopbarIcon = (ImageView)mActivity.findViewById(R.id.battery_topbar_icon);
        mVideoResolution = (ImageView)mActivity.findViewById(R.id.video_resolution);
        mSettingButton = (ImageView)mActivity.findViewById(R.id.setting_button);
        mRtspView = (CustomVideoView)mActivity.findViewById(R.id.rtsp_view);

        mSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelf.onSettingButtonClick();
            }
        });

        mActivity.findViewById(R.id.model_selection_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ModeSelectionView(mActivity);
            }
        });

        // start rtsp stream. This function may be fail, if the rtsp stream has been started already.
        // so ignore its error, play rtsp stream always.
        mActivity.getCamera().startViewFinder(new ActionCameraCommandCallback() {
            @Override
            public void onInvoke() {
                mRtspView.play(mActivity.getCamera().getRtspURL());
            }
        }, new ActionCameraCommandCallback1<YICameraSDKError>() {
            @Override
            public void onInvoke(YICameraSDKError val) {
                mRtspView.play(mActivity.getCamera().getRtspURL());
            }
        });
    }

    private void updateVideoResolutionIcon(VideoResolution value) {
        int drawableId = -1;
        switch (value) {
            case v_3840x2160_30p_16x9:
                drawableId = R.drawable.video_4k30;
                break;

            case v_3840x2160_30p_16x9_super:
                drawableId = R.drawable.video_4ku30;
                break;

            case v_1920x1440_30p_4x3:
                drawableId = R.drawable.prw_mod_1440p30;
                break;

            case v_1920x1440_60p_4x3:
                drawableId = R.drawable.prw_mod_1440p60;
                break;

            case v_1920x1080_30p_16x9:
                drawableId = R.drawable.prw_mod_1080p30;
                break;

            case v_1920x1080_60p_16x9:
                drawableId = R.drawable.prw_mod_1080p60;
                break;

            case v_1920x1080_120p_16x9:
                drawableId = R.drawable.prw_mod_1080p120;
                break;

            case v_1920x1080_30p_16x9_super:
                drawableId = R.drawable.prw_mod_1080pu30;
                break;

            case v_1920x1080_60p_16x9_super:
                drawableId = R.drawable.prw_mod_1080pu60;
                break;

            case v_1920x1080_120p_16x9_super:
                drawableId = R.drawable.prw_mod_1080pu120;
                break;

            case v_1280x960_60p_4x3:
                drawableId = R.drawable.prw_mod_960p60;
                break;

            case v_1280x960_120p_4x3:
                drawableId = R.drawable.prw_mod_960p120;
                break;

            case v_1280x720_240p_16x9:
                drawableId = R.drawable.prw_mod_720p240;
                break;

            case v_1280x720_60p_16x9_super:
                drawableId = R.drawable.prw_mod_720pu60;
                break;

            case v_1280x720_120p_16x9_super:
                drawableId = R.drawable.prw_mod_720pu120;
                break;
        }

        if (drawableId >= 0) {
            mVideoResolution.setImageDrawable(mActivity.getResources().getDrawable(drawableId));
            mVideoResolution.setVisibility(View.VISIBLE);
        } else {
            mVideoResolution.setVisibility(View.INVISIBLE);
        }
    }

    private void onSettingButtonClick() {
        new NormalRecordingSettingView(mActivity);
    }
}
