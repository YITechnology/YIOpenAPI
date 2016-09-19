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

import UIKit;

class CameraGridViewController: UIViewController, UICollectionViewDataSource, CameraListener {
    @IBOutlet weak var mActionMenu: UIView!

    var mCapacity = 0;
    private var mScanCameraExitEvent = NSCondition();
    private var mPendingCameras = [Camera]();
    private var mCameras = [Camera]();
    private var mCollectionView: UICollectionView!;
    
    override func viewDidLoad() {
        super.viewDidLoad();
        
        title = "YI360Demo";
        navigationItem.hidesBackButton = true;
        startScanCameras();
    }

    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        mCollectionView = collectionView;
        return mCapacity;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("cameraCell", forIndexPath: indexPath) as! CameraCell;
        if (indexPath.item < mCameras.count) {
            cell.setCamera(mCameras[indexPath.item]);
        } else {
            cell.setCamera(nil);
        }
        return cell;
    }
    
    // This function will be invoked when camera state has been changed
    func onStateChanged(camera: Camera, newState: Camera.State, oldState: Camera.State) {
        if (newState == .Connected) {
            removeFrom(camera, queue: &mPendingCameras);
            
            // do we have free slot?
            if (mCameras.count == mCapacity) {
                return;
            }
            
            // add to mCameras queue
            mCameras.append(camera);
            
            // attach to cell
            if let cell = mCollectionView.cellForItemAtIndexPath(NSIndexPath(forItem: mCameras.count - 1, inSection: 0)) as? CameraCell {
                cell.setCamera(camera);
            }
        } else if (newState == .Disconnected) {
            // remove from pending queue
            removeFrom(camera, queue: &mPendingCameras);
            
            // remove from mCameras queue
            if let index = mCameras.indexOf({$0.getIP() == camera.getIP()}) {
                if let cell = mCollectionView.cellForItemAtIndexPath(NSIndexPath(forItem: index, inSection: 0)) as? CameraCell {
                    cell.setCamera(nil);
                }
                mCameras.removeAtIndex(index);
            }
        }
    }
    
    @IBAction func onMenuBtnClicked(sender: AnyObject) {
        mActionMenu.hidden = !mActionMenu.hidden;
    }
    
    @IBAction func onGestureRecognizer(sender: AnyObject) {
        if (!mActionMenu.hidden) {
            mActionMenu.hidden = true;
        }
    }
    
    @IBAction func onExitBtnClicked(sender: AnyObject) {
        (parentViewController as! UINavigationController).popViewControllerAnimated(true);
        stopScanCameras();
    }
    
    @IBAction func onStartRecordingBtnClicked(sender: AnyObject) {
        mActionMenu.hidden = true;
        
        // Start recording after 10 seconds.
        let startTime = NSDate(timeIntervalSinceNow: 10);
        for (var i = 0; i < mCameras.count; ++i) {
            mCameras[i].startRecording(startTime);
        }
        showMessageBox("Recording will be started after 10 seconds");
    }
    
    @IBAction func onStopRecordingBtnClicked(sender: AnyObject) {
        mActionMenu.hidden = true;
        
        for (var i = 0; i < mCameras.count; ++i) {
            mCameras[i].stopRecording();
        }
    }
    
    // Start camera scan thread
    private func startScanCameras() {
        NSThread(target: self, selector: #selector(scanCameras), object: nil).start();
    }
    
    // Stop camera scan thread
    private func stopScanCameras() {
        mScanCameraExitEvent.lock();
        mScanCameraExitEvent.signal();
        mScanCameraExitEvent.unlock();
    }
    
    // Remove camera from camera queue
    private func removeFrom(camera: Camera, inout queue: [Camera]) {
        if let index = queue.indexOf({$0.getIP() == camera.getIP()}) {
            queue.removeAtIndex(index);
        }
    }
    
    private func processIPList(ipList: [String]) {
        for (var i = 0; i < ipList.count; i = i + 2) {
            // if we don't have free slot, do nothing.
            if (mCapacity == mCameras.count) {
                return;
            }
            
            let ip = ipList[i];
            let host = ipList[i + 1];
            print("found ip: \(ip), host: \(host)");

            // check whether this is in pending queue or in final queue
            var index = mPendingCameras.indexOf({$0.getIP() == ip});
            if (index == nil) {
                index = mCameras.indexOf({$0.getIP() == ip});
                if (index == nil) {
                    // new ip, create a camera, put to pending queue, and connect it.
                    let camera = Camera(ip: ip, host: host);
                    camera.setListener(self);
                    mPendingCameras.append(camera);
                    camera.connect();
                }
            }
        }
    }
    
    // Camera scan thread
    @objc private func scanCameras() {
        mScanCameraExitEvent.lock();
        while (true) {
            dispatch_async(dispatch_get_main_queue()) {
                var ips = [String]();
                // If iPhone is used as wifi hotspot, the client ip range is 172.20.10.1~172.20.10.10
                for (var i = 1; i <= 10; i += 1) {
                    ips.append("172.20.10.\(i)");
                    ips.append("172.20.10.\(i)");
                }
                self.processIPList(ips);
            }
            
            // wait for exit event or re-scan after 10 seconds
            if (mScanCameraExitEvent.waitUntilDate(NSDate(timeIntervalSinceNow: 10))) {
                // exit event is fired, exit
                break;
            }
        }
        mScanCameraExitEvent.unlock();
    }
    
    private func showMessageBox(message: String) {
        let alert = UIAlertView();
        alert.message = message;
        alert.addButtonWithTitle("Ok")
        alert.show()
    }
}

