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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import static java.lang.Integer.valueOf;

public class SuspensionsActivity extends AppCompatActivity {

    Button startButton = null;
    Button optionsButton = null;
    Button timerStopButton;
    ProgressBar progressBar = null;
    ProgressBar progressBar2 = null;
    CountDownTimerPausable timerE = null;
    CountDownTimerPausable timerRest = null;
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
    ConstraintLayout mask;
    CheckBox maxHangsCheckBox;
    SeekBar repSeekBar;
    SeekBar setSeekBar;

    Vibrator v;


    long suspensionTime = 10;
    long restTime = 10;
    long restTimeBtSets = 60;
    long timerDuration = suspensionTime;
    int nbrSet = 5;
    int nbrRep = 5;
    int setRealises = 0;
    int repRealises = 0;
    boolean timerStarted = false;
    boolean rest = true;
    boolean endSet = false;
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
        mask = findViewById(R.id.mask);
        maxHangsCheckBox = findViewById(R.id.maxHangsCheckBox);
        setSeekBar = findViewById(R.id.setSeekBar);
        repSeekBar = findViewById(R.id.repSeekBar);

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

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etatTimer == "Stop") {
                    startButton.setText("Pause");
                    timerStarted = true;
                    repRealises = 0;
                    setRealises = 0;
                    timerDuration = suspensionTime;
                    progressBar.setProgress(0);
                    progressBar2.setProgress(0);
                    repTextView.setText(repRealises + " / " + nbrRep);
                    setTextView.setText(setRealises + " / " + nbrSet);
                    etatTimer = "Start";
                    endSet = false;
                    rest = false;
                    exercice();
                }
                else if (etatTimer == "Start"){
                    startButton.setText("Resume");
                    timerStopButton.setVisibility(View.VISIBLE);
                    etatTimer = "Pause";
                    if (!endSet) {
                        timerE.pause();
                    }
                    else {
                        timerRest.pause();
                    }
                }
                else if (etatTimer == "Pause"){
                    startButton.setText("Start");
                    etatTimer = "Start";
                    timerStopButton.setVisibility(View.GONE);
                    if (!endSet) {
                        timerE.start();
                    }
                    else {
                        timerRest.start();
                    }
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
                    restTimeBtSets = 1000*(restTimeBtSetNPmin.getValue() * 60 + restTimeBtSetNPsec.getValue());
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
                timerRest.cancel();
                timerStopButton.setVisibility(View.GONE);
                etatTimer = "Stop";
                startButton.setText("Start");
                progressBar.setProgress(0);
                progressBar2.setProgress(0);
                repTextView.setText("  ");
                setTextView.setText("  ");
                chronoTextView.setText(0+"");
                rest = false;
                endSet = false;
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
                else if (repRealises == nbrRep){
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
}
