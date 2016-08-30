//
//  SensorFeature.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/25.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SensorFeature : NSObject
{
    NSMutableArray *values;
}

//@property NSString *UUIDString;
@property NSString *name;
@property int dimension;
@property int valueSize;
@property int valueOffset;
@property NSString *unit;
@property int precision;
@property float ratio;

- (id)initWithConfig: (NSDictionary*)sensorConfig;
- (BOOL)parseData: (NSData*)data;
- (NSArray*)values;
- (NSString*)valueString;
- (NSString*)valueString: (NSNumber*)value;

@end
