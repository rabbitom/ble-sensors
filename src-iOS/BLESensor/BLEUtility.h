//
//  BLEUtility.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BLEUtility : NSObject

+ (NSString*)serviceName: (CBUUID*)serviceUUID;

+ (NSString*)centralState:(CBCentralManagerState)state;

@end
