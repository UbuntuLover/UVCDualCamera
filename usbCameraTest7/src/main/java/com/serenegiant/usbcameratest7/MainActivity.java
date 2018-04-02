/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.usbcameratest7;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.telephony.CellInfoLte;
import android.telephony.cdma.CdmaCellLocation;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.sql.SQLOutput;
import java.util.List;

/**
 * Show side by side view from two camera.
 * You cane record video images from both camera, but secondarily started recording can not record
 * audio because of limitation of Android AudioRecord(only one instance of AudioRecord is available
 * on the device) now.
 */



public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
    private static final boolean DEBUG = true;    // FIXME set false when production
    private static final String TAG = "MainActivity";
    //带宽因子
    private static final float[] BANDWIDTH_FACTORS = {1.0f, 1.0f};

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;

    private Button shot_R;
    private UVCCameraHandler mHandlerR;
    private CameraViewInterface mUVCCameraViewR;
    private ImageButton mCaptureButtonR;
    private Surface mRightPreviewSurface;

    private Button shot_L;
    private UVCCameraHandler mHandlerL;
    private CameraViewInterface mUVCCameraViewL;
    private ImageButton mCaptureButtonL;
    private Surface mLeftPreviewSurface;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //此行代码与应用无关。
//		findViewById(R.id.RelativeLayout1).setOnClickListener(mOnClickListener);
        mUVCCameraViewL = (CameraViewInterface) findViewById(R.id.camera_view_L);
        mUVCCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        //左边画框相关组件。
        /**
         * 所使用的相关组件有：
         * 1.UVCCameraTextureView
         * 2.ImageButton
         * 3.对应的Handler
         * */
        ((UVCCameraTextureView) mUVCCameraViewL).setOnClickListener(mOnClickListener);
        mCaptureButtonL = (ImageButton) findViewById(R.id.capture_button_L);
        mCaptureButtonL.setOnClickListener(mOnClickListener);
//        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mHandlerL = UVCCameraHandler.createHandler(this, mUVCCameraViewL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[0]);
        shot_L = findViewById(R.id.shot_L);
        shot_L.setOnClickListener(mOnClickListener);


        mUVCCameraViewR = (CameraViewInterface) findViewById(R.id.camera_view_R);
        mUVCCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
        ((UVCCameraTextureView) mUVCCameraViewR).setOnClickListener(mOnClickListener);
        mCaptureButtonR = (ImageButton) findViewById(R.id.capture_button_R);
        mCaptureButtonR.setOnClickListener(mOnClickListener);
//        mCaptureButtonR.setVisibility(View.INVISIBLE);
        mHandlerR = UVCCameraHandler.createHandler(this, mUVCCameraViewR, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BANDWIDTH_FACTORS[1]);
        shot_R = findViewById(R.id.shot_R);
        shot_R.setOnClickListener(mOnClickListener);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);


    }


    @Override
    protected void onStart() {
        super.onStart();
        //注册相关的广播接收器
        mUSBMonitor.register();
        if (mUVCCameraViewR != null) mUVCCameraViewR.onResume();
        if (mUVCCameraViewL != null) mUVCCameraViewL.onResume();


    }

    @Override
    protected void onStop() {
        mHandlerR.close();
        if (mUVCCameraViewR != null) mUVCCameraViewR.onPause();
        mHandlerL.close();
        mCaptureButtonR.setVisibility(View.INVISIBLE);
        if (mUVCCameraViewL != null) mUVCCameraViewL.onPause();
        mCaptureButtonL.setVisibility(View.INVISIBLE);
        mUSBMonitor.unregister();
        Log.i(TAG, "onStop: ");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        if (mHandlerR != null) {
            mHandlerR = null;
        }
        if (mHandlerL != null) {
            mHandlerL = null;
        }
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
        mUVCCameraViewR = null;
        mCaptureButtonR = null;
        mUVCCameraViewL = null;
        mCaptureButtonL = null;
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(final View view) {
            switch (view.getId()) {
                case R.id.camera_view_L:
                    if (mHandlerL != null) {
                        if (!mHandlerL.isOpened()) {

                            List<UsbDevice> deviceList = ConnectNum();
                            int DEVICE_AMOUNT = deviceList.size();
                            showToast(DEVICE_AMOUNT + "ge");
                            if (DEVICE_AMOUNT != 0) {
                                boolean permissionL = mUSBMonitor.hasPermission(deviceList.get(0));
//                            showToast(permissionL+"PermissionL");
                                if (!permissionL) {
                                    mUSBMonitor.requestPermission(deviceList.get(0));
                                }
                                mUSBMonitor.processConnect(deviceList.get(0));
                            }
//					CameraDialog.showDialog(MainActivity.this);
                        } else {
                            mHandlerL.close();
//					setCameraButton();
                        }
                    }
                    break;
                case R.id.capture_button_L:
                    if (mHandlerL != null) {
                        if (mHandlerL.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerL.isRecording()) {
                                    mCaptureButtonL.setColorFilter(0xffff0000);    // turn red
                                    mHandlerL.startRecording();
                                } else {
                                    mCaptureButtonL.setColorFilter(0);    // return to default color
                                    mHandlerL.stopRecording();
                                }
                            }
                        }
                    }
                    break;
                case R.id.shot_L:
                    //TODO
                    /**
                     * 此处是拍照业务逻辑处理地方。
                     * */
                    showToast("你点击了左边的拍照按钮");
                    if (mHandlerL.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mHandlerL.captureStill();
                            showToast("左边拍照完成！");
                        }
                    }
                    break;


                case R.id.camera_view_R:

                    if (mHandlerR != null) {
                        if (!mHandlerR.isOpened()) {
                            //屏蔽对话框。
//						CameraDialog.showDialog(MainActivity.this);
                            List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(MainActivity.this, R.xml.device_filter);
                            List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(filters.get(0));
                            int DEVICE_AMOUNT = deviceList.size();

                            if (DEVICE_AMOUNT > 0) {
                                if (DEVICE_AMOUNT > 1 && DEVICE_AMOUNT < 3)
                                    mUSBMonitor.requestPermission(deviceList.get(1));
                            } else {
                                mHandlerR.close();
//						setCameraButton();
                            }
                        }
                    }
                    break;
                case R.id.capture_button_R:
                    if (mHandlerR != null) {
                        if (mHandlerR.isOpened()) {
                            if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                if (!mHandlerR.isRecording()) {
                                    mCaptureButtonR.setColorFilter(0xffff0000);    // turn red
                                    showToast("右边边画框宽，高" + String.valueOf(mHandlerR.getWidth()) + "," + String.valueOf(mHandlerR.getHeight()));
                                    mHandlerR.startRecording();
                                } else {
                                    mCaptureButtonR.setColorFilter(0);    // return to default color
                                    mHandlerR.stopRecording();
                                }
                            }
                        }
                    }
                    break;
                /**
                 * 右边CameraView拍照。
                 * */
                case R.id.shot_R:
                    showToast("你点击了右边的拍照按钮。");

                    if (mHandlerR.isOpened()) {
                        if (checkPermissionWriteExternalStorage()) {
                            mHandlerR.captureStill();
                            showToast("拍照成功右边！");
                        }
                    }
                    break;
            }
        }
    };

    private final OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "onAttach: 来源于MainActivity");

        }

        //接收到USB 事件的广播之后再USBMonitor的processConnect里通过Handler尝试建立与USB 摄像头的连接，建立连接之后会由Monitor去触发onConnect
        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            Log.i(TAG, "onConnect:来源于" + TAG);
            if (DEBUG) Log.v(TAG, "onConnect:" + device);
            if (!mHandlerL.isOpened()) {
                mHandlerL.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewL.getSurfaceTexture();

                mHandlerL.startPreview(new Surface(st));
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						mCaptureButtonL.setVisibility(View.VISIBLE);
//					}
//				});
            }
            if (!mHandlerR.isOpened()) {
                mHandlerR.open(ctrlBlock);
                final SurfaceTexture st = mUVCCameraViewR.getSurfaceTexture();
                mHandlerR.startPreview(new Surface(st));
//				runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						mCaptureButtonR.setVisibility(View.VISIBLE);
//					}
//				});
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.v(TAG, "onDisconnect:" + device);
            Toast.makeText(MainActivity.this, "Disconn", Toast.LENGTH_SHORT).show();
            if ((mHandlerL != null) && !mHandlerL.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerL.close();
                        if (mLeftPreviewSurface != null) {
                            mLeftPreviewSurface.release();
                            mLeftPreviewSurface = null;
                        }
//						setCameraButton();
                    }
                }, 0);
            } else if ((mHandlerR != null) && !mHandlerR.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mHandlerR.close();
                        if (mRightPreviewSurface != null) {
                            mRightPreviewSurface.release();
                            mRightPreviewSurface = null;
                        }
//						setCameraButton();
                    }
                }, 0);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onDettach:" + device);
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onCancel:");
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//					setCameraButton();
                }
            }, 0);
        }
    }

//	private void setCameraButton() {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if ((mHandlerL != null) && !mHandlerL.isOpened() && (mCaptureButtonL != null)) {
//					mCaptureButtonL.setVisibility(View.INVISIBLE);
//				}
//				if ((mHandlerR != null) && !mHandlerR.isOpened() && (mCaptureButtonR != null)) {
//					mCaptureButtonR.setVisibility(View.INVISIBLE);
//				}
//			}
//		}, 0);
//	}


    //工具方法，展示Toast所用。
    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    //工具方法，检测链接设备.
    private List<UsbDevice> ConnectNum() {
        List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(MainActivity.this, R.xml.device_filter);
        List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(deviceFilters);

        return deviceList;
    }


}
