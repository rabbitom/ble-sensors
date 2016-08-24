//
//  BLEDevice.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "BLEDevice.h"
#import "BLEDevicesManager.h"
#import "CoolUtility.h"

@interface BLEDevice()
{
    NSDictionary *advertisementData;

    NSMutableArray *servicesOnDiscover;//peripheral services to discover characteristics
    NSMutableArray *characteristicUUIDsToDiscover;

    NSMutableDictionary *propertyCharacteristics;
}

@end


@implementation BLEDevice

- (id)initWithPeripheral: (CBPeripheral*)peripheral advertisementData: (NSDictionary*)ad {
    if(self = [super init]) {
        _peripheral = peripheral;
        _peripheral.delegate = self;
        advertisementData = ad;
        propertyCharacteristics = [NSMutableDictionary dictionary];
        servicesOnDiscover = [NSMutableArray array];
    }
    return self;
}

- (void)updateAdvertisementData: (NSDictionary*)ad {
    advertisementData = ad;
}

- (NSString*)deviceKey {
    return [self.peripheral.identifier UUIDString];
}

- (int)deviceRSSI {
    return [self.peripheral.RSSI intValue];
}

- (NSString*)deviceNameByDefault: (NSString*)defaultName {
    NSString *deviceName = advertisementData[CBAdvertisementDataLocalNameKey];
    if(deviceName == nil)
        deviceName = self.peripheral.name;
    return STRING_BY_DEFAULT(deviceName, defaultName);
}

+ (CBUUID *)mainServiceUUID {
    return nil;
}

+ (NSDictionary *)characteristics {
    return nil;
}

- (void)onConnected {
    if(self.peripheral.services == nil) {
        [servicesOnDiscover removeAllObjects];
        NSDictionary *characteristics = [self.class characteristics];
        if(characteristics != nil)
            characteristicUUIDsToDiscover = [NSMutableArray arrayWithArray:characteristics.allKeys];
        else
            characteristicUUIDsToDiscover = nil;
        [self.peripheral discoverServices:nil];
    }
    else {
        DLog(@"already discovered services: %@", self.peripheral.services);
        [self setReady];
    }
}

- (void)setReady {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.Ready" object:self];
}

- (void)onReceiveData: (NSData*)data forProperty: (NSString*)propertyName {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.ReceviedData" object:self userInfo:@{@"data":data, @"property":propertyName}];
}

- (void)onPropertyValueChanged: (NSString*)propertyName {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.ValueChanged" object:self];
}

- (void)connect {
    CBCentralManager *central = [BLEDevicesManager central];
    if(self.peripheral.state != CBPeripheralStateConnected) {
        if(central.state == CBCentralManagerStatePoweredOn)
            [central connectPeripheral:self.peripheral options:nil];
        else
            DLog(@"central not powered on");
    }
    else {
        DLog(@"periperal already connected");
        [self onConnected];
    }
}

- (BOOL)isConnected {
    return self.peripheral.state == CBPeripheralStateConnected;
}

- (void)disconnect {
    [[BLEDevicesManager central] cancelPeripheralConnection:self.peripheral];
}

- (void)writeData: (NSData*)data forProperty: (NSString*)propertyName {
    CBCharacteristic *characteristic = propertyCharacteristics[propertyName];
    if(characteristic != nil) {
        if(characteristic.properties & CBCharacteristicPropertyWrite)
            [self.peripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithResponse];
        else if(characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse)
            [self.peripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithoutResponse];
        else
            DLog(@"characteristic cannot be written");
    }
    else
        DLog(@"property has no charactristic");
}

#pragma mark - methods for CBPeripheralDelegate

//发现了服务
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    if(error != nil) {
        DLog(@"CBPeripheral discover services error: %@", error);
        return;
    }
    [servicesOnDiscover addObjectsFromArray:peripheral.services];
    for(CBService *service in [peripheral services])
        //发现特性
        [peripheral discoverCharacteristics:nil forService:service];
}

//发现了特性
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    if(error != nil) {
        DLog(@"CBPeripheral discover characteristics of service %@ error: %@", service, error);
        return;
    }
    [servicesOnDiscover removeObject:service];
    if((characteristicUUIDsToDiscover != nil) && (characteristicUUIDsToDiscover.count > 0))
        for(CBCharacteristic *characteristic in [service characteristics])
        {
            CBUUID *characteristicUUID = characteristic.UUID;
            if([characteristicUUIDsToDiscover containsObject:characteristicUUID]) {
                NSString *propertyName = [self.class characteristics][characteristicUUID];
                [propertyCharacteristics setObject:characteristic forKey:propertyName];
                [characteristicUUIDsToDiscover removeObject:characteristicUUID];
                break;
            }
        }
    if(servicesOnDiscover.count == 0)
        [self setReady];
}

//接收读特性的返回和通知特性
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if(error != nil) {
        DLog(@"CBPeripheral update value of characteristic %@ error: %@", characteristic, error);
        return;
    }
    NSString *propertyName = [[self.class characteristics] objectForKey:characteristic.UUID];
    if(propertyName != nil)
        [self onReceiveData:characteristic.value forProperty:propertyName];
}

@end
