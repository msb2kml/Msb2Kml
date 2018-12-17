package org.js.msb2kml.Common;


import android.content.Context;
import android.widget.Toast;

import org.js.msb2kml.BrowseLog.filterMeta;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.R;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.lang.String;
import java.io.File;
import java.util.Arrays;


/**
 * Created by js on 2/1/17.
 */

public class listing {

    private ArrayList <String> Liste =new ArrayList<String>();
    private ArrayList <String> FileListe=new ArrayList<String>();
    private Context context;
    private String pathMSBlog;

    public boolean set(Context cont, String path){
        context=cont;
        pathMSBlog=path;
        File dir=new File(pathMSBlog);
        return (dir.exists());
    }

    public void createDir() {
        File dir=new File(pathMSBlog);
        dir.mkdirs();
    }

    public void unique(String msbname){
        Liste.clear();
        FileListe.clear();
        FileListe.add(msbname+".txt");
        return;
    }

    public String[] get(){
        Liste.clear();
        FileListe.clear();
        File f1=new File(pathMSBlog);
        FilenameFilter meta=new filterMeta("MSB_\\d{4}+\\.txt");
        if (f1.isDirectory()) {
            String s[] = f1.list(meta);
            if (s.length>0) {
                Arrays.sort(s);
                metaData m=new metaData(pathMSBlog);
                for (int i = 0; i < s.length; i++) {
                    String sName=s[i].replace(".txt","");
                    if (!m.extract(context,sName)) continue;
                    String line=sName+" / "+m.getDay()+" / "+m.getPlane()+" / "+m.getComment();
                    Liste.add(line);
                    FileListe.add(s[i]);
                }
            }
        }
        String[] ar= Liste.toArray(new String[0]);
        return(ar);
    }

    public void showIt(int i){
        String path=pathMSBlog+"/"+FileListe.get(i);
        Toast toast=Toast.makeText(context,path,Toast.LENGTH_LONG);
        toast.show();
    }

    public String getBase(int i){
        String base=FileListe.get(i).replace(".txt","");
        return base;
    }

    public String getTxt(int i){
        String path=pathMSBlog+"/"+FileListe.get(i);
        return path;
    }

    public String getCsv(int i) {
        long modF=0l;
        long modD=0l;
        String name=FileListe.get(i).replace(".txt","f.csv");
        String pathf=pathMSBlog+"/"+name;
        File csvf=new File(pathf);
        if (csvf.exists()) modF=csvf.lastModified();
        else pathf=null;
        name=FileListe.get(i).replace(".txt","d.csv");
        String pathd=pathMSBlog+"/"+name;
        File csvd=new File(pathd);
        if (csvd.exists()) {
            modD=csvd.lastModified();
            if (modD>modF) return pathd;
            return pathf;
        } else return pathf;
    }

    public String getHtml(int i) {
        String name=FileListe.get(i).replace(".txt",".html");
        String path=pathMSBlog+"/"+name;
        File html=new File(path);
        if (html.exists()) { return path; }
        return null;
    }

    public String getGpx(int i) {
        String name=FileListe.get(i).replace(".txt",".gpx");
        String path=pathMSBlog+"/"+name;
        File gpx=new File(path);
        if (gpx.exists()) { return path; }
        return null;
    }

    public String getKml(int i) {
        String name=FileListe.get(i).replace(".txt",".kml");
        String path=pathMSBlog+"/"+name;
        File kml=new File(path);
        if (kml.exists()) { return path; }
        return null;
    }
}
