package com.arrkays.poutre;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.GestureDetectorCompat;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import static java.lang.Math.floor;

public class SuspensionsActivity extends AppCompatActivity {

    Button startButton;
    Button button_excerciceSettings;
    Button timerStopButton;
    Button button_option;
    Button button_settings;
    ProgressBar progressBar = null;
    ProgressBar progressBar2 = null;
    ConstraintLayout optionsLayout = null;
    NumberPicker suspensionsTimeNPsec = null;
    NumberPicker suspensionsTimeNPmin = null;
    NumberPicker restTimeNPsec = null;
    NumberPicker restTimeNPmin;
    NumberPicker restTimeBtSetNPmin;
    NumberPicker restTimeBtSetNPsec;
    TextView chronoTextView;
    TextView repTextView;
    TextView setTextView;
    TextView repChooseTextView;
    TextView setChooseTextView;
    TextView lastPullTextView;
    TextView lastPullPourcTextView;
    TextView suspensionTimeTextView;
    ConstraintLayout mask;
    CheckBox maxHangsCheckBox;
    SeekBar repSeekBar;
    SeekBar setSeekBar;
    ConstraintLayout layout_navigationMenu;

    CountDownTimerPausable timerE;
    CountDownTimerPausable timerRest;
    CountDownTimerPausable timerHangs;

    Vibrator v;
    private GestureDetectorCompat mDetector;

    long suspensionTime = 10;
    long restTime = 10;
    long restTimeBtSets = 60;
    long timerDuration = suspensionTime;
    long timeStart = 0;
    long lastPullDuaration = 0;
    double maxLastPull = 0.0;
    int nbrSet = 5;
    int nbrRep = 5;
    int setRealises = 0;
    int repRealises = 0;
    boolean timerStarted = false;
    boolean rest = true;
    boolean endSet = false;
    String etatTimer = "Stop";
    String currentTheme;

    public Handler suspensionActivityHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == Res.CHANGE_VALUE_MAX_PULL){
                lastPullTextView.setText( String.valueOf((double)msg.obj) );
                lastPullPourcTextView.setText( String.valueOf( Res.round((double)msg.obj/Res.poids *100.0, 1) ) );
            }

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Res.store.getCurrentTheme().equals("Dark")) {
            setTheme(R.style.AppDarkTheme);
            currentTheme = "Dark";
        } else {
            setTheme(R.style.AppLightTheme);
            currentTheme = "Light";
        }

        setContentView(R.layout.activity_suspensions);

        mDetector = new GestureDetectorCompat(this, new ActivityChangeGestureListener());

        startButton = findViewById(R.id.startButton);
        button_excerciceSettings = findViewById(R.id.button_excerciceSettings);
        button_option = findViewById(R.id.button_option);
        timerStopButton = findViewById(R.id.timerStopButton);
        button_settings = findViewById(R.id.button_Settings);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        optionsLayout = (ConstraintLayout) findViewById(R.id.optionsLayout);
        suspensionsTimeNPsec = (NumberPicker) findViewById(R.id.suspensionTimeNPsec);
        suspensionsTimeNPmin = (NumberPicker) findViewById(R.id.suspensionTimeNPmin);
        restTimeNPsec = (NumberPicker) findViewById(R.id.restTimeNPsec);
        restTimeNPmin = (NumberPicker) findViewById(R.id.restTimeNPmin);
        restTimeBtSetNPmin = findViewById(R.id.restTimeBtSetNPmin);
        restTimeBtSetNPsec = findViewById(R.id.restTimeBtSetNPsec);
        chronoTextView = (TextView) findViewById(R.id.chronoTextView);
        setTextView = (TextView) findViewById(R.id.setTextView);
        repTextView = (TextView) findViewById(R.id.repTextView);
        repChooseTextView = findViewById(R.id.repChooseTextView);
        setChooseTextView = findViewById(R.id.setChooseTextView);
        lastPullTextView = findViewById(R.id.lastPullTextView);
        lastPullPourcTextView = findViewById(R.id.lastPullPourcTextView);
        suspensionTimeTextView = findViewById(R.id.suspensionTimeTextView);
        mask = findViewById(R.id.mask);
        maxHangsCheckBox = findViewById(R.id.maxHangsCheckBox);
        setSeekBar = findViewById(R.id.setSeekBar);
        repSeekBar = findViewById(R.id.repSeekBar);
        layout_navigationMenu = findViewById(R.id.navigationMenu);

        setSeekBar.setProgress(nbrSet);
        setChooseTextView.setText(nbrSet+"");
        repSeekBar.setProgress(nbrRep);
        repChooseTextView.setText(nbrRep+"");

        suspensionsTimeNPmin.setMinValue(0);
        suspensionsTimeNPmin.setMaxValue(59);
        suspensionsTimeNPmin.setValue(0);
        suspensionsTimeNPsec.setMinValue(0);
        suspensionsTimeNPsec.setMaxValue(59);
        suspensionsTimeNPsec.setValue(10);
        restTimeNPmin.setMinValue(0);
        restTimeNPmin.setMaxValue(59);
        restTimeNPmin.setValue(0);
        restTimeNPsec.setMinValue(0);
        restTimeNPsec.setMaxValue(59);
        restTimeNPsec.setValue(10);
        restTimeBtSetNPmin.setMinValue(0);
        restTimeBtSetNPmin.setMaxValue(59);
        restTimeBtSetNPmin.setValue(1);
        restTimeBtSetNPsec.setMinValue(0);
        restTimeBtSetNPsec.setMaxValue(59);
        restTimeBtSetNPsec.setValue(0);

        suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
        restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
        restTimeBtSets = 1000*(restTimeBtSetNPmin.getValue() * 60 + restTimeBtSetNPsec.getValue());

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        button_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (layout_navigationMenu.getVisibility() == View.VISIBLE) {
                    layout_navigationMenu.setVisibility(View.GONE);
                }
                else {
                    layout_navigationMenu.setVisibility(View.VISIBLE);
                }
            }
        });
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(SuspensionsActivity.this, SettingsActivity.class);
                SuspensionsActivity.this.startActivity(myIntent);
                layout_navigationMenu.setVisibility(View.GONE);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etatTimer == "Stop") {
                    startButton.setText("Pause");
                    repRealises = 0;
                    setRealises = 0;
                    progressBar.setProgress(0);
                    progressBar2.setProgress(0);
                    repTextView.setText(repRealises + " / " + nbrRep);
                    setTextView.setText(setRealises + " / " + nbrSet);
                    etatTimer = "Start";
                    if (maxHangsCheckBox.isChecked()){
                        lastPullTextView.setVisibility(View.VISIBLE);
                        chronoTextView.setText("GO");
                        Res.weightNotif.addListener(weightListenerHangs);
                    }
                    else {
                        timerStarted = true;
                        timerDuration = suspensionTime;
                        endSet = false;
                        rest = false;
                        exercice();
                    }
                }
                else if (etatTimer == "Start"){
                    startButton.setText("Resume");
                    timerStopButton.setVisibility(View.VISIBLE);
                    etatTimer = "Pause";
                    if (maxHangsCheckBox.isChecked()){
                        if (timerHangs!=null){
                            timerHangs.pause();
                        }
                    }
                    else {
                        if (!endSet) {
                            timerE.pause();
                        } else {
                            timerRest.pause();
                        }
                    }
                }
                else if (etatTimer == "Pause"){
                    startButton.setText("Start");
                    etatTimer = "Start";
                    timerStopButton.setVisibility(View.GONE);
                    if (maxHangsCheckBox.isChecked()) {
                        if (timerHangs!=null) {
                            timerHangs.start();
                        }
                    }
                    else {
                        if (!endSet) {
                            timerE.start();
                        } else {
                            timerRest.start();
                        }
                    }
                }

            }
        });

        button_excerciceSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (optionsLayout.getVisibility() == View.VISIBLE){
                    optionsLayout.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                    suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
                    restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
                    restTimeBtSets = 1000*(restTimeBtSetNPmin.getValue() * 60 + restTimeBtSetNPsec.getValue());
                }
                else {
                    optionsLayout.setVisibility(View.VISIBLE);
                    mask.setVisibility(View.VISIBLE);
                }
            }
        });
        // Enleve la possibilité de choisir le temps de suspension si on choisi de faire de la force max
        maxHangsCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxHangsCheckBox.isChecked()){
                    suspensionTimeTextView.setVisibility(View.GONE);
                    suspensionsTimeNPmin.setVisibility(View.GONE);
                    suspensionsTimeNPsec.setVisibility(View.GONE);
                }
                else {
                    suspensionTimeTextView.setVisibility(View.VISIBLE);
                    suspensionsTimeNPmin.setVisibility(View.VISIBLE);
                    suspensionsTimeNPsec.setVisibility(View.VISIBLE);
                }
            }
        });
        timerStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timerStopButton.setVisibility(View.GONE);
                etatTimer = "Stop";
                startButton.setText("Start");
                progressBar.setProgress(0);
                progressBar2.setProgress(0);
                repTextView.setText("  ");
                setTextView.setText("  ");
                chronoTextView.setText(0+"");

                if (maxHangsCheckBox.isChecked()) {
                    lastPullTextView.setVisibility(View.VISIBLE);
                    Res.weightNotif.removeListener(weightListenerHangs);
                }
                else {
                    timerE.cancel();
                    timerRest.cancel();
                    rest = false;
                    endSet = false;
                }
            }
        });

        suspensionsTimeNPsec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
            }
        });

        mask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (button_excerciceSettings.getVisibility() == View.VISIBLE) {
                    optionsLayout.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                    suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
                    restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
                    restTimeBtSets = 1000*(restTimeBtSetNPmin.getValue() * 60 + restTimeBtSetNPsec.getValue());
                }
            }
        });

        repSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                repChooseTextView.setText(i+"");
                nbrRep = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        setSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setChooseTextView.setText(i+"");
                nbrSet = i;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (Res.store.getCurrentTheme().equals(currentTheme)) {}
        else {
            //recreate();
        }

    }

    /**
     * Fonction pour faire des suspensios de type rési
     */
    public void exercice() {
        timerE = new CountDownTimerPausable(timerDuration, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!rest) {
                    progressBar.setProgress((int) (1 + (suspensionTime - (double) (millisUntilFinished - 10)) / suspensionTime * 10000));
                    chronoTextView.setText((int) (1 + (millisUntilFinished - 10) / 1000) + "");
                }
                else if (rest) {
                    progressBar2.setProgress((int) (1 + (restTime - (double) (millisUntilFinished - 10)) / restTime * 10000));
                    chronoTextView.setText((int) (1 + (millisUntilFinished - 10) / 1000) + "");
                }
            }
            @Override
            public void onFinish() {
                //v.vibrate(100);
                chronoTextView.setText(0 + "");
                if (rest) {
                    timerDuration = suspensionTime;
                    repRealises++;
                    progressBar.setProgress(0);
                    progressBar2.setProgress(0);
                }
                else {
                    timerDuration = restTime;
                }
                rest = !rest;
                if (repRealises == 0){
                    repRealises++;
                }
                else if (repRealises == nbrRep) {
                    timerDuration = suspensionTime;
                }
                exercice();
            }
        };
        timerRest = new CountDownTimerPausable(restTimeBtSets, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                    progressBar.setProgress((int) (1 + (restTimeBtSets - (double) (millisUntilFinished - 10)) / restTimeBtSets * 10000));
                    progressBar2.setProgress((int) (1 + (restTimeBtSets - (double) (millisUntilFinished - 10)) / restTimeBtSets * 10000));
                    chronoTextView.setText((int) (1 + (millisUntilFinished - 10) / 1000) + "");
            }
            @Override
            public void onFinish() {
                progressBar.setProgress(0);
                progressBar2.setProgress(0);
                endSet = false;
                exercice();
            }
        };

        if (!rest){
            repTextView.setText(repRealises + " / " + nbrRep);
            setTextView.setText(setRealises + " / " + nbrSet);
            timerE.start();
        }
        else if (setRealises < nbrSet) {
            if (repRealises >= nbrRep){
                repRealises = 0;
                setRealises ++;
                endSet = true;
                rest = false;
                repTextView.setText(repRealises + " / " + nbrRep);
                setTextView.setText(setRealises + " / " + nbrSet);
                timerRest.start();
            }
            else {
                repTextView.setText(repRealises + " / " + nbrRep);
                setTextView.setText(setRealises + " / " + nbrSet);
                timerE.start();
            }
        }
    }


    /*
        Fonction pour faire des suspension max. Lance un chrono lorsque l'utilisateur se met dessus et lance un temps de repos lorsqu'il tombe.
     */
    WeightListener weightListenerHangs = new WeightListener() {

        @Override
        public void onChange(double weight, boolean comp[]) {
            if (weight > 5.0 && timeStart == 0) {
                timeStart = Calendar.getInstance().getTimeInMillis();
                chronoTextView.setText(String.valueOf(0));
                maxLastPull = 0;
            }
            else if (weight >= 5.0 && timeStart != 0) {
                chronoTextView.setText(String.valueOf(floor(((double) Calendar.getInstance().getTimeInMillis() - (double) timeStart) / 100.0) / 10.0));
                if (maxLastPull < weight){
                    maxLastPull = weight;
                    Message msg = new Message();
                    msg.arg1 = Res.CHANGE_VALUE_MAX_PULL;
                    msg.obj = maxLastPull;
                    //Log.d(Res.TAG,maxLastPull+"");
                    suspensionActivityHandler.sendMessage(msg);
                }
            }
            else if (weight < 5.0 && timeStart != 0) {
                lastPullDuaration = timeStart;
                timeStart = 0;
                Res.weightNotif.removeListener(weightListenerHangs);
                new updateNumbers().execute((Void[])null);
                excerciceMaxHangs(weight);
            }
        }
    };
    public void excerciceMaxHangs(double weight) {
        Looper.prepare();
        timerHangs = new CountDownTimerPausable(restTime, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                //progressBar.setProgress((int) (1 + (restTime - (double) (millisUntilFinished - 10)) / restTime * 10000));
                //progressBar2.setProgress((int) (1 + (restTime - (double) (millisUntilFinished - 10)) / restTime * 10000));
                chronoTextView.setText(String.valueOf( (int) (1 + (millisUntilFinished - 10) / 1000) ));
            }
            @Override
            public void onFinish() {
                progressBar.setProgress( 10000 );
                progressBar2.setProgress(  10000 );
                chronoTextView.setText("GO");
                if (Res.vibration){
                    v.vibrate(200);
                }
                Res.weightNotif.addListener(weightListenerHangs);
            }
        }.start();
        Looper.loop();
    }

    private class updateNumbers extends AsyncTask<Void,Void,Void> {
        protected Void doInBackground(Void...param){
            repRealises ++;
            if (repRealises >= nbrRep){
                if (setRealises < nbrSet) {
                    setRealises++;
                    repRealises = 0;
                }
                else {
                    setRealises ++;
                }
            }
            return null;
        }
        protected void onPostExecute(Void param) {
            //lastPullTextView.setText(String.valueOf(floor(((double) Calendar.getInstance().getTimeInMillis() - (double) lastPullDuaration) / 100.0) / 10.0)); // affiche la durée de la dernière suspension
            repTextView.setText(repRealises + " / " + nbrRep);
            setTextView.setText(setRealises + " / " + nbrSet);
        }
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
            if (event2.getX() - event1.getX() > 300.0) {
                if (Math.abs(event1.getY() - event2.getY()) < 150.0){
                    Log.d(Res.TAG_gesture, "onFling: " + event1.getX() + event2.getX());
                    goToMainActivity();
                }
            }
            return true;
        }
    }
    private void goToMainActivity() {
        Intent myIntent = new Intent(SuspensionsActivity.this, MainActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        SuspensionsActivity.this.startActivity(myIntent);
    }
}

