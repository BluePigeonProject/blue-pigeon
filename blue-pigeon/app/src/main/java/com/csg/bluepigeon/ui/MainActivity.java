
package com.csg.bluepigeon.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.csg.bluepigeon.R;
import com.csg.bluepigeon.util.BlueChirp;
import com.csg.bluepigeon.ui.dialog.C2Dialog;
import com.csg.bluepigeon.ui.dialog.SecretDialog;
import com.csg.bluepigeon.ui.foreground.ForegroundBuilder;
import com.csg.bluepigeon.util.Config;

import java.io.File;

/**
 * Main Activity for Blue Pigeon
 */
public class MainActivity extends AppCompatActivity {

    public static String BLUE_COOP_URL;
    public static String SECRET_PASSPHRASE;

    private final String TAG = "MainActivity";
    private Button btn_stopBlueCoop;
    private Button btn_startBlueCoop;
    private ImageView btn_url;
    private ImageView btn_secret;
    private BlueChirp blueChirp;

    public static BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_stopBlueCoop = (Button) findViewById(R.id.btn_stopBlueCoop);
        btn_stopBlueCoop.setOnClickListener(blueCoopStopListener);
        btn_startBlueCoop = (Button) findViewById(R.id.btn_startBlueCoop);
        btn_startBlueCoop.setOnClickListener(blueCoopStartListener);
        btn_url = (ImageView) findViewById(R.id.btn_url);
        btn_url.setOnClickListener(urlListener);
        btn_secret = (ImageView) findViewById(R.id.btn_secret);
        btn_secret.setOnClickListener(secretListener);
        context = this;

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        // Check for android permissions
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Continuously attempt to create the required folders, will keep attempting until permission is granted
        boolean directoriesCreated = createDirectories();
        while (!directoriesCreated) {
            directoriesCreated = createDirectories();
        }

        // Set up BlueChirp aka tinylog
        blueChirp = new BlueChirp(Config.LOGGING_DIR_PATH);

        // Load C2 Server Url
        loadServerUrl();
        // Load secret passphrase
        loadSecret();

    }

    /**********************************************
     * Start listening for messages
     **********************************************/
    View.OnClickListener blueCoopStartListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            blueChirp.log("Blue pigeon is starting up", "info");

            // Turn bluetooth on if it is turned off
            if(!bAdapter.isEnabled())
                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);

            // Make device discoverable on bluetooth for 24 hours (using BluetoothDiscoverModule)
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
            discoverableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(discoverableIntent);

            // Start up foreground notification
            startForegroundService();
            Toast.makeText(context, "Started!", Toast.LENGTH_SHORT).show();
        }
    };

    /**********************************************
     * Stop listening for messages
     **********************************************/
    View.OnClickListener blueCoopStopListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            blueChirp.log("Blue Pigeon is stopping now", "info");

            // Stop up foreground notification
            stopForegroundService();

            // Turn bluetooth off
            Toast.makeText(context, "Stop Battery Optimization", Toast.LENGTH_SHORT).show();
            if(bAdapter.isEnabled()) {
                bAdapter.disable();
            }
        }
    };

    /**********************************************
     * Upon click, a modal will be opened for user to input their Blue Coop/c2 endpoint
     **********************************************/
    View.OnClickListener urlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Open c2Dialog
            C2Dialog c2Dialog = new C2Dialog();
            c2Dialog.show(getSupportFragmentManager(), "c2");
        }
    };

    /**********************************************
     * Upon click, a modal will be opened for user to input their secret passphrase
     **********************************************/
    View.OnClickListener secretListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Open secretDialog
            SecretDialog secretDialog = new SecretDialog();
            secretDialog.show(getSupportFragmentManager(), "secret");
        }
    };

    /**********************************************
     * Check for permissions
     **********************************************/
    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**********************************************
     * Start Foreground service
     **********************************************/
    private void startForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundBuilder.class);
        startService(serviceIntent);
    }

    /**********************************************
     * Stop Foreground service
     **********************************************/
    private void stopForegroundService() {
        Intent serviceIntent = new Intent(this, ForegroundBuilder.class);
        stopService(serviceIntent);
    }

    /**********************************************
     * Load c2 endpoint that was previously saved
     **********************************************/
    private void loadServerUrl() {
        SharedPreferences sharedPref = getSharedPreferences(Config.MEMORY_SLOT, Context.MODE_PRIVATE);
        BLUE_COOP_URL = sharedPref.getString(Config.ENDPOINT_DATA, Config.DEFAULT_URL);
    }

    /**********************************************
     * Load secret passphrase that was previously saved
     **********************************************/
    private void loadSecret(){
        SECRET_PASSPHRASE = Config.getSecret();
    }

    /**********************************************
     * Create required directories
     **********************************************/
    private boolean createDirectories() {
        boolean created = false;

        // Create logging directory
        File loggingDir = new File(Config.LOGGING_DIR_PATH);
        if (!loggingDir.exists()) {
            loggingDir.mkdirs();
        }
        // Create backup directory
        File backupDir = new File(Config.BACKUP_DIR_PATH);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Create config directory
        File configDir = new File(Config.CONFIG_DIR_PATH);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        created = loggingDir.exists() && backupDir.exists() && configDir.exists();
        return created;
    }
 }