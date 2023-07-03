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
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ReactModule(name = ChangeIconModule.NAME)
public class ChangeIconModule extends ReactContextBaseJavaModule implements Application.ActivityLifecycleCallbacks {
    public static final String NAME = "ChangeIcon";

    private final String packageName;

    private List<String> classesToKill = new ArrayList<>();

    private Boolean iconHasChanged = false;

    private String currentActiveClassName = "";

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

        if (currentActiveClassName.isEmpty()) {
            currentActiveClassName = activity.getComponentName().getClassName();
        }

        String currentActiveIconName = currentActiveClassName.split("MainActivity")[1];

        promise.resolve(currentActiveIconName.isEmpty() ? null : currentActiveIconName);
    }

    @ReactMethod
    public void changeIcon(String iconName, Promise promise) {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.reject("ACTIVITY_NOT_FOUND", "Activity was not found");
            return;
        }

        if (iconName.isEmpty()) {
            promise.reject("EMPTY_ICON_STRING", "Icon provided is empty string");
            return;
        }

        if (currentActiveClassName.isEmpty()) {
            currentActiveClassName = activity.getComponentName().getClassName();
        }

        final String nextActiveClassName = packageName + ".MainActivity" + iconName;
        if (currentActiveClassName.equals(nextActiveClassName)) {
            promise.reject("ICON_ALREADY_USED", "Icon already in use");
            return;
        }

        try {
            activity.getPackageManager().setComponentEnabledSetting(
                new ComponentName(packageName, nextActiveClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            promise.resolve(iconName);
        } catch (Exception e) {
            promise.reject("SYSTEM_ERROR", e.getMessage());
            return;
        }

        // Queue current active class for cleanup.
        classesToKill.add(currentActiveClassName);

        currentActiveClassName = nextActiveClassName;

        activity.getApplication().registerActivityLifecycleCallbacks(this);

        iconHasChanged = true;
    }

    private void killPrevActiveClass() {
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

    @Override
    public void onActivityPaused(Activity activity) {
        killPrevActiveClass();
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
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }
}
