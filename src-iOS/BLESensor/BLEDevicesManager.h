//
//  BLEDevicesManager.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEDevicesManager : NSObject <CBCentralManagerDelegate>

+ (instancetype)getInstance;
+ (CBCentralManager*)central;

- (void)addDeviceClass: (Class)deviceClass;

- (void)searchDevices;
- (void)stopSearching;
//- (NSArray*)devicesOfClass: (NSString*)className sortBy: (NSString*)key max: (int)count;

- (id)findDevice: (NSUUID*)deviceId;

@end
