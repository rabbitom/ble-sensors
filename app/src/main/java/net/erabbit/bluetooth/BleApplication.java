
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Tom on 2015/9/17.
 */
public class BleApplication extends Application {

    protected Object curDevice;

    public Object getCurDevice() {
        return curDevice;
    }

    public void setCurDevice(Object curDevice) {
        this.curDevice = curDevice;
    }

    protected ArrayList<BleDevice> devices;

    public int getDeviceCount() {
        return (devices == null) ? 0 : devices.size();
    }

    public ArrayList<BleDevice> getDevices() {
        if(devices == null)
            devices = new ArrayList<>();
        return devices;
    }

    public BleDevice findDevice(String address) {
        if(devices != null) {
            for(BleDevice device : devices) {
                if(device.getBtAddress().equals(address))
                    return device;
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BleDevice.setBluetoothManager(bluetoothManager);

        BaseDevice.msgReady = BleDeviceMsgHandler.MSG_DEVICE_READY;
        BaseDevice.msgConnectFailed = BleDeviceMsgHandler.MSG_DEVICE_CONNECT_FAILED;
        BaseDevice.msgConnectEnded = BleDeviceMsgHandler.MSG_DEVICE_CONNECT_ENDED;
        BaseDevice.msgReceivedData = BleDeviceMsgHandler.MSG_DEVICE_RECEIVED_DATA;
        BaseDevice.msgValueChanged = BleDeviceMsgHandler.MSG_DEVICE_VALUE_CHANGED;
    }

    protected static final String deviceListFile = "devices";

    @SuppressWarnings("unchecked")
    public void loadDeviceList() {
        Object deviceList = loadObject(deviceListFile);
        if(deviceList != null)
            devices = (ArrayList<BleDevice>)deviceList;
        else
            devices = new ArrayList<>();
    }

    public void saveDeviceList() {
        archiveObject(devices, deviceListFile);
    }

    public Object loadObject(String filename) {
        Object object = null;
        try {
            InputStream is = openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(is);
            object = ois.readObject();
            ois.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public void archiveObject(Serializable object, String fileName) {
        if(object != null) {
            try {
                OutputStream os = openFileOutput(fileName, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(object);
                oos.flush();
                oos.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
