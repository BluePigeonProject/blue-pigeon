package com.csg.bluepigeon.bluebeak;

import android.util.Log;

import com.csg.bluepigeon.util.Config;
import com.csg.bluepigeon.util.EncryptionManager;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BlueBeakHook implements IXposedHookLoadPackage {
    private static final String TAG="BlueBeakHook";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.bluetooth"))
            return;

        XposedHelpers.findAndHookMethod("com.android.bluetooth.opp.BluetoothOppReceiveFileInfo", lpparam.classLoader, "choosefilename", String.class, new XC_MethodHook(){
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("BlueBeakHook is ready");
                Log.d(TAG, "BlueBeakHook is ready");
                String hint = (String) param.args[0];
                String passphrase = Config.getSecret();

                // Picky beak always expects only 71-length wormies! "BP-64rand.txt"
                if (hint.length() != 71) {
                    param.args[0] = null;
                    XposedBridge.log("Rejecting bad wormie as it does not meet satisfactory requirements (not length 71) : " + hint);
                    Log.d(TAG, "Rejecting bad wormie as it does not meet satisfactory requirements (not length 71) : " + hint);
                } else if (exists(hint)){
                    param.args[0] = null;
                    XposedBridge.log("Pigeon doesn't like repeated wormies! Throwing it away : " + hint);
                    Log.d(TAG, "Pigeon doesn't like repeated wormies! Throwing it away : " + hint);
                } else if(!EncryptionManager.verify(hint, passphrase)){
                    param.args[0] = null;
                    XposedBridge.log("Rejecting bad wormie as it does not meet satisfactory requirements (failed verification) : " + hint);
                    Log.d(TAG, "Rejecting bad wormie as it does not meet satisfactory requirements (failed verification) : " + hint);
                } else {
                    XposedBridge.log("Incoming wormie has been consumed by Blue Beak : " + hint);
                    Log.d(TAG, "Incoming wormie has been consumed by Blue Beak : " + hint);
                }

            }
        });
    }

    private static boolean exists(String filename){
        boolean fileExists = new File(Config.BLUETOOTH_DIR_PATH+filename).exists() || new File(Config.DOWNLOAD_DIR_PATH+filename).exists() || new File(Config.BACKUP_DIR_PATH+filename).exists() ;
        return fileExists;
    }

}