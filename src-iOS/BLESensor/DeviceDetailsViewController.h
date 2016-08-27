//
//  DeviceDetailsViewController.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/24.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BLEDevice.h"

@protocol DeviceDetailController <NSObject>

@property BLEDevice *device;

@end


@interface DeviceDetailsViewController : UITabBarController <DeviceDetailController>

@end
