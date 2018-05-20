package com.example.mala.textwall;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.ToggleButton;
import android.widget.Toast;
import android.view.View;


import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //per bluetooth
    public UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter mBluetoothAdapter=null;
    BluetoothSocket mmSocket=null;
    BluetoothDevice mmDevice=null;
    OutputStream outStream;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private NotificationBroadcastReceiver NotificationBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ToggleButton tgb = (ToggleButton) findViewById(R.id.connection);
        final EditText mac = (EditText)findViewById(R.id.mac_address);
        final Button intensity = (Button)findViewById(R.id.intensity);
        final Button sendButton = (Button) findViewById(R.id.send);
        final EditText mEdit = (EditText) findViewById(R.id.sendtext);
        final SeekBar speed = (SeekBar)findViewById(R.id.speedbar);
        final Button inEff = (Button)findViewById(R.id.effecti);
        final Button ouEff = (Button)findViewById(R.id.effecto);
        final Button invertBtn = (Button)findViewById(R.id.invert);
        final Button resetBtn = (Button)findViewById(R.id.resetbtn);

        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        NotificationBroadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.github.chagall.notificationlistenerexample");
        registerReceiver(NotificationBroadcastReceiver,intentFilter);

        //evento: tap sul togglebutton per la connessione del bluetooth
        tgb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (tgb.isChecked())//controlla che sia attivo il toggle button
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (mBluetoothAdapter == null)//controlla se il devices è supportato
                    {
                        // IL BLUETOOTH NON E' SUPPORTATO
                        Toast.makeText(MainActivity.this, "BlueTooth non supportato", Toast.LENGTH_LONG).show();
                        tgb.setChecked(false);
                    } else {
                        if (!mBluetoothAdapter.isEnabled())//controlla che sia abilitato il devices
                        {
                            //  NON E' ABILITATO IL BLUETOOTH
                            Toast.makeText(MainActivity.this, "BlueTooth non abilitato", Toast.LENGTH_LONG).show();
                            tgb.setChecked(false);
                        } else {
                            //  IL BLUETOOTH E' ABILITATO
                            String macAddress = mac.getText().toString();
                            mmDevice = mBluetoothAdapter.getRemoteDevice(macAddress); //MAC address del bluetooth di arduino
                            try {
                                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                            } catch (IOException e) {
                                tgb.setChecked(false);
                            }
                            try {
                                // CONNETTE IL DISPOSITIVO TRAMITE IL SOCKET mmSocket
                                mmSocket.connect();
                                outStream = mmSocket.getOutputStream();
                                Toast.makeText(MainActivity.this, "CONNECTED.", Toast.LENGTH_SHORT).show();//bluetooth è connesso
                                sendMessageBluetooth("*M"+"Connessione Stabilita"+"~");
                            } catch (IOException closeException) {
                                tgb.setChecked(false);
                                try {
                                    //TENTA DI CHIUDERE IL SOCKET
                                    mmSocket.close();
                                } catch (IOException ceXC) {

                                }
                                Toast.makeText(MainActivity.this, "Connessione non riuscita", Toast.LENGTH_SHORT).show();
                            }
                        }   //CHIUDE l'else di isEnabled
                    }  //CHIUDE l'else di mBluetoothAdapter == null
                }  // CHIUDE if (tgb.isChecked())
                else {
                    try {
                        //TENTA DI CHIUDERE LA SOCKET
                        outStream.close();
                        mmSocket.close();
                    } catch (IOException ceXC) {
                    }
                }
            } // CHIUDE public void OnClick(View view)
        }); //chiude il tgb.listener


        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                int progress = speed.getProgress();
                sendMessageBluetooth("*S" + progress + "~");
                String str = mEdit.getText().toString();
                sendMessageBluetooth("*M" + str  + "~");
            }

        });



        intensity.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                sendMessageBluetooth("*B~");
            }
        });

        invertBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                sendMessageBluetooth("*V~");
            }
        });

        inEff.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                sendMessageBluetooth("*I~");
            }
        });

        ouEff.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                sendMessageBluetooth("*O~");
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                sendMessageBluetooth("*R~");
            }
        });




    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NotificationBroadcastReceiver);
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
    public class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle n = intent.getExtras();
            if (n != null) {
                notifParsing(n);
            }
            else {
                sendMessageBluetooth("*M ~");
                Toast.makeText(MainActivity.this, "Lista notifiche svuotata!",  Toast.LENGTH_SHORT).show();

            }

        }
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
    public void runInfoActivity(View v)
    {
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        startActivity(intent);
    }
    public void notifParsing(Bundle bundle) {
        String notif_cat = bundle.getString("Category");
        String notif_title = bundle.getString("Title");
        String notif_message = bundle.getString("Message");
        String notif_key = bundle.getString("Key");
        if(TextUtils.equals(notif_cat, "call")) {
            sendMessageBluetooth("*MChiamata da: " + notif_title + "~");
        }
        else if(TextUtils.equals(notif_cat, "msg")) {
            sendMessageBluetooth("*MMsg da: " + notif_title + " : " + notif_message + "~");
        }
        else if(notif_message != null && notif_title != null) {
            sendMessageBluetooth("*M" + notif_title + " : " + notif_message + "~");
        }

        //Toast.makeText(MainActivity.this, notif_cat,  Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this, notif_title,  Toast.LENGTH_SHORT).show();
        //Toast.makeText(MainActivity.this, notif_message,  Toast.LENGTH_SHORT).show();
    }

    public void sendMessageBluetooth(String message)
    {
        if (outStream == null)
        {
            return;
        }
        byte[] msgBuffer = message.getBytes();
        try
        {
            outStream.write(msgBuffer);
        }
        catch (IOException e)
        {
            Toast.makeText(MainActivity.this,"Messaggio non inviato.", Toast.LENGTH_SHORT).show();
        }
    }

}
