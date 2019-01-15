package com.arrkays.poutre;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class ChartsHold extends LineChart {


    public ChartsHold(Context context) {
        super(context);
        constructeur();
    }

    public ChartsHold(Context context, AttributeSet attrs) {
        super(context, attrs);
        constructeur();
    }

    public ChartsHold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        constructeur();
    }

    private void constructeur() {
        //setting
        setDoubleTapToZoomEnabled(false);
        setScaleXEnabled(true);
        setDragDecelerationEnabled(true);

        //formater les date sur l'axe x
        getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Prehension.getDate((long)value);
            }
        });

        getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return Math.round(value)+" kg";
            }
        });

    }

    void updateData(){
        List<Entry> entries = new ArrayList<>();

        for(Pull p : Res.currentPrehension.historic){
            entries.add(new Entry((float) p.date,(float)p.pull));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Pull");
        LineData lineData = new LineData(dataSet);
        setData(lineData);
        invalidate(); // refresh
    }
}
