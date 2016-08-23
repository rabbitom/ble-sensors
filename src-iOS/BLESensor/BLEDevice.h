//
//  BLEDevice.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEDevice : NSObject <CBPeripheralDelegate>
{
    CBService *mainService;
}

@property CBPeripheral *peripheral;

@property (readonly) NSString *deviceKey;
@property (readonly) int deviceRSSI;
@property (readonly) NSString *deviceName;

- (id)initWithPeripheral: (CBPeripheral*)peripheral advertisementData: (NSDictionary*)ad;

+ (CBUUID*) mainServiceUUID;

+ (NSDictionary*) characteristics;

- (void)onConnected;
- (void)onReceiveData: (NSData*)data forProperty: (NSString*)propertyName;

- (void)connect;
- (BOOL)isConnected;
- (void)disconnect;

- (void)writeData: (NSData*)data forProperty: (NSString*)propertyName;

@end
