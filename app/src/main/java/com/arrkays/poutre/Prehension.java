package com.arrkays.poutre;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Prehension {
    double maxPull;
    int maxHang = 0;
    int pourcentage = 0;
    String nom;
    String description;
    Date dateRecord;
    PullDate toDayPull;

    List<PullDate> historique;

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

    public void setTodayPull(double todayPull) {
        if(getDate().equals(getDate(historique.get(historique.size()-1).date))) {//si il y a deja une entrer aujourd hui
            PullDate lastIndex = historique.get(historique.size()-1);
            if(todayPull > lastIndex.pull) {//si le record de la session est batue*
                lastIndex.pull=todayPull;
                updateDBHistory(lastIndex);//on update la base de donnée
            }
        }
        else{//si premier entrer aujourd'hui
            PullDate today = new PullDate(todayPull);
            historique.add(today);
            updateDBHistory(today);
        }
    }

    private void updateDBHistory(PullDate lastIndex) {
    }

    public void setRecordPull(double pull){
        maxPull = pull;
        pourcentage = Res.getPour(pull);
        dateRecord =  Calendar.getInstance().getTime();
    }

    /**
     *
     * @param time timestamp
     * @return date au format: DD-MM-AAAA
     */
    private String getDate(long time){
        return new Time(time).getDay()+"-"+new Time(time).getMonth()+"-"+new Time(time).getYear();
    }
    private String getDate(){

        long time =  Calendar.getInstance().getTime().getTime();
        return new Time(time).getDay()+"-"+new Time(time).getMonth()+"-"+new Time(time).getYear();
    }

    @Override
    public String toString() {
        return nom;
    }

    class PullDate{

        PullDate(double pull){
            date = Calendar.getInstance().getTime().getTime();
            this.pull = pull;
        }

        PullDate(long pull, long date){
            this.pull = pull;
            this.date = date;
        }
        double pull;
        long date;
    }
}

