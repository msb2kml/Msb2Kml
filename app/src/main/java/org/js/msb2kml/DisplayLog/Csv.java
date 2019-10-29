package org.js.msb2kml.DisplayLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.regex.Pattern;

public class Csv {

    String CsvPath=null;
    Calendar StartTime=null;
    BufferedReader f=null;
    Long StartMs;
    String fieldsHead[]=null;
    Pattern patSemi=Pattern.compile(";");
    Long curMSec=null;
    String fieldVal[]=null;

    public Integer open(String path, Calendar start){
        if (f!=null) close();
        CsvPath=path;
        StartTime=start;
        StartMs=StartTime.getTimeInMillis();
        String when=StartMs.toString();
        File fi=new File(CsvPath);
        if (!fi.exists() || !fi.canRead()) return null;
        try {
            FileInputStream input=new FileInputStream(CsvPath);
            InputStreamReader reader=new InputStreamReader(input);
            f=new BufferedReader(reader);
            String line=f.readLine();
            if (line==null) return null;
            fieldsHead=patSemi.split(line);
            curMSec=rdLine();
            if (curMSec==null) return null;
            return fieldsHead.length;
        } catch (Exception e) { return null; }
    }

    public void close(){
        if (f!=null){
            try {
                f.close();
                f=null;
            } catch (Exception e){ f=null; }
        }
        CsvPath=null;
        StartTime=null;
    }

    Long rdLine(){
        if (f==null) return null;
        try {
            String line=f.readLine();
            if (line==null) return null;
            fieldVal=patSemi.split(line);
            Float ms=Float.parseFloat(fieldVal[0])*1000.0F;
            return StartMs+ms.longValue();
        } catch (Exception e) {return null;}
    }

    public Float nextPt(Long before, Integer index){
        if (curMSec==null || index==null || index<0 ||
                index>=fieldsHead.length) return null;
        String[] fieldsCp=fieldVal.clone();
        while (curMSec<before){
            curMSec=rdLine();
            if (curMSec==null) return null;
            fieldsCp=fieldVal.clone();
        }
        return Float.parseFloat(fieldsCp[index]);
    }
}
