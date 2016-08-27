//
//  DeviceDataViewController.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/27.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DeviceDetailsViewController.h"

@interface DeviceDataViewController : UIViewController<DeviceDetailController>

@property NSString* propertyName;

@end
