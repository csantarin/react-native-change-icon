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

    private Boolean iconChanged = false;

    private String componentClass = "";

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

        if (this.componentClass.isEmpty()) {
            this.componentClass = activity.getComponentName().getClassName();
        }

        String currentIcon = this.componentClass.split("MainActivity")[1];

        promise.resolve(currentIcon.isEmpty() ? null : currentIcon);
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

        if (this.componentClass.isEmpty()) {
            this.componentClass = activity.getComponentName().getClassName();
        }

        final String activeClass = this.packageName + ".MainActivity" + iconName;
        if (this.componentClass.equals(activeClass)) {
            promise.reject("ICON_ALREADY_USED", "Icon already in use");
            return;
        }

        try {
            activity.getPackageManager().setComponentEnabledSetting(
                new ComponentName(this.packageName, activeClass),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            promise.resolve(iconName);
        } catch (Exception e) {
            promise.reject("SYSTEM_ERROR", e.getMessage());
            return;
        }

        this.classesToKill.add(this.componentClass);

        this.componentClass = activeClass;

        activity.getApplication().registerActivityLifecycleCallbacks(this);

        iconChanged = true;
    }

    private void completeIconChange() {
        if (!iconChanged) return;

        final Activity activity = getCurrentActivity();
        if (activity == null) return;

        classesToKill.forEach((cls) -> activity.getPackageManager().setComponentEnabledSetting(
            new ComponentName(this.packageName, cls),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        ));

        classesToKill.clear();

        iconChanged = false;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        completeIconChange();
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
