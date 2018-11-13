package com.arrkays.poutre;

import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class WeightFunctions extends Weight{
    String TAG = "debug-bluetooth";
    double bodyWeight = 0;
    boolean bodyWeightAsked = false;
    double minWeight = 15.0;
    long timeBetweenMeasures = 2000;
    long timeMax = 15000;
    List<Double> weightMeasures = new ArrayList<Double>();
    long timeFirstMeasure = 0;

    MainActivity ma;
    public WeightFunctions(MainActivity a){
        ma = a;
    }

    @Override
    public void onWeightChange(double weight) {
        if (bodyWeightAsked && (weight > minWeight)){
            weightMeasures.add(weight);
            if (weightMeasures.size() == 1){
                timeFirstMeasure = Calendar.getInstance().getTimeInMillis();
            }
            else if (weightMeasures.size() >= 2 && (Calendar.getInstance().getTimeInMillis() - timeFirstMeasure > timeBetweenMeasures)){ // Si il y au moins 2 valeurs et assez de temps entre les 2
                if (Calendar.getInstance().getTimeInMillis() - timeFirstMeasure >= timeMax){
                    bodyWeightAsked = false;
                    weightMeasures.clear();
                    Log.i(TAG, "trop long, mesure poids abandonnée");
                    // Too long, there must be an error
                }
                else {
                    bodyWeightMeasure();
                }
            }
        }
    }

    public double bodyWeightMeasure(){
        double weightSum = 0;
        double min = Collections.min(weightMeasures);
        double max = Collections.max(weightMeasures);
        if (weightMeasures.size() > 5){ // pour ne garder que les 5 dernières mesures
            weightMeasures.subList(0, weightMeasures.size()-5);
        }
        //Log.i(TAG, String.valueOf((max-min)/min) );
        if ( (max-min)/min < 0.05 ) { // écart max entre le min et le max de 5%
            for (int i = 0; i < weightMeasures.size(); i++) {
                weightSum += weightMeasures.get(i);
            }
            bodyWeight = Math.floor(weightSum/weightMeasures.size() * 10) / 10; // fait la moyenne et garde un seul chiffre après la virgule
            bodyWeightAsked = false;
            Log.i(TAG, "body weight measured");
            ma.monPoid.setText(String.valueOf(bodyWeight)+ " kg"); // affichage dans le textView
            weightMeasures.clear();
            return bodyWeight;
        }
        else {
            return 0.0;
        }

    }


}
