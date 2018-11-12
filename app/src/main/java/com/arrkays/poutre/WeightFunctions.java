package com.arrkays.poutre;

import java.security.cert.CollectionCertStoreParameters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class WeightFunctions extends Weight{

    double bodyWeight = 0;
    boolean bodyWeightAsked = false;
    double minWeight = 15.0;
    long timeBetweenMeasures = 2000;
    long timeMax = 10000;
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
            else if (weightMeasures.size() >= 2 && (Calendar.getInstance().getTimeInMillis() - timeFirstMeasure > timeBetweenMeasures)){
                if (Calendar.getInstance().getTimeInMillis() - timeFirstMeasure >= timeMax){
                    // Too long, there should be an error
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

        if ( (max-min)/min > 0.05) {
            for (int i = 0; i < weightMeasures.size(); i++) {
                weightSum += weightMeasures.get(i);
            }
            return weightSum/weightMeasures.size();
        }
        else {
            return 0.0;
        }

    }


}
