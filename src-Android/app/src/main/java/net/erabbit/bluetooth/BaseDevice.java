
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Tom on 2015/9/25.
 * class for peripheral of bluetooth le and classic
 */
public abstract class BaseDevice implements Serializable {

    public static int msgReady = 0;
    public static int msgConnectFailed = 0;
    public static int msgConnectEnded = 0;
    public static int msgReceivedData = 0;
    public static int msgValueChanged = 0;

    protected transient BluetoothDevice btDevice;

    public BluetoothDevice getBtDevice() {
        return btDevice;
    }

    public String getBtAddress() {
        return (btDevice != null) ? btDevice.getAddress() : "";
    }

    public String getBtName(String defaultName) {
        if(btDevice == null)
            return null;
        String btName = btDevice.getName();
        return (btName != null) ? btName : defaultName;
    }

    protected transient Handler msgHandler;

    public void setMsgHandler(Handler msgHandler) {
        this.msgHandler = msgHandler;
    }

    public BaseDevice() {}

    public BaseDevice(BluetoothDevice device) {
        btDevice = device;
    }

    public abstract void connect(Context context, Handler handler);
    public abstract void sendData(byte[] dataToSend);
    public abstract void disconnect();
    public abstract boolean isConnected();
    public abstract void startReceiveData();

    public void onConnect() {
        if((msgHandler != null) && (msgReady > 0))
            msgHandler.sendMessage(msgHandler.obtainMessage(msgReady, getBtAddress()));
    }

    public void onConnectFail() {
        if((msgHandler != null) && (msgConnectFailed > 0))
            msgHandler.sendMessage(msgHandler.obtainMessage(msgConnectFailed, getBtAddress()));
    }

    public void onConnectEnd() {
        if((msgHandler != null) && (msgConnectEnded > 0))
            msgHandler.sendMessage(msgHandler.obtainMessage(msgConnectEnded, getBtAddress()));
    }

    protected void onValueChange() {
        if(msgHandler != null)
            msgHandler.sendMessage(msgHandler.obtainMessage(msgValueChanged, getBtAddress()));
    }

    protected void onValueChange(int valueId, int valueParam) {
        if(msgHandler != null)
            msgHandler.sendMessage(msgHandler.obtainMessage(msgValueChanged, valueId, valueParam, getBtAddress()));
    }

    protected void onReceiveData(byte[] data) {
        if((msgHandler != null) && (msgReceivedData > 0))
            msgHandler.sendMessage(msgHandler.obtainMessage(msgReceivedData, data));
    }

    protected static UUID UUIDFromShort(String shortStr) {
        return UUID.fromString("0000" + shortStr + "-0000-1000-8000-00805f9b34fb");
    }
}
