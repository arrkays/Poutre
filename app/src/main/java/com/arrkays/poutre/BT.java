package com.arrkays.poutre;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Message;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

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
        connect();
    }

    public void connect(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!connected && mBluetoothAdapter != null){
            ble();
        }
    }

    /**
     *recherche et connect le module blutooth
     */
    void ble(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ma.startActivityForResult(enableBtIntent, 0);
        }
        else {
            boolean isDeviceFound = false;
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("POUTRE")) {
                        Log.i(TAG,"POUTRE find");
                        board = device;
                        isDeviceFound = true;
                        break;
                    }
                }
            }

            if(!isDeviceFound){//si le module BT n'est pas déja connecter lancer une rechercher
                //TODO scann
            }

            if(board != null){
                //call back
                BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        //si le BLE est bien connecter
                        if(newState == BluetoothProfile.STATE_CONNECTED){
                            BTconected(true);
                            Log.i(TAG,"STATE_CONNECTED");
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
                        Log.i(TAG, "le msg :\""+byteToString(characteristic.getValue())+"\" abien été envoyer");
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        Message msg= new Message();
                        msg.arg1=Res.BTDATA;
                        msg.obj=byteToString(characteristic.getValue());
                        ma.myHandler.sendMessage(msg);
                    }
                };

                bluetoothGatt = board.connectGatt(ma, true, mGattCallback);
            }
        }
    }

    /**
     * update data and graphic when conection status change
     * @param b
     */
    private void BTconected(boolean b) {
        connected = b;
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