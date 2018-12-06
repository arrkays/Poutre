package com.arrkays.poutre;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

public class Res {

    public static MainActivity ma;

    public static String TAG = "debug-bluetooth";

    public static final int MESURE_POID = 3;

    /**
     * data from  bluetooth device
     */
    public static int BT_DATA=1;

    /**
     * update le statu du bluetooth: connecter ou non
     */
    public static int BT_STATUS_UPDATE = 2;
    /**
     * poid de l'utilisateur
     */
    public static double POID=0;

    public static ArrayList<Prehension> prehensions = new ArrayList<Prehension>();

    public static Prehension currentPrehension;
    public static double currentWeight;


    /**
     * add hold in list prehensions
     * add hold in dataBase
     * @param ma
     * @param name
     * @return nouvelle prehenssion
     */

    public static Prehension addNewHold(MainActivity ma, String name){
        Prehension newHold = new Prehension(name);
        ma.dataBase.addHold(newHold);
        prehensions.add(newHold);
        return newHold;
    }

    /**
     * ajouter ou suprimer listener pour etre notifier des changement de poid
     */
    public static WeightNotif weightNotif= new WeightNotif();

    //********************************************************SETTING**********************************************************
    //********************************************************SETTING**********************************************************
    //********************************************************SETTING**********************************************************

    /**
     * mode des reccorde:<br>
     * 1 : max pull
     * 2 : max poucentage
     */
    public static final int mode = 1;


    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    /**
     * @return revoi un int qui est le pourcentage du pull par raport au poid
     * renvoi 0 si poid pas mesuré
     * @param pull
     */
    public static int getPour(double pull){
        if(POID == 0)
            return 0;
        else
            return (int)((pull/POID)*100);
    }

    private static List<String> getListPrehenssionString(){
        List<String> list = new ArrayList<>();
        for (Prehension p :prehensions){
            list.add(p.toString());
        }
        return list;
    }

    public static Message msg(int id, Object message){
        Message msg= new Message();
        msg.arg1=id;
        msg.obj=message;
        return msg;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(Activity a){
        InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int dpToPixel(Activity context, int dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return  (int) (dp * scale + 0.5f);
    }

    public static double nbChiffreApresVirgule(double x, int i){
        double res = x * Math.pow(10,i);
        res = (int)res;
        res /= Math.pow(10,i);
        return res;
    }

}


