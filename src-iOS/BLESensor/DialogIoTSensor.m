//
//  DialogIoTSensor.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DialogIoTSensor.h"

@implementation DialogIoTSensor

static CBUUID* _mainServiceUUID;

+ (CBUUID *)mainServiceUUID {
    if(_mainServiceUUID == nil)
        _mainServiceUUID = [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402400"];
    return _mainServiceUUID;
}

static NSDictionary* _characteristics = nil;

+ (NSDictionary *)characteristics {
    if(_characteristics == nil)
        _characteristics = @{//CBUUID:String
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402401"] : @"ACCELEROMETER",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402402"] : @"GYROSCOPE",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402403"] : @"MAGNETOMETER",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402404"] : @"BAROMETER",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402405"] : @"HUMIDITY",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402406"] : @"TEMPERATURE",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402407"] : @"SFL",//notify
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402408"] : @"info",//read
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402409"] : @"config",//write
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f40240a"] : @"data",//notify
                             };
    return _characteristics;
}

@end
