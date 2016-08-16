
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.blesensor;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.erabbit.bluetooth.BleDevice;
import net.erabbit.bluetooth.BleDeviceScanFragment;
import net.erabbit.bluetooth.BleDeviceScanHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity
        implements BleDeviceScanHandler.BleDeviceScanListener, AdapterView.OnItemClickListener {

    protected class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {
        int layout_res;

        DeviceAdapter(Context context, int resource, ArrayList<BluetoothDevice> devicesList) {
            super(context, resource, devicesList);
            layout_res = resource;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View cell = convertView;
            if(cell == null) {
                LayoutInflater inflater = getLayoutInflater();
                cell = inflater.inflate(layout_res, parent, false);
            }
            TextView nameLabel = (TextView)cell.findViewById(R.id.nameText);
            TextView addressLabel = (TextView)cell.findViewById(R.id.addrText);
            TextView signalLabel = (TextView)cell.findViewById(R.id.rssiText);
            BluetoothDevice device = getItem(position);
            nameLabel.setText(device.getName());
            addressLabel.setText(device.getAddress());
            if(deviceRSSIs.containsKey(device.getAddress()))
                signalLabel.setText(getString(R.string.rssi_format, deviceRSSIs.get(device.getAddress())));
            return cell;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, IoTSensorActivity.class);
        intent.putExtra("BluetoothDevice", devices.get(position));
        startActivity(intent);
    }

    protected ArrayList<BluetoothDevice> devices = new ArrayList<>();
    protected DeviceAdapter deviceAdapter;
    protected ListView deviceList;

    protected Map<String,Integer> deviceRSSIs = new TreeMap<>();

    protected DeviceScanFragment deviceScanFragment;

    protected ProgressDialog progressDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceList = (ListView)findViewById(R.id.deviceList);
        deviceAdapter = new DeviceAdapter(this, R.layout.device_list_row, devices);
        deviceList.setAdapter(deviceAdapter);
        deviceList.setOnItemClickListener(this);
        FragmentManager fm = getFragmentManager();
        deviceScanFragment = new DeviceScanFragment();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(deviceScanFragment, "DeviceScan");
        ft.commit();
    }

    @Override
    public void onStartScan() {
        devices.clear();
        deviceAdapter.notifyDataSetChanged();
        progressDlg = ProgressDialog.show(MainActivity.this,
                getString(R.string.scanning_title), //title
                getString(R.string.scanning_msg), //msg
                true, //indeterminate
                true, //cancelable
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        deviceScanFragment.stopScan();
                        progressDlg.dismiss();
                    }
                });
    }

    @Override
    public void onFoundDevice(Object device, int rssi) {
        devices.add((BluetoothDevice) device);
        deviceRSSIs.put(((BluetoothDevice)device).getAddress(), rssi);
        deviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScanTimeout() {
        progressDlg.dismiss();
    }

    @Override
    public void onScanRSSIUpdated(Object device, int rssi) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                    PackageManager pm = getPackageManager();
                    try {
                        PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
                        String info = String.format("%s\n%s\n%s:%s",
                                getString(R.string.copyright),
                                getString(R.string.vendor_name),
                                getString(R.string.version),
                                pi.versionName);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.app_name))
                                .setMessage(info)
                                .setPositiveButton(R.string.ok_btn, null)
                                .show();
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.i("OptionsItem", e.toString());
                    }
                }
                break;
            case R.id.action_scan:
                deviceScanFragment.tryScan();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}
