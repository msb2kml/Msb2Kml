package org.js.msb2kml.DisplayLog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.js.msb2kml.Common.listing;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.valueOf;

public class Chart extends AppCompatActivity {

    Context context;
    String MsbName=null;
    String pathMSBlog;
    metaData m;
    listing l=new listing();
    String pathCsv=null;
    Map headings=new HashMap();
    Pattern patSemi= Pattern.compile(";");
    String xHead=null;
    LineChart chart=null;
    ArrayList<String> ylHead=new ArrayList<String>();
    ArrayList<String> yrHead=new ArrayList<String>();
    SharedPreferences pref=null;
    String fieldsHead[]=null;
    int colors[]={Color.BLACK,Color.BLUE,Color.CYAN,Color.MAGENTA,
                                  Color.GREEN,0XFFC05800,Color.RED};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        context = getApplicationContext();
        Intent intent = getIntent();
        MsbName = intent.getStringExtra("MsbName");
        pathMSBlog=intent.getStringExtra("MSBlog");
        m=new metaData(pathMSBlog);
        if (MsbName == null) finish();
        l.set(context,pathMSBlog);
        l.unique(MsbName);
        m.fetchPref(context);
        m.extract(context, MsbName);
        pathCsv = l.getCsv(0);
        try {
            BufferedReader buf = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(pathCsv)));
            String line = buf.readLine();
            if (line == null) finish();
            fieldsHead = patSemi.split(line);
            for (int i = 0; i < fieldsHead.length; i++) {
                headings.put(fieldsHead[i], i);
            }
            buf.close();
        } catch (Exception e) {
            finish();
        }
        xHead=m.getChartX();
        if (xHead == null) xHead = fieldsHead[0];
        else if (!headings.containsKey(xHead)) xHead = fieldsHead[0];
        Set<String> set=m.getChartYL();
        if (set != null) {
            for (String head : set) {
                if (headings.containsKey(head)) ylHead.add(head);
            }
        }
        set = m.getChartYR();
        if (set != null) {
            for (String head : set) {
                if (headings.containsKey(head)) yrHead.add(head);
            }
        }
        if (ylHead.isEmpty() && yrHead.isEmpty()) ylHead.add(fieldsHead[1]);
        chart = (LineChart) findViewById(R.id.chart);
        Button buttonLY = (Button) findViewById(R.id.button1);
        buttonLY.setTextColor(colors[0]);
        buttonLY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectYl();
            }
        });
        Button buttonX = (Button) findViewById(R.id.button2);
        buttonX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectX();
            }
        });
        Button buttonRY = (Button) findViewById(R.id.button3);
        buttonRY.setTextColor(colors[colors.length-1]);
        buttonRY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectYr();
            }
        });
        Button buttonG=(Button) findViewById(R.id.button4);
        buttonG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!chart.isEmpty()) saveGraph();
            }
        });
        Button buttonB=(Button) findViewById(R.id.button5);
        buttonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m.putPrefChart(context,xHead,ylHead,yrHead);
                finish();
            }
        });
        mkChart();
    }

    void saveGraph(){
        String name=MsbName+"_"+valueOf((int)headings.get(xHead));
        for (int i=0;i<ylHead.size();i++) name+="_"+valueOf((int)headings.get(ylHead.get(i)));
        for (int i=0;i<yrHead.size();i++) name+="_"+valueOf((int)headings.get(yrHead.get(i)));
        if (chart.saveToGallery(name,"Msb2Kml",MsbName, Bitmap.CompressFormat.JPEG,80)){
            Toast toast = Toast.makeText(this,
                    "Chart saved in Gallery: "+name,Toast.LENGTH_LONG);
            toast.show();
        }
    }

    void selectX(){
        AlertDialog.Builder build=new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        build.setTitle("Select the column for X axis")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mkChart();
                        }
                })
                .setItems(fieldsHead, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        xHead=fieldsHead[which];
                        chart.clear();
                        mkChart();
                    }
                });
        build.show();
    }

    void selectYl(){
        final boolean[] checked=new boolean[fieldsHead.length];
        Arrays.fill(checked,false);
        for (int i = 0; i < ylHead.size(); i++){
            checked[(int) headings.get(ylHead.get(i))]=true;
        }
        AlertDialog.Builder build=new AlertDialog.Builder(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        build.setTitle("Select the column(s) sharing the left Y axis")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mkChart();
                    }
                })
                .setMultiChoiceItems(fieldsHead, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                       checked[which]=isChecked;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ylHead.clear();
                        for (int i=0;i<fieldsHead.length;i++){
                            if (checked[i]) ylHead.add(fieldsHead[i]);
                        }
                        chart.clear();
                        mkChart();
                    }
                });
        build.show();
    }

    void selectYr(){
        final boolean[] checked=new boolean[fieldsHead.length];
        Arrays.fill(checked,false);
        for (int i = 0; i < yrHead.size(); i++){
            checked[(int) headings.get(yrHead.get(i))]=true;
        }
        AlertDialog.Builder build=new AlertDialog.Builder(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        build.setTitle("Select the column(s) sharing the right Y axis")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mkChart();
                    }
                })
                .setMultiChoiceItems(fieldsHead, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which]=isChecked;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yrHead.clear();
                        for (int i=0;i<fieldsHead.length;i++){
                            if (checked[i]) yrHead.add(fieldsHead[i]);
                        }
                        chart.clear();
                        mkChart();
                    }
                });
        build.show();
    }

    void mkChart(){
        int indX=(int)headings.get(xHead);
        int indYl[]=null;
        int indYr[]=null;
        ArrayList<Entry>[] entriesL=null;
        ArrayList<Entry>[] entriesR=null;
        LineDataSet dataSetL[]=null;
        LineDataSet dataSetR[]=null;
        if (ylHead.size()>0) {
            indYl = new int[ylHead.size()];
            entriesL = new ArrayList[ylHead.size()];
            for (int i = 0; i < ylHead.size(); i++) {
                indYl[i] = (int) headings.get(ylHead.get(i));
                entriesL[i]=new ArrayList<Entry>();
            }
        }
        if (yrHead.size()>0){
            indYr=new int[yrHead.size()];
            entriesR=new ArrayList[yrHead.size()];
            for (int i=0;i<yrHead.size();i++){
                indYr[i]=(int)headings.get(yrHead.get(i));
                entriesR[i]=new ArrayList<Entry>();
            }
        }
        try {
            BufferedReader buf = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(pathCsv)));
            String line=buf.readLine();
            while (line!=null){
                line=buf.readLine();
                if (line == null) continue;
                String fields[]=patSemi.split(line);
                Float X=Float.parseFloat(fields[indX]);
                for (int i=0;i<ylHead.size();i++){
                    entriesL[i].add(new Entry(X,Float.parseFloat(fields[indYl[i]])));
                }
                for (int i=0;i<yrHead.size();i++){
                    entriesR[i].add(new Entry(X,Float.parseFloat(fields[indYr[i]])));
                }
            }
            buf.close();
        }
        catch (Exception e){
            finish();
        }
        ArrayList<ILineDataSet> iLineDataSets=new ArrayList<ILineDataSet>();
        XAxis xAxis = chart.getXAxis();
        YAxis ylAxis=chart.getAxisLeft();
        YAxis yrAxis=chart.getAxisRight();
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        if (ylHead.size()>0){
            ylAxis.setEnabled(true);
            dataSetL=new LineDataSet[ylHead.size()];
            for (int i=0;i<ylHead.size();i++){
                dataSetL[i]=new LineDataSet(entriesL[i],ylHead.get(i)+"/"+xHead);
                dataSetL[i].setDrawCircles(false);
                dataSetL[i].setLineWidth(0.5f);
                dataSetL[i].setAxisDependency(YAxis.AxisDependency.LEFT);
                dataSetL[i].setColor(colors[i%colors.length]);
                iLineDataSets.add(dataSetL[i]);
            }
            ylAxis.setGridColor(colors[0]);
            ylAxis.setAxisLineColor(colors[0]);
            ylAxis.setTextColor(colors[0]);
        } else ylAxis.setEnabled(false);
        if (yrHead.size()>0){
            yrAxis.setEnabled(true);
            dataSetR=new LineDataSet[yrHead.size()];
            for (int i=0;i<yrHead.size();i++){
                dataSetR[i]=new LineDataSet(entriesR[i],yrHead.get(i)+"/"+xHead);
                dataSetR[i].setDrawCircles(false);
                dataSetR[i].setLineWidth(0.5f);
                dataSetR[i].setAxisDependency(YAxis.AxisDependency.RIGHT);
                dataSetR[i].setColor(colors[(colors.length-i-1)%colors.length]);
                iLineDataSets.add(dataSetR[i]);
            }
            yrAxis.setAxisLineColor(colors[colors.length-1]);
            yrAxis.setGridColor(colors[colors.length-1]);
            yrAxis.setTextColor(colors[colors.length-1]);
        } else yrAxis.setEnabled(false);

        LineData lineData=new LineData(iLineDataSets);
        chart.setData(lineData);
        Description des=new Description();
        des.setText(m.getPlane()+" / "+m.getComment());
        chart.setDescription(des);
        chart.invalidate();
    }
}
