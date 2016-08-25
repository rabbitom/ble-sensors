//
//  CoolUtility.h
//  BLESensor
//
//  Created by 郝建林 on 16/8/23.
//  Copyright © 2016年 CoolTools. All rights reserved.
//

#ifndef CoolUtility_h
#define CoolUtility_h

#import <Foundation/Foundation.h>

#define STRING_BY_DEFAULT(a,b) (a != nil) ? a : b

int toIntLE(Byte* bytes, int offset, int length);

#endif /* CoolUtility_h */
