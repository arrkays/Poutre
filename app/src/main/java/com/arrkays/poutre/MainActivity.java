package com.arrkays.poutre;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
    LinearLayout listPrise = null;
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
        listPrise = findViewById(R.id.listPrise);

        graph.handler = myHandler;


        //Ajouter prehenssion dans select
        checkPrehension();
        updateSpinner();
        addAddButton();
        buildListHold();

        //instruction*************************************
        blutoothManager = new BT(this);
        blutoothManager.connect();
        weightFunctions = new WeightFunctions(this); // instantiation de la classe pour mesurer le poids de corps
        record.setText(Res.currentPrehension.maxPull+" kg");
        recordPullPour.setText(Res.currentPrehension.pourcentage+"%");
        startPullUpdate();

        //set title up
        titleActivity.setText(titreActivity);

        //Event*******************************************

        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(weightFunctions.bodyWeightAsked) {
                    Log.d(TAG, "stop bodyweight mask");
                    weightFunctions.stopBodyWeightMeasurement();
                }
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

        findViewById(R.id.titreActivite).requestFocus();
    }

    /**
     * vérifie si aucune préhension existe. si c'est le cas en créée une
     */
    private void checkPrehension() {
        if(Res.currentPrehension == null){
            if(Res.prehensions.size() == 0){
                Res.currentPrehension = new Prehension("Prise 1");
                Res.prehensions.add(Res.currentPrehension);
                Res.prehensions.add(new Prehension("Prise 2"));
                Res.prehensions.add(new Prehension("Prise 3"));
                Res.prehensions.add(new Prehension("Prise 4"));
                Res.prehensions.add(new Prehension("Prise 5"));
            }
            else{
                Res.currentPrehension = Res.prehensions.get(0);
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

        updateRecord(pull);
        displayRecord();
        displayCurrentPull(pull);


        graph.setPull(pull);
    }

    void displayCurrentPull(double pull){
        currentPull.setText(pull+" Kg");
        if(Res.getPour(pull) == 0)
            currentPullPour.setText("");
        else
            currentPullPour.setText(Res.getPour(pull)+"%");
    }

    void displayRecord(){
        record.setText(Res.currentPrehension.maxPull+" kg");
        if(Res.currentPrehension.pourcentage != 0)
            recordPullPour.setText(Res.currentPrehension.pourcentage+"%");
    }

    void updateRecord(double pull){
        if(pull>Res.currentPrehension.maxPull) {//verifie si le record absolue est batue
            //TODO Faire annimation est feedback sonnor
            Res.currentPrehension.setRecordPull(pull);
            recordPullPour.setText(Res.getPour(pull)+"%");
        }

        if(Res.getPour(pull) > Res.currentPrehension.pourcentage){//verifie si le record relatif est batue
            Res.currentPrehension.pourcentage = Res.getPour(pull);
        }
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

    /****************************************************************************************************************************************************************************************************************************/
    /**************************************************************************************************LISTE PRISE***************************************************************************************************************/
    /****************************************************************************************************************************************************************************************************************************/
    int widthNom = 400;
    int heightNom = 50;

    void buildListHold(){
        int i = 0;
        for(Prehension p : Res.prehensions){
            listPrise.addView(creatLine(p));
            i++;
        }

        listPrise.addView(ligneAdd());

    }

    private LinearLayout creatLine(final Prehension p){
        LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams(80,80);
        paramsIcon.setMargins(10,10,10,10);

        LinearLayout.LayoutParams paramsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(widthNom,LinearLayout.LayoutParams.MATCH_PARENT);
        //paramsTextView.setMargins(50,-50,0,0);

        //text view
        TextView nom = new TextView(this);
        nom.setText(p.nom);
        nom.setLayoutParams(paramsTextView);
        nom.setGravity(Gravity.CENTER_VERTICAL);
        nom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPrehenssion(p);
            }
        });
        nom.setTextColor(Color.BLACK);
        //nom.setTextSize(20);

        //edit
        Button edit = new Button(this);
        edit.setBackground(ContextCompat.getDrawable(this,R.drawable.edit_list_prise));
        edit.setLayoutParams(paramsIcon);

        //edit.setWidth(40);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrehenssion(p);
            }
        });

        //delet
        Button del = new Button(this);
        del.setLayoutParams(paramsIcon);
        del.setBackground(ContextCompat.getDrawable(this,R.drawable.remove_list_prise));
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletPrehenssion(p);
            }
        });

        //la ligne
        LinearLayout ligne = new LinearLayout(this);
        ligne.setOrientation(LinearLayout.HORIZONTAL);
        ligne.setBackground(ContextCompat.getDrawable(this,R.drawable.border));
        ligne.setPadding(10,10,10,10);
        ligne.setLayoutParams(paramsLine);
        ligne.setBaselineAligned(false);
        ligne.setGravity(Gravity.CENTER_VERTICAL);

        ligne.addView(nom);
        ligne.addView(edit);
        ligne.addView(del);

        return ligne;
    }

    private LinearLayout ligneAdd(){

        //parametre
        //edit Text
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthNom,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);

        //bouton
        LinearLayout.LayoutParams paramsButton = new LinearLayout.LayoutParams(80,80);
        paramsButton.setMargins(60,10,60,10);
        paramsButton.gravity = Gravity.CENTER_VERTICAL;

        //Layout
        LinearLayout.LayoutParams paramsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        final EditText edit = new EditText(this);
        edit.setLayoutParams(params);
        edit.setSingleLine(true);
        edit.setImeActionLabel("Valider", EditorInfo.IME_ACTION_DONE);
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addPrehenssion(edit);
                return false;
            }
        });
        Button add = new Button(this);
        add.setLayoutParams(paramsButton);
        add.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_ok));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPrehenssion(edit);
            }
        });

        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setBackground(ContextCompat.getDrawable(this,R.drawable.border));
        line.setLayoutParams(paramsLine);
        line.setBaselineAligned(false);
        line.addView(edit);
        line.addView(add);

        return line;
    }

    private void selectPrehenssion(Prehension p) {
        Log.d(TAG,"select "+p);
    }

    private void deletPrehenssion(Prehension p) {

        listPrise.removeViewAt(Res.prehensions.indexOf(p));
        Res.prehensions.remove(p);
        Log.d(TAG,"remove "+p.nom);
    }

    private void setPrehenssion(Prehension p){
        Log.d(TAG,"modif "+p);
    }

    public void addPrehenssion(EditText nom){
        Prehension p = new Prehension(nom.getText().toString());
        Res.prehensions.add(p);
        listPrise.addView(creatLine(p), listPrise.getChildCount()-1);
        nom.setText("");

        Log.d(TAG,"ajout "+nom);
    }

}

