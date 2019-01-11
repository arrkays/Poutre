package com.arrkays.poutre;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.os.SystemClock.sleep;

public class BT {
    boolean connected = false;

    String TAG = "debug-bluetooth";

    final UUID serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    final UUID charUUID = UUID.fromString("ffe1-0000-1000-8000-00805f9b34fb");
    final UUID configCharIn = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    final UUID configCharOut = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    BluetoothDevice board = null;
    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothGatt bluetoothGatt = null;

    MainActivity ma;
    public BT(MainActivity a){
        ma = a;
    }

    public void connect(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!connected && mBluetoothAdapter != null){
            ble();
        }
        else{
            Log.d(TAG,"bluetooth inactif");
        }
    }

    /**
     *recherche et connect le module blutooth
     */
    void ble(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ma.startActivityForResult(enableBtIntent, 0);
            Log.d(TAG,"bluetooth inactif");
        }
        else {
            boolean isDeviceFound = false;
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    Log.d(TAG,"device bound : "+device.getName());
                    if (device.getName().equals("POUTRE")) {
                        Log.d(TAG,"POUTRE found");
                        board = device;
                        isDeviceFound = true;
                        break;
                    }
                }
            }

            if(!isDeviceFound){//si le module BT n'est pas déja connecter lancer une rechercher
                Log.d(TAG,"start le scann");
                final ScanCallback scannCallback = scanne();
                final BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

                scanner.startScan(scannCallback );

                //Stop scann dans 15s
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sleep(15000);
                        Log.d(TAG,"Stop le scann");
                        scanner.stopScan(new ScanCallback() {
                            @Override
                            public void onScanResult(int callbackType, ScanResult result) {

                            }
                        });
                    }
                }).start();
            }
            else{
                setupComunication();
            }


        }
    }

    ScanCallback scanne(){
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                Log.d(TAG,"divice find : "+result.getDevice().getName());
                if(result.getDevice().getName().equals("POUTRE")){
                    board = result.getDevice();
                    board.setPin(new byte[]{49,50,51,52,53,54});//123456
                    board.createBond();
                }
            }
        };





        return scanCallback;
    }

    void setupComunication(){
        if(board != null){
            //call back
            BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    //si le BLE est bien connecter
                    if(newState == BluetoothProfile.STATE_CONNECTED){
                        BTconected(true);
                        Log.d(TAG,"STATE_CONNECTED");
                        //on lance la recuperation des service / characteristic
                        gatt.discoverServices();
                    }
                    else{
                        BTconected(false);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    //on recupere la bonne characteristic et son descriptor
                    BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(charUUID);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(configCharIn);

                    //on autorise les notif aka INPUT DATA
                    gatt.setCharacteristicNotification(characteristic, true);

                    //input
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    //output
                    BluetoothGattDescriptor descriptorOut = characteristic.getDescriptor(configCharOut);
                    descriptorOut.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    gatt.writeDescriptor(descriptorOut);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Log.i(TAG, "le msg :\""+byteToString(characteristic.getValue())+"\" a bien été envoyer");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            double weight;
                            try {
                                weight = Double.parseDouble(byteToString(characteristic.getValue()));
                            }
                            catch(NumberFormatException e){
                                weight = 0;
                            }
                        /*Message msg= new Message();
                        msg.arg1=Res.BT_DATA;
                        msg.obj=weight;
                        ma.myHandler.sendMessage(msg);*/
                            if(weight < 0){
                                //TODO ERROR?
                            }
                            else{
                                Message msg = new Message();
                                msg.arg1 = Res.BT_DATA;
                                msg.obj = weight;
                                ma.myHandler.sendMessage(msg);
                            }
                        }
                    }).start();
                }
            };

            bluetoothGatt = board.connectGatt(ma, true, mGattCallback);
        }
    }

    /**
     * update data and graphic when conection status change
     * @param b
     */
    private void BTconected(boolean b) {
        connected = b;
        Message msg = new Message();
        msg.arg1 = Res.BT_STATUS_UPDATE;
        msg.obj = b;
        ma.myHandler.sendMessage(msg);

        if(b){
            //TODO afficher feedback dans menu
        }
        else{
            //TODO afficher feed back
        }
    }

    /**
     * envoi le message msg au remote device
     * @param msg
     */
    public void sendMsg(String msg){
        msg.getBytes(StandardCharsets.UTF_8);
        BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(serviceUUID).getCharacteristic(charUUID);
        characteristic.setValue(msg.getBytes(StandardCharsets.UTF_8));
        bluetoothGatt.writeCharacteristic(characteristic);
    }


    private String byteToString(byte[] tab){
        String msg="";
        for(byte b : tab){
            msg += (char)(b & 0xFF);
        }
        return msg;
    }
}
