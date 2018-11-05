package com.arrkays.poutre;

import java.util.Date;

public class Prehension {
    double maxPull;
    int maxHang;
    String nom;
    String description;
    Date dateRecord;

    Prehension(String nom){
        maxHang=0;
        maxPull=0;
        description="";
    }

    Prehension(String nom, String description){
        maxHang=0;
        maxPull=0;
        this.description=description;
    }

}
