# YI Open API SDK

## Introduction

**YI Open API SDK** can be used by developer to develop applications using YI action camera.
Developer can use this SDK to connect to a YI action camera and control it to do operations like
start recording, stop recording, take picture, etc.

## Quick Start

Download our SDK library, add to your project as a dependent library. See below for a simple 
call flow.

1. Invoke `Platform.initialize()` to initialize the platform.
2. Create an object of `ActionCamera` and invoke `ActionCamera.connect()`.
3. Wait for `ActionCameraListener.onConnected()` callback.
4. Send command to camera and process kinds of camera notifications.
5. Invoke `ActionCamera.disconnect()`. to disconnect from the camera.
6. Wait for `ActionCameraListener.onClosed()` callback.
7. Invoke `Platform.uninitialize()` uninitialize the platform.

You can also check our samples to see how to use our SDK.
