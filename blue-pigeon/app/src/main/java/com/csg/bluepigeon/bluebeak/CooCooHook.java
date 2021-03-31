package com.csg.bluepigeon.bluebeak;

import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CooCooHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.settings"))
            return;

        XposedHelpers.findAndHookMethod("com.android.settingslib.bluetooth.BluetoothDiscoverableTimeoutReceiver", lpparam.classLoader, "setDiscoverableAlarm", Context.class, long.class, new XC_MethodHook(){

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("CooCooHook is ready");
                Long newAlarmTime = (long) param.args[1] + 86400000l;
                param.args[1] = newAlarmTime;
                XposedBridge.log("The updated setDiscoverableAlarm alarmTime is " + param.args[1]);
            }
        });
    }
}
