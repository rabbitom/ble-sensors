//
//  DialogIoTSensor.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DialogIoTSensor.h"
#import "SensorFeature.h"

#define DEVICE_FEATURES @"Device Features"
#define CONTROL_POINT   @"Control Point"
#define COMMAND_REPLY   @"Command Reply"

@interface DialogIoTSensor()
{
    NSMutableDictionary *features;
    NSString *firmwareVersion;
    BOOL isSensorOn;
}
@end

@implementation DialogIoTSensor

static NSDictionary* _services;

+ (NSDictionary *) services {
    if(_services == nil)
        _services = @{[CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402400"] : @"Dialog Wearable"};
    return _services;
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
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402408"] : DEVICE_FEATURES,//read
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f402409"] : CONTROL_POINT,//write
                             [CBUUID UUIDWithString:@"2ea78970-7d44-44bb-b097-26183f40240a"] : COMMAND_REPLY//notify
                             };
    return _characteristics;
}

//CommandId
enum : Byte {
    WriteSettigns = 10,
    ReadSettings = 11,
    SensorOn = 1,
    SensorOff = 0
};

- (void)setReady {
    [self readData:DEVICE_FEATURES];
    [self startReceiveData:COMMAND_REPLY];
    [super setReady];
}

static NSDictionary* _featureConfigs;

+ (NSDictionary*)featureConfigs {
    if(_featureConfigs == nil) {
        _featureConfigs = @{@"ACCELEROMETER": @{
                               @"dimension": @3,
                               @"valueSize": @2,
                               @"unit": @"g",
                               @"precision": @2
                               },
                           @"GYROSCOPE": @{
                               @"dimension": @3,
                               @"valueSize": @2,
                               @"unit": @"deg/s",
                               @"precision": @2
                               },
                           @"MAGNETOMETER": @{
                               @"dimension": @3,
                               @"valueSize": @2,
                               @"unit": @"uT",
                               @"precision": @0
                               },
                           @"BAROMETER": @{
                               @"dimension": @1,
                               @"valueSize": @4,
                               @"unit": @"Pa",
                               @"precision": @0
                               },
                           @"TEMPERATURE": @{
                               @"dimension": @1,
                               @"valueSize": @4,
                               @"unit": @"°C",
                               @"precision": @2
                               },
                           @"HUMIDITY": @{
                               @"dimension": @1,
                               @"valueSize": @4,
                               @"unit": @"%",
                               @"precision": @2
                               },
                           @"SFL": @{
                               @"dimension": @4,
                               @"valueSize": @2,
                               @"unit": @"",
                               @"precision": @0
                               }
                           };
    }
    return _featureConfigs;
}

- (NSArray*)features {
    return features.allValues;
}

- (BOOL)isSensorOn {
    return isSensorOn;
}

- (void)onReceiveData: (NSData*)data forProperty: (NSString*)propertyName {
    SensorFeature *feature = features[propertyName];
    if(feature != nil) {
        if([feature parseData:data])
            [self onValueChanged:feature ofProperty:propertyName];
    }
    else if([propertyName isEqualToString:DEVICE_FEATURES]) {
        if(data.length <= 7)
            return;
        if(features == nil)
            features = [NSMutableDictionary dictionary];
        Byte* bytes = (Byte*)data.bytes;
        int index = 0;
        for(NSString *propertyName in @[@"ACCELEROMETER",
                                        @"GYROSCOPE",
                                        @"MAGNETOMETER",
                                        @"BAROMETER",
                                        @"TEMPERATURE",
                                        @"HUMIDITY",
                                        @"SFL"]) {
            if(bytes[index] == 1)
                [self addFeatureOf:propertyName];
            index++;
        }
        if(features[@"SFL"] != nil) {
            [self addFeatureOf:@"ACCELEROMETER"];
            [self addFeatureOf:@"GYROSCOPE"];
            [self addFeatureOf:@"MAGNETOMETER"];
        }
        firmwareVersion = [NSString stringWithCString:(const char*)(bytes+7) encoding:NSASCIIStringEncoding];
    }
    else if([propertyName isEqualToString:COMMAND_REPLY]) {
        Byte commandId = ((Byte*)data.bytes)[1];
        switch(commandId) {
            case ReadSettings:
                break;
            case SensorOn:
                isSensorOn = YES;
                [self onValueChanged:@YES ofProperty: @"SensorStatus"];
                break;
            case SensorOff:
                isSensorOn = NO;
                [self onValueChanged:@NO ofProperty:@"SensorStatus"];
                break;
        }
    }
}

- (void)addFeatureOf: (NSString*)propertyName {
    if(features[propertyName] != nil)
        return;
    NSDictionary *featureConfig = [self.class featureConfigs][propertyName];
    if(featureConfig != nil) {
        SensorFeature *feature = [[SensorFeature alloc] initWithConfig:featureConfig];
        feature.name = propertyName;
        feature.valueOffset = 3;
        [features setObject: feature forKey: propertyName];
    }
}

- (void)startReceiveData:(NSString *)propertyName {
    if((features[propertyName] != nil) && (!self.isSensorOn)) {
        Byte commandId = SensorOn;
        [self writeData:[NSData dataWithBytes:&commandId length:1] forProperty:CONTROL_POINT];
    }
    [super startReceiveData:propertyName];
}

- (BOOL)isReceivingData:(NSString *)propertyName {
    return self.isSensorOn && [super isReceivingData:propertyName];
}

@end
