package com.arrkays.poutre;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WeightNotif {

    private List<WeightListener> listeners = new ArrayList<WeightListener>();//liste des methode qui vont etre notifier
    private Iterator<WeightListener> it = null; //pour eviter exeption si on enlève un objet (removeListener )de la liste pendant qu'on la parcour (updateWeight)

    //les listenner doivent etre unique
    public void addListener(WeightListener toAdd) {
        boolean exist = false;
        for(WeightListener wl : listeners){
            if(wl == toAdd)
                exist =true;
        }
        if(!exist)
            listeners.add(toAdd);
    }

    public void removeListener(WeightListener toRemove){
        if(it == null)
            listeners.remove(toRemove);
        else
            it.remove();
    }

    public void updateWeight(double w, boolean[] evolutionPoid) {
        try {
            // Notify everybody that may be interested.
            for (it = listeners.iterator(); it.hasNext(); ) {
                WeightListener hl = it.next();
                hl.onChange(w, evolutionPoid);
            }
            it = null;
        }
        catch (NullPointerException e) {
            Log.d(Res.TAG, "weightListener cassé : problème remove listener");
        }
    }
}

interface WeightListener {
    void onChange(double w, boolean[] evolutionPoid);
}