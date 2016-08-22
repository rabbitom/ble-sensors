//
//  DeviceListViewController.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/22.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DeviceListViewController.h"
#import "BLEDevicesManager.h"

#define SEARCH_DEVICES_TIME 5.0

@interface DeviceListViewController ()
{
    NSMutableArray *devices;
    BLEDevicesManager *devicesManager;
    NSTimer *searchTimer;
    UIAlertView *searchAlert;
}
@end

@implementation DeviceListViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    if(devices == nil)
        devices = [NSMutableArray array];
    if(devicesManager == nil) {
        devicesManager = [BLEDevicesManager getInstance];
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onFoundBLEDevice:) name:@"BLEDevice.FoundDevice" object:devicesManager];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)scan:(id)sender {
    [devicesManager searchDevices];
    [devices removeAllObjects];
    [self.tableView reloadData];
    searchTimer = [NSTimer scheduledTimerWithTimeInterval:SEARCH_DEVICES_TIME target:self selector:@selector(onSearchTimeout:) userInfo:nil repeats:NO];
    searchAlert = [[UIAlertView alloc]
                   initWithTitle:@"Scan"
                   message:@"Scanning for devices..."
                   delegate:self
                   cancelButtonTitle:@"Cancel"
                   otherButtonTitles:nil];
    [searchAlert show];
}

- (void)onSearchTimeout: (NSTimer*)timer {
    [devicesManager stopSearching];
    [searchAlert dismissWithClickedButtonIndex:0 animated:YES];
}

- (void)alertViewCancel:(UIAlertView *)alertView {
    if(alertView == searchAlert) {
        NSLog(@"search alert canceled");
        [devicesManager stopSearching];
    }
}

//
//- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
//    if(alertView == searchAlert) {
//        if(buttonIndex == alertView.cancelButtonIndex)
//            [devicesManager stopSearching];
//    }
//}

#pragma mark - notification observer

- (void)onFoundBLEDevice: (NSNotification*)notification {
    [devices addObject:notification.userInfo];
    [self.tableView reloadData];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return devices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"DeviceItem" forIndexPath:indexPath];
    
    UILabel *deviceName = [cell viewWithTag:1];
    UILabel *deviceDesc = [cell viewWithTag:2];
    UILabel *deviceRSSI = [cell viewWithTag:3];
    
    NSDictionary *deviceInfo = devices[indexPath.row];
    deviceName.text = [NSString stringWithFormat:@"%@ - %@", [deviceInfo objectForKey:@"name"], [deviceInfo objectForKey:@"class"]];
    deviceDesc.text = [[deviceInfo objectForKey:@"id"] UUIDString];
    deviceRSSI.text = [[deviceInfo objectForKey:@"rssi"] stringValue];
    
    return cell;
}

/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
