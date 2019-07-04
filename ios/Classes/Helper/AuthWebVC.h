//
//  AuthWebVC.h
//  Runner
//
//  Created by 鞠汶成 on 2019/7/3.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface AuthWebVC : UIViewController

- (instancetype)initWithUrlString:(NSString *)urlString;
@property (nonatomic, copy) FlutterResult result;
@property (nonatomic, readonly) UIWebView *webView;
- (void)changeUrl:(NSString *)url;
@end

NS_ASSUME_NONNULL_END
