package com.arrkays.poutre;

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
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

public class Graph extends View {

    String TAG = "debug-bluetooth";
    int x=150;
    int y=150;
    public double poid = 60;
    public double pull = 0;
    public double maxPull = 0;
    int width;
    int height;
    double ratio = 0.9;//ratio beetween sub weight pull and fuul graph
    double ratio2 = 0.5;//ratio beetween half weight pull and fuul graph
    Rect rec = new Rect();
    public int max;
    private Paint p = new Paint();
    public Handler handler = null;

    public Graph(Context c, AttributeSet attrs) {
        super(c, attrs);


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
        p.setColor(Color.rgb(0,190,0));
        p.setStyle(Paint.Style.FILL);
        if(poid==0)
            poid =60;

        //bare vert
        int top = (int)(((double)height*ratio) * pull/poid);
        rec.bottom = height;
        rec.left = 0;
        rec.right = width;
        rec.top = height-top;
        c.drawRect(rec, p);

        //bar orange
        if(ratio2 <= pull/poid){
            Log.d(TAG,"Orange, height:"+height+" , bottom: "+(int)(double)(((height*ratio))*ratio2)+" top: "+(height-top));
            rec.set(0,height-top, width,height-(int)(double)(((height*ratio))*ratio2));
            p.setColor(Color.rgb(255,140 ,0));
            c.drawRect(rec, p);
        }

        //bar rouge si pull depace poid
        if(1 <= pull/poid){
            rec.set(0,height-top, width, (int)(height*(1-ratio)));
            p.setColor(Color.RED);
            c.drawRect(rec, p);
        }

        //ligne tout les 10kg
        double unKiloEnPixel =  (((double)height * ratio) /  poid);
        p.setColor(Color.BLACK);
        for(double i = 0; i < poid ; i+=10){
            c.drawLine(0,(float)(unKiloEnPixel*i), width,(float)(unKiloEnPixel*i),p);
        }

        //bare max
        int topBar = (int)(((double)height*ratio) * maxPull/poid);
        if(topBar>height-32)
            topBar=height - 32;
        p.setColor(Color.GREEN);
        rec.set(0,height-topBar-4,width,height-topBar);
        c.drawRect(rec, p);

        //text
        p.setTextSize(30);
        if(Res.POID == 0)
            c.drawText(maxPull+" Kg",2,height-topBar-12,p);
        else
            c.drawText(maxPull+" Kg ("+Res.getPour(pull)+"%)",2,height-topBar-12,p);

        //cadre
        p.setStyle(Paint.Style.STROKE);
        c.drawRect(0,0,width,height,p);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        maxPull = 0;
        super.invalidate();
    }
}
