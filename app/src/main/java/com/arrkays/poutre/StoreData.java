package com.arrkays.poutre;

import android.content.Context;
import android.content.SharedPreferences;

public class StoreData {

    SharedPreferences store;
    SharedPreferences.Editor editor;
    MainActivity ma;

    StoreData(MainActivity context){
        ma = context;
        store = ma.getPreferences(Context.MODE_PRIVATE);
        editor = store.edit();
        updateCoef();
    }

    public void setPoid(double p){
        editor.putFloat("poid",(float)p);
        editor.commit();
    }

    public double getPoid(){
        return Res.round(store.getFloat("poid",0),1);
    }

    public void setCurrentPrehenssionIndex(int i){
        editor.putInt("currentPrehenssion",i);
        editor.commit();
    }

    public int getCurrentPrehensionIndex() {
        return store.getInt("currentPrehenssion",0);
    }

    ////////////////////////////////////////// PREFERENCES //////////////////////////////////////////
    ///// Thème //////
    public void setCurrentTheme(String theme) {
        editor.putString("currentTheme", theme);
        editor.commit();
    }
    public String getCurrentTheme() {return store.getString("currentTheme", "Light");}
    ///// Vibrations /////

    //coef
    public double[] updateCoef() {
        Res.coef[0] = Res.round(store.getFloat("coefBG", 1f), 2);
        Res.coef[1] = Res.round(store.getFloat("coefHG", 1f), 2);
        Res.coef[2] = Res.round(store.getFloat("coefHD", 1f), 2);
        Res.coef[3] = Res.round(store.getFloat("coefBD", 1f), 2);

        return Res.coef;
    }

    public void setCoefBG(double n){
        editor.putFloat("coefBG",(float)n);
        Res.store.editor.commit();
        Res.coef[0] = n;
    }
    public void setCoefHG(double n){
        editor.putFloat("coefHG",(float)n);
        Res.store.editor.commit();
        Res.coef[1] = n;
    }
    public void setCoefHD(double n){
        editor.putFloat("coefHD",(float)n);
        Res.store.editor.commit();
        Res.coef[2] = n;
    }
    public void setCoefBD(double n){
        editor.putFloat("coefBD",(float)n);
        Res.store.editor.commit();
        Res.coef[3] = n;
    }
}
