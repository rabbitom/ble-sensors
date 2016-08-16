
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import net.erabbit.common_lib.CoolUtility;

import java.io.Serializable;

/**
 * Created by Tom on 16/3/28.
 */
public class BeaconData implements Parcelable, Serializable {
    public BeaconData() {
        //required
    }

    protected BeaconData(Parcel in) {
        uuid = in.createByteArray();
        major = in.readInt();
        minor = in.readInt();
        txPower = in.readInt();
    }

    public static final Creator<BeaconData> CREATOR = new Creator<BeaconData>() {
        @Override
        public BeaconData createFromParcel(Parcel in) {
            return new BeaconData(in);
        }

        @Override
        public BeaconData[] newArray(int size) {
            return new BeaconData[size];
        }
    };

    public byte[] getUuid() {
        if(uuid == null)
            uuid = new byte[16];
        return uuid;
    }

    public void setUuid(byte[] uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    protected byte[] uuid;
    protected int major;
    protected int minor;
    protected int txPower;

    public double getDistance(int rssi) {
        double ratio_db = txPower - rssi;
        double ratio_linear = Math.pow(10, ratio_db / 10);
        return Math.sqrt(ratio_linear);
    }

    public static BeaconData parseFormScanRecord(byte[] scanRecord) {
        int offset = 0;
        while(scanRecord.length > offset) {
            int length = CoolUtility.toInt(scanRecord[offset]);
            if(length > 0) {
                int key = CoolUtility.toInt(scanRecord[offset + 1]);
                if ((length == 0x1A) && (key == 0xFF))
                    return BeaconData.parseFromValueBytes(scanRecord, offset+2);
                offset += (length + 1);
            }
            else
                break;
        }
        return null;
    }

    public static BeaconData parseFromValueBytes(byte[] bytes, int offset) {
        if(offset + 25 >= bytes.length) {
            BeaconData beaconData = new BeaconData();
            System.arraycopy(bytes, offset + 4, beaconData.getUuid(), 0, 16);
            beaconData.setMajor(CoolUtility.toIntBE(bytes, offset + 20, 2));
            beaconData.setMinor(CoolUtility.toIntBE(bytes, offset + 22, 2));
            beaconData.setTxPower(bytes[offset + 24]);
            return beaconData;
        }
        else
            return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(uuid);
        dest.writeInt(major);
        dest.writeInt(minor);
        dest.writeInt(txPower);
    }
}
