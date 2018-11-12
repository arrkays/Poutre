package com.arrkays.poutre;

import java.util.ArrayList;
import java.util.List;

public class Res {
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
}
