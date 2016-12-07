package com.xiaoyi.yivirtualcamera;

import android.app.Activity;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.xiaoyi.action.ActionCamera;
import com.xiaoyi.action.ActionCameraCommandCallback;
import com.xiaoyi.action.ActionCameraSettings;
import com.xiaoyi.action.ColorMode;
import com.xiaoyi.action.ExposureValue;
import com.xiaoyi.action.FieldOfView;
import com.xiaoyi.action.ISO;
import com.xiaoyi.action.MeteringMode;
import com.xiaoyi.action.Quality;
import com.xiaoyi.action.Sharpness;
import com.xiaoyi.action.Timestamp;
import com.xiaoyi.action.VideoResolution;
import com.xiaoyi.action.WhiteBalance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xyb on 11/17/2016.
 */

class SettingItem {
    int imgId;
    boolean disabled;
    boolean isActive;
    View.OnClickListener onClickListener;

    SettingItem(int imgId) {
        this.imgId = imgId;
    }

    SettingItem(int imgId, View.OnClickListener onClickListener) {
        this.imgId = imgId;
        this.onClickListener = onClickListener;
    }

    SettingItem(int imgId, boolean disabled) {
        this.imgId = imgId;
        this.disabled = disabled;
    }

    SettingItem setActive(boolean isActive) {
        this.isActive = isActive;
        return this;
    }
}

public class NormalRecordingSettingView implements CameraObserver {
    private VirtualCameraActivity mActivity;
    private ListView mSettingListView;
    private ImageView mBasicIcon;
    private ImageView mSettingIcon;
    private ImageView mExitIcon;
    private View mSelfView;
    private View mLodingView;

    // 0: basic recording mode, 1: setting mode
    // 2: selection for resolution 1440
    // 3: selection for resolution 1080, 4: selection for resolution 1080 ultra
    // 5: selection for resolution 960
    // 6: selection for resolution 720 ultra
    private int mMode;

    NormalRecordingSettingView(VirtualCameraActivity activity) {
        mActivity = activity;
        showContent();
        mActivity.RegisterCameraObserver(this);
    }

    @Override
    public void onCameraSettingsUpdated() {
        // Update the UI
        mLodingView.setVisibility(View.INVISIBLE);
        selectMode(mMode >= 2 ? 0 : mMode);
    }

    @Override
    public void onBatteryLifeChanged(int restBatteryLife) {
    }

    @Override
    public void onVideoFinderChanged(boolean started) {
    }

    private void showContent() {
        mSelfView = LayoutInflater.from(mActivity).inflate(R.layout.normal_recording_setting, null);
        mActivity.getScreenPanel().addView(mSelfView);
        mBasicIcon = (ImageView)mActivity.findViewById(R.id.setting_basic);
        mSettingIcon = (ImageView)mActivity.findViewById(R.id.setting_setting);
        mExitIcon = (ImageView)mActivity.findViewById(R.id.setting_exit);
        mSettingListView = (ListView)mActivity.findViewById(R.id.setting_list_view);
        mLodingView = mSelfView.findViewById(R.id.loadingImg);

        // hook up button event
        mBasicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectBasicMode();
            }
        });

        mSettingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectSettingMode();
            }
        });

        mExitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((int)mExitIcon.getTag() ==  R.drawable.set_back_left_nor) {
                    selectMode(0);
                } else {
                    mActivity.getScreenPanel().removeView(mSelfView);
                }
            }
        });
    }

    private void selectMode(int mode) {
        mMode = mode;
        switch (mode) {
            case 0:
                selectBasicMode();
                showExitIcon();
                break;

            case 1:
                selectSettingMode();
                showExitIcon();
                break;

            case 2:
                selectVideoResolution1440();
                break;

            case 3:
                selectVideoResolution1080();
                break;

            case 4:
                selectVideoResolution1080ultra();
                break;

            case 5:
                selectVideoResolution960();
                break;

            case 6:
                selectVideoResolution720ultra();
                break;
        }
    }

    private void selectBasicMode() {
        mBasicIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.setting_normal_video_left));
        mBasicIcon.setBackgroundColor(0xff00d356);
        mSettingIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.setting_left));
        mSettingIcon.setBackgroundColor(0xff000000);
        mSettingListView.setAdapter(new MyAdapater(mActivity, getBasicData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectSettingMode() {
        mBasicIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.set_vid_left_nor));
        mBasicIcon.setBackgroundColor(0xff000000);
        mSettingIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.set_set_left_hl));
        mSettingIcon.setBackgroundColor(0xff00d356);
        mSettingListView.setAdapter(new MyAdapater(mActivity, getSettingData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectVideoResolution1440() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getResolution1440Data(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectVideoResolution1080() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getResolution1080Data(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectVideoResolution1080ultra() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getResolution1080ultraData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectVideoResolution960() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getResolution960Data(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private void selectVideoResolution720ultra() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getResolution720ultraData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
    }

    private List<SettingItem> getBasicData()
    {
        ActionCameraSettings settings = mActivity.getCameraSettings();
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(recordingResolutionToResourceId(settings.videoResolution), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecordingResolutionSelection();
            }
        }));

        list.add(new SettingItem(recordingFieldOfViewToResourceId(settings.videoFieldOfView), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoFieldOfViewSelection();
            }
        }));

        list.add(new SettingItem(meterModeToResourceId(settings.meteringMode), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMeteringModeSelection();
            }
        }));

        list.add(new SettingItem(R.drawable.set_vp_set_alt_off_g, true));

        list.add(new SettingItem(videoQualityToResourceId(settings.videoQuality), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoQualitySelection();
            }
        }));

        list.add(new SettingItem(videoWhiteBalanceToResourceId(settings.videoWhiteBalance), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoWhiteBalanceSelection();
            }
        }));

        list.add(new SettingItem(videoColorModeToResourceId(settings.videoColorMode), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoColorModeSelection();
            }
        }));

        list.add(new SettingItem(videoISOToResourceId(settings.videoISO), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoISOSelection();
            }
        }));

        list.add(new SettingItem(videoSharpnessToResourceId(settings.videoSharpness), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoSharpnessSelection();
            }
        }));

        list.add(new SettingItem(videoExposureValueToResourceId(settings.videoExposureValue), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoExposureValueSelection();
            }
        }));

        list.add(new SettingItem(R.drawable.set_vid_eis_off_g, true));

        list.add(new SettingItem(videoTimestampToResourceId(settings.videoTimestamp), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showVideoTimestampSelection();
            }
        }));

        return list;
    }

    private List<SettingItem> getSettingData()
    {
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(R.drawable.setup_ald_off_g, true));
        list.add(new SettingItem(R.drawable.setup_wifi_nor_g, true));
        list.add(new SettingItem(R.drawable.setup_sd_nor));
        list.add(new SettingItem(R.drawable.setup_dcm_lum_nor));
        list.add(new SettingItem(R.drawable.setup_btp_par_nor));
        list.add(new SettingItem(R.drawable.setup_bri_hig_nor));
        list.add(new SettingItem(R.drawable.setup_vol_hig_nor));
        list.add(new SettingItem(R.drawable.video_mute_on));
        list.add(new SettingItem(R.drawable.setup_led_on_nor));
        list.add(new SettingItem(R.drawable.underwater_mode_off));
        list.add(new SettingItem(R.drawable.setup_vf_ntsc_nor));
        list.add(new SettingItem(R.drawable.setup_adv_nor));
        list.add(new SettingItem(R.drawable.setup_inf_nor));
        list.add(new SettingItem(R.drawable.setup_fac_rst_nor));
        list.add(new SettingItem(R.drawable.set_location_us_nor));
        list.add(new SettingItem(R.drawable.set_about_nor));
        return list;
    }

    private List<SettingItem> getRecordingResolutionData() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        int activeResolutionResId = recordingResolutionToResourceId(activeResolution);
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(R.drawable.set_vid_4k30_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_3840x2160_30p_16x9) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_3840x2160_30p_16x9, null, null);
                }
            }
        }).setActive(activeResolutionResId == R.drawable.set_vid_res_4k30_nor));

        list.add(new SettingItem(R.drawable.set_vid_4ku30_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_3840x2160_30p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_3840x2160_30p_16x9_super, null, null);
                }
            }
        }).setActive(activeResolutionResId == R.drawable.set_vid_res_4ku30_nor));

        //list.add(new SettingItem(R.drawable.set_vid_27k30_nor));
        //list.add(new SettingItem(R.drawable.set_vid_27ku30_nor));
        //list.add(new SettingItem(R.drawable.set_vid_27k4330_nor));

        if (activeResolutionResId == R.drawable.set_vid_res_1440p60_nor) {
            list.add(new SettingItem(R.drawable.set_vid_1440p60_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(2);
                }
            }).setActive(true));
        }
        else {
            list.add(new SettingItem(R.drawable.set_vid_1440p30_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(2);
                }
            }).setActive(activeResolutionResId == R.drawable.set_vid_res_1440p30_nor));
        }

        if (activeResolutionResId == R.drawable.set_vid_res_1080p60_nor) {
            list.add(new SettingItem(R.drawable.set_vid_1080p60_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(3);
                }
            }).setActive(true));
        } else if (activeResolutionResId == R.drawable.set_vid_res_1080p120_nor) {
            list.add(new SettingItem(R.drawable.set_vid_1080p120_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(3);
                }
            }).setActive(true));
        } else {
            list.add(new SettingItem(R.drawable.set_vid_1080p30_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(3);
                }
            }).setActive(activeResolutionResId == R.drawable.set_vid_res_1080p30_nor));
        }

        if (activeResolutionResId == R.drawable.set_vid_res_1080pu60_nor) {
            list.add(new SettingItem(R.drawable.set_vid_1080pu60_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(4);
                }
            }).setActive(true));
        } else if (activeResolutionResId == R.drawable.set_vid_res_1080pu120_nor) {
            list.add(new SettingItem(R.drawable.set_vid_1080pu120_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(4);
                }
            }).setActive(true));
        } else {
            list.add(new SettingItem(R.drawable.set_vid_1080pu30_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(4);
                }
            }).setActive(activeResolutionResId == R.drawable.set_vid_res_1080pu30_nor));
        }

        if (activeResolutionResId == R.drawable.set_vid_res_960p120_nor) {
            list.add(new SettingItem(R.drawable.set_vid_960p120_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(5);
                }
            }).setActive(true));
        } else {
            list.add(new SettingItem(R.drawable.set_vid_960p60_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(5);
                }
            }).setActive(activeResolutionResId == R.drawable.set_vid_res_960p60_nor));
        }

        list.add(new SettingItem(R.drawable.set_vid_720p240_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1280x720_240p_16x9) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1280x720_240p_16x9, null, null);
                }
            }
        }).setActive(activeResolutionResId == R.drawable.set_vid_res_720p240_nor));

        if (activeResolutionResId == R.drawable.set_vid_res_720pu120_nor) {
            list.add(new SettingItem(R.drawable.set_vid_720pu120_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(6);
                }
            }).setActive(true));
        } else {
            list.add(new SettingItem(R.drawable.set_vid_720pu60_nor, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectMode(6);
                }
            }).setActive(activeResolutionResId == R.drawable.set_vid_res_720pu60_nor));
        }

        //list.add(createVideoResolutionItem(R.drawable.set_vid_480p240_nor, activeResolution));
        return list;
    }

    private List<SettingItem> getVideoFieldOfViewData() {
        VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        final FieldOfView activeFieldOfView = mActivity.getCameraSettings().videoFieldOfView;
        List<SettingItem> list = new ArrayList<>();

        // Wide is always enabled
        list.add(new SettingItem(R.drawable.setup_fov_sec_wide_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeFieldOfView != FieldOfView.Wide) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoFieldOfView(FieldOfView.Wide, null, null);
                }
            }
        }).setActive(activeFieldOfView == FieldOfView.Wide));

        // Middle is not always enabled
        switch (activeResolution) {
            case v_1920x1440_30p_4x3:
            case v_1920x1080_30p_16x9:
            case v_1920x1080_60p_16x9:
            case v_1280x960_60p_4x3:
                list.add(new SettingItem(R.drawable.setup_fov_sec_medium_nor, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (activeFieldOfView != FieldOfView.Medium) {
                            mLodingView.setVisibility(View.VISIBLE);
                            mActivity.getCamera().setVideoFieldOfView(FieldOfView.Medium, null, null);
                        }
                    }
                }).setActive(activeFieldOfView == FieldOfView.Medium));
                break;
        }

        // Narrow is not always enabled
        switch (activeResolution) {
            case v_1920x1440_30p_4x3:
            case v_1920x1080_30p_16x9:
            case v_1920x1080_60p_16x9:
                list.add(new SettingItem(R.drawable.setup_fov_sec_narrow_nor, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (activeFieldOfView != FieldOfView.Narrow) {
                            mLodingView.setVisibility(View.VISIBLE);
                            mActivity.getCamera().setVideoFieldOfView(FieldOfView.Narrow, null, null);
                        }
                    }
                }).setActive(activeFieldOfView == FieldOfView.Narrow));
                break;
        }
        return  list;
    }

    private List<SettingItem> getMeteringModelData() {
        final MeteringMode activeMeteringMode = mActivity.getCameraSettings().meteringMode;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_pht_set_cen_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeMeteringMode != MeteringMode.Center) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setMeteringMode(MeteringMode.Center, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().meteringMode = MeteringMode.Center;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeMeteringMode == MeteringMode.Center));

        list.add(new SettingItem(R.drawable.set_pht_set_spt_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeMeteringMode != MeteringMode.Spot) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setMeteringMode(MeteringMode.Spot, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().meteringMode = MeteringMode.Spot;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeMeteringMode == MeteringMode.Spot));

        list.add(new SettingItem(R.drawable.set_pht_set_ave_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeMeteringMode != MeteringMode.Average) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setMeteringMode(MeteringMode.Average, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().meteringMode = MeteringMode.Average;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeMeteringMode == MeteringMode.Average));

        return list;
    }

    private List<SettingItem> getVideoQualityData() {
        final Quality activeVideoQuality = mActivity.getCameraSettings().videoQuality;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.setup_hig_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoQuality != Quality.High) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoQuality(Quality.High, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoQuality = Quality.High;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoQuality == Quality.High));

        list.add(new SettingItem(R.drawable.setup_mid_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoQuality != Quality.Middle) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoQuality(Quality.Middle, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoQuality = Quality.Middle;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoQuality == Quality.Middle));

        list.add(new SettingItem(R.drawable.setup_low_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoQuality != Quality.Low) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoQuality(Quality.Low, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoQuality = Quality.Low;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoQuality == Quality.Low));

        return list;
    }

    private List<SettingItem> getVideoWhiteBalanceData() {
        final WhiteBalance activeVideoWhiteBalance = mActivity.getCameraSettings().videoWhiteBalance;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_aut_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoWhiteBalance != WhiteBalance.Auto) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoWhiteBalance(WhiteBalance.Auto, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoWhiteBalance = WhiteBalance.Auto;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoWhiteBalance == WhiteBalance.Auto));

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_nat_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoWhiteBalance != WhiteBalance.Native) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoWhiteBalance(WhiteBalance.Native, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoWhiteBalance = WhiteBalance.Native;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoWhiteBalance == WhiteBalance.Native));

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_3000k_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoWhiteBalance != WhiteBalance.Tungsten) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoWhiteBalance(WhiteBalance.Tungsten, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoWhiteBalance = WhiteBalance.Tungsten;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoWhiteBalance == WhiteBalance.Tungsten));

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_5500k_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoWhiteBalance != WhiteBalance.Daylight) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoWhiteBalance(WhiteBalance.Daylight, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoWhiteBalance = WhiteBalance.Daylight;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoWhiteBalance == WhiteBalance.Daylight));

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_6500k_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoWhiteBalance != WhiteBalance.Cloudy) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoWhiteBalance(WhiteBalance.Cloudy, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoWhiteBalance = WhiteBalance.Cloudy;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoWhiteBalance == WhiteBalance.Cloudy));

        return list;
    }

    private List<SettingItem> getVideoColorModeData() {
        final ColorMode activeVideoColorMode = mActivity.getCameraSettings().videoColorMode;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_color_sec_yi_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoColorMode != ColorMode.YIColor) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoColorMode(ColorMode.YIColor, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoColorMode = ColorMode.YIColor;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoColorMode == ColorMode.YIColor));

        list.add(new SettingItem(R.drawable.set_color_sec_flat_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoColorMode != ColorMode.Flat) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoColorMode(ColorMode.Flat, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoColorMode = ColorMode.Flat;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoColorMode == ColorMode.Flat));

        return list;
    }

    private List<SettingItem> getVideoISOData() {
        final ISO activeVideoISO = mActivity.getCameraSettings().videoISO;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_pht_set_wb_sec_aut_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoISO != ISO.iso_Auto) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoISO(ISO.iso_Auto, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoISO = ISO.iso_Auto;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoISO == ISO.iso_Auto));

        list.add(new SettingItem(R.drawable.set_pht_set_iso_sec_400_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoISO != ISO.iso_400) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoISO(ISO.iso_400, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoISO = ISO.iso_400;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoISO == ISO.iso_400));

        list.add(new SettingItem(R.drawable.set_pht_set_iso_sec_1600_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoISO != ISO.iso_1600) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoISO(ISO.iso_1600, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoISO = ISO.iso_1600;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoISO == ISO.iso_1600));

        list.add(new SettingItem(R.drawable.set_pht_set_iso_sec_6400_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoISO != ISO.iso_6400) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoISO(ISO.iso_6400, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoISO = ISO.iso_6400;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoISO == ISO.iso_6400));

        return list;
    }

    private List<SettingItem> getVideoSharpnessData() {
        final Sharpness activeVideoSharpness = mActivity.getCameraSettings().videoSharpness;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.setup_hig_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoSharpness != Sharpness.High) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoSharpness(Sharpness.High, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoSharpness = Sharpness.High;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoSharpness == Sharpness.High));

        list.add(new SettingItem(R.drawable.setup_mid_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoSharpness != Sharpness.Medium) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoSharpness(Sharpness.Medium, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoSharpness = Sharpness.Medium;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoSharpness == Sharpness.Medium));

        list.add(new SettingItem(R.drawable.setup_low_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoSharpness != Sharpness.Low) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoSharpness(Sharpness.Low, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoSharpness = Sharpness.Low;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoSharpness == Sharpness.Low));

        return list;
    }

    private List<SettingItem> getVideoExposureValueData() {
        final ExposureValue activeVideoExposureValue = mActivity.getCameraSettings().videoExposureValue;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_pht_set_ev_20_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_positive_2) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_positive_2, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_positive_2;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_positive_2));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_15_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_positive_1_point_5) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_positive_1_point_5, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_positive_1_point_5;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_positive_1));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_10_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_positive_1) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_positive_1, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_positive_1;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_positive_1));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_05_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_positive_0_point_5) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_positive_0_point_5, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_positive_0_point_5;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_positive_0_point_5));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_0_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_0) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_0, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_0;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_0));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_n05_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_negative_0_point_5) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_negative_0_point_5, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_negative_0_point_5;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_negative_0_point_5));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_n10_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_negative_1) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_negative_1, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_negative_1;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_negative_1));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_n15_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_negative_1_point_5) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_negative_1_point_5, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_negative_1_point_5;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_negative_1_point_5));

        list.add(new SettingItem(R.drawable.set_pht_set_ev_n20_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoExposureValue != ExposureValue.ev_negative_2) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoExposureValue(ExposureValue.ev_negative_2, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoExposureValue = ExposureValue.ev_negative_2;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoExposureValue == ExposureValue.ev_negative_2));

        return list;
    }

    private List<SettingItem> getVideoTimestampData() {
        final Timestamp activeVideoTimestamp = mActivity.getCameraSettings().videoTimestamp;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.setup_adv_vr_sec_off_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoTimestamp != Timestamp.Off) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoTimestamp(Timestamp.Off, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoTimestamp = Timestamp.Off;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoTimestamp == Timestamp.Off));

        list.add(new SettingItem(R.drawable.set_pht_set_dat_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoTimestamp != Timestamp.Date) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoTimestamp(Timestamp.Date, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoTimestamp = Timestamp.Date;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoTimestamp == Timestamp.Date));

        list.add(new SettingItem(R.drawable.set_pht_set_tim_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoTimestamp != Timestamp.Time) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoTimestamp(Timestamp.Time, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoTimestamp = Timestamp.Time;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoTimestamp == Timestamp.Time));

        list.add(new SettingItem(R.drawable.set_pht_set_dat_tim_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeVideoTimestamp != Timestamp.DateAndTime) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoTimestamp(Timestamp.DateAndTime, new ActionCameraCommandCallback() {
                        @Override
                        public void onInvoke() {
                            mActivity.getCameraSettings().videoTimestamp = Timestamp.DateAndTime;
                            onCameraSettingsUpdated();
                        }
                    }, null);
                }
            }
        }).setActive(activeVideoTimestamp == Timestamp.DateAndTime));

        return list;
    }

    private List<SettingItem> getResolution1440Data() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(R.drawable.set_vid_thr_30_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1440_30p_4x3) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1440_30p_4x3, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1440p30_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_60_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1440_60p_4x3) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1440_60p_4x3, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1440p60_nor));
        return list;
    }

    private List<SettingItem> getResolution1080Data() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(R.drawable.set_vid_thr_30_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_30p_16x9) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_30p_16x9, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080p30_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_60_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_60p_16x9) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_60p_16x9, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080p60_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_120_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_120p_16x9) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_120p_16x9, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080p120_nor));
        return list;
    }

    private List<SettingItem> getResolution1080ultraData() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        List<SettingItem> list = new ArrayList<>();
        list.add(new SettingItem(R.drawable.set_vid_thr_30_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_30p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_30p_16x9_super, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080pu30_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_60_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_60p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_60p_16x9_super, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080pu60_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_120_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1920x1080_120p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1920x1080_120p_16x9_super, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_1080pu120_nor));
        return list;
    }

    private List<SettingItem> getResolution960Data() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_vid_thr_60_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1280x960_60p_4x3) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1280x960_60p_4x3, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_960p60_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_120_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1280x960_120p_4x3) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1280x960_120p_4x3, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_960p120_nor));
        return list;
    }

    private List<SettingItem> getResolution720ultraData() {
        final VideoResolution activeResolution = mActivity.getCameraSettings().videoResolution;
        List<SettingItem> list = new ArrayList<>();

        list.add(new SettingItem(R.drawable.set_vid_thr_60_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1280x720_60p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1280x720_60p_16x9_super, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_720pu60_nor));

        list.add(new SettingItem(R.drawable.set_vid_thr_120_nor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activeResolution != VideoResolution.v_1280x720_120p_16x9_super) {
                    mLodingView.setVisibility(View.VISIBLE);
                    mActivity.getCamera().setVideoResolution(VideoResolution.v_1280x720_120p_16x9_super, null, null);
                }
            }
        }).setActive(recordingResolutionToResourceId(activeResolution) == R.drawable.set_vid_res_720pu120_nor));
        return list;
    }

    private void showRecordingResolutionSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getRecordingResolutionData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoFieldOfViewSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoFieldOfViewData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showMeteringModeSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getMeteringModelData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoQualitySelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoQualityData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoWhiteBalanceSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoWhiteBalanceData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoColorModeSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoColorModeData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoISOSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoISOData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoSharpnessSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoSharpnessData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoExposureValueSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoExposureValueData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showVideoTimestampSelection() {
        mSettingListView.setAdapter(new MyAdapater(mActivity, getVideoTimestampData(), (int)Math.ceil(mActivity.getScreenPanel().getHeight() / 3.0)));
        showBackIcon();
    }

    private void showBackIcon() {
        mExitIcon.setTag(R.drawable.set_back_left_nor);
        mExitIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.set_back_left_nor));
    }

    private void showExitIcon() {
        mExitIcon.setTag(R.drawable.setting_exit_left);
        mExitIcon.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.setting_exit_left));
    }

    private int recordingResolutionToResourceId(VideoResolution videoResolution) {
        switch (videoResolution) {
            case v_3840x2160_30p_16x9:
                return R.drawable.set_vid_res_4k30_nor;

            case v_3840x2160_30p_16x9_super:
                return R.drawable.set_vid_res_4ku30_nor;

            case v_1920x1440_30p_4x3:
                return R.drawable.set_vid_res_1440p30_nor;

            case v_1920x1440_60p_4x3:
                return R.drawable.set_vid_res_1440p60_nor;

            case v_1920x1080_30p_16x9:
                return R.drawable.set_vid_res_1080p30_nor;

            case v_1920x1080_60p_16x9:
                return R.drawable.set_vid_res_1080p60_nor;

            case v_1920x1080_120p_16x9:
                return R.drawable.set_vid_res_1080p120_nor;

            case v_1920x1080_30p_16x9_super:
                return R.drawable.set_vid_res_1080pu30_nor;

            case v_1920x1080_60p_16x9_super:
                return R.drawable.set_vid_res_1080pu60_nor;

            case v_1920x1080_120p_16x9_super:
                return R.drawable.set_vid_res_1080pu120_nor;

            case v_1280x960_60p_4x3:
                return R.drawable.set_vid_res_960p60_nor;

            case v_1280x960_120p_4x3:
                return R.drawable.set_vid_res_960p120_nor;

            case v_1280x720_240p_16x9:
                return R.drawable.set_vid_res_720p240_nor;

            case v_1280x720_60p_16x9_super:
                return R.drawable.set_vid_res_720pu60_nor;

            case v_1280x720_120p_16x9_super:
                return R.drawable.set_vid_res_720pu120_nor;

            default:
                return -1;
        }
    }

    private int recordingFieldOfViewToResourceId(FieldOfView fieldOfView) {
        switch (fieldOfView) {
            case Wide:
                return R.drawable.setup_fov_wide_nor;

            case Medium:
                return R.drawable.setup_fov_medium_nor;

            case Narrow:
                return R.drawable.setup_fov_narrow_nor;

            default:
                return -1;
        }
    }

    private int meterModeToResourceId(MeteringMode meteringMode) {
        switch (meteringMode) {
            case Center:
                return R.drawable.set_pht_set_met_cen_nor;

            case Spot:
                return R.drawable.set_pht_set_met_spt_nor;

            case Average:
                return R.drawable.set_pht_set_met_ave_nor;

            default:
                return -1;
        }
    }

    private int videoQualityToResourceId(Quality quality) {
        switch (quality) {
            case High:
                return R.drawable.set_vid_qua_hig_nor;

            case Middle:
                return R.drawable.set_vid_qua_nor_nor;

            case Low:
                return R.drawable.set_vid_qua_low_nor;

            default:
                return -1;
        }
    }

    private int videoWhiteBalanceToResourceId(WhiteBalance whiteBalance) {
        switch (whiteBalance) {
            case Native:
                return R.drawable.set_pht_set_wb_nat_nor;

            case Auto:
                return R.drawable.set_pht_set_wb_aut_nor;

            case Cloudy:
                return R.drawable.set_pht_set_wb_6500k_nor;

            case Daylight:
                return R.drawable.set_pht_set_wb_5500k_nor;

            case Tungsten:
                return R.drawable.set_pht_set_wb_3000k_nor;

            default:
                return -1;
        }
    }

    private int videoColorModeToResourceId(ColorMode color) {
        switch (color) {
            case YIColor:
                return R.drawable.set_color_yi_nor;

            case Flat:
                return R.drawable.set_color_flat_nor;

            default:
                return -1;
        }
    }

    private int videoISOToResourceId(ISO iso) {
        switch (iso) {
            case iso_Auto:
                return R.drawable.set_pht_set_iso_aut_nor;

            case iso_400:
                return R.drawable.set_pht_set_iso_400_nor;

            case iso_1600:
                return R.drawable.set_pht_set_iso_1600_nor;

            case iso_6400:
                return R.drawable.set_pht_set_iso_6400_nor;

            default:
                return -1;
        }
    }

    private int videoSharpnessToResourceId(Sharpness sharpness) {
        switch (sharpness) {
            case High:
                return R.drawable.setup_sharp_high_nor;

            case Medium:
                return R.drawable.setup_sharp_mid_nor;

            case Low:
                return R.drawable.setup_sharp_low_nor;

            default:
                return -1;
        }
    }

    private int videoExposureValueToResourceId(ExposureValue ev) {
        switch (ev) {
            case ev_positive_2:
                return R.drawable.set_pht_set_ev_20_nor;

            case ev_positive_1_point_5:
                return R.drawable.set_pht_set_ev_15_nor;

            case ev_positive_1:
                return R.drawable.set_pht_set_ev_10_nor;

            case ev_positive_0_point_5:
                return R.drawable.set_pht_set_ev_05_nor;

            case ev_0:
                return R.drawable.set_pht_set_ev_0_nor;

            case ev_negative_0_point_5:
                return R.drawable.set_pht_set_ev_n05_nor;

            case ev_negative_1:
                return R.drawable.set_pht_set_ev_n10_nor;

            case ev_negative_1_point_5:
                return R.drawable.set_pht_set_ev_n15_nor;

            case ev_negative_2:
                return R.drawable.set_pht_set_ev_n20_nor;

            default:
                return -1;
        }
    }

    private int videoTimestampToResourceId(Timestamp timestamp) {
        switch (timestamp) {
            case Off:
                return R.drawable.set_pht_set_tim_off_nor;

            case Date:
                return R.drawable.set_pht_set_tim_dat_nor;

            case Time:
                return R.drawable.set_pht_set_tim_tim_nor;

            case DateAndTime:
                return R.drawable.set_pht_set_tim_dat_tim_nor;

            default:
                return -1;
        }
    }
}

class ViewHolder {
    public View view;
    public ImageView img;
    public ImageView activeIcon;
}

class MyAdapater extends BaseAdapter {
    private VirtualCameraActivity mActivity;
    private List<SettingItem> mData;
    private int mItemHeight;

    MyAdapater(VirtualCameraActivity activity, List<SettingItem> data, int itemHeight) {
        mActivity = activity;
        mData = data;
        mItemHeight = itemHeight;
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = mActivity.getLayoutInflater().inflate(R.layout.setting_item_view, null);
            holder.view = view;

            ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            view.setLayoutParams(param);

            holder.img = (ImageView)view.findViewById(R.id.setting_item_img);
            holder.activeIcon = (ImageView)view.findViewById(R.id.setting_selection_icon);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        SettingItem data = (SettingItem)mData.get(i);
        holder.view.setOnClickListener(data.onClickListener);
        holder.img.setImageDrawable(mActivity.getResources().getDrawable(data.imgId));
        holder.activeIcon.setVisibility(data.isActive ? View.VISIBLE : View.INVISIBLE);
        return view;
    }
}