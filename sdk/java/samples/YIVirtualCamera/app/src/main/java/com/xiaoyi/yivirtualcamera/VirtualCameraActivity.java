package com.xiaoyi.yivirtualcamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.xiaoyi.action.*;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by xyb on 11/6/2016.
 */

public class VirtualCameraActivity extends Activity {
    private ActionCamera mCamera;
    private ViewGroup mScreenPanel;
    private ActionCameraSettings mCachedCameraSettings;
    private ArrayList<CameraObserver> mCameraObservers = new ArrayList<>();
    private android.os.Handler mUIThreadHandler;
    private boolean mShowCloseMessage = true;

    public ViewGroup getScreenPanel() {
        return mScreenPanel;
    }

    public void RegisterCameraObserver(CameraObserver observer) {
        if (mCameraObservers.indexOf(observer) < 0) {
            mCameraObservers.add(observer);
            observer.onCameraSettingsUpdated();
        }
    }

    public ActionCameraSettings getCameraSettings() {
        return mCachedCameraSettings;
    }

    public ActionCamera getCamera() {
        return mCamera;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virtual_camera_activity);
        mScreenPanel = (ViewGroup)findViewById(R.id.contentPanel);
        mUIThreadHandler = new android.os.Handler();

        try {
            createCamera();
        } catch (Exception ex) {
        }
    }

    public void onConnectBtnClicked(View view) {
        findViewById(R.id.powerOnBtn).setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingImg).setVisibility(View.VISIBLE);
        mCamera.connect("tcp:192.168.42.1:7878");
    }

    private void onCameraConnected() {
        // Connect successfully, read camera settings.
        mCamera.startCommandGroup()
               .setSystemMode(SystemMode.Record, null, null)
               .setVideoStandard(VideoStandard.NTSC, null, null)
               .setVideoResolution(VideoResolution.v_3840x2160_30p_16x9, null, null)
               .getSettings(new ActionCameraCommandCallback1<ActionCameraSettings>() {
                    @Override
                    public void onInvoke(ActionCameraSettings cameraSettings) {
                        mCachedCameraSettings = cameraSettings;
                    }
                }, null)
               .submitCommandGroup(new ActionCameraCommandCallback() {
                    @Override
                    public void onInvoke() {
                        // Show content panel
                        findViewById(R.id.powerOnBtn).setVisibility(View.INVISIBLE);
                        findViewById(R.id.loadingImg).setVisibility(View.INVISIBLE);
                        findViewById(R.id.contentPanel).setVisibility(View.VISIBLE);
                        updateScreen();
                    }
                }, new ActionCameraCommandCallback1<YICameraSDKError>() {
                    @Override
                    public void onInvoke(YICameraSDKError val) {
                        showMessage("Initialize camera failed. Please connect to your camera's WIFI, set your camera to Video mode and try again.");
                        mShowCloseMessage = false;
                        mCamera.disconnect();
                    }
                });
    }

    private void onCameraClosed() {
        if (mShowCloseMessage) {
            showMessage("Camera is disconnected.");
        } else {
            mShowCloseMessage = true;
        }
        findViewById(R.id.loadingImg).setVisibility(View.INVISIBLE);
        findViewById(R.id.powerOnBtn).setVisibility(View.VISIBLE);
    }

    private void onCameraSettingChanged(ActionCameraSettings newCameraSetting) {
        updateSettingCache(newCameraSetting);
        for (CameraObserver observer: mCameraObservers) {
            observer.onCameraSettingsUpdated();
        }
    }

    private void onCameraBatteryLifeChanged(int restBatteryLife) {
        for (CameraObserver observer: mCameraObservers) {
            observer.onBatteryLifeChanged(restBatteryLife);
        }
    }
    private void createCamera() throws IOException {
        // Initialize YIActionCamera platform
        Platform.initialize(new Logger() {
            @Override
            public void verbose(String message) {
                    Log.v("YI", message);
                }

            @Override
            public void info(String message) {
                    Log.i("YI", message);
                }

            @Override
            public void warning(String message) {
                    Log.w("YI", message);
                }

            @Override
            public void error(String message) {
                    Log.e("YI", message);
                }
        });

        // Enable network function in UI thread
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Create camera
        mCamera = new ActionCamera(new ActionCameraListener() {
            @Override
            public void onConnected() {
                onCameraConnected();
            }

            @Override
            public void onClosed(YICameraSDKError error) {
                onCameraClosed();
            }

            @Override
            public void onSettingChanged(ActionCameraSettings newSettings) {
                onCameraSettingChanged(newSettings);
            }

            @Override
            public void onBatteryLifeChanged(int restBattery) {
                onCameraBatteryLifeChanged(restBattery);
            }

            @Override
            public void onViewFinderStarted() {
                for (CameraObserver observer: mCameraObservers) {
                    observer.onVideoFinderChanged(true);
                }
            }
        }, new YICameraSDKDispatchQueue() {
            @Override
            public void dispatch(Runnable task) {
                mUIThreadHandler.post(task);
            }
        });
    }

    // Show different content page for different camera settings
    private void updateScreen() {
        switch (mCachedCameraSettings.systemMode) {
            //case Capture:
            //    contentPanel.addView(LayoutInflater.from(this).inflate(R.layout.capture_screen, null));
            //    break;

            default:// Record:
                new RecordScreenView(this);
                break;
        }
    }

    private void showMessage(String msg) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle("YIVirtualCamera");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void updateSettingCache(ActionCameraSettings newSettings) {
        if (mCachedCameraSettings == null) {
            return;
        }

        if (newSettings.videoMuteState != null) {
            mCachedCameraSettings.videoMuteState = newSettings.videoMuteState;
        }

        if (newSettings.videoResolution != null) {
            mCachedCameraSettings.videoResolution = newSettings.videoResolution;
        }

        if (newSettings.videoFieldOfView != null) {
            mCachedCameraSettings.videoFieldOfView = newSettings.videoFieldOfView;
        }

        if (newSettings.meteringMode != null) {
            mCachedCameraSettings.meteringMode = newSettings.meteringMode;
        }

        if (newSettings.videoQuality != null) {
            mCachedCameraSettings.videoQuality = newSettings.videoQuality;
        }

        if (newSettings.videoWhiteBalance != null) {
            mCachedCameraSettings.videoWhiteBalance = newSettings.videoWhiteBalance;
        }

        if (newSettings.videoColorMode != null) {
            mCachedCameraSettings.videoColorMode = newSettings.videoColorMode;
        }

        if (newSettings.videoISO !=  null) {
            mCachedCameraSettings.videoISO = newSettings.videoISO;
        }

        if (newSettings.videoSharpness != null) {
            mCachedCameraSettings.videoSharpness = newSettings.videoSharpness;
        }

        if (newSettings.videoExposureValue != null) {
            mCachedCameraSettings.videoExposureValue = newSettings.videoExposureValue;
        }

        if (newSettings.videoTimestamp != null) {
            mCachedCameraSettings.videoTimestamp = newSettings.videoTimestamp;
        }
    }
}
