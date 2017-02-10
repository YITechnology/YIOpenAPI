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

class CameraGridViewController: UIViewController, UICollectionViewDataSource, CameraListener {
    @IBOutlet weak var mActionMenu: UIView!

    var mCapacity = 0
    private var mScanCameraExitEvent = NSCondition()
    private var mPendingCameras: [Camera] = []
    private var mCameras: [Camera] = []
    private var mCollectionView: UICollectionView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "YI360Demo"
        navigationItem.hidesBackButton = true
        startScanCameras()
    }

    func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        mCollectionView = collectionView
        return mCapacity
    }
    
    func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCell(withReuseIdentifier: "cameraCell", for: indexPath) as! CameraCell
        if (indexPath.item < mCameras.count) {
            cell.setCamera(camera: mCameras[indexPath.item])
        } else {
            cell.setCamera(camera: nil)
        }
        return cell
    }
    
    // This function will be invoked when camera state has been changed
    func onStateChanged(camera: Camera, newState: Camera.State, oldState: Camera.State) {
        if (newState == .Connected) {
            removeFrom(camera: camera, queue: &mPendingCameras)
            
            // do we have free slot?
            if (mCameras.count == mCapacity) {
                return
            }
            
            // add to mCameras queue
            mCameras.append(camera)
            
            // attach to cell
            if let cell = mCollectionView.cellForItem(at: IndexPath(item: mCameras.count-1, section: 0)) as? CameraCell {
                cell.setCamera(camera: camera)
            }
        } else if (newState == .Disconnected) {
            // remove from pending queue
            removeFrom(camera: camera, queue: &mPendingCameras)
            
            // remove from mCameras queue
            if let index = mCameras.index(where: {$0.getIP() == camera.getIP()}) {
                if let cell = mCollectionView.cellForItem(at: IndexPath(index: index)) as? CameraCell {
                    cell.setCamera(camera: nil)
                }
                mCameras.remove(at: index)
            }
        }
    }
    
    @IBAction func onMenuBtnClicked(_ sender: Any) {
        mActionMenu.isHidden = !mActionMenu.isHidden
    }
    
    @IBAction func onGestureRecognizer(_ sender: Any) {
        if (!mActionMenu.isHidden) {
            mActionMenu.isHidden = true
        }
    }
    
    @IBAction func onExitBtnClicked(_ sender: Any) {
        (parent as! UINavigationController).popViewController(animated: true)
        stopScanCameras()
    }
    
    @IBAction func onStartRecordingBtnClicked(_ sender: Any) {
        mActionMenu.isHidden = true
        
        // Start recording after 10 seconds.
        let startTime = NSDate(timeIntervalSinceNow: 10)
        
        for i in 0..<mCameras.count {
            self.mCameras[i].startRecording(startTime: startTime)
        }
        showMessageBox(message: "Recording will be started after 10 seconds")
    }
    
    @IBAction func onStopRecordingBtnClicked(_ sender: Any) {
        mActionMenu.isHidden = true
        
        for i in 0..<mCameras.count {
            mCameras[i].stopRecording()
        }
    }
    
    // Start camera scan thread
    private func startScanCameras() {
        Thread(target: self, selector: #selector(scanCameras), object: nil).start()
    }
    
    // Stop camera scan thread
    private func stopScanCameras() {
        mScanCameraExitEvent.lock()
        mScanCameraExitEvent.signal()
        mScanCameraExitEvent.unlock()
    }
    
    // Remove camera from camera queue
    private func removeFrom(camera: Camera, queue: inout [Camera]) {
        if let index = queue.index(where: {$0.getIP() == camera.getIP()}) {
            queue.remove(at: index)
        }
    }
    
    private func processIPList(ipList: [String]) {
        for i in 0..<ipList.count {
            // if we don't have free slot, do nothing.
            if (mCapacity == mCameras.count) {
                return
            }
            
            let ip = ipList[i]
            let host = "Host: \(ipList[i])"
            print("found ip: \(ip), \(host)")

            // check whether this is in pending queue or in final queue
            var index = mPendingCameras.index(where: {$0.getIP() == ip})
            if (index == nil) {
                index = mCameras.index(where: {$0.getIP() == ip})
                if (index == nil) {
                    // new ip, create a camera, put to pending queue, and connect it.
                    let camera = Camera(ip: ip, host: host)
                    camera.setListener(listener: self)
                    mPendingCameras.append(camera)
                    camera.connect()
                }
            }
        }
    }
    
    // Camera scan thread
    @objc private func scanCameras() {
        mScanCameraExitEvent.lock()
        while (true) {
            DispatchQueue.main.async {
                var ips = [String]()
                // If iPhone is used as wifi hotspot, the client ip range is 172.20.10.1~172.20.10.99
                for i in 1...30 {
                    ips.append("172.20.10.\(i)")
                }
                self.processIPList(ipList: ips)
            }
            
            // wait for exit event or re-scan after 10 seconds
            if (mScanCameraExitEvent.wait(until: NSDate(timeIntervalSinceNow: 10) as Date)) {
                // exit event is fired, exit
                break
            }
        }
        mScanCameraExitEvent.unlock()
    }
    
    private func showMessageBox(message: String) {
        let alert = UIAlertView()
        alert.message = message
        alert.addButton(withTitle: "Ok")
        alert.show()
    }
}

