//
//  BLEDevice.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "BLEDevice.h"
#import "BLEDevicesManager.h"

@interface BLEDevice()
{
    NSMutableDictionary *characteristicsOnDiscover;
    NSMutableDictionary *propertyCharacteristics;
    BOOL fullyDiscoverd;
}

@end


@implementation BLEDevice

- (id)initWithPeripheral: (CBPeripheral*)peripheral {
    if(self = [super init]) {
        _peripheral = peripheral;
        _peripheral.delegate = self;
        fullyDiscoverd = false;
    }
    return self;
}

+ (CBUUID *)mainServiceUUID {
    return nil;
}

+ (NSDictionary *)characteristics {
    return nil;
}

- (void)onConnected {
    if(!fullyDiscoverd) {
        characteristicsOnDiscover = [NSMutableDictionary dictionaryWithDictionary:[self.class characteristics]];
        [_peripheral discoverServices:nil];
    }
    else
        [self setReady];
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
    if(self.centralManager == nil)
        self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];//will auto connect when powered on
    else
        [self.centralManager connectPeripheral:self.peripheral options:nil];
}

- (BOOL)isConnected {
    return self.peripheral.state == CBPeripheralStateConnected;
}

- (void)disconnect {
    if(self.centralManager != nil)
        [self.centralManager cancelPeripheralConnection:self.peripheral];
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

#pragma mark - methods for CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    if(central.state == CBCentralManagerStatePoweredOn) {
        [central connectPeripheral:self.peripheral options:nil];
    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    if(peripheral == self.peripheral) {
        [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.Connected" object:self];
        [self onConnected];
    }
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    if(peripheral == self.peripheral)
        [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.Diconnected" object:self userInfo:@{@"error": (error != nil) ? error : [NSNull null]}];
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    if(peripheral == self.peripheral)
        [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.FailedToConnect" object:self userInfo:@{@"error": (error != nil) ? error : [NSNull null]}];
}

#pragma mark - methods for CBPeripheralDelegate

//发现了服务
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    if(error != nil) {
        NSLog(@"CBPeripheral discover services error: %@", error);
        return;
    }
    for(CBService *service in [peripheral services])
    {
        if([service.UUID isEqual: [self.class mainServiceUUID]])
            mainService = service;
        //发现特性
        [peripheral discoverCharacteristics:nil forService:service];
    }
}

//发现了特性
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    if(error != nil) {
        DLog(@"");
        //NSLog(@"CBPeripheral discover characteristics of service %@ error: %@", service, error);
        return;
    }
    if((characteristicsOnDiscover == nil) || (characteristicsOnDiscover.count == 0))
        return;
    for(CBCharacteristic *characteristic in [service characteristics])
    {
        for(CBUUID *uuid in characteristicsOnDiscover.allKeys) {
            if([uuid isEqual:characteristic.UUID]) {
                NSString *propertyName = characteristicsOnDiscover[uuid];
                if(propertyCharacteristics == nil)
                    propertyCharacteristics = [NSMutableDictionary dictionary];
                [propertyCharacteristics setObject:characteristic forKey:propertyName];
                [characteristicsOnDiscover removeObjectForKey:uuid];
                break;
            }
        }
    }
    if(characteristicsOnDiscover.count == 0)
        [self setReady];
}

//接收读特性的返回和通知特性
- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if(error != nil) {
        DLog(@"");
        //NSLog(@"CBPeripheral update value of characteristic %@ error: %@", characteristic, error);
        return;
    }
    NSString *propertyName = [[self.class characteristics] objectForKey:characteristic.UUID];
    if(propertyName != nil)
        [self onReceiveData:characteristic.value forProperty:propertyName];
}

@end
