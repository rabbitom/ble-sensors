//
//  DeviceDataViewController.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/27.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "DeviceDataViewController.h"
#import "DialogIoTSensor.h"
#import "SensorFeature.h"
#import "ChartView.h"

@interface DeviceDataViewController ()
{
    DialogIoTSensor *sensor;
}

@property (weak, nonatomic) IBOutlet UILabel *property;
@property (weak, nonatomic) IBOutlet UISwitch *notification;
@property (weak, nonatomic) IBOutlet UILabel *curValue;
@property (weak, nonatomic) IBOutlet UILabel *maxValue;
@property (weak, nonatomic) IBOutlet UILabel *minValue;
@property (weak, nonatomic) IBOutlet UIButton *settings;
@property (weak, nonatomic) IBOutlet ChartView *chartView;

@property SensorFeature *feature;

@end

@implementation DeviceDataViewController

@synthesize feature;

- (BLEDevice*)device {
    return sensor;
}

- (void)setDevice:(BLEDevice *)device {
    if(device.class == DialogIoTSensor.class) {
        sensor = (DialogIoTSensor*)device;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)showValueViews: (BOOL)visible {
    for(UIView *view in @[self.property, self.notification, self.curValue, self.maxValue, self.minValue, self.settings])
        view.hidden = !visible;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if(sensor.features != nil) {
        if(self.propertyName != nil)
            self.feature = sensor.features[self.propertyName];
        else if((self.feature == nil) && (sensor.features.count > 0))
            self.feature = sensor.features.allValues[0];
    }
    if(self.feature == nil) {
        self.property.text = @"No Available Sensor";
        [self showValueViews:NO];
    }
    else {
        [self showValueViews:YES];
        self.property.text = self.feature.name;
        self.notification.on = [self.device isReceivingData:self.feature.name];
        self.chartView.dimension = self.feature.dimension;
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceValueChanged:) name:@"BLEDevice.ValueChanged" object:self.device];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)onDeviceValueChanged: (NSNotification*)notification {
    NSString *key = notification.userInfo[@"key"];
    if(key == nil)
        return;
    if([key isEqualToString:self.feature.name]) {
        self.curValue.text = self.feature.valueString;
        [self.chartView addValues:self.feature.values];
        self.maxValue.text = [NSString stringWithFormat:@"%@ %@",
                              [self.feature valueString:
                               [NSNumber numberWithFloat:self.chartView.maxValue]],
                              self.feature.unit];
        self.minValue.text = [NSString stringWithFormat:@"%@ %@",
                              [self.feature valueString:
                               [NSNumber numberWithFloat:self.chartView.minValue]],
                              self.feature.unit];
    }
}

- (IBAction)toggleNotification:(id)sender {
    if(self.notification.isOn)
        [self.device startReceiveData:self.feature.name];
    else
        [self.device stopReceiveData:self.feature.name];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
