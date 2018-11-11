package com.arrkays.poutre;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Permission;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothDevice board = null;
    MyBluetoothService BTServices;
    BluetoothAdapter mBluetoothAdapter = null;
    String TAG = "debug-bluetooth";
    MainActivity ma = this;
    //VIEW
    Graph graph = null;
    TextView monPoid = null;
    TextView currentPull = null;
    TextView record = null;
    TextView recordPullPour = null;
    TextView currentPullPour = null;


    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == Res.BTDATA){
                pullUptade(Double.parseDouble(msg.obj.toString()));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPrehension();
        graph = (Graph)findViewById(R.id.graph);
        graph.handler = myHandler;

        //instanciation des Views
        monPoid = (TextView)findViewById(R.id.monPoid);
        record = (TextView)findViewById(R.id.record);
        currentPull = (TextView)findViewById(R.id.currentPull);
        recordPullPour = (TextView)findViewById(R.id.recordPullPourcentage);
        currentPullPour = (TextView)findViewById(R.id.currentPullPoucentage);
        Button BTbutton = (Button)findViewById(R.id.buttonTestBT);

        //Event
        BTbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //BTConnexion.startConnexionBT(ma);
                ble();
                return false;
            }
        });


        Log.d("debug-bluetooth", "device bond state : "+BluetoothDevice.DEVICE_TYPE_LE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        checkPrehension();

        //new AcceptThread().start();
        //bluetoothClient();
        ble();
    }

    /**
     * vérifie si aucune préhension existe. si c'est le cas en créée une
     */
    private void checkPrehension() {
        if(Res.currentPrehension == null){
            if(Res.prehensions.size() == 0){
                Res.currentPrehension = new Prehension("Default");
                Res.prehensions.add(Res.currentPrehension);
            }
            else{
                Res.prehensions.get(0);
            }
        }
    }

    /**
     * updatePullexercice
     * @param pull
     */
    public void pullUptade(double pull){
        graph.setPull(pull);
        if(pull>Res.currentPrehension.maxPull) {//verifie si le record est batue
            //TODO Faire annimation est feedback sonnor
            Res.currentPrehension.setRecordPull(pull);
            record.setText(pull+" Kg");
            recordPullPour.setText(Res.currentPrehension.pourcentage+"%");
            recordPullPour.setText(Res.getPour(pull)+"%");
        }
        currentPull.setText(pull+" Kg");
        if(Res.getPour(pull) == 0)
            currentPullPour.setText("");
        else
            currentPullPour.setText(Res.getPour(pull)+"%");

    }


    void ble(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "bluetooth activé");
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d("debug-bluetooth", "device paired: ");
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("POUTRE")) {
                        board = device;
                    }
                }
            }
        }


        final UUID serviceUUID = convertFromInteger(0xFFE0);
        final UUID charUUID = convertFromInteger(0xFFE1);
        final UUID configChar = convertFromInteger(0x2902);

        if(board != null){

            //call back
            BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                    if(newState == BluetoothProfile.STATE_CONNECTED){
                        Log.d(TAG,"STATE_CONNECTED");
                        Log.d(TAG,"discover services "+gatt.discoverServices());
                    }

                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    Log.d(TAG, "callback onServicesDiscovered ");
                    Log.d(TAG, "list of Services: ");
                    for(BluetoothGattService e : gatt.getServices()){
                        Log.d(TAG, e.toString());
                        Log.d(TAG, "service uuid "+e.getUuid());
                        Log.d(TAG, "Characteristic of this service : ");
                        for(BluetoothGattCharacteristic c : e.getCharacteristics()){
                            Log.d(TAG, "    "+c.toString());
                            Log.d(TAG, "    value "+c.getValue());
                            Log.d(TAG, "    uuid "+c.getUuid());
                            Log.d(TAG, "    descriptor of the characteristique : ");
                            for(BluetoothGattDescriptor d : c.getDescriptors()){
                                Log.d(TAG, "        "+d.toString());
                                Log.d(TAG, "        "+d.getUuid());
                                d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(d);
                                gatt.setCharacteristicNotification(c, true);
                            }
                        }
                    }


                    /*BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(charUUID);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(configChar);

                    Log.d(TAG,"set descriptot to ENABLE_NOTIFICATION_VALUE");
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.d(TAG,"desc val : "+descriptor.getValue());
                   // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                   // Log.d(TAG,"desc val : "+descriptor.getValue());*/
                    //gatt.writeDescriptor(descriptor);
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.d(TAG,"onDescriptorWrite");

                    /*BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(charUUID);
                    BluetoothGattDescriptor descriptor2 = characteristic.getDescriptor(convertFromInteger(0x2901));
                    descriptor2.setValue(new byte[]{1, 1});
                    gatt.writeDescriptor(descriptor2);*/
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.d(TAG, "onDescriptorRead!!!");
                }



                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.d(TAG, "onchar!!!");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.d(TAG,"Characteristique: "+characteristic.getUuid());
                    String msg="";
                    for(byte b : characteristic.getValue()){

                        msg += (char)(b & 0xFF);
                    }
                    Log.d(TAG,"msg : "+msg);
                }
            };


            BluetoothGatt mBluetoothGatt = board.connectGatt(this, true, mGattCallback);
        }


    }

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }



    void bluetoothClient(){
        if (mBluetoothAdapter != null) {
            Log.d(TAG, "bluetooth activé");
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                Log.d("debug-bluetooth", "device paired: ");
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if(device.getName().equals("POUTRE")) {
                        board = device;

                        Log.d("debug-bluetooth", "name : " + board.getName());
                        Log.d("debug-bluetooth", "bond state : " + board.getBondState());
                        Log.d("debug-bluetooth", "type : " + board.getType());
                        Log.d("debug-bluetooth", "uuids : " + board.getUuids());
                        Log.d("debug-bluetooth", "Mac Adresse : " + board.getAddress());
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

    /*
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
    */
}

