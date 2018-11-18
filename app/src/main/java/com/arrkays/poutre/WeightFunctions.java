package com.arrkays.poutre;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class WeightFunctions {
    String TAG = "debug-bluetooth";
    double bodyWeight = 0;
    boolean bodyWeightAsked = false;
    double minWeight = 15.0;
    long timeBetweenMeasures = 5000;
    long timeMax = 15000;
    List<Double> weightMeasures = new ArrayList<Double>();
    long timeFirstMeasure = 0;

    MainActivity ma;
    public WeightFunctions(MainActivity a){
        ma = a;
    }

    WeightListener weightListenerBody = new WeightListener() {
        @Override
        public void onChange(double weight) {
            if (weight > minWeight){
                weightMeasures.add(weight);
                bodyWeightMeasure();
            }
        }
    };

    public void bodyWeightMeasure(){
        if (weightMeasures.size() == 1){
            timeFirstMeasure = Calendar.getInstance().getTimeInMillis();
        }
        else if (Calendar.getInstance().getTimeInMillis() - timeFirstMeasure > timeMax){
            //stopBodyWeightMeasurement();
        }
        else if (weightMeasures.size() >= 2) {
            double weightSum = 0;
            double min = Collections.min(weightMeasures);
            double max = Collections.max(weightMeasures);
            if ((max - min) / min < 1.0) {
                //ma.myHandler.sendMessage(Res.msg(Res.MESURE_POID, 50)); //
                double diff = (double) Calendar.getInstance().getTimeInMillis() - (double) timeFirstMeasure;
                double prog = (100.0 * diff / timeBetweenMeasures);
                if (prog > 100.0) {prog = 100.0;}
                ma.loaderMonPoids.setProgress((int) prog); //

                if (diff > timeBetweenMeasures) { // écart max entre le min et le max de 5%
                    for (int i = 0; i < weightMeasures.size(); i++) {
                        weightSum += weightMeasures.get(i);
                    }
                    bodyWeight = Math.floor(weightSum / weightMeasures.size() * 10) / 10; // fait la moyenne et garde un seul chiffre après la virgule
                    ma.monPoid.setText(bodyWeight + " kg"); // affichage dans le textView
                    weightMeasures.clear();
                    //stopBodyWeightMeasurement();
                }
            }
            else {
                    timeFirstMeasure = Calendar.getInstance().getTimeInMillis();
            }
        }
    }


    public void startBodyWeightMeasurement(){
        Res.weightNotif.addListener(weightListenerBody);
        bodyWeightAsked = true;
        ma.popUpMesurepoids.setVisibility(View.VISIBLE);
    }
    public void stopBodyWeightMeasurement(){
        Res.weightNotif.removeListener(weightListenerBody);
        bodyWeightAsked = false;
        ma.popUpMesurepoids.setVisibility(View.GONE);
        ma.mask.setVisibility(View.GONE);
    }





}
