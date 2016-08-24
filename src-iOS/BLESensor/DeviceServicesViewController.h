//
//  DeviceDetailsViewController.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BLEDevice.h"

@interface DeviceServicesViewController : UIViewController<UITableViewDataSource, UITableViewDelegate>

@property BLEDevice *device;

@end
