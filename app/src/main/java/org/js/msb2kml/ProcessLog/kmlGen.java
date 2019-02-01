package org.js.msb2kml.ProcessLog;

import android.location.Location;

import org.js.msb2kml.Common.metaData;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by js on 3/1/17.
 */

public class kmlGen {

    FileWriter outKml=null;
    metaData m;

    public boolean prolog(FileWriter out, metaData md, String title,
                          Location loc, Map<String,String> lineColor){

        outKml=out;
        m=md;
        try {
            outKml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            outKml.write(("<kml xmlns=\"http://www.opengis.net/kml/2.2\""));
            outKml.write("        xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n");
            outKml.write("  <Document>\n");
            outKml.write(String.format("<name>%s</name>\n",title));
            outKml.write("    <LookAt>\n");
            outKml.write("      <altitudeMode>relativeToGround</altitudeMode>\n");
            outKml.write(String.format(Locale.US,"      <longitude>%.8f</longitude>\n",
                    loc.getLongitude()));
            outKml.write(String.format(Locale.US,"      <latitude>%.8f</latitude>\n",
                    loc.getLatitude()));
            outKml.write("      <altitude>300.00</altitude>\n");
            outKml.write("       <range>300.00</range>\n");
            outKml.write("    </LookAt>\n");
            outKml.write("    <Style id=\"RedLine\">\n");
            outKml.write("      <LineStyle>\n");
            outKml.write("        <color>FF0000FF</color>\n");
            outKml.write("        <width>3</width>\n");
            outKml.write("      </LineStyle>\n");
            outKml.write("    </Style>\n");
            outKml.write("    <Style id=\"BlueLine\">\n");
            outKml.write("      <LineStyle>\n");
            outKml.write("        <color>FFFF0000</color>\n");
            outKml.write("        <width>3</width>\n");
            outKml.write("      </LineStyle>\n");
            outKml.write("    </Style>\n");
            if (lineColor != null){
                Set s=lineColor.entrySet();
                Iterator it=s.iterator();
                while (it.hasNext()){
                    Map.Entry me=(Map.Entry)it.next();
                    String key=(String)me.getKey();
                    String value=(String)me.getValue();
                    outKml.write(String .format(Locale.US,"<Style id=\"%s\">\n",key));
                    outKml.write("      <LineStyle>\n");
                    outKml.write(String.format(Locale.US,"        <color>%s</color>\n",value));
                    outKml.write("        <width>3</width>\n");
                    outKml.write("      </LineStyle>\n");
                    outKml.write("    </Style>\n");
                }
            }
            outKml.write("    <Folder>\n");
            outKml.write("      <name>Tracks</name>\n");
            outKml.write("      <Folder>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean trail(){
        try {
            outKml.write("      </Folder>\n");
            outKml.write("    </Folder>\n");
            outKml.write("  </Document>\n");
            outKml.write("</kml>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean beginTrack(boolean red, int  currentMin){
        try {
            outKml.write("<Placemark>\n");
            outKml.write(String .format(Locale.US,"<name>Minute %d</name>\n",currentMin));
            if (red) outKml.write("<styleUrl>#RedLine</styleUrl>\n");
            else outKml.write("<styleUrl>#BlueLine</styleUrl>\n");
// red=! red;
            outKml.write("<gx:Track>\n");
            outKml.write("<altitudeMode>absolute</altitudeMode>\n");
            outKml.write("<tessellate>1</tessellate>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean trackPoint(Calendar time, float lon, float lat, String height){
        try {
            outKml.write(String.format(Locale.US," <when>%tFT%tTZ</when>\n",time,time));
            outKml.write(String.format(Locale.US,"<gx:coord>%.8f %.8f %s</gx:coord>\n",lon,lat,height));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean endTrack(){
        try {
            outKml.write("</gx:Track>\n");
            outKml.write("</Placemark>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean beginLine(boolean red, int currentMin){
        try {
            outKml.write("<Placemark>\n");
            outKml.write(String .format(Locale.US,"<name>Minute %d</name>\n",currentMin));
            if (red) outKml.write("<styleUrl>#RedLine</styleUrl>\n");
            else outKml.write("<styleUrl>#BlueLine</styleUrl>\n");
//   red=! red;
            outKml.write("<LineString>\n");
            outKml.write("<altitudeMode>absolute</altitudeMode>\n");
            outKml.write("<tessellate>1</tessellate>\n");
            outKml.write("<coordinates>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean beginLineColor(String color, int currentMin){
        try {
            outKml.write("<Placemark>\n");
            outKml.write(String.format(Locale.US,"<name>Minute %d</name>\n",currentMin));
            outKml.write(String .format(Locale.US,"<styleUrl>#%s</styleUrl>\n",color));
            outKml.write("<LineString>\n");
            outKml.write("<altitudeMode>absolute</altitudeMode>\n");
            outKml.write("<tessellate>1</tessellate>\n");
            outKml.write("<coordinates>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean endLine(){
        try {
            outKml.write("</coordinates>\n");
            outKml.write("</LineString>\n");
            outKml.write("</Placemark>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
