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
    Rect rec = new Rect();
    public int max;
    private Paint p = new Paint();

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
        int top = (int)(((double)height*ratio) * pull/poid);
        rec.bottom = height;
        rec.left = 0;
        rec.right = width;
        rec.top = height-top;
        c.drawRect(rec, p);

        //bar rouge si pull depace poid
        if(1 <= pull/poid){
            rec.set(0,height-top,width,(int)(height*(1-ratio)));
            p.setColor(Color.RED);
            c.drawRect(rec, p);
        }

        //bare max
        top = (int)(((double)height*ratio) * maxPull/poid);
        p.setColor(Color.BLACK);
        rec.set(0,height-top-4,width,height-top);
        c.drawRect(rec, p);

        //text
        p.setTextSize(30);
        c.drawText(maxPull+" Kg",2,height-top-12,p);
    }

    private getTop

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"pull"+pull);
        super.invalidate();
        if(event.getY() > (height/2)){
            setPull(pull-1);
        }
        else
        {
            setPull(pull+1);
        }
        return super.onTouchEvent(event);
    }
}
