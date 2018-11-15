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

import static java.lang.Integer.valueOf;

public class SuspensionsActivity extends AppCompatActivity {

    EditText suspensionsTimeText = null;
    EditText suspensionsRestTimeText = null;
    Button startButton = null;
    Button optionsButton = null;
    ProgressBar progressBar = null;
    ProgressBar progressBar2 = null;
    CountDownTimer timer = null;
    CountDownTimer restTimer = null;
    ConstraintLayout optionsLayout = null;
    NumberPicker suspensionsTimeNPsec = null;
    NumberPicker suspensionsTimeNPmin = null;
    NumberPicker restTimeNPsec = null;
    NumberPicker restTimeNPmin = null;

    long suspensionTime = 10;
    long restTime = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspensions);

        suspensionsTimeText = (EditText) findViewById(R.id.suspensionTimeText);
        suspensionsRestTimeText = (EditText) findViewById(R.id.suspensionRestTimeText);
        startButton = (Button) findViewById(R.id.startButton);
        optionsButton = (Button) findViewById(R.id.optionsButton);
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

        final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);



        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setProgress(0);
                progressBar2.setProgress(0);
                Log.i("timer", "go");
                //final double duration =  suspensionTime * 1000;
                timer = new CountDownTimer(suspensionTime, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progressBar.setProgress( (int)((suspensionTime - (double)(millisUntilFinished-100)) / suspensionTime * 100));
                    }
                    @Override
                    public void onFinish() {
                        v.vibrate(400);
                        restTimer = new CountDownTimer(restTime, 100){
                            @Override
                            public void onTick(long millisUntilFinishedRest) {
                                progressBar2.setProgress( (int)((restTime - (double)(millisUntilFinishedRest-100)) / restTime * 100));
                            }
                            @Override
                            public void onFinish(){
                                v.vibrate(400);
                            }
                        };
                        restTimer.start();
                    }
                };
                timer.start();

            }
        });
        optionsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (optionsLayout.getVisibility() == View.VISIBLE){
                    optionsLayout.setVisibility(View.GONE);
                    suspensionTime = 1000*(suspensionsTimeNPmin.getValue() * 60 + suspensionsTimeNPsec.getValue());
                    restTime = 1000*(restTimeNPmin.getValue() * 60 + restTimeNPsec.getValue());
                }
                else {
                    optionsLayout.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });
        suspensionsTimeNPsec.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected number from picker
            }
        });


    }
}
