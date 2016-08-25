//
//  DeviceListViewController.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/22.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DeviceListViewController.h"
#import "BLEDevicesManager.h"
#import "DeviceDetailsViewController.h"

#define SEARCH_DEVICES_TIME 5.0

@interface DeviceListViewController ()
{
    NSMutableArray *devices;
    BLEDevicesManager *devicesManager;
    NSTimer *searchTimer;
    UIAlertView *searchAlert;
    UIImage *deviceConnectionStatusImage;
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
        @try {
            NSString *devicesPath = [[NSBundle mainBundle] pathForResource:@"bledevices" ofType:@"json"];
            if(devicesPath == nil)
                @throw [NSException exceptionWithName:@"FileNotFound" reason:@"bledevices.json file not found" userInfo:nil];
            NSData *devicesData = [NSData dataWithContentsOfFile:devicesPath];
            NSError *error = nil;
            id bleDevices = [NSJSONSerialization JSONObjectWithData:devicesData options:NSJSONReadingAllowFragments error:&error];
            if(error)
                @throw [NSException exceptionWithName:@"JSON Error" reason:@"Cannot parse JSON" userInfo:@{@"error": error}];
            if(![bleDevices isKindOfClass:[NSArray class]])
                @throw [NSException exceptionWithName:@"JSON Error" reason:@"JSON object is not NSArray" userInfo:nil];
            for(NSDictionary *deviceSpec in (NSArray*)bleDevices) {
                NSString *className = deviceSpec[@"className"];
                NSString *mainServiceUUIDString = deviceSpec[@"mainService"];
                id deviceClass = NSClassFromString(className);
                CBUUID *mainServiceUUID = [CBUUID UUIDWithString:mainServiceUUIDString];
                [devicesManager addDeviceClass:deviceClass byMainService:mainServiceUUID];
            }
        }
        @catch (NSException *exception) {
            DLog(@"parse bledevices failed: %@", exception);
        }
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onFoundBLEDevice:) name:@"BLEDevice.FoundDevice" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceConnectionStatusChanged:) name:@"BLEDevice.Connected" object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceConnectionStatusChanged:) name:@"BLEDevice.Disconnected" object:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)scan:(id)sender {
    [devicesManager searchDevices];
    NSMutableArray *connectedDevices = [NSMutableArray array];
    for(BLEDevice *device in devices) {
        if(device.isConnected)
            [connectedDevices addObject:device];
    }
    [devices removeAllObjects];
    [devices addObjectsFromArray:connectedDevices];
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

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if(alertView == searchAlert) {
        if(buttonIndex == alertView.cancelButtonIndex) {
            [devicesManager stopSearching];
            [searchTimer invalidate];
        }
    }
}

#pragma mark - notification observer

- (void)onFoundBLEDevice: (NSNotification*)notification {
    [devices addObject:notification.object];
    [self.tableView reloadData];
}

- (void)onDeviceConnectionStatusChanged: (NSNotification*)notification {
    id device = notification.object;
    NSUInteger index = [devices indexOfObject:device];
    if(index != NSNotFound) {
        UITableViewCell *cell = [self.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:index inSection:0]];
        [self updateCell:cell ofDevice:device];
    }
}

#pragma mark - Table view data source

- (void)updateCell: (UIView*)cell ofDevice: (BLEDevice*)device {
    UILabel *deviceName = [cell viewWithTag:1];
    UILabel *deviceDesc = [cell viewWithTag:2];
    UILabel *deviceRSSI = [cell viewWithTag:3];
    UIImageView *connectionStatus = [cell viewWithTag:4];
    if(deviceConnectionStatusImage == nil)
        deviceConnectionStatusImage =[connectionStatus.image imageWithRenderingMode:UIImageRenderingModeAlwaysTemplate];
    connectionStatus.image = deviceConnectionStatusImage;
    
    deviceName.text = [device deviceNameByDefault: @"<Unnamed>"];
    deviceDesc.text = device.deviceKey;
    deviceRSSI.text = [NSString stringWithFormat:@"RSSI: %d", device.rssi];
    connectionStatus.hidden = !device.isConnected;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return devices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"DeviceItem" forIndexPath:indexPath];
    
    BLEDevice *device = devices[indexPath.row];
    [self updateCell:cell ofDevice:device];
    
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

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [self performSegueWithIdentifier:@"ShowDeviceDetails" sender:devices[indexPath.row]];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if([segue.identifier isEqualToString:@"ShowDeviceDetails"]) {
        DeviceDetailsViewController *vc = segue.destinationViewController;
        vc.device = sender;
    }
}

@end
