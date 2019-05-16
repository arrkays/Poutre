package com.arrkays.poutre;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

public class GraphSimple extends View {
    String TAG = "graph";
    int marge = 5 ;
    public double poid = 20;
    public double pull = 0;
    int width;
    int height;
    double ratio = 0.9;//ratio beetween sub weight pull and fuul graph
    double ratio2 = 0.5;//ratio beetween half weight pull and fuul graph
    Rect rec = new Rect();
    protected Paint p = new Paint();
    protected Paint p2 = new Paint();
    //public Handler handler = null;

    protected int rouge = Color.RED;
    protected int orange = Color.rgb(255,140 ,0);
    protected int vert = Color.rgb(0,190 ,0);

    public GraphSimple(Context c, AttributeSet attrs) {
        super(c, attrs);
        p2.setColor(Color.rgb(5,5,5));
        p2.setStyle(Paint.Style.FILL);

    }

    public GraphSimple(Context context) {
        super(context);
    }

    public void setPull(double p){
        pull = p;

        super.invalidate();
    }

    protected void updateSize(){
        width = getWidth();
        height = getHeight();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        updateSize();
        p.setColor(vert);
        p.setStyle(Paint.Style.FILL);

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

        //ligne tout les 2kg
        int ecare = 5;
        double unKiloEnPixel =  (((double)height * ratio) /  poid);
        //p.setColor(Color.BLACK);
        for(double i = 0; i < poid+11 ; i+=ecare){
            c.drawRect(10,(float)(height-unKiloEnPixel*i)-1,width-10,(float)(height-unKiloEnPixel*i),p2);
        }



        //choix couleur bar max en fonction du max
        p.setColor(Color.BLACK);

        rec.set(0,height-4,width,height);
        c.drawRect(rec, p);

        //text
        p.setTextSize(40);
        c.drawText(pull+"",marge,height-14,p);

    }


    protected int color(int R){
        return ResourcesCompat.getColor(getResources(), R, null);
    }

    protected Rect barePull(double kg, int taille){
        int top = (int)(((double)height*ratio) * kg/poid);
        return new Rect(0,height-top,width,height-top+taille);
    }

    protected Rect barePourc(double kg, int taille){
        int top = (int)(((double)height*ratio) * kg/poid);
        return new Rect(0,height-top,width,height-top+taille);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}
