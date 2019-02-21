package com.arrkays.poutre;

import android.content.Context;
import android.content.res.TypedArray;
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
    String TAG = "Graph_Cirular_Debug";
    float thickness = 50;
    float elevationCadran=0; //en pixel
    float angleDebutFin; //  en degrée
    float epaisseurCadran; //  ratio du rayon (entre 0 et 1)
    float ratioMax;
    float ratioMoy;

    int colorInf;
    int colorMoy;
    int colorSup ;
    int colorCadran;
    int colorBackground ;
    int colorTrai10Kg ;

    boolean showLastPull;

    Bitmap needle = BitmapFactory.decodeResource(getResources(),R.drawable.needle_512);

    public GraphMaxPullCirular(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a=getContext().obtainStyledAttributes(attrs,R.styleable.GraphMaxPullCirular);
        //Use a
        elevationCadran = a.getFloat(R.styleable.GraphMaxPullCirular_custom_elevationCadran, 0);
        ratioMax = a.getFloat(R.styleable.GraphMaxPullCirular_custom_ratioMax, 0.9f);
        ratioMoy = a.getFloat(R.styleable.GraphMaxPullCirular_custom_ratioMoy, 0.5f);
        epaisseurCadran = a.getFloat(R.styleable.GraphMaxPullCirular_custom_epaisseurCadran, 0.15f);
        colorCadran = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorCadran,  Color.rgb(230,230,230));
        colorInf = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorInf, Color.rgb(0,190 ,0));
        colorMoy = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorMoy, Color.rgb(255,106,0));
        colorSup = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorSup, Color.rgb(255,0,0));
        colorBackground = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorBackground, Color.WHITE);
        colorTrai10Kg = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorTrai10Kg, Color.GRAY);
        showLastPull = a.getBoolean(R.styleable.GraphMaxPullCirular_custom_showLastPull, true);

        //Don't forget this
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.updateSize();

        //dRAW background
        p.setStyle(Paint.Style.FILL);
        p.setColor(colorBackground);
        canvas.drawRect(0,0,width,height,p);

        //update poid
        if(Res.poids==0)
            poid =60;
        else
            poid = Res.poids;

        //calcul du rayon
        int r;
        if(width < height + elevationCadran){//portrait
            r = width/2;
        }
        else{//paysage
            r = width/2;
            if(r > height-elevationCadran)
                r = (int) (height-elevationCadran);
        }

        //calcule de l'angle de dépar et de fin
        //elevationCadran = (float) (Math.cos(Math.toRadians(90-angleDebutFin)) * r);
        angleDebutFin = (float) Math.toDegrees( Math.asin((elevationCadran/(double)r)));
        //thisckness
        thickness = r*epaisseurCadran;
        float thicknessLastPull = thickness * 0.9f;

        int left = width/2 - r;
        int top = (int) (height-r-elevationCadran);
        int right = width/2 + r;
        int bottom = (int) (height+r-elevationCadran);

        //calculation taille du cadrant
        RectF oval = new RectF(left,top,right,bottom);
        RectF ovalMask = new RectF(left+thickness,top+thickness,right-thickness,bottom-thickness);
        RectF ovalLastPull = new RectF(left+thicknessLastPull,top+thicknessLastPull,right-thicknessLastPull,bottom-thicknessLastPull);


        //dessiner background du cadran
        p.setColor(colorCadran);
        p.setStyle(Paint.Style.FILL);
        canvas.drawOval(oval,p);

        //calcule du pull
        float angle =(float)( (180+(angleDebutFin*2) * ratioMax) * (pull / poid));

        //dessin du pull
        drawPullCadran(canvas, oval, pull);


        //dessin lastpull
        if(showLastPull)
            drawPullCadran(canvas, ovalLastPull, maxPull);

        //dessin bar record
        barDeRccord(canvas,oval);
        /*//all time
        drawBar(canvas,oval,R.color.record, Res.currentPrehension.getAllTimeRecordPull().pull);
        //last session
        drawBar(canvas,oval,R.color.last_session, Res.currentPrehension.getToDayPull().pull);
        //today
        drawBar(canvas,oval,R.color.today_record, Res.currentPrehension.getToDayPull().pull);
        //max pull
        drawBar(canvas,oval,Color.BLACK, maxPull);*/


        //ligne tout les 10kg
        double kiloAngle =  (180+(2*angleDebutFin) * ratio) /  poid;
        p.setColor(colorTrai10Kg);
        for(double i = 10; i < poid+11 ; i+=10){
            canvas.drawArc(oval, 180-angleDebutFin+(float)(i*kiloAngle), 0.4f, true, p);
        }

        //dessiner le maske
        p.setColor(Color.WHITE);
        canvas.drawOval(ovalMask,p);

        canvas.drawArc(oval, 180-angleDebutFin, (-180+angleDebutFin*2), true, p);

        //dessiner l'oval autour
        /*p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawOval(oval,p);*/


        //aiguille
        Matrix m = new Matrix();

        float decalageTroueAiguille = 29;
        float needleScale = ((float)r-thickness+decalageTroueAiguille)/needle.getWidth();
        float needleHeigth =  61;//(float)needle.getHeight()*needleScale;
        float needleWidth =  512;//(float)needle.getWidth()*needleScale;

        m.setTranslate(thickness+left,height-(needleHeigth/2)-elevationCadran);
        m.preScale(needleScale,needleScale);

        float px = r+left;
        float py = height-elevationCadran;

        m.postRotate(angle-angleDebutFin, px, py);
        canvas.drawBitmap(needle, m, p);
        p.setColor(Color.RED);
        p.setStyle(Paint.Style.FILL);
        canvas.drawCircle(px,py,2,p);

    }


    void drawPullCadran(Canvas canvas, RectF oval, double  pull){
        p.setStyle(Paint.Style.FILL);

        p.setColor(colorInf);
        float angle =(float)( (180+(angleDebutFin*2) * ratioMax) * (pull / poid));
        canvas.drawArc(oval, 180-angleDebutFin , angle, true, p);

        if(pull >= poid*ratioMoy ){
            float starAngle = (float)( (180+(2*angleDebutFin) * ratioMax) * ratioMoy);
            p.setColor(colorMoy);
            canvas.drawArc(oval, 180-angleDebutFin+starAngle, angle-starAngle, true, p);
        }

        if(pull >= poid){
            float starAngle = (float)( (180+(2*angleDebutFin) * ratioMax) );
            p.setColor(colorSup);
            canvas.drawArc(oval, 180-angleDebutFin+starAngle, angle-starAngle, true, p);
        }
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
        //float a = (float)((180-angleDebutFin) + (180+2*angleDebutFin) * ratio * (maxPull/poid));
        float a = ((float)( (180+(angleDebutFin*2) * ratio) * (maxPull / poid)))- angleDebutFin;
        c.drawArc(o, a+180,0.8f,true, p);

    }

    void drawBar(Canvas c, RectF o, int color, double barPull){
        p.setColor(color);
        float a = ((float)( (180+(angleDebutFin*2) * ratio) * (barPull / poid)))- angleDebutFin;
        c.drawArc(o, a+180,0.8f,true, p);
    }


}
