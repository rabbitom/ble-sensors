
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.app.Fragment;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleDeviceScanFragment extends Fragment
implements BluetoothAdapter.LeScanCallback {

    protected BluetoothAdapter mBluetoothAdapter;
    private boolean isScanning = false;
    public boolean isScanning() {
        return isScanning;
    }
    protected ArrayList<BluetoothDevice> devices = new ArrayList<>();
    public ArrayList<BluetoothDevice> getDevices() {
        return devices;
    }

    protected static final int REQUEST_BT_ENABLE = 1;

    protected int scanTime = 5000;
    public void setScanTime(int scanTime) {
        this.scanTime = scanTime;
    }

    //扫描设备的回调函数
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //扫描到设备以后的动作
        if(!devices.contains(device)) {
            devices.add(device);
            Log.i(getClass().getName(), String.format("onFoundDevice, name = %s, address = %s", device.getName(), device.getAddress()));
            onDeviceAdded(device, rssi, scanRecord);
        }
    }

    protected void onDeviceAdded(BluetoothDevice device, int rssi, byte[] scanRecord) {
        deviceHandler.sendMessage(deviceHandler.obtainMessage(BleDeviceScanHandler.MSG_FOUND_DEVICE, rssi, 0, device));
    }

    protected BleDeviceScanHandler deviceHandler;

    public BleDeviceScanFragment() {
        // Required empty public constructor
    }

    public void tryScan() {
        Activity activity = getActivity();
        if(activity == null)
            return;
        if(deviceHandler == null)
            deviceHandler = new BleDeviceScanHandler((BleDeviceScanHandler.BleDeviceScanListener)activity);
        if(mBluetoothAdapter == null) {
            //初始化蓝牙适配器
            BluetoothManager bluetoothManager = BleDevice.getBluetoothManager();
            if(bluetoothManager == null) {
                bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
                BleDevice.setBluetoothManager(bluetoothManager);
            }
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter != null) {
            //检查蓝牙是否已打开
            if (!mBluetoothAdapter.isEnabled()) {
                //显示对话框要求用户启用蓝牙
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
            } else
                startScan();
        }
    }

    protected void startScan() {
        //数据初值
        devices.clear();
        //开始搜索
        isScanning = mBluetoothAdapter.startLeScan(this);
        if(isScanning) {
            deviceHandler.sendEmptyMessage(BleDeviceScanHandler.MSG_START_SCAN);
            if(scanTime > 0) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    public void run() {
                        if (isScanning) {
                            stopScan();
                            deviceHandler.sendEmptyMessage(BleDeviceScanHandler.MSG_SCAN_TIMEOUT);
                        }
                    }
                }, scanTime);
            }
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if((requestCode == REQUEST_BT_ENABLE) && (resultCode != Activity.RESULT_CANCELED))
            startScan();
    }

    public void stopScan() {
        isScanning = false;
        mBluetoothAdapter.stopLeScan(this);
    }
}
