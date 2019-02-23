package com.arrkays.poutre;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

public class GraphMaxPullCirular extends Graph {
    String TAG = "Graph_Cirular_Debug";
    float thickness = 50;
    float elevationCadran = 0; //en pourcentage 1 = 100%
    float elevationRatio; //en pourcentage 1 = 100%
    float angleDebutFin; //  en degrée
    float epaisseurCadran; //  ratio du rayon (entre 0 et 1)
    float ratioMax;
    float ratioMoy;
    float graduation; //en kilo

    int colorInf;
    int colorMoy;
    int colorSup ;
    int colorCadran;
    int colorBackground ;
    int colorGraduation ;
    int colorGraduationTexe ;

    boolean showLastPull;
    boolean graduationTextOn;

    Bitmap needle = BitmapFactory.decodeResource(getResources(),R.drawable.needle_512);

    private float swipeAngleCadran;
    public GraphMaxPullCirular(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a=getContext().obtainStyledAttributes(attrs,R.styleable.GraphMaxPullCirular);
        //Use a
        elevationRatio = a.getFloat(R.styleable.GraphMaxPullCirular_custom_elevationRatio, 0);
        ratioMax = a.getFloat(R.styleable.GraphMaxPullCirular_custom_ratioMax, 0.9f);
        ratioMoy = a.getFloat(R.styleable.GraphMaxPullCirular_custom_ratioMoy, 0.5f);
        epaisseurCadran = a.getFloat(R.styleable.GraphMaxPullCirular_custom_epaisseurCadran, 0.15f);
        graduation = a.getFloat(R.styleable.GraphMaxPullCirular_custom_graduation, 10f);

        colorCadran = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorCadran,  Color.rgb(230,230,230));
        colorInf = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorInf, Color.rgb(0,190 ,0));
        colorMoy = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorMoy, Color.rgb(255,106,0));
        colorSup = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorSup, Color.rgb(255,0,0));
        colorBackground = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorBackground, Color.WHITE);
        colorGraduation = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorGraduation, Color.GRAY);
        colorGraduationTexe = a.getColor(R.styleable.GraphMaxPullCirular_custom_colorGraduationTexe, Color.GRAY);

        showLastPull = a.getBoolean(R.styleable.GraphMaxPullCirular_custom_showLastPull, true);
        graduationTextOn = a.getBoolean(R.styleable.GraphMaxPullCirular_custom_graduationTextOn, true);

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


        //elevation cadran
        elevationCadran = height * elevationRatio;

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

        //calcule du cadran 100% du poid
        swipeAngleCadran = (180+angleDebutFin*2) * ratioMax;

        //calculation taille du cadrant
        RectF oval = new RectF(left,top,right,bottom);
        RectF ovalMask = new RectF(left+thickness,top+thickness,right-thickness,bottom-thickness);
        RectF ovalLastPull = new RectF(left+thicknessLastPull,top+thicknessLastPull,right-thicknessLastPull,bottom-thicknessLastPull);


        //dessiner background du cadran
        p.setColor(colorCadran);
        p.setStyle(Paint.Style.FILL);
        canvas.drawOval(oval,p);

        //calcule du pull
        float angle = (float) (swipeAngleCadran * (pull / poid));

        //dessin du pull
        drawPullCadran(canvas, oval, pull);


        //dessin lastpull
        if(showLastPull)
            drawPullCadran(canvas, ovalLastPull, maxPull);

        //dessin bar record
        //barDeRccord(canvas,oval);
        //all time
        if(Res.currentPrehension.getAllTimeRecordPull() != null)
            drawBar(canvas,oval,color(R.color.record), Res.currentPrehension.getAllTimeRecordPull().pull);
        //last session
        if(Res.currentPrehension.getLastDay() != null)
            drawBar(canvas,oval,color(R.color.last_session), Res.currentPrehension.getLastDay().pull);
        //today
        if(Res.currentPrehension.getToDayPull() != null)
            drawBar(canvas,oval,color(R.color.today_record), Res.currentPrehension.getToDayPull().pull);
        //max pull
        drawBar(canvas,oval,Color.BLACK, maxPull);

        //ligne tout les graduation kg
        double kiloAngle =  swipeAngleCadran /  poid;
        p.setColor(colorGraduation);
        for(double i = graduation; i < poid+graduation+1 ; i+=graduation){
            float angleTrait = 180-angleDebutFin+(float)(i*kiloAngle);
            canvas.drawArc(oval, angleTrait, 0.4f, true, p);
        }

        //dessiner le maske
        p.setColor(Color.WHITE);
        canvas.drawOval(ovalMask,p);

        canvas.drawArc(oval, 180-angleDebutFin, (-180+angleDebutFin*2), true, p);

        //afficher text sur les graduation
        if(graduationTextOn){
            p.setColor(colorGraduationTexe);
            p.setTextSize(40);
            p.setTextAlign(Paint.Align.CENTER);
            for(double i = graduation; i < poid+graduation+1 ; i+=graduation){
                float angleTrait = 180-angleDebutFin+(float)(i*kiloAngle);

                Path path = new Path();
                float decalagePath = 38;
                path.addArc(left+thickness+decalagePath, top + thickness+decalagePath, right - thickness- decalagePath, bottom - thickness - decalagePath, angleTrait - 10,20);
                canvas.drawTextOnPath((int)i+"", path, 0,0,p);

            }
        }

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
        float angle = (float) (swipeAngleCadran * (pull / poid));
        canvas.drawArc(oval, 180-angleDebutFin , angle, true, p);

        if(pull >= poid*ratioMoy ){
            float starAngle = swipeAngleCadran * ratioMoy;
            p.setColor(colorMoy);
            canvas.drawArc(oval, 180-angleDebutFin+starAngle, angle-starAngle, true, p);
        }

        if(pull >= poid){
            float starAngle = swipeAngleCadran;
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

        float angle = (float) (swipeAngleCadran * (barPull / poid));
        c.drawArc(o, angle-180-angleDebutFin , 0.8f, true, p);
    }


}
