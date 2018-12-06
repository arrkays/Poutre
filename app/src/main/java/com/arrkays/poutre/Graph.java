package com.arrkays.poutre;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v13.view.DragStartHelper;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

public class Graph extends View {

    String TAG = "debug-bluetooth";
    int marge = 5 ;
    public double poid = 60;
    public double pull = 0;
    public double maxPull = 0;
    int width;
    int height;
    double ratio = 0.9;//ratio beetween sub weight pull and fuul graph
    double ratio2 = 0.5;//ratio beetween half weight pull and fuul graph
    Rect rec = new Rect();
    private Paint p = new Paint();
    private Paint p2 = new Paint();
    public Handler handler = null;

    private int rouge = Color.RED;
    private int orange = Color.rgb(255,140 ,0);
    private int vert = Color.rgb(0,190 ,0);

    public Graph(Context c, AttributeSet attrs) {
        super(c, attrs);
        p2.setColor(Color.rgb(5,5,5));
        p2.setStyle(Paint.Style.FILL);

    }

    public void setPull(double p){
        pull = p;
        if(pull>maxPull)
            maxPull=pull;
        super.invalidate();
    }

    private void getSize(){
        width = this.getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        getSize();
        p.setColor(vert);
        p.setStyle(Paint.Style.FILL);
        if(Res.POID==0)
            poid =60;
        else
            poid = Res.POID;

        //bare vert
        int top = (int)(((double)height*ratio) * pull/poid);
        rec.bottom = height;
        rec.left = 0;
        rec.right = width;
        rec.top = height-top;
        c.drawRect(rec, p);

        //bar orange
        if(ratio2 <= pull/poid){
            rec.set(0,height-top, width,height-(int)(double)(((height*ratio))*ratio2));
            p.setColor(orange);
            c.drawRect(rec, p);
        }

        //bar rouge si pull depace poid
        if(1 <= pull/poid){
            rec.set(0,height-top, width, (int)(height*(1-ratio)));
            p.setColor(rouge);
            c.drawRect(rec, p);
        }

        //ligne tout les 10kg
        double unKiloEnPixel =  (((double)height * ratio) /  poid);
        //p.setColor(Color.BLACK);
        for(double i = 0; i < poid+11 ; i+=10){
            c.drawRect(0,(float)(height-unKiloEnPixel*i)-1,width,(float)(height-unKiloEnPixel*i),p2);
        }


        //Bar Record
        p.setColor(color(R.color.record));
        if(Res.currentPrehension.getAllTimeRecordPull() != null)
            c.drawRect(barePull(Res.currentPrehension.getAllTimeRecordPull().pull,4), p);

        //Bar last day
        p.setColor(color(R.color.last_session));
        if(Res.currentPrehension.getLastDay() != null)
            c.drawRect(barePull(Res.currentPrehension.getLastDay().pull,4), p);

        //bare toDay
        p.setColor(color(R.color.today_record));
        if(Res.currentPrehension.getToDayPull() != null)
        c.drawRect(barePull(Res.currentPrehension.getToDayPull().pull, 4), p);

        //bare max
        int maxTopBar = 50;// bar a x dp avant le top max
        int topBar = (int)(((double)height*ratio) * maxPull/poid);
        if(topBar>height-maxTopBar)
            topBar=height - maxTopBar;
        //choix couleur bar max en fonction du max
        p.setColor(Color.BLACK);

        rec.set(0,height-topBar-4,width,height-topBar);
        c.drawRect(rec, p);

        //text
        p.setTextSize(30);
        if(Res.POID == 0)
            c.drawText(maxPull+" Kg",marge,height-topBar-14,p);
        else
            c.drawText(maxPull+" Kg ("+Res.getPour(pull)+"%)",marge,height-topBar-14,p);

    }

    //RAZ de max pull qd on touche la bar
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        maxPull = 0;
        super.invalidate();
        return super.onTouchEvent(event);
    }

    private int color(int R){
        return ResourcesCompat.getColor(getResources(), R, null);
    }

    private Rect barePull(double kg, int taille){
        int top = (int)(((double)height*ratio) * kg/poid);
        return new Rect(0,height-top,width,height-(top+taille));
    }

    private Rect barePourc(double kg, int taille){
        int top = (int)(((double)height*ratio) * kg/poid);
        return new Rect(0,height-top,width,height-(top+taille));
    }
}
