package com.csg.bluepigeon.bluebeak;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoPopupHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.bluetooth"))
            return;

        XposedHelpers.findAndHookMethod("com.android.bluetooth.opp.BluetoothOppManager", lpparam.classLoader, "isWhitelisted", String.class, new XC_MethodHook(){

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            XposedBridge.log("NoPopupHook is ready: isWhitelisted method is hooked");
            String address = (String) param.args[0];
            XposedBridge.log("NoPopupHook: Incoming file transfer request's device address is " + address);
            param.setResult(true);
            }
        });
    }
}
