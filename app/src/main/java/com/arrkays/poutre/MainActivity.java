package com.arrkays.poutre;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothDevice board = null;
    MyBluetoothService BTServices;
    BluetoothAdapter mBluetoothAdapter = null;
    String TAG = "debug-bluetooth";
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 1){
                bluetoothClient();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Graph g = (Graph)findViewById(R.id.graph);

        Log.d("debug-bluetooth", "device bond state : "+BluetoothDevice.DEVICE_TYPE_LE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //new AcceptThread().start();
        //bluetoothClient();
    }

    void bluetoothClient(){
        if (mBluetoothAdapter != null) {
            Log.d("debug-bluetooth", "bluetooth activé");
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d("debug-bluetooth", "device paired: ");
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if(device.getName().equals("POUTRE")) {
                        board = device;

                        Log.d("debug-bluetooth", "name : " + device.getName());
                        Log.d("debug-bluetooth", "bond state : " + device.getBondState());
                        Log.d("debug-bluetooth", "type : " + device.getType());
                        Log.d("debug-bluetooth", "uuids : " + device.getUuids());
                        Log.d("debug-bluetooth", "Mac Adresse : " + device.getAddress());
                        Log.d("debug-bluetooth", "------------------------");
                    }
                }
            }
        }
        else
        {
            Log.d("debug-bluetooth", "no bluetooth adaptater found");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("debug-bluetooth", "thread start");
                Log.d("debug-bluetooth", "Mac Adresse : "+board.getAddress());
                Log.d("debug-bluetooth", "fetch uuid : "+board.fetchUuidsWithSdp());
                Log.d("debug-bluetooth", "get uuid : "+board.getUuids());

                try {
                    byte uuid[] = {0,0};
                    BluetoothSocket socket = board.createInsecureRfcommSocketToServiceRecord(UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB"));
                    Log.d("debug-bluetooth", "socket init");
                    Log.d("debug-bluetooth", "connecting socket...");
                    mBluetoothAdapter.cancelDiscovery();
                    socket.connect();
                    Log.d("debug-bluetooth", "socket connecter!");
                    BTServices = new MyBluetoothService(socket);
                }catch(IOException e){
                    e.getCause();
                    Log.d("debug-bluetooth", "cant open socket");

                };
            }
        }).start();

    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                byte uuid[] = {0,0};
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Poutre", UUID.nameUUIDFromBytes(uuid));
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    Log.d(TAG, "Socketserver wait...");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    Log.d(TAG, "Socket's accept!!!");
                    new MyBluetoothService(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

