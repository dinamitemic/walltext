package com.example.mala.notify2nd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class TextActivity extends AppCompatActivity {

    public UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter mBluetoothAdapter=null;
    BluetoothSocket mmSocket=null;
    BluetoothDevice mmDevice=null;
    OutputStream outStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        final ToggleButton tgb =(ToggleButton) findViewById(R.id.connect);



        //evento: tap sul togglebutton per la connessione del bluetooth
        tgb.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if(tgb.isChecked())//controlla che sia attivo il toggle button
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null)//controlla se il devices è supportato
                        {
                            // IL BLUETOOTH NON E' SUPPORTATO
                            Toast.makeText(TextActivity.this, "BlueTooth non supportato", Toast.LENGTH_LONG).show();
                            tgb.setChecked(false);
                        }
                        else
                        {
                            if (!mBluetoothAdapter.isEnabled())//controlla che sia abilitato il devices
                            {
                                //  NON E' ABILITATO IL BLUETOOTH
                                Toast.makeText(TextActivity.this, "BlueTooth non abilitato", Toast.LENGTH_LONG).show();
                                tgb.setChecked(false);
                            }
                            else
                            {
                                //  IL BLUETOOTH E' ABILITATO
                                mmDevice=mBluetoothAdapter.getRemoteDevice("98:D3:31:FC:16:8C"); //MAC address del bluetooth di arduino
                                try
                                {
                                    mmSocket=mmDevice.createRfcommSocketToServiceRecord(uuid);
                                }
                                catch (IOException e)
                                {
                                    tgb.setChecked(false);
                                }
                                try
                                {
                                    // CONNETTE IL DISPOSITIVO TRAMITE IL SOCKET mmSocket
                                    mmSocket.connect();
                                    outStream = mmSocket.getOutputStream();
                                    Toast.makeText(TextActivity.this, "ON",  Toast.LENGTH_SHORT).show();//bluetooth è connesso
                                    sendMessageBluetooth("CAZZO.PUTTANA EVA E TUTTI QUANTI CIAO CIAO CAIO");
                                }
                                catch (IOException closeException)
                                {
                                    tgb.setChecked(false);
                                    try
                                    {
                                        //TENTA DI CHIUDERE IL SOCKET
                                        mmSocket.close();
                                    }
                                    catch (IOException ceXC)
                                    {

                                    }
                                    Toast.makeText(TextActivity.this, "Connessione non riuscita",  Toast.LENGTH_SHORT).show();
                                }
                            }   //CHIUDE l'else di isEnabled
                        }  //CHIUDE l'else di mBluetoothAdapter == null
                    }  // CHIUDE if (tgb.isChecked())
                    else
                    {
                        try
                        {
                            //TENTA DI CHIUDERE IL SOCKET
                            outStream.close();
                            mmSocket.close();
                        }
                        catch (IOException ceXC){}
                    }
                } // CHIUDE public void OnClick(View view)
            }); //chiude il tgb.listener


        final Button sendButton = (Button) findViewById(R.id.send);
        final EditText mEdit = (EditText) findViewById(R.id.sendtext);

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.v("EditText", mEdit.getText().toString());
                //sendTextBluetooth("Ciao mich mich.");
            }
        });

    }

    public void startMainActivity(View v)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
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
            Toast.makeText(TextActivity.this,"Messaggio non inviato.", Toast.LENGTH_SHORT).show();
        }
    }


}
