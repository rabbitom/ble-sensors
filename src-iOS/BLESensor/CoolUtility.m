//
//  CoolUtility.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/25.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <Foundation/Foundation.h>

int toIntLE(Byte* bytes, int offset, int length) {
    int value = 0;
    for(int i=length-1; i>=0; i--) {//低位在前
        value = value << 8;
        value = value | bytes[offset+i];
    }
    return value;
}