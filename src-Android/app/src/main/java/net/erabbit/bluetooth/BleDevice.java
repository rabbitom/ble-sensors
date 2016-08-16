
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import net.erabbit.common_lib.CoolUtility;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BleDevice extends BaseDevice implements Serializable {
	
    protected static BluetoothManager bluetoothManager;
    public static BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }
	public static void setBluetoothManager(BluetoothManager bluetoothManager) {
		BleDevice.bluetoothManager = bluetoothManager;
	}

	protected transient BluetoothGatt btGatt;
	protected transient BluetoothGattService btService;

	public BleDevice(BluetoothDevice device) {
		super(device);
	}

	public void sendData(byte[] dataToSend) {
		if((btGatt != null) && (btService != null) && (UUID_MAIN_CONFIG != null))
			WriteCharacteristicValue(btGatt, btService, UUID_MAIN_CONFIG, dataToSend);
	}
	
	//连接
	public void connect(Context context, Handler handler) {
		msgHandler = handler;
		if(btDevice != null) {
			Log.i("Ble", "connect device: " + getBtAddress());
			if (btGatt == null) {
				if(mGattCallback == null)
					mGattCallback = getGattCallback();
				btGatt = btDevice.connectGatt(context, false, mGattCallback);
			}
			else
				btGatt.connect();
		}
	}
	
	//清空GATT缓存
	//http://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android
	private boolean refreshDeviceCache(BluetoothGatt gatt){
	    try {
	        Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
	        if (localMethod != null) {
	        	boolean bool = (Boolean) localMethod.invoke(gatt, new Object[0]);
	            Log.i("Ble", "refresh gatt cache " + (bool ? "succeed" : "failed"));
	        	return bool;
	        }
	    } 
	    catch (Exception localException) {
	        Log.e("Ble", "An exception occured while refreshing device");
	    }
	    return false;
	}
	
	public void disconnect() {
		if(btGatt != null)
			btGatt.disconnect();
	}

	public boolean isConnected() {
		return (btDevice != null) && (bluetoothManager.getConnectionState(btDevice, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED);
	}
	
   	//开始接收数据
	public void startReceiveData() {
		if((btGatt != null) && (btService != null) && (UUID_MAIN_DATA != null))
	   		EnableNotification(btGatt, btService, UUID_MAIN_DATA);
	}

	public static final int VALUE_OF_RSSI = -1;

	public void readRSSI() {
		if(btGatt != null)
			btGatt.readRemoteRssi();
	}

	//服务和特性UUID
	protected UUID
		UUID_MAIN_SERVICE, 
		UUID_MAIN_CONFIG, 
		UUID_MAIN_DATA;

	protected void loadUUIDs() {}

	//GATT操作
	private transient Queue<BluetoothGattOperation> gattOperationQueue;

	private void addOperation(BluetoothGattOperation operation) {
        if(gattOperationQueue == null)
            gattOperationQueue = new LinkedList<>();
        gattOperationQueue.add(operation);
		if(gattOperationQueue.size() == 1) {
			boolean executeResult = operation.Execute();
			if(!executeResult)
				gattOperationQueue.remove();
		}
	}
	
	private void executeNextOperation() {
		gattOperationQueue.remove();
        if(gattOperationQueue.size() > 0) {
        	boolean executeResult = gattOperationQueue.element().Execute();
        	if(!executeResult)
        		executeNextOperation();
        }
	}
	
	//读特性
	protected void ReadCharacteristic(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid) {
		BluetoothGattOperation operation = new BluetoothGattOperation(
				BluetoothGattOperation.READ_CHARACTERISTIC,
				gatt,
				service,
				charaUuid,
				null);
		addOperation(operation);
	}
	
	//写特性
	protected void WriteCharacteristicValue(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid, byte[] value) {
		BluetoothGattOperation operation = new BluetoothGattOperation(
				BluetoothGattOperation.WRITE_CHARACTERISTIC,
				gatt,
				service,
				charaUuid,
				value);
		addOperation(operation);
	}
	
	//使能通知
	protected void EnableNotification(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid) {
		BluetoothGattOperation operation = new BluetoothGattOperation(
				BluetoothGattOperation.ENABLE_NOTIFICATION,
				gatt,
				service,
				charaUuid,
				null);
		addOperation(operation);
	}
	
	//禁止通知
	protected void DisableNotification(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid) {
		BluetoothGattOperation operation = new BluetoothGattOperation(
				BluetoothGattOperation.DISABLE_NOTIFICATION,
				gatt,
				service,
				charaUuid,
				null);
		addOperation(operation);
	}
	
	//开始接收通知
	private void StartReceiveNotification(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid) {
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(charaUuid);
		gatt.setCharacteristicNotification(characteristic, true);
	}
	
	//停止接收通知
	private void StopReceiveNotification(BluetoothGatt gatt, BluetoothGattService service, UUID charaUuid) {
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(charaUuid);
		gatt.setCharacteristicNotification(characteristic, false);
	}
	
    //GATT回调函数
    private transient BluetoothGattCallback mGattCallback;

	private BluetoothGattCallback getGattCallback() {
		return new BluetoothGattCallback() {
			//连接状态改变
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					Log.i("Ble", "connected to device");
					//refreshDeviceCache(gatt);
					if(gatt.getServices().size() == 0)
						gatt.discoverServices();
					else {
						Log.i("Ble", "device already has services, skip discover");
						onConnect();
					}
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					Log.i("Ble", "device disconnected");
					onConnectEnd();
				}
			}

			//服务发现
			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					Log.i("Ble", "services discovered");
					if (UUID_MAIN_SERVICE == null)
						loadUUIDs();
					if (UUID_MAIN_SERVICE == null)
						onConnect();
					else {
						btService = gatt.getService(UUID_MAIN_SERVICE);
						if (btService != null) {
							List<BluetoothGattCharacteristic> characteristics = btService.getCharacteristics();
							for (BluetoothGattCharacteristic characteristic : characteristics) {
								List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
								Log.i("Ble", "Characteristic UUID=" + characteristic.getUuid() + " properties=" + characteristic.getProperties() + " permissions=" + BleUtils.getCharPermission(characteristic.getPermissions()) + " descriptorsCount=" + descriptors.size());
								for (BluetoothGattDescriptor descriptor : descriptors) {
									byte[] value = descriptor.getValue();
									Log.i("Ble", "Descriptor UUID=" + descriptor.getUuid() + " cachedValue=" + ((value != null) ? CoolUtility.MakeHexString(value) : "null"));
								}
							}
							//初始化
							onConnect();
						} else {
							Log.i("Ble", "no required service found");
							disconnect();
							onConnectFail();
						}
					}
				} else {
					Log.i("Ble", "discover services failed");
					disconnect();
					onConnectFail();
				}
			}

			//读特性操作完成
			@Override
			public void onCharacteristicRead(BluetoothGatt gatt,
											 BluetoothGattCharacteristic characteristic,
											 int status) {
				if(status == BluetoothGatt.GATT_SUCCESS)
					onReceiveCharacteristicValue(characteristic);
				executeNextOperation();
			}

			//写特性操作完成
			@Override
			public void onCharacteristicWrite(BluetoothGatt gatt,
											  BluetoothGattCharacteristic characteristic,
											  int status) {
				executeNextOperation();
			}

			//写描述符操作完成
			@Override
			public void onDescriptorWrite(BluetoothGatt gatt,
										  BluetoothGattDescriptor descriptor,
										  int status) {
				executeNextOperation();
			}

			//接收特性通知
			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt,
												BluetoothGattCharacteristic characteristic) {
				onReceiveCharacteristicValue(characteristic);
			}

			@Override
			public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
				//super.onReadRemoteRssi(gatt, rssi, status);
				if(status == BluetoothGatt.GATT_SUCCESS)
					onValueChange(VALUE_OF_RSSI, rssi);
			}
		};
	}

	protected void onReceiveCharacteristicValue(BluetoothGattCharacteristic characteristic) {
		UUID uuid = characteristic.getUuid();
		byte[] data = characteristic.getValue();
		if(data != null) {
			if((UUID_MAIN_DATA != null) && uuid.equals(UUID_MAIN_DATA)) {
				Log.i("Ble", "received data: " + CoolUtility.MakeHexString(data));
				onReceiveData(data);
			}
			else
				onReceiveData(uuid, data);
		}
	}

	protected void onReceiveData(UUID uuid, byte[] data) {
		Log.i("Ble", "received data for " + uuid.toString() + ": " + CoolUtility.MakeHexString(data));
	}
}
