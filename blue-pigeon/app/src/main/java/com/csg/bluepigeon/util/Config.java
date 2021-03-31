package com.csg.bluepigeon.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Config {
    protected static String TAG = "Config";

    // File directories
    public static String INTERNAL_STORAGE = Environment.getExternalStorageDirectory().toString();
    public static String BLUETOOTH_DIR_PATH = INTERNAL_STORAGE + "/bluetooth/";
    public static String DOWNLOAD_DIR_PATH = INTERNAL_STORAGE + "/Download/";
    public static String BACKUP_DIR_PATH = INTERNAL_STORAGE + "/blue_pigeon/backup/";
    public static String LOGGING_DIR_PATH = INTERNAL_STORAGE + "/blue_pigeon/logging/";
    public static String CONFIG_DIR_PATH = INTERNAL_STORAGE +"/blue_pigeon/config/";
    public static String SECRET_PASSPHRASE_PATH = CONFIG_DIR_PATH + "/key.txt";

    // SharedPreferences variables for saving/loading c2 endpoint
    public static final String MEMORY_SLOT = "slot1";
    public static final String ENDPOINT_DATA = "c2url";

    public static final String DEFAULT_URL = "https://YourBlueCoopURLHere.com/";
    private static String TEMP_PASSPHRASE = "testkey";

    // Config Getters and Setters
    public static String getSecret() {
        String secret = TEMP_PASSPHRASE; //default key, user is encouraged to change it.
        File f = new File(SECRET_PASSPHRASE_PATH);

        //file already there
        if (f.exists()) {
            try {
                secret = new String(Files.readAllBytes(Paths.get(f.toURI())));
            } catch (Exception e) {
                Log.d(TAG, "exception occurred trying to read secret file");
            }
        } else { //file not created yet
            try {
                Files.write(f.toPath(), secret.getBytes());
            } catch (Exception e) {
                Log.d(TAG,"exception occurred trying to write to secret file");
            }
        }
        return secret;
    }

    public static void setSecret(String secret) {
        File f = new File(SECRET_PASSPHRASE_PATH);
        try {
            Files.write(f.toPath(), secret.getBytes());
        } catch (Exception e) {
            Log.d(TAG, "exception occurred trying to update to secret file");
        }
    }

}
