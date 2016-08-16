//
//  BLEDevice.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "BLEDevice.h"

@interface BLEDevice()
{
    NSMutableDictionary *characteristicsOnDiscover;
}

@end


@implementation BLEDevice

- (id)initWithPeripheral: (CBPeripheral*)peripheral {
    if(self = [super init]) {
        _peripheral = peripheral;
        _peripheral.delegate = self;
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
    characteristicsOnDiscover = [NSMutableDictionary dictionaryWithDictionary:[self.class characteristics]];
    [_peripheral discoverServices:nil];
}

- (void)setReady {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice Ready" object:self];
}

- (void)onReceiveData: (NSData*)data ofCharacteristic: (CBUUID*)uuid {
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice RecevieData" object:self userInfo:@{@"data":data, @"uuid":uuid}];
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
        NSLog(@"CBPeripheral discover characteristics of service %@ error: %@", service, error);
        return;
    }
    if((characteristicsOnDiscover == nil) || (characteristicsOnDiscover.count == 0))
        return;
    for(CBCharacteristic *characteristic in [service characteristics])
    {
        for(CBUUID *uuid in characteristicsOnDiscover.allKeys) {
            if([uuid isEqual:characteristic.UUID]) {
                NSString *propertyName = characteristicsOnDiscover[uuid];
                [self setValue:characteristic forKey:propertyName];
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
        NSLog(@"CBPeripheral update value of characteristic %@ error: %@", characteristic, error);
        return;
    }
    [self onReceiveData:[characteristic value] ofCharacteristic:characteristic.UUID];
}

@end
