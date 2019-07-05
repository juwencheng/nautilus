//
//  AuthWebVC.m
//  Runner
//
//  Created by 鞠汶成 on 2019/7/3.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

#import "AuthWebVC.h"
@interface AuthWebVC ()<UIWebViewDelegate>
@property (nonatomic, readwrite) UIWebView *webView;
@property(nonatomic, copy) NSString *originalUrl;
@property(nonatomic, strong) UIView *loadingView;
@end

@implementation AuthWebVC

- (instancetype)initWithUrlString:(NSString *)urlString {
    if (self = [super init]) {
        _originalUrl = urlString;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupWebView];
    [self setupLoadingView];
    self.title = @"淘宝授权";
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"关闭" style:UIBarButtonItemStylePlain target:self action:@selector(dismissVC)];
    
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:self.originalUrl] cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    [self.webView loadRequest:request];
    self.loadingView.hidden = NO;
    [self.view bringSubviewToFront:self.loadingView];
}

- (void)dismissVC {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)setupWebView {
    [self.view addSubview:self.webView];
    self.webView.translatesAutoresizingMaskIntoConstraints = NO;
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[webview]|" options:0 metrics:nil views:@{@"webview": self.webView}]];
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[webview]|" options:0 metrics:nil views:@{@"webview": self.webView}]];
    [self.view addConstraint:[NSLayoutConstraint constraintWithItem:self.webView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self.topLayoutGuide attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
}

- (void)setupLoadingView {
    [self.view addSubview:self.loadingView];
    self.loadingView.translatesAutoresizingMaskIntoConstraints = NO;
    if (@available(iOS 9.0, *)) {
        [[self.loadingView.centerXAnchor constraintEqualToAnchor:self.view.centerXAnchor] setActive:YES];
        [[self.loadingView.centerYAnchor constraintEqualToAnchor:self.view.centerYAnchor] setActive:YES];
    } else {
    }
}

- (void)changeUrl:(NSString *)url {
    _originalUrl = url;
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:self.originalUrl] cachePolicy:NSURLRequestReloadIgnoringLocalCacheData timeoutInterval:10];
    [self.webView loadRequest:request];
}

- (void)uploadRequestLog:(NSString *)uploadUrl {
    NSError *error;
    NSURLSession *session = [NSURLSession sharedSession];
    NSURL *url = [NSURL URLWithString:@"https://ps-log.52meiquan.com/gelf"];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:60.0];
    
    [request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request addValue:@"application/json" forHTTPHeaderField:@"Accept"];
    
    [request setHTTPMethod:@"POST"];
    NSDictionary *mapData = @{@"host": @"pushuo", @"version": @"1.1", @"level": @"1", @"short_message": @"debug",@"full_message": uploadUrl};
    NSData *postData = [NSJSONSerialization dataWithJSONObject:mapData options:0 error:&error];
    [request setHTTPBody:postData];
    
    
    NSURLSessionDataTask *postDataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
    }];
    
    [postDataTask resume];
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    NSString *uploadUrl = request.URL.absoluteString;
    [self uploadRequestLog:uploadUrl];
    if ([uploadUrl hasPrefix:@"http://app.pslife.com.cn/api/tbk/specialAuthv1"]) {
        if (self.result) {
            self.result(@{@"result": @1, @"data": uploadUrl});
        }
        [self dismissVC];

//        NSURLSession *session = [NSURLSession sharedSession];
//        NSURL *url = [NSURL URLWithString:uploadUrl];
//        NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
//                                                               cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
//                                                           timeoutInterval:60.0];
//        [request addValue:@"application/json" forHTTPHeaderField:@"Accept"];
//
//        [request setHTTPMethod:@"GET"];
//        NSURLSessionDataTask *postDataTask = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
//            if (error == nil) {
//                NSDictionary *root = [NSJSONSerialization JSONObjectWithData:data options:0 error:nil];
//                if ([root[@"code"] isEqualToString:@"1"]) {
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        if (self.result) {
//                            self.result(@{@"result": @0, @"message": root[@"msg"]});
//                        }
//                        [self dismissVC];
//                    });
//                }else {
//                    NSDictionary *json = root[@"data"];
//
//                    NSDictionary *result = @{
//                                             @"result": @1,
//                                             @"special_id": [NSString stringWithFormat:@"%@", json[@"specialId"]],
//                                             @"user_id": [NSString stringWithFormat: @"%@", json[@"userId"]]
//                                             };
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        if (self.result) {
//                            self.result(result);
//                        }
//                        [self dismissVC];
//                    });
//                }
//            }else {
//                dispatch_async(dispatch_get_main_queue(), ^{
//                    if (self.result) {
//                        self.result(@{@"result": @0, @"message": error.localizedDescription});
//                    }
//                    [self dismissVC];
//                });
//            }
//        }];
//        [postDataTask resume];
        return NO;
    } else if ([request.URL.absoluteString hasPrefix:@"pushuo"]) {
        NSString *queryString = [request.URL.absoluteString stringByReplacingOccurrencesOfString:@"pushuo://" withString:@""];
        NSArray *components = [queryString componentsSeparatedByString:@"#"];
        NSMutableDictionary *result = [NSMutableDictionary dictionaryWithDictionary:@{}];
        if (components.count == 2) {
            result[@"result"] = @1;
            result[@"special_id"] = components[0];
            result[@"user_id"] = components[1];
            if (self.result) {
                self.result(result);
            }
        }else {
            self.result(@{@"result": @0, @"message": @"授权失败"});
        }
        
        [self dismissVC];
    }
    return YES;
}

- (void)webViewDidStartLoad:(UIWebView *)webView {
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.view sendSubviewToBack:self.loadingView];
        self.loadingView.hidden = YES;
    });
}

- (UIWebView *)webView {
    if (!_webView) {
        _webView = [[UIWebView alloc] init];
        _webView.delegate = self;
    }
    return _webView;
}

- (UIView *)loadingView {
    if (!_loadingView) {
        _loadingView = [[UIView alloc] init];
        
        UIActivityIndicatorView *indicator = [[UIActivityIndicatorView alloc] init];
        indicator.color = [UIColor colorWithRed:0.96 green:0.27 blue:0.27 alpha:1.00];
        UILabel *label = [[UILabel alloc] init];
        label.text = @"加载中，请稍等...";
        label.font = [UIFont systemFontOfSize:16];
        label.textAlignment = NSTextAlignmentCenter;
        label.textColor = [UIColor colorWithRed:0.96 green:0.27 blue:0.27 alpha:1.00];
        [_loadingView addSubview:indicator];
        [_loadingView addSubview:label];
        indicator.translatesAutoresizingMaskIntoConstraints = NO;
        label.translatesAutoresizingMaskIntoConstraints = NO;
        [_loadingView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[indicator(==50)]-[label]|" options:0 metrics:nil views:@{@"indicator": indicator, @"label": label}]];
        [_loadingView addConstraint:[NSLayoutConstraint constraintWithItem:indicator attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:_loadingView attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
        [_loadingView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[label(==200)]|" options:0 metrics:nil views:@{@"label": label}]];
        [indicator startAnimating];
    }
    return _loadingView;
}

@end
