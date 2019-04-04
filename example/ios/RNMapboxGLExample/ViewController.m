//
//  ViewController.m
//  RNMapboxGLExample
//
//  Created by Dinesh Gajjar on 30/03/19.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "ViewController.h"
#import <Hype/Hype.h>
#import <React/RCTLog.h>
#import <React/RCTEventEmitter.h>

@interface EventPass : RCTEventEmitter<RCTBridgeModule>

@end

@implementation EventPass

BOOL hasListeners;

RCT_EXPORT_MODULE()

+ (id)allocWithZone:(struct _NSZone *)zone {
  static EventPass *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [super allocWithZone:zone];
  });
  return sharedInstance;
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[@"locationUpdate"];
}

// Will be called when this module's first listener is added.
- (void)startObserving {
  hasListeners = YES;
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void)stopObserving {
  hasListeners = NO;
}

- (void)onLocationUpdate:(NSDictionary *)location {
  if (hasListeners) {
    [self sendEventWithName:@"locationUpdate" body:@{ @"location": location}];
  }
}


@end

@interface ViewController () <HYPStateObserver, HYPNetworkObserver, HYPMessageObserver>

@property(strong, nonatomic) NSMutableDictionary *instances;

@end

@implementation ViewController

// This would name the module NativeMainViewController instead
RCT_EXPORT_MODULE(NativeMainViewController);

RCT_EXPORT_METHOD(sendText:(NSString *)name location:(NSDictionary *)location)
{
  dispatch_async(dispatch_get_main_queue(), ^{
    ViewController *vc = nil;
    UIViewController *rootVC = [[UIApplication sharedApplication] keyWindow].rootViewController;
    if ([rootVC isKindOfClass:ViewController.class]) {
      vc = (ViewController *)rootVC;
    }
    
    for (NSString *key in vc.instances.allKeys) {
      HYPInstance *instance = vc.instances[key];
      if (location != nil) {
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:location];
        [HYP sendData:data toInstance:instance];
      }
    }
    RCTLogInfo(@"Pretending to create an event %@ at %@", name, location);
  });
}


- (instancetype)init {
  self = [super init];
  if (self) {
    self.instances = [[NSMutableDictionary alloc] initWithCapacity:10];
  }
  return self;
}

- (void)viewDidLoad {

  [super viewDidLoad];
  
  static dispatch_once_t dispatchOnce;
  
  dispatch_once(&dispatchOnce, ^{
    
    // Adding itself as an Hype state observer makes sure that the application gets
    // notifications for lifecycle events being triggered by the SDK. These events
    // include starting and stopping, as well as some error handling.
    [HYP addStateObserver:self];
    
    // Network observer notifications include other devices entering and leaving the
    // network. When a device is found all observers get a -hypeDidFindInstance:
    // notification, and when they leave -hypeDidLoseInstance:error: is triggered
    // instead. This observer also gets notifications for -hypeDidResolveInstance:
    // when an instance is resolved.
    [HYP addNetworkObserver:self];
    
    // Message notifications indicate when messages are received, sent, or delivered.
    // Such callbacks are called with progress tracking indication.
    [HYP addMessageObserver:self];
    
    // App identifiers are used to segregate the network. Apps with different identifiers
    // do not communicate with each other, although they still cooperate on the network.
    [HYP setAppIdentifier:@"e6961486"];
    
    // Requesting Hype to start is equivalent to requesting the device to publish
    // itself on the network and start browsing for other devices in proximity. If
    // everything goes well, the -hypeDidStart: delegate method gets called, indicating
    // that the device is actively participating on the network.
    [HYP start];
  });
}

- (void)hypeDidStart {
  NSLog(@"Hype started");
}

- (void)hypeDidFailStartingWithError:(HYPError *)error {
  NSLog(@"Hype failed starting [%s]", [error description]);
}

- (NSString *)hypeDidRequestAccessTokenWithUserIdentifier:(NSUInteger)userIdentifier {
  return @"4e38959a17cbb204";
}

- (BOOL)shouldResolveInstance:(HYPInstance *)instance {

  // This method should decide whether an instance is interesting for communicating.
  // For that purpose, the implementation could use instance.userIdentifier, but it's
  // noticeable that announcements may not be available yet. Announcements are only
  // exchanged during the handshake.
  return YES;
}

- (void)hypeDidFindInstance:(HYPInstance *)instance {

  NSLog(@"Hype found instance: %@", instance.stringIdentifier);
  
  // Instances need to be resolved before being ready for communicating. This will
  // force the two of them to perform an handshake.
  if ([self shouldResolveInstance:instance]) {
    [HYP resolveInstance:instance];
  }
}

- (void)hypeDidResolveInstance:(HYPInstance *)instance {

  NSLog(@"Hype resolved instance: %@", instance.stringIdentifier);
  
  // At this point the instance is ready to communicate. Sending and receiving
  // content is possible at any time now.
  self.instances[instance.stringIdentifier] = instance;
  // [self sendText:@"Hello World" toInstance:instance acknowledge:YES];
}

- (void)hypeDidLoseInstance:(HYPInstance *)instance error:(NSError *)error {
  NSLog(@"Hype lost instance: %@", instance.stringIdentifier);
  
  // This instance is no longer available for communicating. If the instance
  // is somehow being tracked, such as by a map of instances, this would be
  // the proper time for cleanup.
  [self.instances removeObjectForKey:instance.stringIdentifier];
}

- (HYPMessage *)sendText:(NSString *)text toInstance:(HYPInstance *)instance acknowledge:(BOOL)acknowledge {
  // When sending content there must be some sort of protocol that both parties
  // understand. In this case, we simply send the text encoded in UTF8. The data
  // must be decoded when received, using the same encoding.
  NSData * data = [text dataUsingEncoding:NSUTF8StringEncoding];
  
  return [HYP sendData:data
            toInstance:instance];
}

- (void)hypeDidReceiveMessage:(HYPMessage *)message fromInstance:(HYPInstance *)instance {
  if (message.data == nil) {
    return;
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    EventPass *eventPass = [EventPass allocWithZone:nil];
    NSDictionary *location = [NSKeyedUnarchiver unarchiveObjectWithData:message.data];
    if (location) {
      [eventPass onLocationUpdate:location];
    }
  });
  
//    NSString * text = [[NSString alloc] initWithData:message.data
//                                            encoding:NSUTF8StringEncoding];
  
//  // If all goes well, this will log the original text
//  NSString *msg = [NSString stringWithFormat: @"Hype received a message from: %@ %@", instance.stringIdentifier, text];
//  NSLog(msg);
//
//  dispatch_async(dispatch_get_main_queue(), ^{
//    UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:@"Message" message:msg delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil];
//    [alertView show];
//  });
}

@end
