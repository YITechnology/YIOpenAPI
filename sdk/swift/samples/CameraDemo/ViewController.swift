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

import YICameraSDK;

class UIDispatchQueue: YICameraSDKDispatchQueue {
    public func dispatch(task: @escaping () -> ()) {
        DispatchQueue.main.async() {
            task();
        }
    }
}

class CameraListener: ActionCameraListener {
    private let mView: ViewController;
    
    init(view: ViewController) {
        mView = view;
    }
    
    override func onConnected() {
        mView.onConnected();
    }
    
    override func onClosed(error: Error?) {
        mView.onDisconnected();
    }
    
    override func onRecordStarted() {
        mView.onRecordStarted();
    }
    
    override func onRecordStopped() {
        mView.onRecordStopped();
    }
    
    override func onRecordStoppedWithFileName(fileName: String) {
        print("file name: \(fileName)");
    }
}

class ViewController: UIViewController {
    @IBOutlet weak var mCameraImg: UIImageView!;
    @IBOutlet weak var mStatusLabel: UILabel!;
    @IBOutlet weak var mConnectBtn: UIButton!;
    @IBOutlet weak var mRecordingBtn: UIButton!;
    
    private enum Status {
        case Disconnected;
        case Connecting;
        case Connected;
        case Recording;
    }
    
    private var mStatus: Status!;
    private var mCamera: ActionCamera!;
    private var mTimer: Timer!;
    private var is4Kplus = false;
    
    override func viewDidLoad() {
        super.viewDidLoad()
        mCamera = ActionCamera(listener: CameraListener(view: self), dispatchQueue: UIDispatchQueue());
        setStatus(status: Status.Disconnected);
    }

    func connect() {
        setStatus(status: Status.Connecting);
        mTimer = Timer.scheduledTimer(timeInterval: 5, target: self, selector: #selector(onConnectTimeout), userInfo: nil, repeats: false);
        mCamera.connect(connectionString: "tcp:192.168.42.1:7878");
    
    }
    
    func disconnect() {
        mCamera.disconnect();
        if (mTimer != nil) {
            mTimer.invalidate();
            mTimer = nil;
        }
    }
    
    func startRecording() {
        mCamera.startRecording(success: nil, fail: nil);
    }
    
    func stopRecording() {
        mCamera.stopRecording(success: nil, fail: nil);
    }
    
    func onConnected() {
        if (mTimer != nil) {
            mTimer.invalidate();
            mTimer = nil;
        }

        // sync the datetime
        mCamera.setDateTime(datetime: NSDate() as Date,
            success: {
                self.setStatus(status: Status.Connected);
            }, fail: {
                error in
                self.disconnect();
            });


        // check if 4k+.
        mCamera.getSettings(
            success: {
                actCamSettings in
                if (actCamSettings.serialNumber!.hasPrefix("Z18")) {
                    self.is4Kplus = true;//
                    self.updateCameraImage(imgName: "yiac3_4Kplus");
                }
            }, fail: {
                error in
//                self.disconnect();
            });
    }
    
    func onDisconnected() {
        setStatus(status: Status.Disconnected);
    }

    func onConnectTimeout() {
        mCamera.disconnect();
        showMessageBox(message: "Connect failed, please make sure you've connected to camera's WIFI already.");
    }
    
    func onRecordStarted() {
        setStatus(status: Status.Recording);
    }
    
    func onRecordStopped() {
        setStatus(status: Status.Connected);
    }
    
    private func setStatus(status: Status) {
        if (mStatus != nil && mStatus == status) {
            print("status error");
            return;
        }
        
        var cameraImgName = "inactive_camera";
        
        if (status == Status.Disconnected) {
            cameraImgName = "inactive_camera";
            mStatusLabel.text = "Disconnected";
            mConnectBtn.setTitle("Connect", for: UIControlState.normal);
            mRecordingBtn.isHidden = true;
        } else if (status == Status.Connecting) {
            mStatusLabel.text = "Connecting...";
            mConnectBtn.setTitle("Disconnect", for: UIControlState.normal);
        } else if (status == Status.Connected) {
            cameraImgName = "white_camera";
            mStatusLabel.text = "Connected";
            mRecordingBtn.setTitle("StartRecording", for: UIControlState.normal);
            mRecordingBtn.isHidden = false;
        } else if (status == Status.Recording) {
            cameraImgName = "recording_camera";
            mStatusLabel.text = "Recording";
            mRecordingBtn.setTitle("StopRecording", for: UIControlState.normal);
        }
        
        mStatus = status;
        
        updateCameraImage(imgName: cameraImgName);
    }
    
    //
    private func updateCameraImage(imgName : String) {
        mCameraImg.image = UIImage(named: imgName);
    }
    
    private func showMessageBox(message: String) {
        let alert = UIAlertView();
        alert.message = message;
        alert.addButton(withTitle: "Ok")
        alert.show()
    }
    
    @IBAction func onConnectBtnClicked(_ sender: Any) {
        if (mStatus == Status.Disconnected) {
            connect();
        } else {
            disconnect();
        }
    }
    
    @IBAction func onRecordingBtnClicked(_ sender: Any) {
        if (mStatus == Status.Connected) {
            startRecording();
        } else if (mStatus == Status.Recording) {
            stopRecording();
        }
    }
}

