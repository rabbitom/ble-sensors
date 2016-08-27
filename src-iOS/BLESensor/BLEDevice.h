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
    NSMutableDictionary *propertyCharacteristics;
}
+ (NSDictionary*) services;//ServiceUUID(CBUUID) : serviceName(String)

+ (NSDictionary*) characteristics;//CharateristicUUID(CBUUID) : propertyName(String)

@property CBPeripheral *peripheral;
@property (readonly) NSArray *advertisements;

@property (readonly) NSString *deviceKey;
@property int rssi;

- (NSString*) deviceNameByDefault: (NSString*)defaultName;

@property (readonly) BOOL isConnected;

- (id)initWithPeripheral: (CBPeripheral*)peripheral advertisementData: (NSDictionary*)ad;
- (void)updateAdvertisementData: (NSDictionary*)ad;

- (void)connect;
- (void)disconnect;

- (void)onConnected;
- (void)onReceiveData: (NSData*)data forProperty: (NSString*)propertyName;
- (void)onValueChanged: (id)value ofProperty: (NSString*)propertyName;

- (void)writeData: (NSData*)data forProperty: (NSString*)propertyName;

- (void)setReady;

- (void)readData: (NSString*)propertyName;
- (void)startReceiveData: (NSString*)propertyName;
- (void)stopReceiveData: (NSString*)propertyName;
- (BOOL)isReceivingData: (NSString*)propertyName;

@end
