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
    NSMutableDictionary *settings;
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
                               @"precision": @2,
                               @"ratio": @100
                               },
                           @"HUMIDITY": @{
                               @"dimension": @1,
                               @"valueSize": @4,
                               @"unit": @"%",
                               @"precision": @2,
                               @"ratio": @1024
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

static NSArray* _settingKeys;

+ (NSArray*)settingKeys {
    if(_settingKeys == nil)
        _settingKeys = @[@"sensorCombination",
                         @"accelerometerRange",
                         @"accelerometerRate",
                         @"gyroscopeRange",
                         @"gyroscopeRate",
                         @"magnetometerRate",
                         @"environmentalRate",
                         @"sensorFusionRate",
                         @"sensorFusionRawDataEnable",
                         @"calibrationMode",
                         @"autoCalibrationMode"];
    return _settingKeys;
}

enum AcclerometerRanges : Byte {
    ACC_RANGE_2G = 3,
    ACC_RANGE_4G = 5,
    ACC_RANGE_8G = 8,
    ACC_RANGE_16G = 12
};

static Byte acclerometerRangeKeys[] = {ACC_RANGE_2G, ACC_RANGE_4G, ACC_RANGE_8G, ACC_RANGE_16G};

+ (int) acclerometerRangeValue: (Byte)key {
    for(int i=0; i<sizeof(acclerometerRangeKeys); i++)
        if(acclerometerRangeKeys[i] == key)
            return pow(2, 1 + i);
    return 0;
}

+ (Byte) acclerometerRangeKeyAtIndex: (int)index {
    return acclerometerRangeKeys[index];
}

static NSMutableArray *_acclerometerRangeValues;

+ (NSArray*) acclerometerRangeValues {
    if(_acclerometerRangeValues == nil) {
        _acclerometerRangeValues = [NSMutableArray array];
        for(int i=0; i<sizeof(acclerometerRangeKeys); i++)
            [_acclerometerRangeValues addObject:
             [NSNumber numberWithInt:
              [DialogIoTSensor acclerometerRangeValue:
               acclerometerRangeKeys[i] ] ] ];
    }
    return _acclerometerRangeValues;
}

//enum GyroScopeRanges : Byte {
//    GYRO_RANGE_2000 = 0,
//    GYRO_RANGE_1000,
//    GYRO_RANGE_500,
//    GYRO_RANGE_250,
//    GYRO_RANGE_125
//};

static NSArray* _gyroscopeRangeValues;

+ (NSArray*) gyroscopeRnageValues {
    if(_gyroscopeRangeValues == nil)
        _gyroscopeRangeValues = @[@2000,
                                  @1000,
                                  @500,
                                  @250,
                                  @125];
    return _gyroscopeRangeValues;
}

+ (int) gyroscopeRangeValue: (Byte)key {
    //return 2000 / pow(2, key);
    NSNumber *value = [DialogIoTSensor gyroscopeRnageValues][key];
    return [value intValue];
}

+ (Byte) gyroscopeRangeKeyAtIndex: (int)index {
    return (Byte)index;
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
        Byte *bytes = (Byte*)data.bytes;
        Byte commandId = bytes[1];
        switch(commandId) {
            case ReadSettings:
            {
                NSArray *settingKeys = [self.class settingKeys];
                if(settings == nil)
                    settings = [NSMutableDictionary dictionary];
                int k = 0;
                for(NSString *key in settingKeys)
                    settings[key] = [NSNumber numberWithUnsignedChar:bytes[k++]];
                //acc range
                NSNumber *acclerometerRangeKey = settings[@"acclerometerRange"];
                if(acclerometerRangeKey != nil) {
                    SensorFeature *acc = features[@"ACCELEROMETER"];
                    if(acc != nil)
                        acc.ratio = 32768 / [self.class acclerometerRangeValue:[acclerometerRangeKey unsignedCharValue]];
                }
                //gyro range
                NSNumber *gyroscopeRangeKey = settings[@"gyroscopeRange"];
                if(gyroscopeRangeKey != nil) {
                    SensorFeature *gyro = features[@"GYROSCOPE"];
                    if(gyro != nil)
                        gyro.ratio = 32800.0 / [self.class gyroscopeRangeValue:[gyroscopeRangeKey unsignedCharValue]];
                }
            }
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
