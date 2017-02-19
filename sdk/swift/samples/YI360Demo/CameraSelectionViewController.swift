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

class CameraSelectionViewController: UIViewController {
    private var mCameraCount = 0
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "YI360Demo"
    }
    
    @IBAction func on6CamerasSelected() {
        showCamerasView(count: 6)
    }
    
    @IBAction func on7CamerasSelected() {
        showCamerasView(count: 7)
    }
    
    @IBAction func on10CamerasSelected() {
        showCamerasView(count: 10)
    }
    
    @IBAction func on24CamerasSelected() {
        showCamerasView(count: 24)
    }
  
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == "showCameraGrid") {
          (segue.destination as! CameraGridViewController).mCapacity = mCameraCount
        }
    }
    
    private func showCamerasView(count: Int) {
        mCameraCount = count
        performSegue(withIdentifier: "showCameraGrid", sender: self)
    }
}
