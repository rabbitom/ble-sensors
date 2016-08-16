
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by Tom on 2015/9/17.
 */
public class BleDeviceMsgHandler extends Handler {

    public static final int MSG_DEVICE_READY = 1;
    public static final int MSG_DEVICE_CONNECT_FAILED = 2;
    public static final int MSG_DEVICE_CONNECT_ENDED = 3;
    public static final int MSG_DEVICE_RECEIVED_DATA = 4;
    public static final int MSG_DEVICE_VALUE_CHANGED = 5;

    public interface BleDeviceMsgListener {
        public void onDeviceReady(String btAddress);
        public void onConnectFailed(String btAddress);
        public void onConnectEnded(String btAddress);
        public void onReceivedData(byte[] data);
        public void onValueChanged(String btAddress, int valueId, int valueParam);
    }

    protected final WeakReference<BleDeviceMsgListener> mActivity;

    public BleDeviceMsgHandler(BleDeviceMsgListener activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        BleDeviceMsgListener activity = mActivity.get();
        if(activity == null)
            return;
        switch(msg.what) {
            case MSG_DEVICE_READY:
                activity.onDeviceReady((String)msg.obj);
                break;
            case MSG_DEVICE_CONNECT_FAILED:
                activity.onConnectFailed((String)msg.obj);
                break;
            case MSG_DEVICE_CONNECT_ENDED:
                activity.onConnectEnded((String)msg.obj);
                break;
            case MSG_DEVICE_RECEIVED_DATA:
                activity.onReceivedData((byte[])msg.obj);
                break;
            case MSG_DEVICE_VALUE_CHANGED:
                activity.onValueChanged((String)msg.obj, msg.arg1, msg.arg2);
                break;
        }
    }
}
