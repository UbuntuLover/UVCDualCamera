package com.serenegiant.usbcameratest7;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gjiazhe.multichoicescirclebutton.MultiChoicesCircleButton;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.widget.CameraViewInterface;
import com.serenegiant.widget.UVCCameraTextureView;

import java.util.ArrayList;
import java.util.List;

public class Main2Activity extends BaseActivity {

    //声明一些所用到的常量。
    private static final boolean DEBUG = true; //TODO set it false when release.
    private static final String TAG = "Main2Activity";
    //带宽因子.
    private static final float[] BAND_FACTORS = {0.5F, 0.5F};
    //访问USB和USB摄像头
    private USBMonitor mUSBMonitor;


    //左边控件
    private UVCCameraHandler mUVCCameraHandlerL;
    private CameraViewInterface mCameraViewL;

    //右边控件。
//    private UVCCameraHandler mUVCCameraHandlerR;
//    private CameraViewInterface mCameraViewR;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置不熄灭屏幕
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.my_own_layout);

        initCircleButton();



        mCameraViewL = (CameraViewInterface) findViewById(R.id.camera_view_by_Alex_L);
        mCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

        ((UVCCameraTextureView) mCameraViewL).setOnClickListener(mOnClickListener);
        //创建为左边服务的Handler。
        mUVCCameraHandlerL = UVCCameraHandler.createHandler(this, mCameraViewL, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BAND_FACTORS[0]);

//        mCameraViewR = (CameraViewInterface) findViewById(R.id.camera_view_by_Alex_R);
//        mCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);

//        ((UVCCameraTextureView) mCameraViewR).setOnClickListener(mOnClickListener);
//        //创建为右边服务的Handler
//        mUVCCameraHandlerR = UVCCameraHandler.createHandler(this, mCameraViewR, UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, BAND_FACTORS[1]);

        //实例化USBMonitor
        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);

    }


    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();

        if (mCameraViewL != null) mCameraViewL.onResume();
//        if (mCameraViewR != null) mCameraViewR.onResume();


    }


    /**
     * 销毁操作，释放资源，避免OOM。
     */
    @Override
    protected void onStop() {
        mUVCCameraHandlerL.close();
//        mUVCCameraHandlerR.close();
        if (mCameraViewL != null) mCameraViewL.onPause();
//        if (mCameraViewR != null) mCameraViewR.onPause();
        mUSBMonitor.unregister();
        Log.i(TAG, "onStop: ");
        super.onStop();
    }

    /**
     * 回收系统资源。
     */
    @Override
    protected void onDestroy() {
        if (mUVCCameraHandlerL != null) mUVCCameraHandlerL = null;
//        if (mUVCCameraHandlerR != null) mUVCCameraHandlerR = null;
        if (mUSBMonitor != null) mUSBMonitor = null;
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    /**
     * 初始化圆形按钮效果。
     */
    private void initCircleButton() {
        //左边的初始化
        MultiChoicesCircleButton.Item item1 = new MultiChoicesCircleButton.Item("拍照", getResources().getDrawable(R.drawable.shot), 30);
        MultiChoicesCircleButton.Item item2 = new MultiChoicesCircleButton.Item("录像", getResources().getDrawable(R.drawable.record_start), 150);
        List<MultiChoicesCircleButton.Item> buttonItems_L = new ArrayList<>();
        buttonItems_L.add(item1);
        buttonItems_L.add(item2);
        MultiChoicesCircleButton multiChoicesCircleButtonL = findViewById(R.id.circle_button_L);
        multiChoicesCircleButtonL.setButtonItems(buttonItems_L);
        multiChoicesCircleButtonL.setOnSelectedItemListener(new MultiChoicesCircleButton.OnSelectedItemListener() {
            @Override
            public void onSelected(MultiChoicesCircleButton.Item item, int index) {
                switch (index) {
                    case 0:
                        //左边拍照逻辑。
                        Toast.makeText(Main2Activity.this, item.getText() + "左边", Toast.LENGTH_SHORT).show();
                        if (mUVCCameraHandlerL.isOpened()) {
                            if (checkPermissionWriteExternalStorage()) {
                                mUVCCameraHandlerL.captureStill();
                            }
                        }
                        break;
                    //左边画框录像。
                    case 1:
                        Toast.makeText(Main2Activity.this, item.getText() + "左边", Toast.LENGTH_SHORT).show();
                        if (mUVCCameraHandlerL != null) {
                            if (mUVCCameraHandlerL.isOpened()) {
                                if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
                                    if (!mUVCCameraHandlerL.isRecording()) {
                                        mUVCCameraHandlerL.startRecording();
                                        showMsg("录像开始");
                                    } else {
                                        mUVCCameraHandlerL.stopRecording();
                                        showMsg("录像停止");
                                    }
                                }
                            }
                        }
                        break;
                }
            }
        });

        // 右边圆圈按钮初始化
//        MultiChoicesCircleButton.Item item3 = new MultiChoicesCircleButton.Item("拍照", getResources().getDrawable(R.drawable.shot), 30);
//        MultiChoicesCircleButton.Item item4 = new MultiChoicesCircleButton.Item("录像", getResources().getDrawable(R.drawable.record_start), 150);
//        List<MultiChoicesCircleButton.Item> buttonItems_R = new ArrayList<>();
//        buttonItems_R.add(item3);
//        buttonItems_R.add(item4);
//        MultiChoicesCircleButton multiChoicesCircleButtonR = findViewById(R.id.circle_button_R);
//        multiChoicesCircleButtonR.setButtonItems(buttonItems_R);
//        multiChoicesCircleButtonR.setOnSelectedItemListener(new MultiChoicesCircleButton.OnSelectedItemListener() {
//            @Override
//            public void onSelected(MultiChoicesCircleButton.Item item, int index) {
//                switch (index) {
//                    case 0:
//                        //右边拍照逻辑
//                        Toast.makeText(Main2Activity.this, item.getText(), Toast.LENGTH_SHORT).show();
//                        if (mUVCCameraHandlerR.isOpened()) {
//                            if (checkPermissionWriteExternalStorage()) {
//                                mUVCCameraHandlerR.captureStill();
//                            }
//                        }
//                        break;
//                    //右边录像逻辑。
//                    case 1:
//                        Toast.makeText(Main2Activity.this, item.getText(), Toast.LENGTH_SHORT).show();
//                        if (mUVCCameraHandlerR != null) {
//                            if (mUVCCameraHandlerR.isOpened()) {
//                                if (checkPermissionAudio() && checkPermissionWriteExternalStorage()) {
//                                    if (!mUVCCameraHandlerR.isRecording()) {
//                                        mUVCCameraHandlerR.startRecording();
//                                        showMsg("录像开始");
//                                    } else {
//                                        mUVCCameraHandlerR.stopRecording();
//                                        showMsg("录像停止");
//                                    }
//                                }
//                            }
//                        }
//                        break;
//                }
//            }
//        });
    }


    private void showMsg(String s) {
        Toast.makeText(Main2Activity.this, s, Toast.LENGTH_SHORT).show();
    }

    //点击的事件监听器
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {


                case R.id.camera_view_by_Alex_L:
                    //获取当前链接的USB设备。
                    List<DeviceFilter> deviceFilter = DeviceFilter.getDeviceFilters(Main2Activity.this, R.xml.device_filter);
                    List<UsbDevice> deviceList = mUSBMonitor.getDeviceList(deviceFilter);
                    int DEVICE_NUM = deviceList.size();
                    if (mUVCCameraHandlerL != null) {
                        if (!mUVCCameraHandlerL.isOpened()) {
                            if (DEVICE_NUM != 0) {
                                mUSBMonitor.requestPermission(deviceList.get(0));
                            }
                        } else {
                            mUVCCameraHandlerL.close();
                        }
                    }

                    break;
//                case R.id.camera_view_by_Alex_R:
//                    //获取当前链接的USB设备。
//                    List<DeviceFilter> deviceFilter1 = DeviceFilter.getDeviceFilters(Main2Activity.this, R.xml.device_filter);
//                    List<UsbDevice> deviceList1 = mUSBMonitor.getDeviceList(deviceFilter1);
//                    int DEVICE_NUM1 = deviceList1.size();

//                    if (mUVCCameraHandlerR != null) {
//                        if (!mUVCCameraHandlerR.isOpened()) {
//                            if (DEVICE_NUM1 == 2) {
//                                mUSBMonitor.requestPermission(deviceList1.get(1));
//                            }
//                        } else {
//                            mUVCCameraHandlerR.close();
//                        }
//                    }
//                    break;
            }

        }
    };

    //USB的状态接口回调事件的实现。
    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(UsbDevice device) {
            if (DEBUG) Log.d(TAG, "onAttach: " + device);
            Toast.makeText(Main2Activity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDettach(UsbDevice device) {
            if (DEBUG) Log.d(TAG, "onDettach: " + device);
            Toast.makeText(Main2Activity.this, "USB_DEVICE_DETTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
            if (!mUVCCameraHandlerL.isOpened()) {
                mUVCCameraHandlerL.open(ctrlBlock);
                final SurfaceTexture st = mCameraViewL.getSurfaceTexture();
                mUVCCameraHandlerL.startPreview(new Surface(st));
            }
//            if (!mUVCCameraHandlerR.isOpened()) {
//                mUVCCameraHandlerR.open(ctrlBlock);
//                final SurfaceTexture st = mCameraViewR.getSurfaceTexture();
//                mUVCCameraHandlerR.startPreview(new Surface(st));
//            }
        }

        @Override
        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
            if (DEBUG) Log.d(TAG, "onDisconnect: ");
            Toast.makeText(Main2Activity.this, "Disconnect", Toast.LENGTH_SHORT).show();
            if (mUVCCameraHandlerL != null && !mUVCCameraHandlerL.isEqual(device)) {
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mUVCCameraHandlerL.close();
                    }
                }, 0);
            }
//            else if (mUVCCameraHandlerR != null && !mUVCCameraHandlerR.isEqual(device)) {
//                queueEvent(new Runnable() {
//                    @Override
//                    public void run() {
//                        mUVCCameraHandlerR.close();
//                    }
//                }, 0);
//            }
        }

        @Override
        public void onCancel(UsbDevice device) {

        }
    };
}

