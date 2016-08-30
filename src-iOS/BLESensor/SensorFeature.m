//
//  SensorFeature.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/25.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "SensorFeature.h"
#import "CoolUtility.h"

@interface SensorFeature()

@end

@implementation SensorFeature

- (id)initWithConfig: (NSDictionary*)sensorConfig {
    if(self = [super init]) {
        self.ratio = 1;
        for (NSString* key in sensorConfig.allKeys) {
            id value = sensorConfig[key];
            [self setValue:value forKey:key];
        }
        values = [NSMutableArray array];
    }
    return self;
}

- (BOOL)parseData: (NSData*)data {
    int minLength = self.valueOffset + self.valueSize * self.dimension;
    if(data.length >= minLength) {
        for(int v=0; v<self.dimension; v++) {
            int value = toIntLE((Byte*)data.bytes, self.valueOffset + v * self.valueSize, self.valueSize);
            if(self.valueSize == 2)
                values[v] = [NSNumber numberWithFloat: (short)value / self.ratio];
            else
                values[v] = [NSNumber numberWithFloat: value / self.ratio];
        }
        return YES;
    }
    return NO;
}

- (NSArray*)values {
    return [NSArray arrayWithArray:values];
}

- (NSString*)valueString {
    if(self.dimension == 1)
        return [NSString stringWithFormat:@"%@ %@", [self valueString: self.values[0]], self.unit];
    else {
        NSMutableArray *valueStrs = [NSMutableArray array];
        for(int v=0; v<self.dimension; v++)
            [valueStrs addObject:
             [self valueString:
              self.values[v] ] ];
        return [NSString stringWithFormat:@"[%@] %@", [valueStrs componentsJoinedByString:@", "], self.unit];
    }
}

- (NSString*)valueString: (NSNumber*)value {
    if(self.precision > 0) {
        NSString *formatString = [NSString stringWithFormat:@"%%.%if", self.precision];
        return [NSString stringWithFormat:formatString, [value floatValue]];
    }
    else
        return [NSString stringWithFormat:@"%i", [value intValue]];
}

- (NSString*)description {
    return [NSString stringWithFormat:@"%@: %@", self.name, self.valueString];
}

@end
