package com.csg.bluepigeon.util;

import java.io.File;

import org.tinylog.Logger;

/**********************************************
 * Blue Chirp provides local logging to the blue_pigeon folder in internal storage
 * The local log can be found in the Android/data/com.example.bluepigeon/files/blue_pigeon/logging/
 * The log file will be rotated daily and kept up to 30 days.
 * To change the configuration, go to app\src\main\resources\tinylog.properties
 **********************************************/
public class BlueChirp {
    private File blueChirpStorage;

    public BlueChirp(String pathname){
        blueChirpStorage = new File(pathname);

        // create logging storage if it does not exist
        if (!blueChirpStorage.exists()) {
            blueChirpStorage.mkdirs();
        }

        System.setProperty("tinylog.directory", blueChirpStorage.getAbsolutePath());
    }

    public void log(String msg, String level) {
        // local file logging
        switch(level) {
            case "trace":
                Logger.trace(msg);
                break;
            case "debug":
                Logger.debug(msg);
                break;
            case "info":
                Logger.info(msg);
                break;
            case "warn":
                Logger.warn(msg);
                break;
            case "error":
                Logger.error(msg);
                break;
        }
    }

}