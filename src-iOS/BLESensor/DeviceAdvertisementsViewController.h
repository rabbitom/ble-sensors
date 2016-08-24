//
//  DeviceAdvertisementsViewController.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/24.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BLEDevice.h"

@interface DeviceAdvertisementsViewController : UIViewController <UITableViewDataSource>

@property BLEDevice *device;

@end
