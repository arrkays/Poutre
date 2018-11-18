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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String titreActivity = "Mesurer force";
    String TAG = "debug-bluetooth";
    BT blutoothManager = null;
    WeightFunctions weightFunctions = null;

    //VIEW
    Graph graph = null;
    TextView titleActivity = null;
    TextView monPoid = null;
    TextView currentPull = null;
    TextView record = null;
    TextView recordPullPour = null;
    TextView currentPullPour = null;
    ImageView bluetoothOn = null;
    ImageView bluetoothOff = null;
    Spinner spinnerPrise = null;
    ConstraintLayout popUpMesurepoids = null;
    ConstraintLayout navigationMenu = null;
    ConstraintLayout mask = null;
    Button cancelWeightMeasurement = null; // bouton du popup mesure du poids
    Button suspensionsButton = null;
    Button showMenuButton = null;
    Button buttonTestPlus = null;
    Button buttonTestMoins = null;
    ProgressBar loaderMonPoids = null;

    //handler sert a faire des modification sur l'UI non initier par l'utilisateur
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == Res.BT_STATUS_UPDATE){
                bluetoothUpdate((boolean) msg.obj);
            }
        }
    };

    WeightListener weightListener = new WeightListener() {
        @Override
        public void onChange(double w) {
            pullUptade(w);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instanciation des Views
        graph = findViewById(R.id.graph);
        monPoid = findViewById(R.id.monPoid);
        titleActivity = findViewById(R.id.titreActivite);
        record = findViewById(R.id.record);
        currentPull = findViewById(R.id.currentPull);
        recordPullPour = findViewById(R.id.recordPullPourcentage);
        currentPullPour = findViewById(R.id.currentPullPoucentage);
        bluetoothOff =  findViewById(R.id.bluetoothNotActiv);
        bluetoothOn =  findViewById(R.id.bluetoothActiv);
        spinnerPrise = findViewById(R.id.selectPrise);
        popUpMesurepoids = findViewById(R.id.popUpMesurePoids);
        navigationMenu = findViewById(R.id.navigationMenu);
        mask = findViewById(R.id.mask);
        cancelWeightMeasurement = findViewById(R.id.annulerMesurePoid);
        suspensionsButton = findViewById(R.id.suspensionsButton);
        showMenuButton = findViewById(R.id.showMenu);
        buttonTestPlus = findViewById(R.id.buttonTestPlus);
        buttonTestMoins = findViewById(R.id.buttonTestMoins);
        loaderMonPoids = findViewById(R.id.loaderMesurePoid);

        graph.handler = myHandler;


        //Ajouter prehenssion dans select
        checkPrehension();
        updateSpinner();
        addAddButton();

        //instruction*************************************
        blutoothManager = new BT(this);
        blutoothManager.connect();
        weightFunctions = new WeightFunctions(this); // instantiation de la classe pour mesurer le poids de corps
        startPullUpdate();

        //set title up
        titleActivity.setText(titreActivity);

        //Event*******************************************

        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weightFunctions.bodyWeightAsked)
                    weightFunctions.stopBodyWeightMeasurement();
                if(navigationMenu.getVisibility() == View.VISIBLE)
                    navigationMenu.setVisibility(View.GONE);

                mask.setVisibility(View.GONE);
            }
        });
            //bouton de test pull
        buttonTestMoins.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Res.weightNotif.updateWeight(graph.pull-1);
                return false;
            }
        });
        buttonTestPlus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Res.weightNotif.updateWeight(graph.pull+1);
                return false;
            }
        });

            //bouton pour ouvrir le menue
        showMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigationMenu.getVisibility()== View.GONE) {
                    navigationMenu.setVisibility(View.VISIBLE);
                    mask.setVisibility(View.VISIBLE);
                }
                else {
                    navigationMenu.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                }
            }
        });
        bluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blutoothManager.connect();
            }
        });
        suspensionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SuspensionsActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });
        //mesure poid
        monPoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //afficher le masque derier le popup
                mask.setVisibility(View.VISIBLE);
                weightFunctions.startBodyWeightMeasurement();
            }
        });
        cancelWeightMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                weightFunctions.stopBodyWeightMeasurement();
            }
        });

        spinnerPrise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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


    void startPullUpdate(){
        Res.weightNotif.addListener(weightListener);
    }

    void stopPullUpdate(){
        Res.weightNotif.removeListener(weightListener);
    }
    /**
     * updatePullexercice
     * @param pull
     */
    public void pullUptade(double pull){
        if(pull < 0)//pull ne peut pas etre negatif
            pull=0;
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

    /**
     * feedback graphique en fonction de l'etat de la connexion bluetooth
     */
    public void bluetoothUpdate(boolean activer){
        if(activer){
            bluetoothOff.setVisibility(View.GONE);
            bluetoothOn.setVisibility(View.VISIBLE);
        }
        else{
            bluetoothOn.setVisibility(View.GONE);
            bluetoothOff.setVisibility(View.VISIBLE);
        }
    }

    /**
     * affiche feedback graphique dans le popupMesurPoid
     * @param w
     */
    private void mesurePoid(double w) {
    }

    public void updateSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item, Res.getListPrehenssionString());
        adapter.add("ajouter †");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPrise.setAdapter(adapter);
    }

    /**
     * ajout du bouton ajout prenssion
     */
    private void addAddButton() {
    }
}

