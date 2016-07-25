
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.blesensor;

import android.bluetooth.BluetoothDevice;

import net.erabbit.bluetooth.BleDeviceScanFragment;

/**
 * Created by Tom on 16/7/25.
 */
public class DeviceScanFragment extends BleDeviceScanFragment {

    @Override
    protected void onDeviceAdded(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if((device.getName() != null) && device.getName().startsWith("IoT-DK"))
            super.onDeviceAdded(device, rssi, scanRecord);
    }
}
