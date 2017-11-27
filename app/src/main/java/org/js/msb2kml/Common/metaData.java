package org.js.msb2kml.Common;

import android.content.Context;
import android.content.SharedPreferences;

import org.js.msb2kml.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.io.FileReader;


/**
 * Created by js on 2/20/17.
 */

public class metaData {

    String date;
    String plane;
    String comment;

    Calendar startTime;
    String Directory;
    boolean Decimated;
    boolean NamedSensors;
    boolean Colored;
    boolean Html;
    boolean Grapher;
    String ChartX;
    Set<String> ChartYL;
    Set<String> ChartYR;


    String MsbName;
    String pathCsv;
    String pathHtml;
    String pathGpx;
    String pathKml;
    String pathAddr;
    String pathTXT;

    public void fetchPref(Context context){
        SharedPreferences pref=context.getSharedPreferences(
                context.getString(R.string.PrefName),0);
        plane=pref.getString("Plane","");
        comment=pref.getString("Comment","");
        startTime=Calendar.getInstance();
        SimpleDateFormat sdf=new SimpleDateFormat(context.getString(R.string.StampFmt));
        date=sdf.format(startTime.getTime());
        date=pref.getString("StartTime",date);
        try {
            startTime.setTime(sdf.parse(date));
        } catch (ParseException e) {
            startTime=Calendar.getInstance();
        }
        Directory=pref.getString("Directory","/mnt");
        String value=pref.getString("Decimated","True");
        Decimated=value.equalsIgnoreCase("true");
        value=pref.getString("SensorName","True");
        NamedSensors=value.equalsIgnoreCase("true");
        value=pref.getString("Colored","True");
        Colored=value.equalsIgnoreCase("true");
        value=pref.getString("HTML","true");
        Html=!(value.equalsIgnoreCase("false"));
        value=pref.getString("Grapher","true");
        Grapher=!(value.equalsIgnoreCase("false"));
        ChartX=pref.getString("ChartX",null);
        ChartYL=pref.getStringSet("ChartYL",null);
        ChartYR=pref.getStringSet("ChartYR",null);
        return;
    }

    public void putPref(Context context){
        SharedPreferences pref=context.getSharedPreferences(
                context.getString(R.string.PrefName),0);
        SharedPreferences.Editor edit=pref.edit();
        edit.putString("Plane",plane);
        edit.putString("Comment",comment);
        edit.putString("StartTime",date);
        edit.putString("Directory",Directory);
        if (Decimated) edit.putString("Decimated","True");
        else edit.putString("Decimated","False");
        if (NamedSensors) edit.putString("SensorName","True");
        else edit.putString("SensorName","False");
        if (Colored) edit.putString("Colored","True");
        else edit.putString("Colored","False");
        if (Html) edit.putString("HTML","True");
        else edit.putString("HTML","False");
        if (Grapher) edit.putString("Grapher","True");
        else edit.putString("Grapher","False");
        edit.commit();
        return;
    }

    public void putPrefChart(Context context, String chartx, ArrayList<String> ylhead,
                             ArrayList<String> yrhead){
        SharedPreferences pref=context.getSharedPreferences(
                context.getString(R.string.PrefName),0);
        SharedPreferences.Editor edit=pref.edit();
        edit.putString("ChartX",chartx);
        Set<String> set = new HashSet<>();
        if (ylhead.size() > 0) {
                set.addAll(ylhead);
                edit.putStringSet("ChartYL", set);
            }
        set.clear();
        if (yrhead.size() > 0) {
                set.addAll(yrhead);
                edit.putStringSet("ChartYR", set);
            }
        edit.commit();
        return;
    }

    public boolean setName (Context context, String name){
        MsbName=name;
        pathAddr=context.getString(R.string.MSBlog)+"/AddrSens.txt";
        File f_gpx=new File(context.getString(R.string.MSBlog)+"/"+MsbName+".gpx");
        if (f_gpx.exists()) return true;
        File f_kml=new File(context.getString(R.string.MSBlog)+"/"+MsbName+".kml");
        return f_kml.exists();
    }

    public boolean extract (Context context, String name){
        pathTXT=context.getString(R.string.MSBlog)+"/"+name+".txt";
        try {
            BufferedReader f=new BufferedReader(new FileReader(pathTXT));
            for (int i = 0; i < 3; i++) {
                String line=f.readLine();
                if (line.startsWith("Date: ")){
                    date=line.replaceFirst("Date: ","");
                    SimpleDateFormat sdf=new SimpleDateFormat(context.getString(R.string.StampFmt));
                    try {
                        Date dd=sdf.parse(date,new ParsePosition(0));
                        startTime.setTime(dd);
                    } catch (Exception e) {
                        startTime=Calendar.getInstance();
                    }
                } else if (line.startsWith("Plane: ")){
                    plane=line.replaceFirst("Plane: ","");
                } else if (line.startsWith("Comment: ")){
                    comment=line.replaceFirst("Comment: ","");
                }
            }
         } catch(IOException e){
            return false;
        }
        return ((date!=null) && (plane!=null) && (comment!=null));
    }

    public FileWriter saveMeta(){
        try {
            FileWriter outMeta=new FileWriter(pathTXT);
            outMeta.write("Date: "+date+"\n");
            outMeta.write("Plane: "+plane+"\n");
            outMeta.write("Comment: "+comment+"\n");
            return outMeta;
        } catch (IOException e) {
            return null;
        }
    }

    public void set (Context context, String p, String c){
        plane=p;
        comment=c;
        return;
    }

    public void setParam (Context context, Calendar start, String directory, boolean decimated,
                boolean namedSensors, boolean colored, boolean html, boolean grapher){
        startTime=start;
        SimpleDateFormat sdf=new SimpleDateFormat(context.getString(R.string.StampFmt));
        date=sdf.format(startTime.getTime());
        Directory=directory;
        Decimated=decimated;
        pathCsv=context.getString(R.string.MSBlog)+"/"+MsbName;
        if (Decimated) pathCsv+="d";
        else pathCsv+="f";
        pathCsv+=".csv";
        NamedSensors=namedSensors;
        Colored=colored;
        Html=html;
        Grapher=grapher;
        pathHtml=context.getString(R.string.MSBlog)+"/"+MsbName+".html";
        pathGpx=context.getString(R.string.MSBlog)+"/"+MsbName+".gpx";
        pathKml=context.getString(R.string.MSBlog)+"/"+MsbName+".kml";
        return;
    }

    public String getDate (){
        return date;
    }
    public String getDay (){
        Pattern pat=Pattern.compile("[ ]");
        String fields[]=pat.split(date);
        return fields[0];
    }

    public Calendar getStartTime(){
        return startTime;
    }

    public String getPlane (){
        return plane;
    }

    public String getComment (){
        return comment;
    }

    public String getDirectory(){
        return Directory;
    }

    public boolean getDecimated(){
        return Decimated;
    }

    public boolean getNamedSensors(){
        return NamedSensors;
    }

    public boolean getColored(){
        return Colored;
    }

    public boolean getHtml(){
        return Html;
    }

    public boolean getGrapher(){
        return Grapher;
    }

    public String getPathCsv(){
        return pathCsv;
    }
    public String getPathHtml(){
        return pathHtml;
    }
    public String getPathGpx(){
        return pathGpx;
    }
    public String getPathKml(){
        return pathKml;
    }
    public String getTitle(){
        if (Decimated) return MsbName+" (decimated)";
        return MsbName;
    }
    public String getPathAddr(){
        return pathAddr;
    }
    public String getChartX(){
        return ChartX;
    }
    public Set<String> getChartYL(){
        return ChartYL;
    }
    public Set<String> getChartYR(){
        return ChartYR;
    }
}
