package com.arrkays.poutre;

import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static android.os.SystemClock.sleep;

public class Prehension {
    //double maxPull;
    //int maxHang = 0;
    //int pourcentage = 0;
    String TAGPB = "PB_pull";
    String nom;
    long id;
    //String description;
    //Date dateRecord;
    Pull allTimeRecordPull = null;
    boolean pullPBBrocken = false;
    Pull allTimeRecordPourc = null;

    boolean pourcPBBrocken = false;
    boolean todayBrocken = false;

    List<Pull> historic;

    /**
     * for existing hold
     * @param id
     * @param nom
     * @param historique
     */
    Prehension(long id, String nom, List<Pull> historique){
        this.nom = nom;
        historic = historique;
        this.id = id;
        setUpAllTimeRecord();
    }

    /**
     * for new hold
     * @param nom
     */
    Prehension(String nom){
        this.nom = nom;
        historic = new ArrayList<Pull>();
        setUpAllTimeRecord();
    }

    public void setUpAllTimeRecord() {
        double maxPull = 0;
        int maxPourc = 0;
        for(Pull p : historic){
            if(p.pull >= maxPull)
                allTimeRecordPull=p;
            if(p.pourcentage >= maxPourc)
                allTimeRecordPourc=p;
        }
    }

    boolean isPullPBBrocken(){
        return pullPBBrocken;
    }

    boolean isPourcPBBrocken(){
        return pourcPBBrocken;
    }

    /**
     * @return null si pas de reccord
     */
    public Pull getAllTimeRecordPull(){
        return allTimeRecordPull;
    }

    /**
     * @return null si pas de reccord
     */
    public Pull getAllTimeRecordPourc(){
        return allTimeRecordPourc;
    }

    /**
     * @return null if no pull today
     */
    public Pull getToDayPull(){
        Pull res = null;
        if(historic.size() != 0)
        {
        if(getDate().equals(getDate(historic.get(historic.size()-1).date))) {//si il y a deja une entrer aujourd hui
            res = historic.get(historic.size() - 1); //on prend le dernier
        }
        else
            res = null;
        }

        return res;
    }

    /**
     *  @return null if no last day
     */
    public Pull getLastDay(){
        Pull res = null;
        if(historic.size() != 0){
            if(getToDayPull() == null)
                res = historic.get(historic.size() - 1);
            else if(historic.size()>1)
                res = historic.get(historic.size() - 2);
        }

        return res;
    }

    public void setTodayPull(double todayPull) {
        Pull p;
        if(getToDayPull() != null) {//si il y a deja une entrer aujourd hui
            p = historic.get(historic.size()-1);
            if(todayPull > p.pull) {//si le record de la session est batue*
                p.pull=todayPull;
                p.pourcentage=Res.getPour(todayPull);
                updatePullDb(p);//on update la base de donnée
                todayBrocken = true;
            }
            else{
                todayBrocken = false;
            }
        }
        else{//si premier entrer aujourd'hui
            todayBrocken = true;
            p = new Pull(todayPull);
            historic.add(p);
            Res.ma.dataBase.addPull(p,Res.currentPrehension);// insert in DataBase   /!\   si ralenticement metre dans un thread
        }
        updatePbs(p);
    }

    public boolean ispullTodayBrocken() {
        return todayBrocken;
    }

    private void updatePbs(Pull p){
        if(allTimeRecordPull == null)
            allTimeRecordPull = p;
        else
        {
            if(allTimeRecordPull.pull >= p.pull)
                allTimeRecordPull = p;
        }

        if(allTimeRecordPourc == null)
            allTimeRecordPourc = p;
        else
        {
            if(allTimeRecordPourc.pull >= p.pull)
                allTimeRecordPourc = p;
        }
        pullPBBrocken = p == allTimeRecordPull;
        pourcPBBrocken = p == allTimeRecordPourc;
    }

    //Update DB TOUDAY Drecord
    Wiat2sUntilUpdateDB wait2s;
    /**
     * update new record todaypull dans DB si il n'est pas batu d'ici 2s
     * @param toDayPull
     */
    private void updatePullDb(Pull toDayPull) {

        if(wait2s != null){
            if(wait2s.isRuning)//si deja lancer
            {
                wait2s.interrupt();
                wait2s = new Wiat2sUntilUpdateDB(toDayPull);
                wait2s.start();
            }
            else{
                wait2s = new Wiat2sUntilUpdateDB(toDayPull);
                wait2s.start();
            }
        }
        else {//si null
            wait2s = new Wiat2sUntilUpdateDB(toDayPull);
            wait2s.start();
        }
    }


    /**
     * @param time timestamp
     * @return date au format: DD-MM-AAAA
     */
    private String getDate(long time){
        Date date = new Date();
        date.setTime(time);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        //Add one to month {0 - 11}
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return day+"-"+month+"-"+year;
    }

    /**
     *
     * @return dd-mm-yyyy of today
     */
    private String getDate(){
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        //Add one to month {0 - 11}
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return day+"-"+month+"-"+year;

    }


    @Override
    public String toString() {
        return nom;
    }
}
class Pull{

    double pull;
    int pourcentage;
    long date;
    long id;

    Pull(double pull){
        date = Calendar.getInstance().getTime().getTime();
        this.pull = pull;
        pourcentage = Res.getPour(pull);
    }

    //id | hold_id | date | pull | pourcentage
    public Pull(long id, long date, double pull, int pourcentage){
        this.id = id;
        this.date = date;
        this.pull = pull;
        this.pourcentage = pourcentage;
    }
}

class Wiat2sUntilUpdateDB extends Thread{

    boolean isRuning = false;
    Pull pull;

    public Wiat2sUntilUpdateDB(Pull p){
        pull = p;
    }

    @Override
    public void run() {
        isRuning = true;
        //super.run();
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(isRuning)
            Res.ma.dataBase.updatePull(pull);
        isRuning = false;
    }

    @Override
    public void interrupt() {
        isRuning = false;
        super.interrupt();
    }
}