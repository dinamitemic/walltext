package com.example.mala.notify2nd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Button;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final ToggleButton tgb =(ToggleButton) findViewById(R.id.connection);
        final Button tb = (Button) findViewById(R.id.buttontext);
        final Button nb = (Button) findViewById(R.id.buttonnotific);
        final Button tetris= (Button) findViewById(R.id.tetris);


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
                    Toast.makeText(MainActivity.this, "BlueTooth non supportato", Toast.LENGTH_LONG).show();
                tgb.setChecked(false);
                }
                else
                {
                    if (!mBluetoothAdapter.isEnabled())//controlla che sia abilitato il devices
                    {
                    //  NON E' ABILITATO IL BLUETOOTH
                    Toast.makeText(MainActivity.this, "BlueTooth non abilitato", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainActivity.this, "ON",  Toast.LENGTH_SHORT).show();//bluetooth è connesso
                            sendMessageBluetooth("*MCIAO.~");
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
                            Toast.makeText(MainActivity.this, "Connessione non riuscita",  Toast.LENGTH_SHORT).show();
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


    }

    public void startTextActivity(View v)
    {
        Intent intent = new Intent(getApplicationContext(), TextActivity.class);
        startActivity(intent);
    }

    public void startNotificationActivity(View v)
    {
        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        startActivity(intent);
    }

    public void startTetris(View v)
    {
        Intent intent = new Intent(getApplicationContext(), TetrisActivity.class);
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
            Toast.makeText(MainActivity.this,"Messaggio non inviato.", Toast.LENGTH_SHORT).show();
        }
    }
}
