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
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    String TAG = "debug-bluetooth";
    BT blutoothManager = null;

    //VIEW
    Graph graph = null;
    TextView monPoid = null;
    TextView currentPull = null;
    TextView record = null;
    TextView recordPullPour = null;
    TextView currentPullPour = null;

    //handler sert a faire des modification sur l'UI non initier par l'utilisateur
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
        graph.handler = myHandler;
        blutoothManager = new BT(this);

        //instanciation des Views
        graph = (Graph)findViewById(R.id.graph);
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
                blutoothManager.connect();
                return false;
            }
        });

        blutoothManager.connect();
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

    private String byteToString(byte[] tab){
        String msg="";
        for(byte b : tab){
            msg += (char)(b & 0xFF);
        }
        return msg;
    }
}

