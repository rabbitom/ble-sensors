//
//  Device3DViewController.m
//  BLESensor
//
//  Created by 郝建林 on 2016/10/10.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "Device3DViewController.h"
#import "DialogIoTSensor.h"
#import "SensorFeature.h"

@interface Device3DViewController ()
{
    NGLMesh *mesh;
    NGLCamera *camera;
    NGLQuaternion *quaternion;
    DialogIoTSensor *sensor;
    SensorFeature *sfl;
    SensorFeature *gyro;
    float ax, ay, az;
}
@end

@implementation Device3DViewController

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
//    nglGlobalColorFormat(NGLColorFormatRGB);
//    nglGlobalFlush();
    mesh = [[NGLMesh alloc] initWithFile: @"Couch.obj" settings:@{kNGLMeshCentralizeYes:@YES, kNGLMeshKeyNormalize:@0.5f} delegate:nil];
    camera = [[NGLCamera alloc] initWithMeshes: mesh, nil];
    quaternion = [[NGLQuaternion alloc] init];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    if(sensor.features != nil) {
        sfl = sensor.features[@"SFL"];
        gyro = sensor.features[@"GYROSCOPE"];
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onDeviceValueChanged:) name:@"BLEDevice.ValueChanged" object:self.device];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (void) drawView {
    //[mesh rotateWithQuaternion:quaternion];
    mesh.rotateX = ax;
    mesh.rotateY = ay;
    mesh.rotateZ = az;
    [camera drawCamera];
}

- (void)onDeviceValueChanged: (NSNotification*)notification {
    NSString *key = notification.userInfo[@"key"];
    if(key == nil)
        return;
    if([key isEqualToString:sfl.name]) {
        float w = [(NSNumber*)sfl.values[0] floatValue];
        float x = [(NSNumber*)sfl.values[1] floatValue];
        float y = [(NSNumber*)sfl.values[2] floatValue];
        float z = [(NSNumber*)sfl.values[3] floatValue];
        [quaternion rotateByQuaternionVector:nglVec4Make(x, y, z, w) mode:NGLAddModeSet];
    }
    if([key isEqualToString:gyro.name]) {
        ax = [(NSNumber*)gyro.values[0] floatValue];
        ay = [(NSNumber*)gyro.values[1] floatValue];
        az = [(NSNumber*)gyro.values[2] floatValue];
    }
}

@end
