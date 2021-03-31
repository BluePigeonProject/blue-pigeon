package com.csg.bluepigeon.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class BlueUtils {
    final static String TAG = "BlueUtils";
    public static final String FILE_EXT = ".txt";
    public static final String BLUE_REQ_PREFIX = "BP-";
    public static final String BLUE_RESP_PREFIX = "TBD in Blue Pigeon 2.0";
    private static BlueChirp blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);

    /**
     * Converts mac address string to colonized form (eg "112233445566" to "11:22:33:44:55:66")
     * @param addrStr
     * @return string eg "11:22:33:44:55:66"
     */
    public static String addColons(String addrStr){
        StringBuilder addrStrWithColon = new StringBuilder();

        for (int i = 0; i < addrStr.length(); i++) {
            addrStrWithColon.append(addrStr.charAt(i));
            if (i%2 != 0 && i != addrStr.length() - 1){
                addrStrWithColon.append(":");
            }
        }
        return addrStrWithColon.toString();
    }

    /**
     * Takes Blue Coop's HTTP response string and package it into an BlueResponse serialised string
     * @param response
     * @param url
     * @return packaged json string
     */
    public static String buildBlueResponseString(String response, String url) throws JSONException {
        Log.d(TAG, "buildBlueResponseString: building blue response json string");
        JSONObject responseJson = new JSONObject();
        String result = "";
        try {
            responseJson.put("Url", url);
            if (response.isEmpty()) {
                response = "";
            }
            responseJson.put("Payload", response);
            responseJson.put("CreatedTime", Calendar.getInstance().getTime().toString());
            result += responseJson.toString().replaceAll("\\\\","");
        } catch (JSONException e) {
            blueChirp.log("buildBlueResponseString: failed to build blue response json string", "error");
            throw e;
        }
        blueChirp.log("buildBlueResponseString: Successfully built blue response json string", "debug");
        return result;
    }
}