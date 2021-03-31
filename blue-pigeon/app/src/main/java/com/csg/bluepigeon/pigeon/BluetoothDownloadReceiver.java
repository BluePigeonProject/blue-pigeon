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
    final String TAG = "BTDownloadReceiver";
    private BlueChirp blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract Blue Pigeon Address
        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        String bpAddress = bluetoothDevice.getAddress();
        String bpName = bluetoothDevice.getName();
        String blueCoopName = BluetoothAdapter.getDefaultAdapter().getName();

        // Put into Data
        Data bpData = new Data.Builder()
                .putString("address", bpAddress)
                .putString("bpName", bpName)
                .putString("blueCoopName", blueCoopName)
                .build();

        // start blue pigeon listener
        blueChirp.log("onReceive: starting blue pigeon on address " + bpAddress, "info");

        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueue(new OneTimeWorkRequest.Builder(BluePigeonListener.class).setInputData(bpData).build());
    }
}
