//
//  ChartView.m
//  BLESensor
//
//  Created by 郝建林 on 16/8/30.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#import "ChartView.h"

@interface ChartView()
{
    NSMutableArray *values;
    int valueSize;
    float minValue, maxValue;
    NSMutableArray *points;
    int _dimension;
    NSMutableArray *lineColors;
}
@end

@implementation ChartView

- (float)maxValue {
    return maxValue;
}

- (float)minValue {
    return minValue;
}

- (int)dimension {
    return _dimension;
}

- (void)setDimension:(int)dimension {
    _dimension = dimension;
    [lineColors removeAllObjects];
    if(self.dimension == 1)
        [lineColors addObject:[self tintColor]];
    else
        for(int i=0; i<self.dimension; i++) {
            [lineColors addObject:[UIColor colorWithHue:1.0 / self.dimension * i saturation:1.0 brightness:1.0 alpha:1.0]];
        }
    [values removeAllObjects];
    [points removeAllObjects];
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    if(self = [super initWithCoder:aDecoder]) {
        values = [NSMutableArray array];
        points = [NSMutableArray array];
        valueSize = 0;
        lineColors = [NSMutableArray arrayWithObject:[self tintColor]];
        _dimension = 1;
    }
    return self;
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
    if(points.count < self.dimension)
        return;
    valueSize = rect.size.width;
    CGMutablePathRef path[self.dimension];
    for(int i=0; i<self.dimension; i++) {
        path[i] = CGPathCreateMutable();
        CGPathMoveToPoint(path[i], nil, rect.origin.x, rect.origin.y + (1 -[points[i] floatValue]) * rect.size.height);
    }
    int valueCount = points.count / self.dimension;
    for(int v=1; v<valueCount; v++)
        for(int d=0; d<self.dimension; d++)
            CGPathAddLineToPoint(path[d], nil, rect.origin.x + v, rect.origin.y + (1 - [points[self.dimension * v + d] floatValue]) * rect.size.height);
    CGContextRef context = UIGraphicsGetCurrentContext();
    for(int i=0; i<self.dimension; i++) {
        CGContextAddPath(context, path[i]);
        CGContextSetStrokeColorWithColor(context, [lineColors[i] CGColor]);
        CGContextDrawPath(context, kCGPathStroke);
    }
}

- (void)addValues: (NSArray*)newValues {
    [values addObjectsFromArray:newValues];
    if((valueSize > 0) && (values.count > valueSize * self.dimension)) {
        [values removeObjectsInRange:NSMakeRange(0, values.count - valueSize * self.dimension)];
    }
    minValue = [newValues[0] floatValue];
    maxValue = minValue;
    for(NSNumber *num in values) {
        float value = [num floatValue];
        if(value < minValue)
            minValue = value;
        if(value > maxValue)
            maxValue = value;
    }
    [points removeAllObjects];
    for(NSNumber *num in values) {
        float value = [num floatValue];
        [points addObject:[NSNumber numberWithFloat:(value - minValue)/(maxValue - minValue)]];
    }
    [self setNeedsDisplay];
}

@end
