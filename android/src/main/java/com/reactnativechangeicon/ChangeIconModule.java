package com.reactnativechangeicon;

import androidx.annotation.NonNull;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.os.Bundle;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ReactModule(name = ChangeIconModule.NAME)
public class ChangeIconModule extends ReactContextBaseJavaModule implements Application.ActivityLifecycleCallbacks {
    public static final String NAME = "ChangeIcon";
    public static final String MAIN_ACTVITY_BASE_NAME = ".MainActivity";

    public static final String CLEANUP_IMMEDIATELY = "immediately";
    public static final String CLEANUP_ON_PAUSE = "onPause";
    public static final String CLEANUP_ON_STOP = "onStop";

    private final String packageName;
    private final List<String> classesToKill = new ArrayList<>();

    private Boolean iconHasChanged = false;
    private String currentActiveClassName = "";
    private String whenToKillPrevActiveClass = "";

    public ChangeIconModule(ReactApplicationContext reactContext, String packageName) {
        super(reactContext);

        this.packageName = packageName;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getIcon(Promise promise) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity was not found");
            return;
        }

        // Recover current active icon state from the operating system.
        backfillCurrentActiveClassName(activity);

        // Current active class name is "com.example.MainActivity".
        // It is likely that <activity-alias> entries have not been added yet.
        // We cannot proceed to split this as per next step or else the app will crash natively.
        // Reject the query and indicate to the developer that <activity-alias> needs setup.
        if (isActivityAlliasConfigured()) {
            String message = "Current active class name lacks a .MainActivity suffix. We cannot proceed. "
                    + "Check that activity aliases exist and that one alias is enabled by default!";
            promise.reject("UNEXPECTED_COMPONENT_CLASS", message);
            return;
        }

        // Current active class name is "com.example.MainActivityDefault".
        // "com.example.MainActivityDefault" => ["com.example", "Default"]
        // Select suffix; "Default" in this case, then return it.
        String[] parts = currentActiveClassName.split(MAIN_ACTVITY_BASE_NAME);
        String currentActiveIconName = parts[1];

        promise.resolve(currentActiveIconName);
    }

    @ReactMethod
    public void changeIcon(String iconName, ReadableMap changeIconOptions, Promise promise) {
        if (iconName == null) {
            promise.reject("NULL_ICON_STRING", "Icon provided is null");
            return;
        }

        if (iconName.isEmpty()) {
            promise.reject("EMPTY_ICON_STRING", "Icon provided is empty string");
            return;
        }

        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity was not found");
            return;
        }

        // Recover current active icon state from the operating system.
        backfillCurrentActiveClassName(activity);

        // Current active class name is "com.example.MainActivity".
        // It is likely that <activity-alias> entries have not been added yet.
        // We cannot proceed to change icons because there is likely no alias yet or else the app will crash natively.
        // Reject the query and indicate to the developer that <activity-alias> needs setup.
        if (isActivityAlliasConfigured()) {
            String message = "Current active class name lacks a .MainActivity suffix. We cannot proceed. "
                    + "Check that activity aliases exist and that one alias is enabled by default!";
            promise.reject("UNEXPECTED_COMPONENT_CLASS", message);
            return;
        }

        // Construct next active class name: "com.example" + ".MainActivity" + "RedIcon" = "com.example.MainActivityRedIcon"
        final String nextActiveClassName = packageName + MAIN_ACTVITY_BASE_NAME + iconName;

        boolean skipIconAlreadyUsedCheck = changeIconOptions != null && changeIconOptions.getBoolean("skipIconAlreadyUsedCheck");
        if (skipIconAlreadyUsedCheck) {
            if (currentActiveClassName.equals(nextActiveClassName)) {
                promise.reject("ICON_ALREADY_USED", "Icon already in use");
                return;
            }
        }

        whenToKillPrevActiveClass = changeIconOptions == null
                ? CLEANUP_IMMEDIATELY
                : changeIconOptions.getString("whenToKillPrevActiveClass");
        whenToKillPrevActiveClass = whenToKillPrevActiveClass == null
                ? CLEANUP_IMMEDIATELY
                : whenToKillPrevActiveClass;

        switch (whenToKillPrevActiveClass) {
            case CLEANUP_IMMEDIATELY:
                try {
                    activity.getPackageManager().setComponentEnabledSetting(
                            new ComponentName(packageName, currentActiveClassName),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                    );
                    activity.getPackageManager().setComponentEnabledSetting(
                            new ComponentName(packageName, nextActiveClassName),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    );

                    whenToKillPrevActiveClass = "";

                    promise.resolve(iconName);
                } catch (Exception exception) {
                    promise.reject("SYSTEM_ERROR", e.getMessage(), exception);
                    return;
                }
                break;

            case CLEANUP_ON_PAUSE:
            case CLEANUP_ON_STOP:
                try {
                    activity.getPackageManager().setComponentEnabledSetting(
                            new ComponentName(packageName, nextActiveClassName),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                    );

                    promise.resolve(iconName);
                } catch (Exception exception) {
                    promise.reject("SYSTEM_ERROR", e.getMessage(), exception);
                    return;
                }

                // Queue current active class for cleanup.
                classesToKill.add(currentActiveClassName);

                currentActiveClassName = nextActiveClassName;

                activity.getApplication().registerActivityLifecycleCallbacks(this);

                iconHasChanged = true;
                break;

            default:
                promise.reject("INVALID_CLEANUP_CHECKPOINT", "Cannot set whenToKillPrevActiveClass to unrecognized checkpoint: " + whenToKillPrevActiveClass);
        }
    }

    private void killPrevActiveClass() {
        whenToKillPrevActiveClass = "";

        if (!iconHasChanged) {
            return;
        }

        final Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }

        for (String prevActiveClassName : classesToKill) {
            activity.getPackageManager().setComponentEnabledSetting(
                    new ComponentName(packageName, prevActiveClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        }

        classesToKill.clear();

        iconHasChanged = false;
    }

    private void backfillCurrentActiveClassName(Activity activity) {
        if (currentActiveClassName.isEmpty()) {
            currentActiveClassName = activity.getComponentName().getClassName();
        }
    }

    private boolean isActivityAlliasConfigured() {
        return currentActiveClassName.endsWith(MAIN_ACTVITY_BASE_NAME);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (whenToKillPrevActiveClass.equals(CLEANUP_ON_PAUSE)) {
            killPrevActiveClass();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (whenToKillPrevActiveClass.equals(CLEANUP_ON_STOP)) {
            killPrevActiveClass();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
