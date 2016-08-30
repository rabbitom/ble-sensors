//
//  ChartView.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/30.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChartView : UIView

@property int dimension;

- (void)addValues: (NSArray*)values;
- (float)maxValue;
- (float)minValue;

@end
