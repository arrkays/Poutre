package com.arrkays.poutre;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WeightNotif {

    private List<WeightListener> listeners = new ArrayList<WeightListener>();//liste des methode qui vont etre notifier
    private Iterator<WeightListener> it = null; //pour eviter exeption si on enlève un objet (removeListener )de la liste pendant qu'on la parcour (updateWeight)

    private Iterator<GestureListener> itGesture = null;

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
    /**
     *
     * @param w poid en kilo
     * @param evolutionPoid
     *  un tableau de bool indiquant quell comportement on été relever:<br>
     * [0] = evolution a peu près stable (pas de changement)<br>
     * [1] = evolution très stable (pas de changement)<br>
     * [2] = poid augmente<br>
     * [3] = suspension : Le poid augmente violement<br>
     * [4] = poid baisse<br>
     * [5] = dé-suspenssion : Le poid baisse violement<br>
     */
    void onChange(double w, boolean[] evolutionPoid);
}

interface GestureListener{
    void onGesture(int gesture);
}