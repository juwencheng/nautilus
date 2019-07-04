//
// Created by mo on 2018/11/23.
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

@interface NautilusTradeHandler : NSObject
- (void)initTradeAsync:(FlutterMethodCall *)call result:(FlutterResult)result;
- (void)openItemDetail:(FlutterMethodCall *)call result:(FlutterResult)result;
- (void)openUrl:(FlutterMethodCall *)call result:(FlutterResult)result;
- (void)openAuthUrl:(FlutterMethodCall *)call result:(FlutterResult)result;
- (void)openMyCart:(FlutterMethodCall *)call result:(FlutterResult)result;
- (void)openOrderList:(FlutterMethodCall *)call result:(FlutterResult)result;
@end
