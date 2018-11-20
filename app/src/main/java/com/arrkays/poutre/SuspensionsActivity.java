package com.arrkays.poutre;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import static java.lang.Integer.valueOf;

public class SuspensionsActivity extends AppCompatActivity {

    Button startButton = null;
    Button optionsButton = null;
    Button timerStopButton;
    ProgressBar progressBar = null;
    ProgressBar progressBar2 = null;
    CountDownTimerPausable timerE = null;
    CountDownTimer restTimer = null;
    ConstraintLayout optionsLayout = null;
    NumberPicker suspensionsTimeNPsec = null;
    NumberPicker suspensionsTimeNPmin = null;
    NumberPicker restTimeNPsec = null;
    NumberPicker restTimeNPmin;
    TextView chronoTextView;
    TextView repTextView;
    TextView setTextView;
    ConstraintLayout mask;

    Vibrator v;


    long suspensionTime = 10;
    long restTime = 10;
    long timerDuration = suspensionTime;
    int nbrSet = 5;
    int nbrRep = 5;
    int setRealises = 0;
    int repRealises = 0;
    boolean timerStarted = false;
    boolean rest = true;
    String etatTimer = "Stop";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspensions);

        startButton = (Button) findViewById(R.id.startButton);
        optionsButton = (Button) findViewById(R.id.optionsButton);
        timerStopButton = findViewById(R.id.timerStopButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        optionsLayout = (ConstraintLayout) findViewById(R.id.optionsLayout) ;
        suspensionsTimeNPsec = (NumberPicker) findViewById(R.id.suspensionTimeNPsec);
        suspensionsTimeNPsec.setMinValue(0);
        suspensionsTimeNPsec.setMaxValue(59);
        suspensionsTimeNPsec.setValue(10);
        suspensionsTimeNPmin = (NumberPicker) findViewById(R.id.suspensionTimeNPmin);
        suspensionsTimeNPmin.setMinValue(0);
        suspensionsTimeNPmin.setMaxValue(59);
        restTimeNPsec = (NumberPicker) findViewById(R.id.restTimeNPsec);
        restTimeNPsec.setMinValue(0);
        restTimeNPsec.setMaxValue(59);
        restTimeNPsec.setValue(10);
        restTimeNPmin = (NumberPicker) findViewById(R.id.restTimeNPmin);
        restTimeNPmin.setMinValue(0);
        restTimeNPmin.setMaxValue(59);
        chronoTextView = (TextView) findViewById(R.id.chronoTextView);
        setTextView = (TextView) findViewById(R.id.setTextView);
        repTextView = (TextView) findViewById(R.id.repTextView);
        mask = findViewById(R.id.mask);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etatTimer == "Stop") {
                    startButton.setText("Pause");
                    timerStarted = true;
                    repRealises = 0;
                    setRealises = 0;
                    progressBar.setProgress(0);
                    progressBar2.setProgress(0);
                    repTextView.setText(repRealises + " / " + nbrRep);
                    setTextView.setText(setRealises + " / " + nbrSet);
                    etatTimer = "Start";
                    exercice();
                }
                else if (etatTimer == "Start"){
                    startButton.setText("Resume");
                    timerStopButton.setVisibility(View.VISIBLE);
                    etatTimer = "Pause";
                    timerE.pause();
                }
                else if (etatTimer == "Pause"){
                    startButton.setText("Start");
                    etatTimer = "Start";
                    timerStopButton.setVisibility(View.GONE);
                    //timerStarted = false;
                    timerE.start();
                }

            }
        });

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (optionsLayout.getVisibility() == View.VISIBLE){
                    optionsLayout.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                    suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
                    restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
                }
                else {
                    optionsLayout.setVisibility(View.VISIBLE);
                    mask.setVisibility(View.VISIBLE);
                }
            }
        });
        timerStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timerE.cancel();
                timerStopButton.setVisibility(View.GONE);
                etatTimer = "Stop";
                startButton.setText("Start");
                progressBar.setProgress(0);
                progressBar2.setProgress(0);
                repTextView.setText("  ");
                setTextView.setText("  ");
                chronoTextView.setText(0+"");
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
                if (optionsButton.getVisibility() == View.VISIBLE) {
                    optionsLayout.setVisibility(View.GONE);
                    mask.setVisibility(View.GONE);
                    suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
                    restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
                }
            }
        });


    }
    public void exercice() {
        timerE = new CountDownTimerPausable(timerDuration, 10) {

            @Override
            public void onTick(long millisUntilFinished) {
                if (!rest) {
                    progressBar.setProgress((int) (1 + (suspensionTime - (double) (millisUntilFinished - 10)) / suspensionTime * 100));
                    chronoTextView.setText((int) (1 + (millisUntilFinished - 10) / 1000) + "");
                }
                else {
                    progressBar2.setProgress((int) (1 + (restTime - (double) (millisUntilFinished - 10)) / restTime * 100));
                    chronoTextView.setText((int) (1 + (millisUntilFinished - 10) / 1000) + "");
                }
            }

            @Override
            public void onFinish() {
                //v.vibrate(100);
                chronoTextView.setText(0 + "");
                if (rest){
                    timerDuration = suspensionTime;
                    repRealises++;
                    progressBar.setProgress(0);
                    progressBar2.setProgress(0);
                }
                else {
                    timerDuration = restTime;
                }
                rest = !rest;
                exercice();
            }
        };

        if (!rest){
            timerE.start();
        }
        else if (setRealises < nbrSet) {
            if (repRealises >= nbrRep){
                repRealises = 0;
                setRealises ++;
            }
            repTextView.setText(repRealises + " / " + nbrRep);
            setTextView.setText(setRealises + " / " + nbrSet);
            timerE.start();

        }

    }

}
