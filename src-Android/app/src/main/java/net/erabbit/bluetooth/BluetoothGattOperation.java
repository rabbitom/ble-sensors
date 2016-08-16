
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import java.util.UUID;

import net.erabbit.common_lib.CoolUtility;

//import cn.cooltools.lifetrack.LifeTrackApp;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

public class BluetoothGattOperation {
	
	public static final int READ_CHARACTERISTIC = 1;
	public static final int WRITE_CHARACTERISTIC = 2;
	public static final int ENABLE_NOTIFICATION = 3;
	public static final int DISABLE_NOTIFICATION = 4;
	
	public final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	public int operation;
	public BluetoothGatt gatt;
	public BluetoothGattService service;
	public UUID characteristicUuid;
	public byte[] value;
	
	//构造函数
	public BluetoothGattOperation(
			int operation,
			BluetoothGatt gatt,
			BluetoothGattService service,
			UUID characteristicUuid,
			byte[] value) {
		this.operation = operation;
		this.gatt = gatt;
		this.service = service;
		this.characteristicUuid = characteristicUuid;
		this.value = value;
	}
	
	//执行操作
	public boolean Execute() {
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
		if(characteristic != null) {
			switch(operation)
			{
			case READ_CHARACTERISTIC:
				return gatt.readCharacteristic(characteristic);
			case WRITE_CHARACTERISTIC:
				Log.i("Ble", "write data: " + CoolUtility.MakeHexString(value));
		   		characteristic.setValue(value);
		   		return gatt.writeCharacteristic(characteristic);
			case ENABLE_NOTIFICATION:
			{
		   		gatt.setCharacteristicNotification(characteristic, true);
		   		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
		   		if(descriptor != null) {
			   		descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			   		return gatt.writeDescriptor(descriptor);
		   		}
			}
		   		break;
			case DISABLE_NOTIFICATION:
			{
		   		gatt.setCharacteristicNotification(characteristic, false);
		   		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
		   		if(descriptor != null) {
			   		descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
			   		return gatt.writeDescriptor(descriptor);
		   		}
			}
				break;
			}
		}
		Log.i("Ble", "operation not executed, type = " + operation);
		return false;
	}
}
