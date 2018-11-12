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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    BluetoothDevice board = null;
    MyBluetoothService BTServices;
    BluetoothAdapter mBluetoothAdapter = null;
    String TAG = "debug-bluetooth";
    MainActivity ma = this;
    BluetoothGatt bluetoothGatt = null;

    final UUID serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    final UUID charUUID = UUID.fromString("ffe1-0000-1000-8000-00805f9b34fb");
    final UUID configChar = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");

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
                try {
                    pullUptade(Double.parseDouble(msg.obj.toString()));
                }
                catch(NumberFormatException e){
                    pullUptade(0);
                }
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

        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(serviceUUID).getCharacteristic(charUUID);
                characteristic.setValue(new byte[] {65,65,65,65});
                bluetoothGatt.writeCharacteristic(characteristic);
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
                    Log.d("debug-bluetooth", device.getName());
                    if (device.getName().equals("POUTRE")) {
                        board = device;
                    }
                }
            }
        }


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

                    BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(charUUID);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(configChar);
                    gatt.setCharacteristicNotification(characteristic, true);

                    //input
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    //output
                    BluetoothGattDescriptor descriptorOut = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptorOut.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    gatt.writeDescriptor(descriptorOut);
                  }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.d(TAG, "le msg :\""+byteToString(characteristic.getValue())+"\" abien été envoyer");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    Log.d(TAG,"msg : "+byteToString(characteristic.getValue()));
                    Message msg= new Message();
                    msg.arg1=Res.BTDATA;
                    msg.obj=byteToString(characteristic.getValue());
                    myHandler.sendMessage(msg);
                }
            };

            bluetoothGatt = board.connectGatt(this, true, mGattCallback);
        }
    }

    private String byteToString(byte[] tab){
        String msg="";
        for(byte b : tab){
            msg += (char)(b & 0xFF);
        }
        return msg;
    }
}

