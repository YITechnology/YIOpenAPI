package com.xiaoyi.yivirtualcamera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by xyb on 11/22/2016.
 */

public class ModeSelectionView {
    private VirtualCameraActivity mActivity;
    private View mSelfView;

    ModeSelectionView(VirtualCameraActivity activity) {
        mActivity = activity;
        showContent();
    }

    private void showContent() {
        mSelfView = LayoutInflater.from(mActivity).inflate(R.layout.model_selection_view, null);
        mActivity.getScreenPanel().addView(mSelfView);

        int totalHeight = mActivity.getScreenPanel().getMeasuredHeight();
        setHeight((LinearLayout)mSelfView.findViewById(R.id.model_row_1), totalHeight / 3);
        setHeight((LinearLayout)mSelfView.findViewById(R.id.model_row_2), totalHeight / 3);
        setHeight((LinearLayout)mSelfView.findViewById(R.id.model_row_3), totalHeight / 3);

        mSelfView.findViewById(R.id.exit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.getScreenPanel().removeView(mSelfView);
            }
        });
    }

    private void setHeight(LinearLayout view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }
}
