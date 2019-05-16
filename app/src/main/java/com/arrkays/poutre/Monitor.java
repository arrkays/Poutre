package com.arrkays.poutre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Monitor extends AppCompatActivity {
    public static Monitor monitor;

    Button button_BG_plus;
    Button button_BG_moins;
    Button button_HG_plus;
    Button button_HG_moins;
    Button button_HD_plus;
    Button button_HD_moins;
    Button button_BD_plus;
    Button button_BD_moins;

    TextView bg;
    TextView hg;
    TextView hd;
    TextView bd;
    TextView total;
    EditText coefBG;
    EditText coefHG;
    EditText coefHD;
    EditText coefBD;

    GraphSimple graphBG;
    GraphSimple graphHG;
    GraphSimple graphHD;
    GraphSimple graphBD;

    Button tare;

    double valueBg;
    double valueHg;
    double valueHd;
    double valueBd;

    WeightListener weightListener = new WeightListener() {
        @Override
        public void onChange(double w, boolean[] evo) {
            Message msg = new Message();
            msg.arg1 = 1;
            myHandler.sendMessage(msg);
        }
    };

    public Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 1){
                afficheWeights();
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        monitor = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_cell);

        MainActivity.addMenuEvent(this);

        button_HG_moins = findViewById(R.id.button_HG_moins);
        button_HG_plus = findViewById(R.id.button_HG_plus);
        button_BG_plus = findViewById(R.id.button_BG_plus);
        button_BG_moins = findViewById(R.id.button_BG_moins);
        button_HD_plus = findViewById(R.id.button_HD_plus);
        button_HD_moins = findViewById(R.id.button_HD_moins);
        button_BD_plus = findViewById(R.id.button_BD_plus);
        button_BD_moins = findViewById(R.id.button_BD_moins);

        bg = findViewById(R.id.BG);
        hg = findViewById(R.id.HG);
        hd = findViewById(R.id.HD);
        bd = findViewById(R.id.BD);
        tare = findViewById(R.id.tere);
        coefBG = findViewById(R.id.coefBG);
        coefHG = findViewById(R.id.coefHG);
        coefHD = findViewById(R.id.coefHD);
        coefBD = findViewById(R.id.coefBD);
        graphBG = findViewById(R.id.graphBG);
        graphHG = findViewById(R.id.graphHG);
        graphHD = findViewById(R.id.graphHD);
        graphBD = findViewById(R.id.graphBD);
        total = findViewById(R.id.total);
        TextView titleActivity = findViewById(R.id.titreActivite);

        //titre page
        titleActivity.setText("Monitor");

        //store data
        displayCoef();

        //event

        event();
        startSensorUpdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSensorUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSensorUpdate();
    }


    void startSensorUpdate(){
        Res.weightNotif.addListener(weightListener);
    }

    void stopSensorUpdate(){
        Res.weightNotif.removeListener(weightListener);
    }


    private void afficheWeights(String weights) {
        String data[] = weights.split(" ");

        try {
            double values[] = {Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), Double.parseDouble(data[3])};
            int coef[] = {Integer.parseInt(coefBG.getText().toString()), Integer.parseInt(coefHG.getText().toString()), Integer.parseInt(coefHD.getText().toString()), Integer.parseInt(coefBD.getText().toString())};
            afficheBG(round(values[0] / coef[0], 3));
            afficheHG(round(values[1] / coef[1], 3));
            afficheHD(round(values[2] / coef[2], 3));
            afficheBD(round(values[3] / coef[3], 3));
            total.setText(round(valueBg+valueHg+valueHd+valueBd,1)+"");
        }
        catch (ArrayIndexOutOfBoundsException | NumberFormatException e){

        }
    }

    private void afficheWeights() {
        afficheBG(Res.round(Res.capteurs[0],1));
        afficheHG(Res.round(Res.capteurs[1],1));
        afficheHD(Res.round(Res.capteurs[2],1));
        afficheBD(Res.round(Res.capteurs[3],1));

        total.setText(round(valueBg+valueHg+valueHd+valueBd,1)+"");
    }

    void afficheBG(double w){
        valueBg = w;
        bg.setText(w+"");
        graphBG.setPull(w);
    }

    void afficheHG(double w){
        valueHg = w;
        hg.setText(w+"");
        graphHG.setPull(w);
    }

    void afficheHD(double w){
        valueHd = w;
        hd.setText(w+"");
        graphHD.setPull(w);
    }

    void afficheBD(double w){
        valueBd = w;
        bd.setText(w+"");
        graphBD.setPull(w);
    }
    /**
     * @param x nombre a arrondir
     * @param i arrondi a i chiffre après la virgule
     * @return
     */
    public static double round(double x, int i){
        return (double)Math.round(x*Math.pow(10,i)) / Math.pow(10,i);
    }

    private void displayCoef() {
        coefBG.setText(Res.coef[0]+"");
        coefHG.setText(Res.coef[1]+"");
        coefHD.setText(Res.coef[2]+"");
        coefBD.setText(Res.coef[3]+"");
    }

    void event(){

        //event text
        tare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Res.ma.blutoothManager.sendMsg("z");
            }
        });
        coefBG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Res.store.setCoefBG(getCoefFromString(s.toString()));
            }
        });

        coefHG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Res.store.setCoefHG(getCoefFromString(s.toString()));
            }
        });

        coefHD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Res.store.setCoefHD(getCoefFromString(s.toString()));
            }
        });

        coefBD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Res.store.setCoefBD(getCoefFromString(s.toString()));
            }
        });


        //button

        button_HG_moins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefHG.setText(Res.coef[1]-0.01+"");
            }
        });

        button_HG_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefHG.setText(Res.coef[1]+0.01+"");
            }
        });
        button_HD_moins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefHD.setText(Res.coef[2]-0.01+"");
            }
        });

        button_HD_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefHD.setText(Res.coef[2]+0.01+"");
            }
        });
        button_BG_moins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefBG.setText(Res.coef[0]-0.01+"");
            }
        });

        button_BG_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefBG.setText(Res.coef[0]+0.01+"");
            }
        });
        button_BD_moins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefBD.setText(Res.coef[3]-0.01+"");
            }
        });

        button_BD_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coefBD.setText(Res.coef[3]+0.01+"");
            }
        });

    }
    double getCoefFromString(String s){
        double res = 1;
        try {
            res = Double.parseDouble(s);
        }catch (NumberFormatException e){
            res=1;
        }
        return res;
    }
}
