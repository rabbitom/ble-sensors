//
//  BLEUtility.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "BLEUtility.h"

@implementation BLEUtility

static NSDictionary *serviceNames;

+ (NSString*)serviceName:(CBUUID *)serviceUUID {
    if(serviceNames == nil)
        serviceNames = @{[CBUUID UUIDWithString:@"1800"] : @"Generic Access",
                         [CBUUID UUIDWithString:@"1801"] : @"Generic Attribute",
                         [CBUUID UUIDWithString:@"1802"] : @"Immediate Alert",
                         [CBUUID UUIDWithString:@"1803"] : @"Link Loss",
                         [CBUUID UUIDWithString:@"1804"] : @"Tx Power",
                         [CBUUID UUIDWithString:@"1805"] : @"Current Time Service",
                         [CBUUID UUIDWithString:@"1806"] : @"Reference Time Update Service",
                         [CBUUID UUIDWithString:@"1807"] : @"Next DST Change Service",
                         [CBUUID UUIDWithString:@"1808"] : @"Glucose",
                         [CBUUID UUIDWithString:@"1809"] : @"Health Thermometer",
                         [CBUUID UUIDWithString:@"180A"] : @"Device Information",
                         [CBUUID UUIDWithString:@"180D"] : @"Heart Rate",
                         [CBUUID UUIDWithString:@"180E"] : @"Phone Alert Status Service",
                         [CBUUID UUIDWithString:@"180F"] : @"Battery Service",
                         [CBUUID UUIDWithString:@"1810"] : @"Blood Pressure",
                         [CBUUID UUIDWithString:@"1811"] : @"Alert Notification Service",
                         [CBUUID UUIDWithString:@"1812"] : @"Human Interface Device",
                         [CBUUID UUIDWithString:@"1813"] : @"Scan Parameters",
                         [CBUUID UUIDWithString:@"1814"] : @"Running Speed and Cadence",
                         [CBUUID UUIDWithString:@"1815"] : @"Automation IO",
                         [CBUUID UUIDWithString:@"1816"] : @"Cycling Speed and Cadence",
                         [CBUUID UUIDWithString:@"1818"] : @"Cycling Power",
                         [CBUUID UUIDWithString:@"1819"] : @"Location and Navigation",
                         [CBUUID UUIDWithString:@"181A"] : @"Environmental Sensing",
                         [CBUUID UUIDWithString:@"181B"] : @"Body Composition",
                         [CBUUID UUIDWithString:@"181C"] : @"User Data",
                         [CBUUID UUIDWithString:@"181D"] : @"Weight Scale",
                         [CBUUID UUIDWithString:@"181E"] : @"Bond Management",
                         [CBUUID UUIDWithString:@"181F"] : @"Continuous Glucose Monitoring",
                         [CBUUID UUIDWithString:@"1820"] : @"Internet Protocol Support",
                         [CBUUID UUIDWithString:@"1821"] : @"Indoor Positioning",
                         [CBUUID UUIDWithString:@"1822"] : @"Pulse Oximeter"
                         };
    return serviceNames[serviceUUID];
}

+ (NSString*)centralState:(CBCentralManagerState)state {
    switch(state) {
        case CBCentralManagerStateUnknown: return @"Unknown";
        case CBCentralManagerStateResetting: return @"Resetting";
        case CBCentralManagerStateUnsupported: return @"Unsupported";
        case CBCentralManagerStateUnauthorized: return @"Unauthorized";
        case CBCentralManagerStatePoweredOff: return @"PoweredOff";
        case CBCentralManagerStatePoweredOn: return @"PoweredOn";
    }
}

@end
