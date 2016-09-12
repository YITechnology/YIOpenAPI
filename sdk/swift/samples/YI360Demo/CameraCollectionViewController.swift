//
//  CameraCollectionViewController.swift
//  testtest
//
//  Created by xyb on 8/24/16.
//  Copyright © 2016 xyb. All rights reserved.
//

import UIKit

private let reuseIdentifier = "Cell"

class CameraCollectionViewController: UIViewController, UICollectionViewDataSource {
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return DataStore.Inst.cameras.count;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("cameraCell", forIndexPath: indexPath) as! CameraCollectionViewCell;
        cell.mCameraImage.image = UIImage(named: "inactive_camera");
        return cell;
    }
}
