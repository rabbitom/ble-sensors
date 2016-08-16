
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by Tom on 2015/9/17.
 */
public class BleDeviceScanHandler extends Handler {

    public static final int MSG_START_SCAN = 1;
    public static final int MSG_FOUND_DEVICE = 2;
    public static final int MSG_SCAN_TIMEOUT = 3;
    public static final int MSG_SCAN_RSSI_UPDATED = 4;

    public interface BleDeviceScanListener {
        void onStartScan();
        void onFoundDevice(Object device, int rssi);
        void onScanTimeout();
        void onScanRSSIUpdated(Object device, int rssi);
    }

    protected final WeakReference<BleDeviceScanListener> mActivity;

    public BleDeviceScanHandler(BleDeviceScanListener activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        BleDeviceScanListener activity = mActivity.get();
        if(activity == null)
            return;
        switch(msg.what) {
            case MSG_START_SCAN:
                activity.onStartScan();
                break;
            case MSG_FOUND_DEVICE:
                activity.onFoundDevice(msg.obj, msg.arg1);
                break;
            case MSG_SCAN_TIMEOUT:
                activity.onScanTimeout();
                break;
            case MSG_SCAN_RSSI_UPDATED:
                activity.onScanRSSIUpdated(msg.obj, msg.arg1);
                break;
        }
    }
}
