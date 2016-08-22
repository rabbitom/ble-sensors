//
//  BLEDevicesManager.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/16.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "BLEDevicesManager.h"
#import "BLEDevice.h"

@implementation BLEDevicesManager
{
    CBCentralManager *centralManager;
    NSMutableDictionary *deviceClasses;//mainServiceUUID:Class
    NSMutableDictionary *devices;//uuid:BLEDevice
    NSMutableArray *deviceBuffer;
}

static id instance;

+ (instancetype)getInstance {
    if(instance == nil)
        instance = [[self.class alloc] init];
    return instance;
}

- (id)init {
    if(self = [super init]) {
        centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil options:@{CBCentralManagerOptionShowPowerAlertKey: @YES}];
        deviceClasses = [NSMutableDictionary dictionary];
    }
    return self;
}

- (void)searchDevices {
    if(deviceBuffer == nil)
        deviceBuffer = [NSMutableArray array];
    else
        [deviceBuffer removeAllObjects];
    [centralManager scanForPeripheralsWithServices:deviceClasses.allKeys options:nil];
}

- (void)stopSearching {
    [centralManager stopScan];
}

- (void)addDeviceClass: (Class)deviceClass {
    CBUUID *mainServiceUUID = [deviceClass mainServiceUUID];
    if(mainServiceUUID != nil)
        [deviceClasses setObject:deviceClass forKey:mainServiceUUID];
}

#pragma mark - CBCentralManagerDelegate

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    DLog(@"CBCentralManager State: %d", central.state);
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    NSLog(@"found peripheral: %@ advertisement: %@ rssi: %@", peripheral.name, advertisementData, RSSI);
    Class deviceClass = nil;
    NSArray *serviceUUIDs = advertisementData[CBAdvertisementDataServiceUUIDsKey];
    if(serviceUUIDs != nil) {
        for(CBUUID *serviceUUID in serviceUUIDs) {
            for(CBUUID *mainServiceUUID in deviceClasses.allKeys) {
                if(serviceUUID == mainServiceUUID) {
                    deviceClass = deviceClasses[mainServiceUUID];
                    break;
                }
            }
            if(deviceClass != nil)
                break;
        }
    }
    if(deviceClass == nil)
        deviceClass = [BLEDevice class];
    id device = [[deviceClass alloc] initWithPeripheral: peripheral];
    if(devices == nil)
        devices = [NSMutableDictionary dictionary];
    if(devices[peripheral.identifier] == nil)
        [devices setObject:device forKey:peripheral.identifier];
    NSString *deviceName = advertisementData[CBAdvertisementDataLocalNameKey];
    if(deviceName == nil)
        deviceName = peripheral.name;
    [[NSNotificationCenter defaultCenter] postNotificationName:@"BLEDevice.FoundDevice" object:self userInfo:@{@"id": peripheral.identifier, @"name":  (deviceName != nil) ? deviceName : [NSNull null], @"rssi": RSSI, @"class": NSStringFromClass(deviceClass)}];
}

@end
