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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

class GridAdapter extends ArrayAdapter<Camera> {
    public  GridAdapter(Context c, ArrayList<Camera> cameraList) {
        super(c, 0, cameraList);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Camera camera = getItem(i);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.camera_item_in_grid, viewGroup, false);
        }

        // update name
        TextView text = (TextView)view.findViewById(R.id.nameText);
        text.setText(camera.getHostname());

        // update image
        ((ImageView)view.findViewById(R.id.camera_image)).setImageResource(getCameraImage(camera));

        // update status string
        text = (TextView)view.findViewById(R.id.statusText);
        text.setText(getCameraStatus(camera));

        return view;
    }

    private int getCameraImage(Camera camera) {
        switch (camera.getState()) {
            default:
            case Disconnected:
                return R.drawable.black_camera;

            case Connected:
            case StartRecording:
                return R.drawable.white_camera;

            case Recording:
                return R.drawable.recording_camera;
        }
    }

    private String getCameraStatus(Camera camera) {
        switch (camera.getState()) {
            default:
            case Disconnected:
                return "connecting";

            case Connected:
                return "connected";

            case StartRecording:
                return "start recording";

            case Recording:
                return "recording";
        }
    }
}
