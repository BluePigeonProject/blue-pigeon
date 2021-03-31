package com.csg.bluepigeon.util;

import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionManager {
    private static final String TAG="EncryptionManager";
    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /* Verifies the filename against the provided passphrase
    * @param filename, passphrase
    * @return boolean
    * @throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
    */
    public static boolean verify(String filename, String passphrase) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        // Filename notation:
        // "BP-"-[--32--Trimmed-SHA256-Hash]-[--32--Random-Hex]-".txt"
        // Sample filename: "BP-e2211d9e19f1669d1a09ea4828d3e2bc171a10bce71cafb53a64179f497273ea.txt"
        if (filename.length() != 71) {
            Log.d(TAG, "Received filename is not 71 characters: " + filename);
            return false;
        }
        filename = filename.substring(3); // strip off BP-
        String providedHash = filename.substring(0, 32);
        String rand = filename.substring(32, 64);
        SecretKeySpec key = new SecretKeySpec((passphrase).getBytes("UTF-8"), "HmacSHA512");
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(key);
        byte[] bytes = mac.doFinal(rand.getBytes());
        String computedHash = Hex.encodeHexString(bytes).substring(0, 32); // we will take the first 32 characters to save some space.

        if (computedHash.equals(providedHash)) {
            Log.d(TAG, "Computed hash matches! : " + computedHash);
            return true;
        } else {
            Log.d(TAG, "Computed hash does not match provided hash: " + computedHash + " vs " + providedHash);
            return false;
        }
    }


    /* Generates a 32-length nonce in the form of a hex string
    * @return String
    */
    public static String generateNonce() {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 32; i++) {
            sb.append(HEX_DIGITS[random.nextInt(16)]);
        }
        return sb.toString();
    }

}
