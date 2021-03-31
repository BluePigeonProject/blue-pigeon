package com.csg.bluepigeon.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.csg.bluepigeon.ui.MainActivity;
import com.csg.bluepigeon.R;
import com.csg.bluepigeon.util.Config;

/**
 * C2Dialog is a popup modal to allow the user to change the C2's endpoint.
 */
public class C2Dialog extends AppCompatDialogFragment {

    private EditText editUrl;
    private final String TAG = "C2Dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle saveInstanceState) {

        // Setting up modal
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.c2_dialog, null);
        editUrl = (EditText) view.findViewById(R.id.editUrl);

        // load the previously saved endpoint
        loadData();

        // Begin building the modal with buttons
        builder.setView(view)
                .setTitle("Blue Coop endpoint")
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

    public void saveData() {
        // Initializing SharedPreferences data
        SharedPreferences sharedPref = getContext().getSharedPreferences(Config.MEMORY_SLOT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Begin saving the new editUrl value
        MainActivity.BLUE_COOP_URL = editUrl.getText().toString();
        editor.putString(Config.ENDPOINT_DATA, editUrl.getText().toString());
        editor.apply();
        Log.i(TAG, "Blue Coop endpoint updated");

        Toast.makeText(getContext(), "Blue Coop Endpoint Updated", Toast.LENGTH_SHORT).show();
    }

    public void loadData() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(Config.MEMORY_SLOT, Context.MODE_PRIVATE);
        editUrl.setText(sharedPref.getString(Config.ENDPOINT_DATA, Config.DEFAULT_URL));
        Log.i(TAG, "Blue Coop endpoint loaded successfully");
    }
}
