package com.arrkays.poutre;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {

    String titreActivity = "Mesurer force";
    String TAG = "debug-bluetooth";
    String TAGPB = "PB_pull";
    String TAGMA = "MainActivity-debug";
    BT blutoothManager = null;
    WeightFunctions weightFunctions = null;
    boolean actif = true;//ce reactive que pull = 0
    //VIEW
    GraphMaxPullCirular graph = null;

    TextView titleActivity = null;
    TextView monPoid = null;
    //TextView currentPull = null;
    TextView record = null;
    TextView recordPullPour = null;
    //TextView currentPullPour = null;
    TextView priseSelected = null;
    TextView poidPopUpMesirePoid = null;
    TextView thisSessionPull = null;
    TextView thisSessionpourc = null;
    TextView lastSessionPull = null;
    TextView lastSessionPourc = null;
    TextView lastPullPull = null;
    TextView lastPullPourc = null;

    ImageView bluetoothOn = null;
    ImageView bluetoothOff = null;

    ConstraintLayout selectPrise = null;
    ScrollView scrollPrise = null;
    ConstraintLayout popUpMesurepoids = null;
    ConstraintLayout containerPoid = null;
    ConstraintLayout navigationMenu = null;
    ConstraintLayout mask = null;
    LinearLayout listPrise = null;
    ConstraintLayout titreSelect = null;
    ConstraintLayout containerCharts = null;
    ConstraintLayout containerInfoPrise = null;
    ChartsHold charts = null;

    Button cancelWeightMeasurement = null; // bouton du popup mesure du poids
    Button suspensionsButton = null;
    Button showMenuButton = null;
    Button toggleSelectPrise = null;
    Button buttonHistoric = null;
    Button buttonRazTodayPull = null;
    Button button_settings;  // bouton pour aller dans l'activité des paramètres

    ProgressBar loaderMonPoids = null;

    private GestureDetectorCompat mDetector;

    DB dataBase = null;
    //StoreData store;

    //handler sert a faire des modification sur l'UI a l'exterieur du thread principal
    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == Res.BT_STATUS_UPDATE){
                bluetoothUpdate((boolean) msg.obj);
            }
            else if(msg.arg1 == Res.BT_DATA){
                pullUptade((Double) msg.obj);
            }
            else if (msg.arg1 == Res.MESURE_POIDS){
                setPoid((double)msg.obj);
            }
            else if (msg.arg1 == Res.POPUP_STATUT_MESURE_POIDS){
                loaderMonPoids.setProgress((int)msg.obj);
            }
            else if (msg.arg1 == Res.POPUP_MESURE_POIDS){
                poidPopUpMesirePoid.setText(msg.obj+" kg");
            }
        }
    };

    WeightListener weightListener = new WeightListener() {
        @Override
        public void onChange(double w, boolean[] evo) {
            Message msg = new Message();
            msg.arg1 = Res.BT_DATA;
            msg.obj = w;
            myHandler.sendMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_main2);
        else
            setContentView(R.layout.activity_main2_landscape);

        mDetector = new GestureDetectorCompat(this, new MainActivity.ActivityChangeGestureListener());

        //on met un reference de this dans les ressource
        Res.ma = this;

        //instanciation des Views
        graph = findViewById(R.id.graph);
        monPoid = findViewById(R.id.monPoid);
        titleActivity = findViewById(R.id.titreActivite);
        record = findViewById(R.id.record);
        //currentPull = findViewById(R.id.currentPull);
        recordPullPour = findViewById(R.id.recordPullPourcentage);
        //currentPullPour = findViewById(R.id.currentPullPoucentage);
        bluetoothOff =  findViewById(R.id.bluetoothNotActiv);
        bluetoothOn =  findViewById(R.id.bluetoothActiv);
        selectPrise = findViewById(R.id.selectPrise);
        priseSelected = findViewById(R.id.priseSelected);
        popUpMesurepoids = findViewById(R.id.popUpMesurePoids);
        navigationMenu = findViewById(R.id.navigationMenu);
        mask = findViewById(R.id.mask);
        cancelWeightMeasurement = findViewById(R.id.annulerMesurePoid);
        suspensionsButton = findViewById(R.id.suspensionsButton);
        showMenuButton = findViewById(R.id.showMenu);
        loaderMonPoids = findViewById(R.id.loaderMesurePoid);
        listPrise = findViewById(R.id.listPrise);
        scrollPrise = findViewById(R.id.scrollPrise);
        toggleSelectPrise = findViewById(R.id.toggleSelectPrise);
        titreSelect = findViewById(R.id.titreSelect);
        containerPoid = findViewById(R.id.containerPoid);
        poidPopUpMesirePoid = findViewById(R.id.poidPopUp);
        thisSessionPull = findViewById(R.id.thisSessionRecord);
        thisSessionpourc = findViewById(R.id.thisSessionPourc);
        lastSessionPull = findViewById(R.id.lastSessionMax);
        lastSessionPourc = findViewById(R.id.lastSessionPourc);
        buttonHistoric = findViewById(R.id.historiquePrise);
        charts = findViewById(R.id.charts);
        containerCharts = findViewById(R.id.containerChart);
        buttonRazTodayPull = findViewById(R.id.buttonRazTodayPull);
        lastPullPull = findViewById(R.id.lastPullTextView);
        lastPullPourc = findViewById(R.id.lastPullPourc);
        containerInfoPrise = findViewById(R.id.containerInfoPrise);
        button_settings = findViewById(R.id.button_Settings);

        graph.handler = myHandler;


        //instanciation CLASS**********************************
        dataBase = new DB(this);
        weightFunctions = new WeightFunctions(this);
        Res.store = new StoreData(this);


        //import prehenssion depuis DATABASE & Ajouter prehenssion dans select
        updatePrehension();
        displayListHold();
        priseSelected.setText(Res.currentPrehension+"");

        //instruction*************************************

        //bluetooth
        blutoothManager = new BT(this);
        blutoothManager.connect();

        // poids :instantiation de la classe pour mesurer le poids de corps
        Res.poids = Res.store.getPoid();
        animateMesurePoid();
        monPoid.setText(Res.round(Res.poids, 1)+" kg");

        //setup hold ang record
        displayRecord();
        displayLastDay();
        displayTodayPull();

        //commencer les relever de poid
        startPullUpdate();
        weightFunctions.starComportement();

        //set title up
        titleActivity.setText(titreActivity);

        //Event
        event();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPullUpdate();
        Log.d(TAGMA, "-----onStop()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPullUpdate();
        Log.d(TAGMA, "-----onPause()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        startPullUpdate();
        Log.d(TAGMA, "-----onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPullUpdate();
        Log.d(TAGMA, "-----onResume()");
    }

    //*************************************************************************EVENT********************************************************************
    private void event(){
        //bouton pour ouvrir le menue
        showMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(navigationMenu.getVisibility()== View.GONE) {
                    setMenuOn();
                }
                else {
                    navigationMenu.setVisibility(View.GONE);
                    setMenuOff();
                }
            }
        });
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(myIntent);
                navigationMenu.setVisibility(View.GONE);
            }
        });

        //bluetooth
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
                navigationMenu.setVisibility(View.GONE);
            }
        });

        //mesure poid
        monPoid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //weightFunctions.startBodyWeightMeasurement();
                if(!weightFunctions.isMesuring){
                    weightFunctions.startMesurePoidBis();
                    actif = false;
                }
            }
        });

        cancelWeightMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //weightFunctions.stopBodyWeightMeasurement();
                weightFunctions.stopMesurePoidBis();
            }
        });

        toggleSelectPrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelectPrise();
            }
        });

        selectPrise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelectPrise();
            }
        });

        //remove today pull
        buttonRazTodayPull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pull p = Res.currentPrehension.getToDayPull();

                if(p != null){
                    Res.currentPrehension.historic.remove(p);
                    dataBase.removePull(p);

                    //on met a jour le reccord au cas ou on vien de le supprimer
                    Res.currentPrehension.setUpAllTimeRecord();
                    displayRecord();
                    displayTodayPull();
                    graph.invalidate();
                }
            }
        });

        //display historic chart of pull
        buttonHistoric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                charts.updateData();

                containerCharts.setAlpha(0);
                containerCharts.setVisibility(View.VISIBLE);
                containerCharts.animate().alpha(1).setListener(null);

                mask.setVisibility(View.VISIBLE);
                mask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mask.setVisibility(View.GONE);
                        containerCharts.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                containerCharts.setVisibility(View.GONE);
                            }
                        });

                    }
                });
            }
        });

        //test de pull simulation du pull
        graph.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getRawY() > Y){ //decent
                    if(Res.currentWeight > 0)
                        Res.currentWeight-=0.2;
                    else
                        Res.currentWeight=0;
                    Res.weightNotif.updateWeight(Res.currentWeight, WeightFunctions.comportement(Res.currentWeight) );
                }
                else if(event.getRawY() < Y){//monte
                    Res.currentWeight += 0.2;
                    Res.weightNotif.updateWeight(Res.currentWeight+1, WeightFunctions.comportement(Res.currentWeight+1) );
                }
                Y = event.getRawY();
                return true;
            }
        });
    }

    //pour test pull a sup
    private float Y =0;




    //MENU *********************************************************************MENU****************************************************************MENU
    //MENU *********************************************************************MENU****************************************************************MENU
    //MENU *********************************************************************MENU****************************************************************MENU
    private void setMenuOn(){
        //positionement AXE Z elevation
        navigationMenu.setElevation(35);
        mask.setElevation(34);

        //Action du mask
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuOff();
            }
        });

        mask.setVisibility(View.VISIBLE);

        //TODO animation
        navigationMenu.setVisibility(View.VISIBLE);
    }

    private void setMenuOff(){
        mask.setVisibility(View.GONE);

        //TODO anmiation
        navigationMenu.setVisibility(View.GONE);

        //positionement AXE Z elevation
        navigationMenu.setElevation(10);
    }

    //***********************************************************************************poids**************************************************************************************
    //***********************************************************************************poids**************************************************************************************
    //***********************************************************************************poids**************************************************************************************

    public void setPoid(double w){
        monPoid.setText(Res.round(w, 1) + " kg");
        Res.store.setPoid(w);
        Res.poids = w;
    }

    //Touche son Pour simuler augmentation poid
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            if(Res.currentWeight > 0)
                Res.currentWeight--;
            else
                Res.currentWeight = 0;
            Res.weightNotif.updateWeight(Res.currentWeight, WeightFunctions.comportement(Res.currentWeight) );
        }
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            Res.currentWeight++;
            Res.weightNotif.updateWeight(Res.currentWeight+1, WeightFunctions.comportement(Res.currentWeight+1) );
        }
        return super.onKeyDown(keyCode, event);
    }


    //animation qui fait rebondire le poid tant qu'il est pas mesurer
    void animateMesurePoid(){
        if(Res.poids == 0) {
            containerPoid.animate()
                    .scaleY(1.1f)
                    .scaleX(1.1f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            containerPoid.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            animateMesurePoid();
                                        }
                                    });
                        }
                    });
        }

    }


//***********************************************************************************************************************************
//********************************************************PULL RECORD GRAPHIQUE******************************************************
//***********************************************************************************************************************************

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

        displayCurrentPull(pull);
        if(actif) {
            Prehension p = Res.currentPrehension;
            p.setTodayPull(pull);

            graph.setPull(pull);
            displayLastPull();

            //si le reccord de pull a été battu
            if (p.getAllTimeRecordPull() != null) {
                if (p.isPullPBBrocken()) {
                    Log.d(TAGPB, "0_PB Pull battue");
                    displayRecord();
                }
            }

            //si le reccord de poucentage a été battu
            if (p.getAllTimeRecordPourc() != null) {
                if (p.isPourcPBBrocken()) {
                    Log.d(TAGPB, "2_PB raport poid puissance battue");
                    //TODO
                }
            }

            //denier session pull battu
            if (p.getLastDay() != null)
                if (p.getLastDay().pull >= pull) {
                    Log.d(TAGPB, "dernier session pull batue");
                    //TODO
                }


            if(p.getToDayPull() != null){
                if(p.ispullTodayBrocken()){
                    displayTodayPull();
                }
            }

        }
        else{
            if(pull == 0)
                actif = true;
        }


    }

    //display methode***********************PULL*****************************display methode
    private void displayTodayPull() {
        Pull p = Res.currentPrehension.getToDayPull();
        if(p != null){
            Log.d(TAGPB, "pull "+p.pull+"  =>  "+p.pourcentage);
            thisSessionPull.setText(p.pull+" kg");
            thisSessionpourc.setText(p.pourcentage+"%");
        }
        else{
            thisSessionPull.setText(R.string._0_kg);
            thisSessionpourc.setText(R.string._0p);
        }
    }

    void displayCurrentPull(double pull){
        //currentPull.setText(pull+" Kg");
        //if(Res.getPour(pull) == 0)
            //currentPullPour.setText("");
        //else
            //currentPullPour.setText(Res.getPour(pull)+"%");
    }

    void displayRecord(){
        Pull p = Res.currentPrehension.getAllTimeRecordPull();
        if(p != null){
            record.setText(p.pull+" kg");
            recordPullPour.setText(p.pourcentage+"%");
        }
        else{
            record.setText(R.string._0_kg);
            recordPullPour.setText(R.string._0p);
        }
    }

    private void displayLastDay(){
        Pull p = Res.currentPrehension.getLastDay();
        if(p != null){
            lastSessionPull.setText(p.pull+" kg");
            lastSessionPourc.setText(p.pourcentage+"%");
        }
        else{
            lastSessionPull.setText(R.string._0_kg);
            lastSessionPourc.setText(R.string._0p);
        }
    }

    void displayLastPull(){
        lastPullPull.setText(graph.maxPull+" kg" );
        lastPullPourc.setText(graph.maxPourcentage+" %" );
    }

    //display methode***********************PULL*****************************display methode


    /**
     * feedback graphique en fonction de l'etat de la connexion bluetooth*****************************Bluetooth**************
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


    //*************************************************************************Select prise stuff*****************************************************************************************************
    //*************************************************************************Select prise stuff*****************************************************************************************************
    private void toggleSelectPrise(){
        if(scrollPrise.getVisibility() == View.VISIBLE){
            replierSelectPrise();
        }
        else{
            deplierSelectPrise();
        }
    }

    private void deplierSelectPrise(){

        //annimate
        listPrise.setTranslationY(-Res.dpToPixel(this,260));
        scrollPrise.setVisibility(View.VISIBLE);

        containerInfoPrise.setElevation(35);
        mask.setElevation(30);
        mask.setVisibility(View.VISIBLE);
        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replierSelectPrise();
                containerInfoPrise.setElevation(10);
                mask.setVisibility(View.GONE);
            }
        });

        listPrise.animate()
                .translationY(0)
                .setDuration(260)
                .setListener(null);

        toggleSelectPrise.animate()
                .rotation(180)
                .setDuration(260);
    }

    private void replierSelectPrise(){
        listPrise.animate()
                .translationY(-Res.dpToPixel(this,260))
                .setDuration(260)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        scrollPrise.setVisibility(View.GONE);
                    }
                });
        toggleSelectPrise.animate()
                .rotation(0)
                .setDuration(260);
    }

    /**
     * vérifie si aucune préhension existe. si c'est le cas en créée une
     */
    private void updatePrehension() {
        if(Res.prehensions.size() == 0) {
            //db
            Log.d(DB.LOG_db, "read DB:");
            Res.prehensions.addAll(dataBase.getHold());

            //si pas de prise encore enregistrer
            if (Res.prehensions.size() == 0) {
                Res.addNewHold(this, "prise 1");
            }

            if (Res.store.getCurrentPrehensionIndex() >= Res.prehensions.size())//si la prise selectioner n'existe plus
                Res.currentPrehension = Res.prehensions.get(0);
            else
                Res.currentPrehension = Res.prehensions.get(Res.store.getCurrentPrehensionIndex());

            priseSelected.setText(Res.currentPrehension.nom);
        }
    }


    /****************************************************************************************************************************************************************************************************************************/
    /**************************************************************************************************DISPLAY LISTE PRISE***************************************************************************************************************/
    /****************************************************************************************************************************************************************************************************************************/
    int widthNom = 400;
    int heightNom = 150;

    void displayListHold(){
        int i = 0;
        for(Prehension p : Res.prehensions){
            listPrise.addView(creatLine(p));
            i++;
        }

        //ligne pour ajouter une nouvelle prise
        listPrise.addView(ligneAdd());

    }

    private LinearLayout creatLine(final Prehension p){
        final LinearLayout ligne = new LinearLayout(this);


        //paramsEdit.setMargins(10,10,10,10);

        //icons
        LinearLayout.LayoutParams paramsIcon = new LinearLayout.LayoutParams(80,80);
        paramsIcon.setMargins(10,10,10,10);

        int nomSize = (titreSelect.getChildAt(0).getWidth() + titreSelect.getChildAt(1).getWidth() )- (paramsIcon.width + paramsIcon.leftMargin+ paramsIcon.rightMargin)*2;

        //layout
        LinearLayout.LayoutParams paramsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,heightNom);

        //text view NOM
        LinearLayout.LayoutParams paramsTextView = new LinearLayout.LayoutParams(widthNom,LinearLayout.LayoutParams.MATCH_PARENT);
        //paramsTextView.setMargins(50,-50,0,0);

        //edit text
        LinearLayout.LayoutParams paramsEdit = new LinearLayout.LayoutParams(widthNom,LinearLayout.LayoutParams.WRAP_CONTENT);

        //bouton vlider modif
        LinearLayout.LayoutParams validModif = new LinearLayout.LayoutParams(80,80);
        validModif.setMargins(60,10,60,10);
        validModif.gravity = Gravity.CENTER_VERTICAL;

        //text view
        TextView nom = new TextView(this);
        nom.setText(p.nom);
        nom.setLayoutParams(paramsTextView);
        nom.setGravity(Gravity.CENTER_VERTICAL);
        nom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPrehenssion(p,true);
            }
        });
        nom.setTextColor(Color.BLACK);
        nom.setTextSize(18);

        //edit
        Button edit = new Button(this);
        edit.setBackground(ContextCompat.getDrawable(this,R.drawable.edit_list_prise));
        edit.setLayoutParams(paramsIcon);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrehenssion(ligne, p);
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

        //Edit Texte
        EditText editNom = new EditText(this);
        editNom.setText(p.nom);
        editNom.setLayoutParams(paramsEdit);
        editNom.setSingleLine(true);
        editNom.setImeActionLabel("Valider", EditorInfo.IME_ACTION_DONE);
        editNom.setSelectAllOnFocus(true);
        editNom.setVisibility(View.GONE);
        editNom.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                validerModif(ligne, p);
                return false;
            }
        });

        //boutton valider modification
        Button validerButton = new Button(this);
        validerButton.setLayoutParams(validModif);
        validerButton.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_ok));
        validerButton.setVisibility(View.GONE);
        validerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validerModif(ligne, p);
            }
        });

        //la ligne

        ligne.setOrientation(LinearLayout.HORIZONTAL);
        //ligne.setBackground(ContextCompat.getDrawable(this,R.drawable.border));
        ligne.setPadding(10,10,10,10);
        ligne.setLayoutParams(paramsLine);
        ligne.setBaselineAligned(false);
        ligne.setGravity(Gravity.CENTER_VERTICAL);
        if(p == Res.currentPrehension) {
            ligne.setBackground(ContextCompat.getDrawable(this, R.drawable.border_1_gris));
            nom.setTextColor(Color.WHITE);
        }
        else {
            ligne.setBackground(ContextCompat.getDrawable(this, R.drawable.border_1_blanc));
            nom.setTextColor(Color.BLACK);
        }

        ligne.addView(nom);//0
        ligne.addView(edit);//1
        ligne.addView(del);//2
        ligne.addView(editNom);//3 gone
        ligne.addView(validerButton);//4 gone

        return ligne;
    }

    //dernier ligne ajouter prise
    private LinearLayout ligneAdd(){

        final LinearLayout line = new LinearLayout(this);
        //parametre
        //edit Text
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthNom,LinearLayout.LayoutParams.WRAP_CONTENT);
        //params.setMargins(10,10,10,10);

        //bouton
        LinearLayout.LayoutParams paramsButton = new LinearLayout.LayoutParams(80,80);
        paramsButton.setMargins(60,10,60,10);
        paramsButton.gravity = Gravity.CENTER_VERTICAL;

        LinearLayout.LayoutParams paramsButton2 = new LinearLayout.LayoutParams(100,100);
        paramsButton2.setMargins(60,15,60,15);
        //paramsButton2.gravity = Gravity.CENTER;

        final Button aparaitre = new Button(this);
        final Button add = new Button(this);

        //Layout
        LinearLayout.LayoutParams paramsLine = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,heightNom);

        final EditText edit = new EditText(this);
        edit.setLayoutParams(params);
        edit.setSingleLine(true);
        edit.setImeActionLabel("Ajouter", EditorInfo.IME_ACTION_DONE);
        edit.setHint("Ajouter prise");
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addPrehenssion(line);
                return false;
            }
        });
        edit.setVisibility(View.GONE);


        //boutton ajouter
        add.setLayoutParams(paramsButton);
        add.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_plus_blanc_256));
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPrehenssion(line);
            }
        });
        add.setVisibility(View.GONE);


        //bouton aparaitre
        aparaitre.setLayoutParams(paramsButton2);
        aparaitre.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_plus_blanc_256));
        aparaitre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEdit(line);
            }
        });

        //Layout
        line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEdit(line);
            }
        });
        line.setOrientation(LinearLayout.HORIZONTAL);
        line.setGravity(Gravity.CENTER);
        //line.setPadding(10,10,10,10);
        line.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_bottom_blanc));
        line.setLayoutParams(paramsLine);
        line.setBaselineAligned(false);

        line.addView(edit);
        line.addView(add);
        line.addView(aparaitre);

        return line;
    }


    /**
     *
     * @param p prehenssion qui va etre selectioner
     * @param repli si vrai replie la selection des prise après la selection
     */
    private void selectPrehenssion(Prehension p, boolean repli) {
        LinearLayout l;
        //feedback
        for(int i = 0; i < listPrise.getChildCount()-1;i++){
            l = (LinearLayout)listPrise.getChildAt(i);
            if(i == Res.prehensions.indexOf(p)) {
                l.setBackground(ContextCompat.getDrawable(this, R.drawable.border_1_gris));
                TextView t = (TextView)(l.getChildAt(0));//textview
                t.setTextColor(Color.WHITE);
            }
            else{
                l.setBackground(ContextCompat.getDrawable(this, R.drawable.border_1_blanc));
                TextView t = (TextView)(l.getChildAt(0));//textview
                t.setTextColor(Color.BLACK);
            }
        }

        //select
        Res.currentPrehension = p;
        Res.store.setCurrentPrehenssionIndex(Res.prehensions.indexOf(p));

        //show
        priseSelected.setText(p.nom);
        displayRecord();
        displayLastDay();
        displayTodayPull();
        graph.resetMaxPull();
        displayLastPull();

        //fermer select
        if(repli)
            toggleSelectPrise();
    }

    private void deletPrehenssion(final Prehension p) {
        //veriffier que ce n'est pas la dernier prise
        if(Res.prehensions.size() > 1){

            //Alert confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.msg_del_hold);

            // Add the buttons
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    //annimation
                    listPrise.removeViewAt(Res.prehensions.indexOf(p));
                    Res.prehensions.remove(p);
                    dataBase.removeHold(p);
                    //verifi que c'etait pas la prehenssion selectionné
                    if(p == Res.currentPrehension){
                        selectPrehenssion(Res.prehensions.get(0), false);
                    }

                    Log.d(TAG,"remove "+p.nom);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //modifier préhenssion
    private void setPrehenssion(LinearLayout l, Prehension p){

        int translation = widthNom+210;

        TextView nom = (TextView)l.getChildAt(0);
        Button buttonEdit = (Button)l.getChildAt(1);
        Button buttonDel= (Button)l.getChildAt(2);
        final EditText edit = (EditText)l.getChildAt(3);
        final Button valider= (Button)l.getChildAt(4);

        //on met le nom dans edit
        edit.setText(nom.getText().toString());

        //onrègle la couleur de l'edit text n fontion de la selection
        if(Res.currentPrehension == p){
            edit.setTextColor(Color.WHITE);
        }
        else{
            edit.setTextColor(Color.BLACK);
        }

        //on les affiche
        edit.setVisibility(View.VISIBLE);
        valider.setVisibility(View.VISIBLE);

        //on anime tout le monde
        edit.animate().translationX(-translation).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                edit.setVisibility(View.VISIBLE);
            }
        });
        valider.animate().translationX(-translation).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                valider.setVisibility(View.VISIBLE);
            }
        });

        nom.animate().translationX(-translation);
        buttonEdit.animate().translationX(-translation);
        buttonDel.animate().translationX(-translation);

        //get FOCUS
        edit.requestFocus();
        Res.showKeyboard(this);

        Log.d(TAG,"modif "+p);
    }


    private void validerModif(LinearLayout l, Prehension p){
        //hide keyboard
        Res.hideKeyboard(this);

        //verification
        final EditText edit = (EditText)l.getChildAt(3);
        TextView nom = (TextView)l.getChildAt(0);
        if(!edit.getText().toString().equals("")){
            p.nom = edit.getText().toString();
            nom.setText(p.nom);
            dataBase.upateHoldName(p);
        }
        //validation

        //view
        Button buttonEdit = (Button)l.getChildAt(1);
        Button buttonDel= (Button)l.getChildAt(2);
        final Button valider= (Button)l.getChildAt(4);
        //int translation = widthNom+210;

        //on anime tout le monde
        edit.animate()
                .translationX(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        edit.setVisibility(View.GONE);
                    }
                })
        ;
        valider.animate()
                .translationX(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        valider.setVisibility(View.GONE);
                    }
        });

        nom.animate().translationX(0);
        buttonEdit.animate().translationX(0);
        buttonDel.animate().translationX(0);

        Log.d(TAG,"modif "+p);
    }

    //Ajout d'un Prise
    public void addPrehenssion(LinearLayout l){
        EditText nom = (EditText)l.getChildAt(0);
        if(!nom.getText().toString().equals("")){
            Res.hideKeyboard(this);
            Prehension p = Res.addNewHold(this,nom.getText().toString());
            listPrise.addView(creatLine(p), listPrise.getChildCount()-1);
            nom.setText("");

            hideEdit(l);

            Log.d(TAG,"ajout "+nom);
        }
        else{
            hideEdit(l);
        }
    }

    private void hideEdit(LinearLayout l){
        final EditText edit = (EditText) l.getChildAt(0);
        final Button add = (Button) l.getChildAt(1);
        final Button apparaitre = (Button) l.getChildAt(2);
        Res.hideKeyboard(this);

        apparaitre.setTranslationY(-150);
        edit.animate()
                .translationY(150)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                edit.setVisibility(View.GONE);
                apparaitre.setVisibility(View.VISIBLE);
                apparaitre.animate().translationY(0).setListener(null);

            }
        });

        add.animate()
                .translationY(150)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        add.setVisibility(View.GONE);
                    }
                });



        new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(200);
                scrollPrise.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollPrise.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }).start();
    }

    private void showEdit(LinearLayout l){

        final EditText edit = (EditText) l.getChildAt(0);
        final Button add = (Button) l.getChildAt(1);
        final Button apparaitre = (Button) l.getChildAt(2);
        final Activity ma = this;

        add.setTranslationY(-150);
        edit.setTranslationY(-150);



        apparaitre.animate().translationY(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                apparaitre.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
                add.setVisibility(View.VISIBLE);

                edit.animate().translationY(0).setListener(null);
                add.animate().translationY(0).setListener(null);
                edit.requestFocus();
                Res.showKeyboard(ma);
            }
        });
    }
    /**
     * classe pour gérer les gestes
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    class ActivityChangeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            //Log.d(Res.TAG_gesture,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getX() - event2.getX() > 300.0) {
                if (Math.abs(event1.getY() - event2.getY()) < 150.0){
                    Log.d(Res.TAG_gesture, "onFling: " + event1.getX() + event2.getX());
                    goToSuspensionActivity();
                }
            }
            return true;
        }
    }

    private void goToSuspensionActivity() {
        Intent myIntent = new Intent(MainActivity.this, SuspensionsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }
}

