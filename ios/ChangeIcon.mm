#import "ChangeIcon.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNChangeIconSpec.h"
#endif

@implementation ChangeIcon
RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

RCT_REMAP_METHOD(getIcon, resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *currentIcon = [[UIApplication sharedApplication] alternateIconName];
        // Return the value as is.
        // - string: alternate app icon
        // - nil: primary (a.k.a. default) app icon
        resolve(currentIcon);
    });
}

RCT_REMAP_METHOD(changeIcon, iconName:(NSString *)iconName changeIconOptions:(NSDictionary *)changeIconOptions resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSError *error = nil;

        if (iconName != nil && [iconName length] == 0) {
            reject(@"EMPTY_ICON_STRING", @"Icon provided is empty string", error);
        }

        if ([[UIApplication sharedApplication] supportsAlternateIcons] == NO) {
            reject(@"ALTERNATE_ICON_NOT_SUPPORTED", @"Alternate icon not supported", error);
            return;
        }

        NSString *currentIcon = [[UIApplication sharedApplication] alternateIconName];

        bool skipIconAlreadyUsedCheck = changeIconOptions != nil && [[changeIconOptions valueForKey:@"skipIconAlreadyUsedCheck"] boolValue];
        if (skipIconAlreadyUsedCheck) {
            if (currentIcon == nil && iconName == nil) {
                reject(@"ICON_ALREADY_USED", @"Icon already in use", error);
                return;
            }
            
            if ([iconName isEqualToString:currentIcon]) {
                reject(@"ICON_ALREADY_USED", @"Icon already in use", error);
                return;
            }
        }

        bool skipSystemResponseDialog = changeIconOptions != nil && [[changeIconOptions valueForKey:@"skipSystemResponseDialog"] boolValue];
        if (skipSystemResponseDialog) {
            [self setAlternateIconName:iconName completionHandler:^(NSError * _Nullable error) {
                if (error) {
                  reject(@"SYSTEM_ERROR", error.localizedDescription, error);
                } else {
                  resolve(iconName);
                }
            }];
        } else {
            [[UIApplication sharedApplication] setAlternateIconName:iconName completionHandler:^(NSError * _Nullable error) {
                if (error) {
                  reject(@"SYSTEM_ERROR", error.localizedDescription, error);
                } else {
                  resolve(iconName);
                }
            }];
        }
    });
}

// https://stackoverflow.com/a/49730130
- (void)setAlternateIconName:(NSString *)iconName completionHandler:(nullable void (^)(NSError *_Nullable error))completionHandler {
    if ([[UIApplication sharedApplication] respondsToSelector:@selector(supportsAlternateIcons)] &&
        [[UIApplication sharedApplication] supportsAlternateIcons])
    {
        NSMutableString *selectorString = [[NSMutableString alloc] initWithCapacity:40];
        [selectorString appendString:@"_setAlternate"];
        [selectorString appendString:@"IconName:"];
        [selectorString appendString:@"completionHandler:"];

        SEL selector = NSSelectorFromString(selectorString);
        IMP imp = [[UIApplication sharedApplication] methodForSelector:selector];
        void (*func)(id, SEL, id, id) = (void (*)(id, SEL, id, id))imp;
        if (func)
        {
            func([UIApplication sharedApplication], selector, iconName, completionHandler);
        }
    }
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeChangeIconSpecJSI>(params);
}
#endif

@end
