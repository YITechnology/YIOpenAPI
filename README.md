# YI Open API

YI Open API provides mobile SDKs and reference designs for software developers and hardware makers to build cool apps and products with YI 4K Action Camera. 

This repository contains the libraries, samples and documentation for using the SDKs; it also contains the design diagrams, firmwares, references specs for building hardware systems.

The following is the tree structure of this repository:

* /sdk -- mobile SDKs for Android and iOS separately
* /sdk/bin -- firmware and config file for features provided in SDKs to work
* /sdk/java -- Android SDK
* /sdk/java/doc -- documentation
* /sdk/java/libs -- SDK library for building your applications
* /sdk/java/samples -- samples demonstrating the use of SDK APIs
* /sdk/swift -- iOS SDK
* /sdk/swift/doc -- documentation
* /sdk/swift/libs -- SDK library for building your applications
* /sdk/swift/samples -- samples demonstrating the use of SDK APIs
* /spec -- reference specs
* /vr -- documents, design diagrams, and firmwares for 6-camera VR rig
* /vr/bin -- firmwares
* /vr/design -- design diagrams (CAD)
* /vr/doc -- documents

## SDK Features

The SDKs provide APIs to support the following 4 areas

- camera control (start/stop recording video, capture photo, turn on/off viewfinder, etc)
- camera settings (datetime, video resolution, photo size, video standard, etc)
- camera state (record started/completed, video finder started etc)
- media management (browse files stored on media, download, delete file)

## Control VR/360/multi-camera system via Wi-Fi

If you have a VR/360 rig or a multi-cameras system, you can have the cameras connected to a Wi-Fi network or a hotspot, and then control the cameras from your application (built using the YI Open API SDK), by following the steps below: 

1. update firmware (/sdk/bin/{country code}/firmware.bin)
   * copy firmware.bin onto a microSD card
   * start the camera with the microSD card
   * wait until the sound of starting music

2. setup config file (/sdk/bin/{country code}/sta.conf)
   * provide correct SSID and password for accessing the Wi-Fi network or the hotspot
   * give each camera a different device name
   * set the correct country code (e.g. CN for China, US for United States etc)
   * save the changes to sta.conf and copy it onto the microSD card
   * start the camera with the microSD card

3. turn on Wi-Fi on camera
   * go to camera settings -> Wi-Fi
   * pick the Wi-Fi frequency matching to the one broadcasting from your Wi-Fi network or hotspot
   * turn on Wi-Fi (note that the ON button is green)
    
4. tryout YI360Demo sample (/sdk/{java, swift}/samples/YI360Demo)
   * build and launch the sample app

## Live video

Interested in doing live video in your application using your YI 4K Action Camera? Try out our API for live video support. This is how it works:

1. update firmware (see above for details).

2. call buildLiveVideoQRCode to generate an binary array and save it as an image. the information needed would include ssid, password, resolution, bitrate, duration, rtmpurl, and the size of the QR code image in pixels. 

3. start your YI 4K Action Camera and choose Live mode (at the bottom of the shooting mode selection screen), you will be prompted to provide the QR code to scan.

4. scan the QR code image using the camera. few seconds later, your live video stream from the camera will be pushed to the rtmp url that you provided.

## Build your VR camera

YI 4K action cameras are perfect building blocks for VR camera. Its video capturing/encoding spec, image quality, battery life and geometry are all great for VR camera. That's why Google chose to work with YI to use YI 4K Action Camera to build their next version of JUMP VR camera.

Now we have a reference design for you as well. And the following is all what you need:

1. introduction. please refer to the doc below: 

   https://github.com/YITechnology/YIOpenAPI/blob/master/vr/doc/Build%20hardware%20synchronized%20360%20VR%20camera%20with%20YI%204K%20action%20cameras.pdf 

2. design. please find the CAD design diagrams here:

   https://github.com/YITechnology/YIOpenAPI/tree/master/vr/design

3. firmwares. to be able to control the 6 cameras on the rig and get them working together using a controller camera, you need update the cameras with the firmwares found here:

   https://github.com/YITechnology/YIOpenAPI/tree/master/vr/bin

4. synchronization cable. for the cable that could be used for doing hardware synchronization of all cameras on the rig, please find its reference spec here:

   https://github.com/YITechnology/YIOpenAPI/blob/master/spec/Multi-endpoint%20Micro%20USB%20Cable.pdf

## Join the YI Open API community

* Website: https://www.facebook.com/groups/YIOpenAPI/

## Signup for YI Open API email updates and news

* Website: http://www.yiopen.com/

## License

The YI Open API SDKs are licensed as described in LICENSE. To download and use YI Open API SDKs, you hearby agree YI Technologies, Inc. End User License Agreement (EULA) as described in EULA.
