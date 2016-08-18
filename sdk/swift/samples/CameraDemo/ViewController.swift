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

class UIDispatchQueue: DispatchQueue {
    @objc func dispatch(task task: () -> ()) {
        dispatch_async(dispatch_get_main_queue(), task);
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
    
    override func onClosed(error error: Error?) {
        mView.onDisconnected();
    }
    
    override func onRecordStarted() {
        mView.onRecordStarted();
    }
    
    override func onRecordStopped() {
        mView.onRecordStopped();
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
    private var mTimer: NSTimer!;
    
    override func viewDidLoad() {
        super.viewDidLoad()
        mCamera = ActionCamera(listener: CameraListener(view: self), dispatchQueue: UIDispatchQueue());
        setStatus(Status.Disconnected);
    }

    func connect() {
        setStatus(Status.Connecting);
        mTimer = NSTimer.scheduledTimerWithTimeInterval(5, target: self, selector: #selector(onConnectTimeout), userInfo: nil, repeats: false);
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
        mCamera.setDateTime(datetime: NSDate(),
            success: {
                self.setStatus(Status.Connected);
            }, fail: {
                error in
                self.disconnect();
            });
    }
    
    func onDisconnected() {
        setStatus(Status.Disconnected);
    }

    func onConnectTimeout() {
        mCamera.disconnect();
        showMessageBox("Connect failed, please make sure you've connected to camera's WIFI already.");
    }
    
    func onRecordStarted() {
        setStatus(Status.Recording);
    }
    
    func onRecordStopped() {
        setStatus(Status.Connected);
    }
    
    private func setStatus(status: Status) {
        if (mStatus != nil && mStatus == status) {
            return;
        }
        
        if (status == Status.Disconnected) {
            mCameraImg.image = UIImage(named: "inactive_camera");
            mStatusLabel.text = "Disconnected";
            mConnectBtn.setTitle("Connect", forState: UIControlState.Normal);
            mRecordingBtn.hidden = true;
        } else if (status == Status.Connecting) {
            mStatusLabel.text = "Connecting...";
            mConnectBtn.setTitle("Disconnect", forState: UIControlState.Normal);
        } else if (status == Status.Connected) {
            mCameraImg.image = UIImage(named: "white_camera");
            mStatusLabel.text = "Connected";
            mRecordingBtn.setTitle("StartRecording", forState: UIControlState.Normal);
            mRecordingBtn.hidden = false;
        } else if (status == Status.Recording) {
            mCameraImg.image = UIImage(named: "recording_camera");
            mStatusLabel.text = "Recording";
            mRecordingBtn.setTitle("StopRecording", forState: UIControlState.Normal);
        }
        mStatus = status;
    }
    
    private func showMessageBox(message: String) {
        let alert = UIAlertView();
        alert.message = message;
        alert.addButtonWithTitle("Ok")
        alert.show()
    }
    
    @IBAction func onConnectBtnClicked(sender: AnyObject) {
        if (mStatus == Status.Disconnected) {
            connect();
        } else {
            disconnect();
        }
    }
    
    @IBAction func onRecordingBtnClicked(sender: AnyObject) {
        if (mStatus == Status.Connected) {
            startRecording();
        } else if (mStatus == Status.Recording) {
            stopRecording();
        }
    }
}

