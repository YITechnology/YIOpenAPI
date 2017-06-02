# YI VR Camera Firmware

There are 2 firmmare files for the cameras on YI VR Camera rig:

* firmware_first.bin - for the 1 controller camera
* firmware_others.bin - for the 6 recording cameras

~~If you need to need to repurpose anyone of the cameras on the rig:~~

~~* firmware.bin - an intermediate version to update the camera before switching to any other version~~

1-18-2017 v1.0.177 Release notes

Added sync button on master camera, which would allow sync the following settings to all the slave cameras:

1. reset to factory settings
2. format sd card
3. mode switch between video/photo/time lapse video
4. IQ setting

6-1-2017 v1.0.179 Release notes

Add the following supports:

1. mp4 file recovery
2. automation script support (v6_auto.txt)
3. U-disk mode support