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

- (id)initWithPeripheral: (CBPeripheral*)peripheral;

+ (CBUUID*) mainServiceUUID;

+ (NSDictionary*) characteristics;

- (void)onConnected;
- (void)onReceiveData: (NSData*)data ofCharacteristic: (CBUUID*)uuid;

@end
