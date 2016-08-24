//
//  DeviceAdvertisementsViewController.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/24.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DeviceAdvertisementsViewController.h"

@interface DeviceAdvertisementsViewController()

@property (weak, nonatomic) IBOutlet UILabel *deviceUUID;
@property (weak, nonatomic) IBOutlet UITableView *tableView;

@end


@implementation DeviceAdvertisementsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.deviceUUID.text = self.device.deviceKey;
    self.tableView.dataSource = self;
    [self.tableView reloadData];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.device.advertisements.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"AdvertisementItem"];
    NSDictionary *kv = self.device.advertisements[indexPath.row];
    cell.textLabel.text = kv[@"key"];
    cell.detailTextLabel.text = kv[@"value"];
    return cell;
}

@end
