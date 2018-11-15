package com.arrkays.poutre;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import static java.lang.Integer.valueOf;

public class SuspensionsActivity extends AppCompatActivity {

    EditText suspensionsTimeText = null;
    EditText suspensionsRestTimeText = null;
    Button startButton = null;
    ProgressBar progressBar = null;
    CountDownTimer timer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspensions);

        suspensionsTimeText = (EditText) findViewById(R.id.suspensionTimeText);
        suspensionsRestTimeText = (EditText) findViewById(R.id.suspensionRestTimeText);
        startButton = (Button) findViewById(R.id.startButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setProgress(0);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("timer", "go");
                final double duration = valueOf(suspensionsTimeText.getText().toString()) * 1000;
                timer = new CountDownTimer((long)duration, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progressBar.setProgress( (int)((duration - (double)(millisUntilFinished-100)) / duration * 100));
                    }
                    @Override
                    public void onFinish() {
                    }
                };
                timer.start();

            }
        });

    }
}
