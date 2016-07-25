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
