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

import android.util.*;
import com.xiaoyi.action.*;
import com.xiaoyi.action.Error;
import java.util.Date;

enum CameraState
{
    Disconnected,
    Connected,
    StartRecording,
    Recording
}

class Camera extends ActionCameraListener {
    private CameraState mState;
    private GridActivity mContext;
    private ActionCamera mCamera;
    private String mIP;
    private String mHostname;

    //region Properties
    public CameraState getState() {
        return mState;
    }

    public String getIp() {
        return mIP;
    }

    public String getHostname() {
        return mHostname;
    }
    //endregion

    public Camera(GridActivity context, String ip, String hostname) {
        mContext = context;
        mState = CameraState.Disconnected;
        mIP = ip;
        if (mIP == null) {
            mIP = "";
        }
        mHostname = hostname;
        if (mHostname == null || mHostname.isEmpty()) {
            mHostname = mIP;
        }
    }

    public void connect() {
        mCamera = new ActionCamera(this, new DispatchQueue() {
            @Override
            public void dispatch(Runnable task) {
                mContext.runOnUiThread(task);
            }
        });

        Log.i("YiCamera", "start connectAsync() to ip: " + mIP);
        mCamera.connect("tcp:" + mIP + ":7878");
    }

    public void disconnect() {
        if (mCamera != null) {
            mCamera.disconnect();
        }
    }

    public void startRecording(Date startTime) {
        if (mState == CameraState.Connected) {
            updateState(CameraState.StartRecording);
            mCamera.stopViewFinder(null, null).stopRecording(null, null)
                    .startRecording(startTime.getHours(), startTime.getMinutes(), startTime.getSeconds(), null, new ActionCameraCommandCallback1<Error>() {
                        @Override
                        public void onInvoke(Error val) {
                            Log.i("YiCamera", "Start recording failed");
                            if (mState == CameraState.StartRecording) {
                                updateState(CameraState.Connected);
                            }
                        }
                    });
        }
    }

    public void stopRecording() {
        if (mState == CameraState.StartRecording || mState == CameraState.Recording) {
            mCamera.stopRecording(null, null);
        }
    }

    //region implementation of ActionCameraListener
    @Override
    public void onConnected() {
        updateState(CameraState.Connected);

        final Camera obj = this;
        mCamera.setDateTime(new Date(),
            new ActionCameraCommandCallback() {
               @Override
               public void onInvoke() {
                   // Ready to use add self to cameras if there is free slot remove self from
                   // pending list
                   mContext.pendingCameras.remove(obj);

                   // add self to cameras if there is free slot
                   for (int i = 0; i < mContext.cameras.size(); ++i) {
                       if (mContext.cameras.get(i).getIp().isEmpty()) {
                           mContext.cameras.set(i, obj);
                           mContext.gridAdapter.notifyDataSetChanged();
                           return;
                       }
                   }

                   // no free slot, disconnect it
                   mCamera.disconnect();
               }
            },
            new ActionCameraCommandCallback1<Error>() {
               @Override
               public void onInvoke(Error val) {
                   // prepare failed, disconnect
                   mCamera.disconnect();
               }
           });
    }

    @Override
    public void onClosed(Error error) {
        updateState(CameraState.Disconnected);

        // remove self from pending list
        mContext.pendingCameras.remove(this);

        // remove self from cameras list
        for (int i = 0; i < mContext.cameras.size(); ++i) {
            if (mContext.cameras.get(i) == this) {
                mContext.cameras.set(i, new Camera(mContext, null, null));
                mContext.gridAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public void onRecordStarted() {
        updateState(CameraState.Recording);
    }

    @Override
    public void onRecordStopped() {
        updateState(CameraState.Connected);
    }
    //endregion

    private void updateState(CameraState state) {
        if (mState != state) {
            mState = state;
            mContext.gridAdapter.notifyDataSetChanged();
        }
    }
}
