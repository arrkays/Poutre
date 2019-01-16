package com.arrkays.poutre;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

public class GraphMaxPullCirular extends Graph {

    float thickness = 50;
    int colorInf = Color.rgb(0,190 ,0);
    int colorMoy = Color.rgb(255,110,0);
    int colorSup = Color.rgb(255,0,0);
    Bitmap imgTest = BitmapFactory.decodeResource(getResources(),R.drawable.main_gauche_droite);
    public GraphMaxPullCirular(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.updateSize();

        int r;
        if(width < height){//portrait
            r = width/2;
        }
        else{//paysage
            r = width/2;
            if(r > height)
                r = height;
        }

        //thisckness
        thickness = r*0.15f;

        int left = width/2 - r;
        int top = height-r;
        int right = width/2 + r;
        int bottom = height+r;

        //calculation taille du cadrant
        RectF oval = new RectF(left,top,right,bottom);
        RectF ovalMask = new RectF(left+thickness,top+thickness,right-thickness,bottom-thickness);

        //calcule du pull
        float angle =(float)( (180 * ratio) * (pull / poid));

        //dessin du pull
        p.setStyle(Paint.Style.FILL);
            p.setColor(colorInf);
            canvas.drawArc(oval, 180, angle, true, p);

        if(pull >= poid/2 ){
            float starAngle = (float)( (180 * ratio) * 0.5);
            p.setColor(colorMoy);
            canvas.drawArc(oval, 180+starAngle, angle-starAngle, true, p);
        }

        if(pull >= poid){
            float starAngle = (float)( (180 * ratio) );
            p.setColor(colorSup);
            canvas.drawArc(oval, 180+starAngle, angle-starAngle, true, p);
        }

        //dessin bar record
        barDeRccord(canvas, oval);

        //ligne tout les 10kg
        double kiloAngle =  (180 * ratio) /  poid;
        p.setColor(Color.GRAY);
        for(double i = 10; i < poid+11 ; i+=10){
            canvas.drawArc(oval, 180+(float)(i*kiloAngle), 0.4f, true, p);
        }

        //dessiner le maske
        p.setColor(Color.WHITE);
        canvas.drawOval(ovalMask,p);


        //dessiner l'oval autour
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawOval(oval,p);

        //test
        Matrix m = new Matrix();
        canvas.drawBitmap(imgTest, m, p);
    }

    void barDeRccord(Canvas c, RectF o){
        //Bar Record

        if(Res.currentPrehension.getAllTimeRecordPull() != null){
            p.setColor(color(R.color.record));
            float a = (float)(180 + 180 * ratio * (Res.currentPrehension.getAllTimeRecordPull().pull/poid));
            c.drawArc(o, a,1,true, p);
        }
        //Bar last day

        if(Res.currentPrehension.getLastDay() != null) {
            p.setColor(color(R.color.last_session));
            float a = (float)(180 + 180 * ratio * (Res.currentPrehension.getLastDay().pull/poid));
            c.drawArc(o, a,1,true, p);
        }

        //bare toDay
        if(Res.currentPrehension.getToDayPull() != null){
            p.setColor(color(R.color.today_record));
            float a = (float)(180 + 180 * ratio * (Res.currentPrehension.getToDayPull().pull/poid));
            c.drawArc(o, a,1,true, p);
        }

        p.setColor(Color.BLACK);
        float a = (float)(180 + 180 * ratio * (maxPull/poid));
        c.drawArc(o, a,0.8f,true, p);

    }



}
