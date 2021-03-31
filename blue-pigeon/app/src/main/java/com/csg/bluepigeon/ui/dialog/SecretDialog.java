package com.csg.bluepigeon.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.csg.bluepigeon.R;
import com.csg.bluepigeon.util.Config;

/**
 * SecretDialog is a popup modal to allow the user to change the secret encryption passphrase.
 */
public class SecretDialog extends AppCompatDialogFragment {

    private EditText editSecret;
    private final String TAG = "SecretDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle saveInstanceState) {

        // Setting up modal
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.secret_dialog, null);
        editSecret = (EditText) view.findViewById(R.id.editSecret);

        loadData();

        builder.setView(view)
                .setTitle("Secret Passphrase")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveData();
                    }
                });

        return builder.create();
    }

    public void saveData(){
        String newSecret = editSecret.getText().toString();
        Config.setSecret(newSecret);
        Toast.makeText(getContext(), "Secret passphrase updated", Toast.LENGTH_SHORT).show();
        editSecret.setText(newSecret);
    }

//    version 2, attempted to use keystore, does not work - pending future fix!
//    public void saveData() {
//        // Initializing SharedPreferences data
//        SharedPreferences sharedPref = getContext().getSharedPreferences(MainActivity.MEMORY_SLOT, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//
//        // Begin saving the new editUrl value
//        MainActivity.SECRET = editSecret.getText().toString();
//
//        Store store = new Store(getContext());
//        if (!store.hasKey(MainActivity.SECRET_DATA)) {
//            SecretKey key = store.generateSymmetricKey(MainActivity.SECRET_DATA, null);
//        }
//        // Get key
//        SecretKey key = store.getSymmetricKey(MainActivity.SECRET_DATA, null);
//        // Encrypt/Decrypt data
//        Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
//        String encryptedData = crypto.encrypt(editSecret.getText().toString(), key);
//        editor.putString(MainActivity.SECRET_DATA, encryptedData);
//        editor.apply();
//        Log.i(TAG, "secret passphrase updated");
//
//        Toast.makeText(getContext(), "Secret passphrase updated", Toast.LENGTH_SHORT).show();
//    }

    public void loadData() {
       // SharedPreferences sharedPref = getContext().getSharedPreferences(MainActivity.MEMORY_SLOT, Context.MODE_PRIVATE);
        editSecret.setText(Config.getSecret());
        Log.i(TAG, "Secret passphrase loaded successfully");
    }
}
