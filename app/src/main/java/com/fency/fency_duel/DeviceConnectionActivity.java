package com.fency.fency_duel;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

//https://github.com/OmarAflak/Bluetooth-Library
import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.BluetoothCallback;
import me.aflak.bluetooth.DeviceCallback;
import me.aflak.bluetooth.DiscoveryCallback;

public class DeviceConnectionActivity extends FencyActivity implements View.OnClickListener {

    private Bluetooth bluetooth;

    private static final int SERVER_WAIT_TIME = 2000; //milliseconds
    public static int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_REQUEST = 119;
    private static final int CHOOSE_SERVER_REQUEST = 120;
    private static final int VISIBILITY_DURATION = 60; //seconds
    private static final String INTERRUPTION_STR = "STOP";

    private String myMacAddress, opponentMacAddress;
    private boolean nfcCompatible, isBtReady;
    private NfcAdapter nfcAdapter;
    private BluetoothAdapter BTAdapter;
    private View btnNfc, btnManualBT;
    private ScrollView scrollListView;
    private ListView listView;

    List<BluetoothDevice> pairedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        cntFullScreen = findViewById(R.id.container_connection);
        btnManualBT = findViewById(R.id.btnManualBT);
        scrollListView = findViewById(R.id.scrollListView);
        listView = findViewById(R.id.listView);

        isBtReady = false;

        //START
        boolean res = checkCompatibility();

        if(!res){
            Toast.makeText(getApplicationContext(),"device not compatible with duel mode",Toast.LENGTH_LONG).show();
            // quit duel-mode
        }
        else {
            btnManualBT.setOnClickListener(this);

            if (!BTAdapter.isEnabled()){
                makeBtPowerOnRequest();
            }
            else {
                initialize();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bluetooth!= null) {
            bluetooth.onStart();
            bluetooth.enable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetooth!= null) {
            bluetooth.onStop();
        }
    }

    public boolean checkCompatibility() {
        boolean res = true;

        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Phone does not support NFC
        if (nfcAdapter == null) {
            nfcCompatible = false;
            Toast.makeText(getApplicationContext(),"NFC not supported",Toast.LENGTH_LONG).show();
        }
        else nfcCompatible = true;

        // Initialize Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BTAdapter = bluetoothManager.getAdapter();

        // Phone does not support Bluetooth
        if (BTAdapter == null) {
            res = false;
            new AlertDialog.Builder(getApplicationContext())
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return res;
    }
    private void makeBtPowerOnRequest(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        setDeviceDiscoverable();

        // Set power-on callback
        final BroadcastReceiver powerOnReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF:
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            break;
                        case BluetoothAdapter.STATE_ON:
                            initialize();
                            unregisterReceiver(this);
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            Toast.makeText(getApplicationContext(),"BT opening...",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(powerOnReceiver, filter);
    }

    private void setDeviceDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }

    private void initialize() {
        Toast.makeText(getApplicationContext(),"Initialize...",Toast.LENGTH_SHORT).show();

        bluetooth = new Bluetooth(this);
        setDeviceDiscoverable();

        myMacAddress = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(), "bluetooth_address");

        if(nfcCompatible){
            if (!nfcAdapter.isEnabled()){
                Toast.makeText(getApplicationContext(),"Please enable NFC",Toast.LENGTH_LONG).show();
                startActivity( new Intent(Settings.ACTION_NFC_SETTINGS));

            }
            else if (!nfcAdapter.isNdefPushEnabled()){
                Toast.makeText(getApplicationContext(),"Please enable Android Beam",Toast.LENGTH_LONG).show();
                startActivity( new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
            }
            else{
                //JSONObject jsonObj = (JSONObject)JSONObject.wrap(myMacAddress);
                String payload = myMacAddress;
                String mimeType = "application/com.fency.fency_duel";
                byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));

                nfcAdapter.setNdefPushMessage( new NdefMessage(
                        new NdefRecord[] {
                                /*createTextRecord(null, myMacAddress)*/
                                // Create the NFC payload.
                                new NdefRecord(
                                        NdefRecord.TNF_MIME_MEDIA,
                                        mimeBytes,
                                        new byte[0],
                                        payload.getBytes()
                                ),
                                // Add the AAR (Android Application Record)
                                NdefRecord.createApplicationRecord("com.fency.fency_duel")
                        }), this);

                Toast.makeText(getApplicationContext(),"NFC message ready", Toast.LENGTH_SHORT).show();
            }
        }
        bluetooth.setBluetoothCallback(new BluetoothCallback() {
            @Override
            public void onBluetoothTurningOn() {}

            @Override
            public void onBluetoothOn() {}

            @Override
            public void onBluetoothTurningOff() {}

            @Override
            public void onBluetoothOff() {}

            @Override
            public void onUserDeniedActivation() {
                // when using bluetooth.showEnableDialog()
                // you will also have to call bluetooth.onActivityResult()
            }
        });

        bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
            @Override
            public void onDiscoveryStarted() {
                Toast.makeText(getApplicationContext(),"scanning...", Toast.LENGTH_SHORT).show();
                //setDeviceDiscoverable();
            }
            @Override
            public void onDiscoveryFinished() {
                Toast.makeText(getApplicationContext(),"end of scan.", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                Toast.makeText(getApplicationContext(),"Found device: " + device.getAddress(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDevicePaired(BluetoothDevice device) {
                Toast.makeText(getApplicationContext(),"Paired to: " + device.getAddress(), Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDeviceUnpaired(BluetoothDevice device) {}
            @Override
            public void onError(String message) {}
        });

        bluetooth.setDeviceCallback( new DeviceCallback() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {}
            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) {
                Toast.makeText(getApplicationContext(), "Device disconnected\n MAC: "+device.getAddress(), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onMessage(String message) {}
            @Override
            public void onError(String message) {}
            @Override
            public void onConnectError(BluetoothDevice device, String message) {}
        });

        listPairedDevices();

        isBtReady = true;
    }//end initialize()

    private void listPairedDevices(){
        pairedDevices = bluetooth.getPairedDevices();
        // If there are paired devices, add them to list
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                TextView tv = new TextView(getApplicationContext());
                String text = device.getName() + " : " + device.getAddress();
                tv.setText(text);
                listView.addHeaderView(tv);
            }
        }
    }

    private void scanToConnect() {
        Toast.makeText(getApplicationContext(),"scan to connect", Toast.LENGTH_SHORT).show();

        bluetooth.startScanning();
    }

    private String getOpponentMacAddress(){
        return opponentMacAddress;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.btnManualBT:
                if(!isBtReady)
                    makeBtPowerOnRequest();
                else {
                    Random rnd = new Random();
                    int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                    btnManualBT.setBackgroundColor(color);
                    //connectAsServer();
                    scanToConnect();
                    listPairedDevices();
                }
                break;
        }
    }
}
