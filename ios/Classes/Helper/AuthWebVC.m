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
    [self.view bringSubviewToFront:self.loadingView];
}

- (void)changeUrl:(NSString *)url {
    _originalUrl = url;
    NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:self.originalUrl] cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:10];
    [self.webView loadRequest:request];
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    if ([request.URL.absoluteString hasPrefix:@"pushuo"]) {
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
    self.loadingView.hidden = NO;
    [self.view bringSubviewToFront:self.loadingView];

}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    [self.view sendSubviewToBack:self.loadingView];
    self.loadingView.hidden = YES;
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
