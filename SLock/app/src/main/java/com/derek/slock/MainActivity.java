package com.derek.slock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button LED;
    Button NFC;
    Button QR;
    ImageView img;
    TextView text;
    //byte[] qbuffer = new byte[8192];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LED = (Button) findViewById(R.id.LED);
        NFC = (Button) findViewById(R.id.NFC);
        QR = (Button) findViewById(R.id.QR);
        img = (ImageView) findViewById(R.id.imgView);
        text = (TextView) findViewById(R.id.btResult);

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.bad);
        img.setImageBitmap(icon);

        LED.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                System.out.println("Okay");
                BTConnect("h");
            }
        });

        NFC.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BTConnect("g");
            }
        });

        QR.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                BTConnect("q");
            }
        });
    }
    public void BTConnect(String msg){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocketWrapper socket = null;
        final BluetoothConnector b;
        BluetoothDevice d = null;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device: pairedDevices){
                if(device.getName().equals("raspberrypi")){
                    d = device;
                    break;
                }
            }
            b = new BluetoothConnector(d, false, mBluetoothAdapter, null);
            try {
                socket = b.connect();
                if(msg.equals("q")){
//                    Bitmap bit = BitmapFactory.decodeByteArray(qbuffer, 0, qbuffer.length);
                    img.setImageBitmap(generateQR("Hello, It's Me Mario"));
                }
                else{
                    String server_words = sendBtMsg(msg, socket);
                    text.setText(server_words);
                }
                socket.close();
            }catch(IOException|WriterException e){
                e.printStackTrace();
            }
        }
    }
    public String sendBtMsg(String msg2send, BluetoothSocketWrapper socket){
        byte[] buffer = new byte[256];
        int bytes;
        try{
            OutputStream mmOut = socket.getOutputStream();
            mmOut.write(msg2send.getBytes());
            InputStream mmIn = socket.getInputStream();
            DataInputStream in = new DataInputStream(mmIn);
            bytes = in.read(buffer);
            String readMess = new String(buffer, 0, bytes);
            //text.setText(readMess);
            return readMess;
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
//    public void getQR(String msg2send, BluetoothSocketWrapper socket){
//        int byteNo;
//        int byteNo2;
//        int bsize = 7340;
//        System.out.println("Here");
//        try{
//            OutputStream mmOut = socket.getOutputStream();
//            mmOut.write(msg2send.getBytes());
//            InputStream mmIn = socket.getInputStream();
//            byteNo = mmIn.read(qbuffer);
//            if(byteNo != - 1){
//                byteNo2 = byteNo;
//                while(byteNo2 != bsize){
//                    bsize = bsize - byteNo2;
//                    byteNo2 = mmIn.read(qbuffer, byteNo, bsize);
//                    if(byteNo2 == -1){
//                        break;
//                    }
//                }
//            }
//            System.out.println("Done");
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//    }
    Bitmap generateQR(String s) throws WriterException{
        BitMatrix result;
        int w = 400;
        int h = 400;
        int WHITE = 0xFFFFFFFF;
        int BLACK = 0xFF000000;

        try{
            result = new MultiFormatWriter().encode(s, BarcodeFormat.QR_CODE, w, h, null);
        }
        catch(IllegalArgumentException e){
            return null;
        }
        int ww = result.getWidth();
        int hh = result.getHeight();
        int[] pixels = new int[ww*hh];
        for(int y=0;y<hh;y++){
            int offset = y * w;
            for(int x=0;x<ww;x++){
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(pixels, 0, ww, 0, 0, ww, hh);
        return b;
    }
}
