
// Copyright (c) 2016 Jianlin Hao. All rights reserved.
// Licensed under the Apache License Version 2.0. See LICENSE file in the project root for full license information.
// https://github.com/rabbitom

package net.erabbit.common_lib;

import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class CoolUtility {

    public static String MakeHexString(byte[] buffer)
    {
        return MakeHexString(buffer, 0, buffer.length, " ");
    }

	public static String MakeHexString(byte[] buffer, int start, int len)
	{
        return MakeHexString(buffer, start, len, " ");
	}
	
	public static String MakeHexString(byte[] buffer, int start, int len, String gap)
	{
		String hexStr = "";
		for(int i=0; i<len; i++)
			hexStr = hexStr.concat(String.format("%1$02X", buffer[start+i]) + gap);
		return hexStr;
	}
	
	public static byte[] fromHexString(String hexStr) {
		ArrayList<Integer> ints = new ArrayList<Integer>();
		char[] hexChars = hexStr.toCharArray();
		int hValue = -1;
		for(int i=0; i<hexChars.length; i++) {
			int value = fromHexChar(hexChars[i]);
			if(hValue == -1)
				hValue = value;
			else if(value != -1) {
				ints.add(hValue * 16 + value);
				hValue = -1;
			}
		}
		int intCount = ints.size();
		if(intCount > 0) {
			byte[] bytes = new byte[intCount];
			for(int i=0; i<intCount; i++)
				bytes[i] = toByte(ints.get(i));
			return bytes;
		}
		else
			return null;
	}
	
	protected static int fromHexChar(char ch) {
		if((ch >= '0') && (ch <= '9'))
			return ch - '0';
		else if((ch >= 'a') && (ch <= 'f'))
			return ch - 'a' + 10;
		else if((ch >= 'A') && (ch <= 'F'))
			return ch - 'A' + 10;
		else
			return -1;
	}
	
	public static byte toByte(int x) {
		return (byte)(x & 0x000000ff);
	}

	public static int toInt(byte b) {
		return 0x000000ff & b;
	}
	
	public static int toIntLE(byte lByte, byte hByte) {
		return ((lByte & 0x000000ff) | (hByte << 8) & 0x0000ff00);
	}

    public static int toIntBE(byte hByte, byte lByte) {
        return ((lByte & 0x000000ff) | (hByte << 8) & 0x0000ff00);
    }
	
    public static int toIntLE(byte[] bytes, int offset, int len) {
        int value = 0;
        for(int i=len-1; i>=0; i--) {//低位在前
            value = value << 8;
            value = value | toInt(bytes[offset+i]);
        }
        return value;
    }

    public static int toIntBE(byte[] bytes, int offset, int len) {
        int value = 0;
        for(int i=0; i<len; i++) {
            value = value << 8;//高位在前
            value = value | toInt(bytes[offset+i]);
        }
        return value;
    }

	public static long toLongLE(byte[] bytes, int offset, int len) {
		long value = 0;
		for(int i=len-1; i>=0; i--) {
			value = value << 8;
			value = value | toInt(bytes[offset+i]);
		}
		return value;
	}

	public static UUID toUUID(byte[] bytes, int offset) {
		if (offset+16 <= bytes.length) {
			long lowerHalf = CoolUtility.toLongLE(bytes, offset, 8);
			long higherHalf = CoolUtility.toLongLE(bytes, offset + 8, 8);
			return new UUID(higherHalf, lowerHalf);
		}
		return null;
	}
/*
    public static int[] toIntArrayLE(byte[] bytes, int offset, int len, int count) {
        int[] array = new int[count];
        for(int i=0; i<count; i++) {
            array[i] = CoolUtility.toIntLE(bytes, offset, len);
            offset += len;
        }
        return array;
    }
*/

}
