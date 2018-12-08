package com.arrkays.poutre;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DB extends SQLiteOpenHelper {
    static String LOG_db = "db_hold";
    static final String name = "data";
    static final int version = 1;

    public DB(Context context) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_db,"onCreate");

        //Hold --> id | name
        db.execSQL("CREATE TABLE Hold ("+
                "id INTEGER PRIMARY KEY,"+
                "name TEXT"+
                ")"
        );
        //         0    1         2      3      4
        //Pull --> id | hold_id | date | pull | pourcentage
        db.execSQL("CREATE TABLE Pull ("+
                "id INTEGER PRIMARY KEY,"+
                "hold_id INTEGER,"+
                "date INTEGER,"+
                "pull REAL,"+
                "pourcentage INTEGER"+
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<Prehension> getHold(){

        ArrayList<Prehension> holdList = new ArrayList<Prehension>();

        Cursor cHold = getReadableDatabase().rawQuery("Select * FROM Hold", null);
        while(cHold.moveToNext()){//pour chagu prise
            long holdId = cHold.getLong(0);
            String holdName = cHold.getString(1);
            ArrayList<Pull> holdHistorique = new ArrayList<Pull>();

            Cursor cPull = getReadableDatabase().rawQuery("Select * FROM Pull WHERE hold_id='"+holdId+"'", null);
            while (cPull.moveToNext()){//pour chaque pull de hold
                long pullId = cPull.getLong(0);
                long pullDate = cPull.getLong(2);
                double pull = cPull.getDouble(3);
                int pourc = cPull.getInt(4);
                Pull p = new Pull(pullId, pullDate, pull, pourc);
                holdHistorique.add(p);
            }

            holdList.add(new Prehension(holdId, holdName, holdHistorique));
        }

        return holdList;
    }

    /**
     * update pourcentage et pull
     * @param p
     */
    public void removePull(Pull p){
        getWritableDatabase().delete("Pull", "id=?",new String[]{p.id+""});
    }

    public void updatePull(Pull p){
        Log.d(LOG_db, "update pull DB : "+p.pull);
        ContentValues v = new ContentValues();
        v.put("pull", p.pull);
        v.put("pourcentage", p.pourcentage);
        getWritableDatabase().update("Pull",v, "id=?",new String[]{p.id+""});
    }

    public void upateHoldName(Prehension p){
        ContentValues v = new ContentValues();
        v.put("name", p.nom);
        getWritableDatabase().update("Hold",v, "id=?",new String[]{p.id+""});
    }

    public void removeHold(Prehension p ){
        //remove hold
        getWritableDatabase().delete("Hold", "id=?",new String[]{p.id+""});

        //remove assiossiate Pull
        getWritableDatabase().delete("Pull", "hold_id=?",new String[]{p.id+""});
    }

    /**
     * //insert new pull and update pull id
     * @param pull
     * @param hold
     */
    public void addPull(Pull pull, Prehension hold){
        //insert row
        getWritableDatabase().execSQL("insert into Pull(hold_id, date, pull, pourcentage) values('"+hold.id+"', '"+pull.date+"', '"+pull.pull+"', '"+pull.pourcentage+"') ");

        //update id in object pull
        Cursor c = getReadableDatabase().rawQuery("Select last_insert_rowid()", null);
        c.moveToNext();
        pull.id=c.getInt(0);
    }

    /**
     * insert hold p in base and set id of p
     * @param p
     */
    public void addHold(Prehension p){
        //insert row
        getWritableDatabase().execSQL("insert into Hold(name) values('"+p.nom+"') ");

        //set id
        Cursor c = getReadableDatabase().rawQuery("Select last_insert_rowid()", null);
        c.moveToNext();
        p.id=c.getInt(0);
    }

}
