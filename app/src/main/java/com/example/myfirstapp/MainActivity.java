package com.example.myfirstapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button lightOnButton = (Button) findViewById(R.id.LED);
        final Button lightOffButton = (Button) findViewById(R.id.NFC);
        //start light on button handler
        lightOnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on temp button click
                System.out.println("Okay");
                BTConnect("h");
            }
        });

        lightOffButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on temp button click
                //System.out.println("Okay");
                BTConnect("g");
            }
        });
    }
    public void BTConnect(String msg){
        //final UUID uuid = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocketWrapper socket = null;
        final BluetoothConnector b;
        BluetoothDevice d = null;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device: pairedDevices){
                if(device.getName().equals("raspberrypi")){
                    d = device;
                    System.out.println("Yess");
                    break;
                }
            }
            b = new BluetoothConnector(d, false, mBluetoothAdapter, null);
            try {
                socket = b.connect();
                sendBtMsg(msg, socket);
                socket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    public void sendBtMsg(String msg2send, BluetoothSocketWrapper socket){
        byte[] buffer = new byte[256];
        int bytes;
        try{
            OutputStream mmOut = socket.getOutputStream();
            mmOut.write(msg2send.getBytes());
            InputStream mmIn = socket.getInputStream();
            DataInputStream in = new DataInputStream(mmIn);
            bytes = in.read(buffer);
            String readMess = new String(buffer, 0, bytes);
            System.out.println(readMess);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}