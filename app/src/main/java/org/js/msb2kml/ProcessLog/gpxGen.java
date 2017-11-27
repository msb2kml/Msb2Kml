package org.js.msb2kml.ProcessLog;

import org.js.msb2kml.Common.metaData;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by js on 2/28/17.
 */

public class gpxGen {

    FileWriter outGpx=null;
    metaData m;

    public boolean beginGpx(FileWriter out, metaData md, String name){
        outGpx=out;
        m=md;
        try {
            outGpx.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            outGpx.write("<gpx version=\"1.0\"\n");
            outGpx.write("   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            outGpx.write("   creator=\"Msb2Kml\"\n");
            outGpx.write("   xmlns=\"http://www.topografix.com/GPX/1/0\"\n");
            outGpx.write("   xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0");
            outGpx.write("http://www.topografix.com/GPX/1/0/gpx.xsd\">\n");
            outGpx.write(String.format(Locale.US,"<trk><name>%s</name><trkseg>\n",name));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean pointGpx(Calendar time, float lon, float lat, String height){
        try {
            outGpx.write(String.format(Locale.US,"<trkpt lat=\"%.8f\" lon=\"%.8f\">\n",lat,lon));
            outGpx.write(String.format(Locale.US," <ele>%s</ele>\n",height));
            outGpx.write(String.format(Locale.US," <time>%tFT%tTZ</time>\n",time,time));
            outGpx.write("</trkpt>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean tailGpx(){
        try {
            outGpx.write("</trkseg></trk>\n");
            outGpx.write("</gpx>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
