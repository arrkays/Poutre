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

}
