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
        public void onChange(double weight, boolean[] evo) {
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
            stopBodyWeightMeasurement();
        }
        else if (weightMeasures.size() >= 2) {
            double weightSum = 0;
            double min = Collections.min(weightMeasures);
            double max = Collections.max(weightMeasures);
            if ((max - min) / min < 0.05) {
                //ma.myHandler.sendMessage(Res.msg(Res.MESURE_POID, 50)); //
                double diff = (double) Calendar.getInstance().getTimeInMillis() - (double) timeFirstMeasure;
                double prog = (100.0 * diff / timeBetweenMeasures);
                if (prog > 100.0) {prog = 100.0;}
                ma.loaderMonPoids.setProgress((int) prog); //

                if (diff > timeBetweenMeasures) { // écart max entre le min et le max de 5%
                    for (int i = 0; i < weightMeasures.size(); i++) {
                        weightSum += weightMeasures.get(i);
                    }
                    bodyWeight = Math.floor((double)weightSum / (double)weightMeasures.size() * 10.0) / 10.0; // fait la moyenne et garde un seul chiffre après la virgule
                    ma.monPoid.setText(bodyWeight + " kg"); // affichage dans le textView
                    weightMeasures.clear();
                    stopBodyWeightMeasurement();
                }
            }
            else {
                timeFirstMeasure = Calendar.getInstance().getTimeInMillis();
                weightMeasures.clear();
            }
        }
    }


    public void startBodyWeightMeasurement(){
        bodyWeightAsked = true;
        Res.weightNotif.addListener(weightListenerBody);
        Log.d(TAG, "pop up visible" + ma.popUpMesurepoids.getVisibility());
        ma.popUpMesurepoids.setVisibility(View.VISIBLE);
    }
    public void stopBodyWeightMeasurement(){
        bodyWeightAsked = false;
        Res.weightNotif.removeListener(weightListenerBody);
        Log.d(TAG, "pop up gone");
        ma.popUpMesurepoids.setVisibility(View.GONE);
        ma.mask.setVisibility(View.GONE);
    }

    //********************************************************************************************************************
    //******************************************************eVOLUTION du poid*************************************************
    //********************************************************************************************************************
    static String TAG2 = "comportement";
    WeightListener comportement = new WeightListener() {
        @Override
        public void onChange(double w, boolean[] evo) {
            Log.d(TAG2, "\ncomportement : \n"+showComportement(evo));
        }
    };



    void starComportement(){
        Res.weightNotif.addListener(comportement);
    }
    void stopComportement(){
        Res.weightNotif.removeListener(comportement);
    }

    /**
     * tableau dans lequel sont stocker les n derniers valeurs
     * la valeur la plus récente étant en position 0 la plus vielle en n
     * /!\la taille du tableau va définire l'intervale des mesure du coeff
     */
    static double[] mesures = buildTab(0,4);

    /**
     *
     * @param w
     * @return un tableau de bool indiquant quell comportement on été relever:
     * [0] = evolution a peu près stable (pas de changement)
     * [1] = evolution très stable (pas de changement)
     * [2] = poid augmente
     * [3] = suspension : Le poid augmente violement
     * [4] = poid baisse
     * [5] = dé-suspenssion : Le poid baisse violement
     */
    static boolean[] comportement(double w){
        addWeight(mesures,w);

        //diff = nouvelle mesure - ancienne mesure
        double diff = mesures[0] - mesures[mesures.length-1];
        Log.d(TAG2,"diff: "+diff);
        boolean resultat[] = {false,false,false,false,false,false};

        if(diff < 3 && diff > -3) //stable
            resultat[0] = true;
        if(diff < 1 && diff > -1) //très stable
            resultat[1] = true;
        if(diff >= 3) //augmente
            resultat[2] = true;
        if(diff > 15) //augmente très vite
            resultat[3] = true;
        if(diff <= -3) //baisse
            resultat[4] = true;
        if(diff < -15)//baisse très vite
            resultat[5] = true;
        return resultat;
    }

    static void addWeight(double tab[], double weight){
        for(int i = tab.length-1; i > 0; i--){
            tab[i]=tab[i-1];
        }
        tab[0] = weight;
    }
    static double[] buildTab(int initValue, int length){
        double tab[] = new double[length];
        for(int i = 0; i<tab.length; i++)
            tab[i]=initValue;
        return tab;
    }

    static String showTab(double[] tab){
        String rez = "";
        for(double d : tab)
            rez += "["+d+"]";
        return rez;
    }

    static String showComportement(boolean tab[]){
        String s = "";

        s += (tab[0])?"true : => poid stable":"false : => poid stable";
        s += "\n";
        s += (tab[1])?"true : ==> poid très stable":"false : ==> poid très stable";
        s += "\n";
        s += (tab[2])?"true : ↗ poid augmente":"false : ↗ poid augmente";
        s += "\n";
        s += (tab[3])?"true : ↑ poid augmente vite":"false : ↑ poid augmente vite";
        s += "\n";
        s += (tab[4])?"true : ↘ poid baisse":"false : ↘ poid baisse";
        s += "\n";
        s += (tab[5])?"true : ↓ poid baisse vitte":"false : ↓ poid baisse vitte";

        return s;
    }
}
