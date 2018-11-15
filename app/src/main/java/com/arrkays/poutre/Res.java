package com.arrkays.poutre;

import android.os.Message;

import java.util.ArrayList;
import java.util.List;

public class Res {

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
    public static int POID=0;

    public static ArrayList<Prehension> prehensions = new ArrayList<Prehension>();

    public static Prehension currentPrehension;
    public static double currentWeight;


    /**
     * ajouter ou suprimer listener pour etre notifier des changement de poid
     */
    public static WeightNotif weightNotif= new WeightNotif();

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

    public static List<String> getListPrehenssionString(){
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
}
