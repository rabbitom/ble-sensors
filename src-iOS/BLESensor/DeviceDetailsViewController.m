//
//  DeviceDetailsViewController.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DeviceDetailsViewController.h"
#import "BLEUtility.h"
#import "CoolUtility.h"

@interface DeviceDetailsViewController ()
{
    BLEDevice *_devcie;
}
@property (weak, nonatomic) IBOutlet UIBarButtonItem *connectBtn;
@end

@implementation DeviceDetailsViewController

- (BLEDevice*)device {
    return _devcie;
}

- (void)setDevice:(BLEDevice *)device {
    DLog("");
    _devcie = device;
    if(self.device != nil) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceReady:) name:@"BLEDevice.Ready" object:self.device];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceConnectionStatusChanged:) name:@"BLEDevice.Connected" object:self.device];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceConnectionStatusChanged:) name:@"BLEDevice.Disconnected" object:self.device];
        if(!self.device.isConnected) {
            [self.device connect];
        }
    }
}

- (void)updateConnectBtn {
    self.connectBtn.title = self.device.isConnected ? @"Disconnect" : @"Connect";
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    self.navigationItem.title = [self.device deviceNameByDefault:@"Unnamed Device"];
    [self updateConnectBtn];
    DLog("");
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)onDeviceReady: (NSNotification*)notification {
    if(notification.object == self.device)
        [self.tableView reloadData];
}

- (void)onDeviceConnectionStatusChanged: (NSNotification*)notification {
    [self updateConnectBtn];
}

- (IBAction)toggleConnect:(id)sender {
    if(self.device.isConnected)
        [self.device disconnect];
    else
        [self.device connect];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.device.peripheral.services.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ServiceItem" forIndexPath:indexPath];
    
    UILabel *serviceName = [cell viewWithTag:1];
    UILabel *serviceUUID = [cell viewWithTag:2];
    UILabel *serviceInfo = [cell viewWithTag:3];
    
    CBService *service = self.device.peripheral.services[indexPath.row];
    NSString *_serviceName = [BLEUtility serviceName:service.UUID];
    serviceName.text = STRING_BY_DEFAULT(_serviceName, @"Unknown Service");
    serviceUUID.text = [service.UUID UUIDString];
    serviceInfo.text = service.isPrimary ? @"PRIMARY" : @"";
    
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
