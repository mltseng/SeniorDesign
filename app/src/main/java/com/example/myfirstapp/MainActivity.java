package com.example.myfirstapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import static android.content.res.Resources.getSystem;

public class MainActivity extends AppCompatActivity {
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    final byte delimiter = 33;
    int readBufferPosition = 0;

    public void sendBtMsg(String msg2send){
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
        try{
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if(!mmSocket.isConnected()){
                mmSocket.connect();
            }

            String msg = msg2send;
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    private Button mBtnLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();
        final TextView myLabel = (TextView) findViewById(R.id.btResult);
        final Button LED_btn = (Button) findViewById(R.id.LED);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final class workerThread implements Runnable {
            private String btMsg;

            public workerThread(String msg){
                btMsg = msg;
            }
            public void run(){
                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted()){
                    int bytesAvailable;
                    boolean workDone = false;
                    try{
                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0){
                            byte[] packetBytes = new byte[bytesAvailable];
                            Log.e("Aquarium recv bt", "bytes available");
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0;i<bytesAvailable;i++){
                                byte b = packetBytes[i];
                                if(b == delimiter){
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable(){
                                        public void run(){
                                            myLabel.setText(data);
                                        }
                                    });
                                    workDone = true;
                                    break;
                                }
                                else{
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                            if(workDone == true){
                                mmSocket.close();
                                break;
                            }
                        }
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        LED_btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                (new Thread(new workerThread("lightOn"))).start();
            }
        });
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBluetooth = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device: pairedDevices){

                if(device.getName().equals("PiLock")){
                    Log.e("Aquarium", device.getName());
                    mmDevice = device;
                    break;
                }
            }
        }
    }
}
