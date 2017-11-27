package org.js.msb2kml.Work;

import android.os.Message;
import android.os.Handler;
import android.text.TextUtils;

import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.ProcessLog.gpxGen;
import org.js.msb2kml.ProcessLog.htmlGen;
import org.js.msb2kml.ProcessLog.kmlGen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.lang.Character;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;


/**
 * Created by js on 2/24/17.
 */

public class fileProcess {

    Message msg;
    int size=0;
    int position=0;
    htmlGen h=new htmlGen();
    gpxGen g=new gpxGen();
    kmlGen k=new kmlGen();
    boolean useDist=false;
    ArrayList<Float> totDist=new ArrayList<Float>();

    public void process(Handler hand, String path, metaData m){
        Map<String,String> lineColor=new HashMap<>();
        lineColor.put("Line00","FFFF0000");
        lineColor.put("Line01","FFDA0024");
        lineColor.put("Line02","FFB60048");
        lineColor.put("Line03","FF91006D");
        lineColor.put("Line04","FF6D0091");
        lineColor.put("Line05","FF4800B6");
        lineColor.put("Line06","FF2400DA");
        lineColor.put("Line07","FF0000FF");
        boolean Decimated=m.getDecimated();
        boolean Grapher=m.getGrapher();
        boolean Html=m.getHtml();
        Calendar startTime=m.getStartTime();
        FileWriter outCsv=null;
        FileWriter outHtml=null;
        FileWriter outGpx=null;
        FileWriter outKml=null;
        int outSec=-1;
        int htmlMin=-1;
        boolean red=true;
        Haversine haver=new Haversine();
        int minute=0;
        int kmlMinute=0;
        boolean inTable=false;
        String prevAnchor="#top";
        String nextAnchor="#bottom";
        Var colorVar=null;
        if (m.getNamedSensors()) readAddr(m);
        try {
            FileInputStream input=new FileInputStream(path);
            InputStreamReader reader=new InputStreamReader(input);
            BufferedReader buf=new BufferedReader(reader);
            FileChannel chan=input.getChannel();
            size=round(chan.size());
            msg=hand.obtainMessage(0,size,size);
            hand.sendMessage(msg);
            String line="";
            int prevPos=0;
            int lineNb=0;
            int dataNb=0;
            int gpsNb=0;
            boolean gpsNext=true;
            int firstLines=0;
            TextUtils.StringSplitter semiColon=new TextUtils.SimpleStringSplitter(';');
            TextUtils.StringSplitter comma=new TextUtils.SimpleStringSplitter(',');
            Pattern patSemi=Pattern.compile(";");
            Pattern patColo=Pattern.compile(",");
            ArrayList <String> Head=new ArrayList<String>();
            ArrayList <String> Head2=new ArrayList<String>();
            ArrayList <Integer> Used=new ArrayList<Integer>();
            ArrayList <String> Data=new ArrayList<String>();
            ArrayList <Float> minData=new ArrayList<Float>();
            ArrayList <Float> maxData=new ArrayList<Float>();
            ArrayList <Float> fData=new ArrayList<Float>();
            ArrayList <Float> compData=new ArrayList<Float>();
            ArrayList <String> compHead=new ArrayList<String>();
            ArrayList <tool> compTool=new ArrayList<tool>();
            String prevFields[]={"0","0"};
            Float prevLat=null;
            Float prevLon=null;
            String currentColor=null;
            int nComp=0;
            int nCol=0;
//------------------------------------------------------------ start reading
            while (line != null){
                line=buf.readLine();
                if (line == null) continue;
                lineNb++;
                position+=line.length()+1;
                if ((position-prevPos)*20 > size) {
                    msg=hand.obtainMessage(1,position,position);
                    hand.sendMessage(msg);
                    prevPos=position;
                }
//------------------------------------------------------------ SETUP1
                if (line.startsWith("$SETUP1;") && firstLines==0){
                    firstLines++;
                    semiColon.setString(line);
                    int i=-1;
                    int ind=0;
                    for (String field : semiColon){
                        i++;
                        if (i==0 || field.isEmpty()) continue;
                        if (m.getNamedSensors() && !translate.isEmpty()){
                           for (triplet tr : translate) {
                               if (field.matches(tr.addr)) {
                                   field = tr.subst;
                                   if (tr.var!=null){
                                       setVar(tr.var,1,(Object)fData,ind);
                                   }
                                   break;
                               }
                           }
                        }
                        Head.add(field);
                        Used.add(i);
                        fData.add(0.0f);
                        ind++;
                    }
                    nCol=Used.size();
                    if (m.getNamedSensors() & !translate.isEmpty()){
                        tb t=new tb();
                        for (triplet tr : translate) {
                            if (tr.addr.startsWith("=")){
                                if (tr.var!=null){
                                    setVar(tr.var,2,(Object)fData,ind);
                                }
                                ind++;
                            }
                        }
                        for (triplet tr : translate){
                            if (tr.addr.startsWith(("="))){
                                tool x=t.toolBox(this,tr.addr,tr.var);
                                fData.add(0.0f);
                                if (x==null) compHead.add("-");
                                else compHead.add(tr.subst);
                                compTool.add(x);
                            }
                        }
                        boolean ok=(compTool.size()<1);
                        while (! ok){
                            ok=true;
                            for (int it=0;it<compTool.size();it++){
                                if (compTool.get(it)!=null) {
                                    if (!compTool.get(it).checkMore()) {
                                        compHead.set(it, "-");
                                        compTool.set(it,null);
                                        ok=false;
                                    }
                                }
                            }
                        }
                        if (m.getColored()) colorVar=getVar('%');
                    }
                    String semi="";
                    line="";
                    for (String field : Head){
                        if (field.matches("-")) continue;
                        line+=semi+field;
                        semi=";";
                    }
                    for (String field : compHead){
                        if (field.matches("-")) continue;
                        line+=semi+field;
                    }
                    outCsv=new FileWriter(m.getPathCsv());
                    outCsv.write(line+"\n");
//------------------------------------------------------------------- SETUP2
                } else if (line.startsWith("$SETUP2;") && firstLines==1){
                    firstLines++;
                    String semi="";
                    String fields[]=patSemi.split(line);
                    line="";
                    char deg='\ufffd';
                    for (int i : Used){
                        Head2.add(fields[i].replace(Character.toString(deg), "&#176"));
                        if (Head.get(Head2.size()-1).matches("-")) continue;
                        line+=semi+fields[i];
                        semi=";";
                    }
                    for (String field : compHead){
                        Head2.add(field);
                        if (field.matches("-")) continue;
                        line+=semi+field;
                    }
                    Head.addAll(compHead);
                    outHtml=new FileWriter(m.getPathHtml());
                    h.beginHtml(outHtml,m,m.getTitle());
                    if (!Grapher) outCsv.write(line+"\n");
//------------------------------------------------------------------ D
                } else if (line.startsWith("$D;") && firstLines==2){
                    String fields[]=patSemi.split(line.replace(",","."));
                    int sec=(int)Float.parseFloat(fields[1]);
                    minute=sec/60;
                    if (Decimated && sec<=outSec) continue;
                    outSec=sec;
                    Data.clear();
                    int ind=0;
                    for (int i : Used){
                        if (fields[i].matches(".*[0-9]+")){
                            Data.add(fields[i]);
                            fData.set(ind,Float.parseFloat(fields[i]));
                        } else if (prevFields.length>i) {
                            Data.add(prevFields[i]);
                            fData.set(ind,Float.parseFloat(prevFields[i]));
                        }
                        else {
                            Data.add(" ");
                            fData.set(ind,0.0f);
                        }
                        ind++;
                    }
                    prevFields=fields.clone();
                    for (int it=0;it<compTool.size();it++){
                        tool t=compTool.get(it);
                        if (t!=null){
                            fData.set(ind,t.compute());
                        }
                        ind++;
                    }
                    if (minData.isEmpty()){
                        minData.addAll(fData);
                        maxData.addAll(fData);
                    } else {
                        for (int i=0;i<fData.size();i++){
                            minData.set(i,Math.min(minData.get(i),fData.get(i)));
                            maxData.set(i,Math.max(maxData.get(i),fData.get(i)));
                        }
                    }
                    line="";
                    String semi="";
                    for (int i=0;i<Head.size();i++){
                        if (Head.get(i).matches("-")) continue;
                        if (i<nCol) line+=semi+Data.get(i);
                        else line+=String.format(Locale.US,";%g",fData.get(i));
                        semi=";";
                    }
                    outCsv.write(line+"\n");
                    dataNb++;
                    gpsNext=true;
                    if (Html){
                        if (minute>htmlMin){
                            htmlMin=minute;
                            Calendar currentTime=(Calendar)startTime.clone();
                            currentTime.add(Calendar.SECOND,sec);
                            if (inTable) h.tableClose();
                            String caption=String.format(Locale.US,"Minute %02d",htmlMin);
                            nextAnchor=String.format(Locale.US,"#Minute %02d",htmlMin+1);
                            h.tableHtml(Head,Head2,caption,prevAnchor,nextAnchor,currentTime);
                            inTable=true;
                            prevAnchor="#"+caption;
                        }
                        h.rowHtml(Head,nCol,Data,fData);
                    }
//--------------------------------------------------------------------------GPGGA
                } else if (line.startsWith("$GPGGA,") && firstLines==2){
                    if (Decimated && !gpsNext) continue;
                    String fields[]=patColo.split(line);
                    Float lat=Float.parseFloat(fields[2])/(float)100.0;
                    Float lon=Float.parseFloat(fields[4])/(float)100.0;
                    if (fields[3].contains("S")) lat=-lat;
                    if (fields[5].contains("W")) lon=-lon;
                    String height=fields[9];
                    if (useDist){
                        if (prevLat!=null){
                            Double w=haver.haversine(prevLat.doubleValue(),prevLon.doubleValue(),
                                    lat.doubleValue(),lon.doubleValue())+totDist.get(0);
                            totDist.set(0,w.floatValue());
                        } else {
                            msg=hand.obtainMessage(5,position,position);
                            hand.sendMessage(msg);
                        }
                        prevLat=lat;
                        prevLon=lon;
                    }
                    Calendar currentTime=Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    currentTime.setTime(startTime.getTime());
                    currentTime.add(Calendar.SECOND,outSec);
                    if (outGpx == null){
                      outGpx=new FileWriter(m.getPathGpx());
                        g.beginGpx(outGpx,m,m.getTitle());
                    }
                    g.pointGpx(currentTime,lon,lat,height);
                    if (outKml == null){
                        outKml=new FileWriter(m.getPathKml());
                        k.prolog(outKml,m,m.getTitle(),lat,lon,lineColor);
                        kmlMinute=minute;
                        if (colorVar==null){
                            k.beginLine(red, kmlMinute);
                            if (m.getColored()) red = !red;
                        } else {
                            currentColor=styleColor(colorVar,lineColor.size());
                            k.beginLineColor(currentColor,minute);
                        }
                    }
                    outKml.write(String .format(Locale.US,"%.8f,%.8f,%s\n",lon,lat,height));
                    if (colorVar==null){
                         if (minute>kmlMinute) {
                            kmlMinute = minute;
                            if (m.getColored()) {
                               k.endLine();
                               k.beginLine(red, kmlMinute);
                               red = !red;
                               outKml.write(String.format(Locale.US,"%.8f,%.8f,%s\n", lon, lat, height));
                            }
                         }
                    } else {
                        String newColor=styleColor(colorVar,lineColor.size());
                        if (!currentColor.contentEquals(newColor)){
                            k.endLine();
                            kmlMinute=minute;
                            k.beginLineColor(newColor,minute);
                            outKml.write(String.format(Locale.US,"%.8f,%.8f,%s\n", lon, lat, height));
                        }
                    }
                    gpsNext=false;
                    gpsNb++;
//---------------------------------------------------------------------- wrong
                } else if (line.startsWith("$SETUP")) {
                    msg=hand.obtainMessage(21,lineNb,firstLines);
                    hand.sendMessage(msg);
                } else {
                    msg=hand.obtainMessage(20,lineNb,firstLines);
                    hand.sendMessage(msg);
                    buf.close();
                    return;
                }
            }
//-------------------------------------------------------------------- end reading
            buf.close();
            if (outCsv!=null) outCsv.close();
            if (outHtml!=null){
                if (inTable) h.tableClose();
                h.closeHtml();
                outHtml.close();
            }
            if (outGpx!=null){
                g.tailGpx();
                outGpx.close();
            }
            if (outKml!=null){
                k.endLine();
                k.trail();
                outKml.close();
            }
            FileWriter outTXT=m.saveMeta();
            if (outTXT == null){
                msg=hand.obtainMessage(666,"Meta file!");
                hand.sendMessage(msg);
            }
            else{
                for (int i=0;i<Head.size();i++){
                    if (Head.get(i).matches("-")) continue;
                    outTXT.write(String.format(Locale.US,"%s: %g ;%g\n",
                            Head.get(i),minData.get(i),maxData.get(i)));
                }
                if (! translate.isEmpty()){
                    outTXT.write("*************** AddrSens.txt *****************\n");
                    File addr=new File(m.getPathAddr());
                    BufferedReader f=new BufferedReader(new FileReader(addr));
                    line="";
                    while (line!=null){
                        line=f.readLine();
                        if (line==null) continue;
                        outTXT.write(line+"\n");
                    }
                    f.close();
                }
                outTXT.close();
            }
            msg=hand.obtainMessage(2,dataNb,gpsNb);
            hand.sendMessage(msg);
        }
        catch (Exception e){
            msg=hand.obtainMessage(666,e.getMessage());
            hand.sendMessage(msg);
        }
    }

    String styleColor(Var colorVar, int nColor){
        ArrayList<Float> zz=(ArrayList<Float>)colorVar.thing;
        Float percent=zz.get(colorVar.index)/100f;
        int v=round(percent*nColor);
        v=max(1,min(nColor,v))-1;
        return String.format("Line%02d",v);
    }

    private class triplet{
        String addr=null;
        String subst=null;
        Character var=null;
    }

    ArrayList<triplet> translate=new ArrayList<>();

    boolean readAddr(metaData m){
        File addr=new File(m.getPathAddr());
        if (! addr.exists()) return false;
        int n=0;
        try {
            BufferedReader f = new BufferedReader(new FileReader(addr));
            String line="";
            while (line!=null){
                line=f.readLine();
                if (line==null) continue;
                if (line.startsWith("*")) continue;
                String[] fields = line.split(";");
                if (fields==null || fields.length<2) continue;
                triplet t=new triplet();
                t.addr=fields[0];
                t.subst=fields[1];
                if (fields.length>2){
                    String sign=fields[2].trim();
                    if (!sign.isEmpty()) t.var=sign.charAt(0);
                }
                translate.add(t);
            }
            f.close();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public class Var{
        Character id=null;
        Object thing=null;
        Integer index=null;
    }

    ArrayList<Var> variables=new ArrayList<>();

    boolean setVar(Character c, Integer t, Object O, Integer ind){
        if (variables!=null) {
            for (Var v : variables) if (v.id == c) return false;
        }
        Var v=new Var();
        v.id=c;
        v.thing=O;
        v.index=ind;
        variables.add(v);
        return true;
    }

    public Var getVar(Character c){
        if (variables==null) return null;
        for (Var v : variables) {
            if (v.id.equals(c)) return v;
        }
        if (c.equals('#')){
            useDist=true;
            totDist.add(0f);
            if (!setVar(c,1,(Object)totDist,0)) return null;
            return  getVar(c);
        }
        return null;
    }

    public boolean delVar(char c){
        if (variables==null) return false;
        for (Var v : variables) if (v.id.equals(c)){
            variables.remove(v);
            return true;
        }
        return false;
    }
}
