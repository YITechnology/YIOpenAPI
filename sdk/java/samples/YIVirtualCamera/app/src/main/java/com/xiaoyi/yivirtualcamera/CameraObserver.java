package com.xiaoyi.yivirtualcamera;

/**
 * Created by xyb on 11/16/2016.
 */

public interface CameraObserver {
    void onCameraSettingsUpdated();
    void onBatteryLifeChanged(int restBatteryLife);
    void onVideoFinderChanged(boolean started);
}
