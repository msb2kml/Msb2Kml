package org.js.msb2kml.FileSelect;

import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by js on 2/13/17.
 */

public class navDir {

    private String curDir="/";
    private Pattern patrn=null;
    private Boolean noDir=true;


    public void setCurDir(String dir){
        curDir=dir;
    }

    public String getDir(){
        return curDir;
    }

    public String upDir(){
        File dir=new File(curDir);
        curDir=dir.getParent();
        return curDir;
    }

    public String dnDir(String down){
        if (!curDir.equals("/")) {
            curDir+="/";
        }
        if (down.endsWith("/")){
            curDir+=down.substring(0,down.length()-1);
        } else curDir+=down;
        return curDir;
    }

    public Boolean setMask(String m){
        if (m==null){
            patrn=null;
            return true;
        }
        try {
            patrn=Pattern.compile(m);
            return true;
        } catch (PatternSyntaxException e) {
            patrn=null;
            return false;
        }
    }

    public void setNoDir(Boolean n){
        noDir=n;
    }

    public String[] get(){
        File dir=new File(curDir);
        if (!dir.exists() || !dir.isDirectory()) {
            curDir="/";
            dir=new File(curDir);
        }
        ArrayList <String> directories=new ArrayList<String>();
        ArrayList <String> files=new ArrayList<String>();
        directories.add("../");
        String s[]=dir.list();
        if (s!=null && s.length>0){
            Arrays.sort(s);
            for (int i=0;i<s.length;i++){
                File f=new File(curDir+"/"+s[i]);
                if (f.isDirectory()){
                  if (f.canRead()) directories.add(s[i]+"/");
                } else {
                    if (patrn==null) files.add(s[i]);
                    else {
                        if (patrn.matcher(s[i]).matches()) files.add(s[i]);

                    }
                }
            }
        }
        ArrayList <String> all=new ArrayList<String>();
        all.addAll(directories);
        all.addAll(files);
        String[] ar=all.toArray(new String[0]);
        return ar;
    }
}