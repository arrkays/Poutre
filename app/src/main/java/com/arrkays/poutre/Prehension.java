package com.arrkays.poutre;

import java.util.Calendar;
import java.util.Date;

public class Prehension {
    double maxPull;
    int maxHang = 0;
    int pourcentage = 0;
    String nom;
    String description;

    Date dateRecord;


    Prehension(String nom){
        this.nom = nom;
        maxHang=0;
        maxPull=0;
        description="";
    }

    Prehension(String nom, String description){
        this.nom = nom;
        maxHang=0;
        maxPull=0;
        this.description=description;
    }

    public void setRecordPull(double pull){
        maxPull = pull;
        pourcentage = Res.getPour(pull);
        dateRecord =  Calendar.getInstance().getTime();
    }
}
