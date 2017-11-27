package org.js.msb2kml.ProcessLog;

import org.js.msb2kml.Common.metaData;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by js on 2/27/17.
 */

public class htmlGen {

    FileWriter outHtml=null;
    metaData m;

    public boolean beginHtml(FileWriter out, metaData md, String title){
        outHtml=out;
        m=md;
        try {
            outHtml.write("<!DOCTYPE html>\n");
            outHtml.write("<html>\n");
            outHtml.write("<head>\n");
            outHtml.write(String.format("<title>%s</title>\n",title));
            outHtml.write("</head><body>\n");
            outHtml.write(String.format(Locale.US,"<H1 id=\"top\">%s</H1>\n",title));
            String plane=m.getPlane();
            if (! plane.isEmpty()){
                outHtml.write(String.format(Locale.US,"<center><b>%s</b></center><br>\n",plane));
            }
            String comment=m.getComment();
            if (! comment.isEmpty()){
                outHtml.write((String.format(Locale.US,"<b>Comment : </b><code>%s</code><br>\n",comment)));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean closeHtml(){
        try {
            outHtml.write("<p id=\"bottom\">\n");
            outHtml.write("</body>\n");
            outHtml.write("</html>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean tableHtml(ArrayList<String> head1, ArrayList<String> head2, String caption,
                             String prev, String next, Calendar time){
        try {
            outHtml.write(String.format(Locale.US,"<p id=\"%s\">\n",caption));
            outHtml.write("<table border=\"0\" width=\"80%\"><tr>\n");
            outHtml.write("<td align=\"center\"><a href=\"#top\">Top</a></td>\n");
            outHtml.write(String.format(Locale.US,
                    "<td align=\"center\"><a href=\"%s\">Previous</a></td>\n",prev));
            outHtml.write(String.format(Locale.US,
                    "<td align=\"center\"><a href=\"%s\">Next</a></td>\n",next));
            outHtml.write("<td align=\"center\"><a href=\"#bottom\">Bottom</a></td>\n");
            outHtml.write("</table>\n");
            outHtml.write("<p>\n");
            if (time != null){
                outHtml.write(String.format(Locale.US,"<h3>%tR</h3>\n",time));
            }
            outHtml.write("<table border>\n");
            outHtml.write(String.format(Locale.US,"<caption>%s</caption>\n",caption));
            outHtml.write("<thead>\n");
            outHtml.write("<tr>\n");
            String H1[]= (String[]) head1.toArray(new String[0]);
            String H2[]=(String[]) head2.toArray(new String[0]);
            for (int i=0;i<head1.size();i++){
                if (H1[i].matches("-")) continue;
                outHtml.write(String .format(Locale.US,"<th>%s</th>\n",H1[i]));
            }
            outHtml.write("</tr><tr>\n");
            for (int i=0;i<head1.size();i++){
                if (H1[i].matches("-")) continue;
                outHtml.write(String.format(Locale.US,"<th>%s</th>\n",H2[i]));
            }
            outHtml.write("</tr>\n");
            outHtml.write("</thead>\n");
            outHtml.write("<tbody>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean tableClose(){
        try {
            outHtml.write("</tbody>\n");
            outHtml.write("</table>\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean rowHtml(ArrayList<String> head, int nCol,
                           ArrayList<String> data, ArrayList<Float> fdata){
        try {
            outHtml.write("<tr>\n");
            String H1[]= (String[]) head.toArray(new String[0]);
            for (int i=0;i<head.size();i++){
                if (H1[i].matches("-")) continue;
                if (i<nCol) outHtml.write(String.format(Locale.US,"<td>%s</td>\n", data.get(i)));
                else outHtml.write(String.format(Locale.US,"<td>%g</td>\n",fdata.get(i)));
            }
            outHtml.write(("</tr>\n"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
