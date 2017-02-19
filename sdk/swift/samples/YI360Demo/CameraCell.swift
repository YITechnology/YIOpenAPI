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

import UIKit

class CameraCell: UICollectionViewCell, CameraListener {
    @IBOutlet weak var mCameraImage: UIImageView!
    @IBOutlet weak var mNameLabel: UILabel!
    @IBOutlet weak var mStateLabel: UILabel!
    
    private var mCamera: Camera?
    
    func setCamera(camera: Camera?) {
        if (mCamera !== camera) {
            mCamera?.setListener(listener: nil)
            mCamera = camera
            mCamera?.setListener(listener: self)
        }
        updateView()
    }
    
    func onStateChanged(camera: Camera, newState: Camera.State, oldState: Camera.State) {
        updateView()
    }
    
    private func updateView() {
        updateNameLabel()
        updateStateLabel()
        updateImage()
    }
    
    private func updateNameLabel() {
        var name = ""
        if (mCamera != nil) {
            name = mCamera!.getHost()
            if (name == "") {
                name = mCamera!.getIP()
            }
        }
        mNameLabel.text = name
    }
    
    private func updateStateLabel() {
        if (mCamera == nil) {
            mStateLabel.text = "Connecting"
        } else {
            switch mCamera!.getStatus() {
            case .Disconnected, .Connecting:
                mStateLabel.text = "Disconnected"
                break
                    
            case .Connected:
                mStateLabel.text = "Connected"
                break
                
            case .StartRecording:
                mStateLabel.text = "Start Recording"
                break
                
            case .Recording:
                mStateLabel.text = "Recording"
                break
            }
        }
    }
    
    private func updateImage() {
        if (mCamera == nil) {
            mCameraImage.image = UIImage(named: "inactive_camera")
        } else {
            switch mCamera!.getStatus() {
            case .Disconnected, .Connecting:
                mCameraImage.image = UIImage(named: "inactive_camera")
                break
                
            case .Connected, .StartRecording:
                mCameraImage.image = UIImage(named: "white_camera")
                break
                
            case .Recording:
                mCameraImage.image = UIImage(named: "recording_camera")
                break
            }
        }
    }
}
