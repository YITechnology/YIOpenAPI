//=============================================================================
// Copyright  2016 YI Technologies, Inc. All Rights Reserved.
//
// This software is the confidential and proprietary information of YI
// Technologies, Inc. ("Confidential Information").  You shall not
// disclose such Confidential Information and shall use it only in
// accordance with the terms of the license agreement you entered into
// with YI.
//
// YI MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
// SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
// IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// PURPOSE, OR NON-INFRINGEMENT. YI SHALL NOT BE LIABLE FOR ANY DAMAGES
// SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
// THIS SOFTWARE OR ITS DERIVATIVES.
//=============================================================================

package com.xiaoyi.yi360demo;

import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.*;
import android.support.v7.app.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import java.net.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import com.xiaoyi.action.*;

public class GridActivity extends AppCompatActivity {
    private boolean mStartScanTimer;
    private GridView gridView;
    private AlertDialog mExitDialog;
    private Menu mMenu;
    public ArrayList<Camera> pendingCameras;
    public ArrayList<Camera> cameras;
    public GridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize YiCameraPlatform
        try {
            Platform.initialize(new Logger() {
                @Override
                public void verbose(String message) {
                    Log.v("YiCameraPlatform", message);
                }

                @Override
                public void info(String message) {
                    Log.i("YiCameraPlatform", message);
                }

                @Override
                public void warning(String message) {
                    Log.w("YiCameraPlatform", message);
                }

                @Override
                public void error(String message) {
                    Log.e("YiCameraPlatform", message);
                }
            });
        } catch (Exception ex) {
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        pendingCameras = new ArrayList<>();
        cameras = new ArrayList<>();
        gridAdapter = new GridAdapter(this, cameras);

        setContentView(R.layout.grid_activity);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(gridAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grid_activity_actionbar, menu);

        mMenu = menu;
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mExitDialog != null && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        } else {
            exit(null);
        }
    }

    public void select6Cameras(View view) {
        selectCameras(6);
    }

    public void select7Cameras(View view) {
        selectCameras(7);
    }

    public void select10Cameras(View view) {
        selectCameras(10);
    }

    public void select24Cameras(View view) {
        selectCameras(24);
    }

    private Camera getCameraByIp(String ip, ArrayList<Camera> cameras) {
        for (Camera camera: cameras) {
            if (camera.getIp().equals(ip)) {
                return camera;
            }
        }
        return null;
    }

    public void startRecording(MenuItem item) {
        // Start recording after 10 seconds
        Date date = new Date();
        date.setTime(date.getTime() + 10 * 1000);
        for (Camera camera: cameras) {
            if (!camera.getIp().isEmpty()) {
                camera.startRecording(date);
            }
        }
    }

    public void stopRecording(MenuItem item) {
        for (Camera camera: cameras) {
            if (!camera.getIp().isEmpty()) {
                camera.stopRecording();
            }
        }
    }

    public void exit(MenuItem item) {
        if (mExitDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true);
            builder.setMessage("Do you want to Exit?");
            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mStartScanTimer = false;
                    stopRecording(null);
                    for (Camera camera : cameras) {
                        camera.disconnect();
                    }
                    finish();
                }
            });
            builder.setNegativeButton("Run at background", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    moveTaskToBack(true);
                }
            });
            mExitDialog = builder.create();
        }
        mExitDialog.show();
    }

    private boolean isWifiHotSpotEnabled() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            Method isWifiApEnabledMethod = wifiManager.getClass().getMethod("isWifiApEnabled");
            return (boolean)isWifiApEnabledMethod.invoke(wifiManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void selectCameras(int cameraCount) {
        // check WifiHotSpot is enabled or not, if wifi hotspot is not enabled, show error
        if (!isWifiHotSpotEnabled()) {
            showMessage("WifiHotspot is not enabled. Please enable it.");
            return;
        }

        for (int i = 0; i < mMenu.size(); i++) {
            mMenu.getItem(i).setVisible(true);
        }

        findViewById(R.id.cameraSelectionView).setVisibility(View.INVISIBLE);
        findViewById(R.id.cameraGridView).setVisibility(View.VISIBLE);

        cameras.clear();
        for (int i = 0; i < cameraCount; ++i) {
            cameras.add(new Camera(this, null, null));
        }
        gridAdapter.notifyDataSetChanged();

        // start to scan camera
        mStartScanTimer = true;
        doScanCamera();
    }

    // Scan cameras and maintain the camera lists every 10 seconds.
    // This function will do following things:
    //      1. Get the list who is connected to the hotspot.
    //      2. Close the camera connection whose ip is not in the list.
    //      3. For new ip in the the list, find a free camera connection to connect.
    //      4. Send heartbeat for all camera connections.
    private void doScanCamera() {
        if (!mStartScanTimer) {
            return;
        }

        Log.i("YiCamera", "scan camera");

        // check whether the cameras list is full
        boolean hasFreeSlot = false;
        for (int i = 0; i < cameras.size(); ++i) {
            if (cameras.get(i).getIp().isEmpty()) {
                hasFreeSlot = true;
                break;
            }
        }

        if (hasFreeSlot) {
            final GridActivity obj = this;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<String> clientIPs = new ArrayList<>();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            String[] splitted = line.split(" +");
                            if (splitted != null && splitted.length >= 4) {
                                String ip = splitted[0];
                                String mac = splitted[3];
                                if (mac.matches("..:..:..:..:..:..")) {
                                    if (InetAddress.getByName(ip).isReachable(1000)) {
                                        clientIPs.add(ip);
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mStartScanTimer) {
                                return;
                            }

                            for (String ip : clientIPs) {
                                if (getCameraByIp(ip, pendingCameras) == null && getCameraByIp(ip, cameras) == null) {
                                    Log.i("YiCamera", "Find new ip: " + ip);
                                    String hostname = "";
                                    try {
                                        hostname = InetAddress.getByName(ip).getHostName();
                                    } catch (Exception ex) {
                                    }
                                    Camera camera = new Camera(obj, ip, hostname);
                                    pendingCameras.add(camera);
                                    camera.connect();
                                }
                            }
                        }
                    });
                }
            });
        } else {
            Log.i("YI360", "No free slot");
        }

        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        doScanCamera();
                    }
                });
            }
        }, 10 * 1000);
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Message");
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }
}
