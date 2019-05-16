package com.arrkays.poutre;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

public class Res {

    public static MainActivity ma;

    public static String TAG = "debug-bluetooth";
    public static String TAG_gesture = "debug-gesture";

    /**
     * modifie la mesuer du poids de l'utilisateur et sauvegarde la mesure
      */
    public static final int MESURE_POIDS = 3;

    /**
     * Actualise l'avancement de la mesure dans le pop up
     */
    public static final int POPUP_STATUT_MESURE_POIDS = 4;

    /**
     * Affiche le poids dans le pop up pendant la mesure du poids
     */
    public static final int POPUP_MESURE_POIDS = 5;

    /**
     * data from  bluetooth device
     */
    public static int BT_DATA=1;

    /**
     * update le statu du bluetooth: connecter ou non
     */
    public static int BT_STATUS_UPDATE = 2;

    // TAG pour lactivité de suspension
    /**
     * Pour changer la valeur du textview donnant la max de la dernière suspension
     */
    public static int CHANGE_VALUE_MAX_PULL=6;

    /**
     * valeur de chaque capteur en kg
     * capteur [↙,↖,↗,↘]
     */
    static double capteurs[] = {0,0,0,0};

    /**
     * coef pour ajuster les capteur
     */
    static public double coef[] = {1,1,1,1};

    /**
     * poid de l'utilisateur
     */
    public static double poids=0;

    public static ArrayList<Prehension> prehensions = new ArrayList<Prehension>();

    public static Prehension currentPrehension;
    public static double currentWeight;

    static StoreData store = null;

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

    /**
     * Autoriser ou non les suspensions
     */
    public static boolean vibration = false;


    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    //*********************UTIL**************//*********************UTIL**************//*********************UTIL**************
    /**
     * @return revoi un int qui est le pourcentage du pull par raport au poid
     * renvoi 0 si poid pas mesuré
     * @param pull
     */
    public static int getPour(double pull){
        if(poids == 0)
            return 0;
        else
            return (int)((pull/poids)*100);
    }

    /**
     * @param x nombre a arrondir
     * @param i arrondi a i chiffre après la virgule
     * @return
     */
    public static double round(double x, int i){
        return (double)Math.round(x*Math.pow(10,i)) / Math.pow(10,i);
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
        res = Math.round(res);
        res /= Math.pow(10,i);
        return res;
    }
}


