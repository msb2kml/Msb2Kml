package org.js.msb2kml.Work;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by js on 3/3/17.
 */

class tool {


    fileProcess fp;

    fileProcess.Var args[];
    char letter[];
    Character label=null;

    public float compute(){
        return 0.0f;
    }

    public boolean check(fileProcess f, String fields[], Character l){
        return false;
    }

    public boolean checkMore(){
        return false;
    }
}

class MEM extends tool{

    public float compute(){
        float x=((ArrayList<Float>)args[0].thing).get(args[0].index);
        return x;
    }

    public boolean check(fileProcess f, String fields[], Character l){
        fp=f;
        if (l!=null) label=l;
        if (fields[1].length()>1 && fields[1].startsWith("$")){
            letter=new char[1];
            args=new fileProcess.Var[1];
            char c=fields[1].charAt(1);
            letter[0]=c;
            fileProcess.Var v=fp.getVar(c);
            if (v!=null){
                args[0]=v;
                return true;
            }
        }
        if (label!=null) fp.delVar(label);
        return false;
    }

    public boolean checkMore(){
        fileProcess.Var v=fp.getVar(letter[0]);
        if (v!=null) return true;
        if (label!=null) fp.delVar(label);
        return false;
    }
}

class DIF extends tool{

    public float compute(){
        float x=((ArrayList<Float>)args[0].thing).get(args[0].index);
        float y=((ArrayList<Float>)args[1].thing).get(args[1].index);
        return x-y;
    }

    public boolean check(fileProcess f, String fields[], Character l){
        if (l!=null) label=l;
        fp=f;
        if (fields.length<3) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[2];
        args=new fileProcess.Var[2];
        for (int i=1;i<3;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore(){
        fileProcess.Var v=fp.getVar(letter[0]);
        if (v==null){
            if (label!=null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u=fp.getVar(letter[1]);
        if (u!=null) return true;
        if (label!=null) fp.delVar(label);
        return false;
    }
}

class PROD extends tool {

    public float compute() {
        float x =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float y =((ArrayList<Float>)args[1].thing).get(args[1].index);
        return x * y;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length < 3) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[2];
        args=new fileProcess.Var[2];
        for (int i=1;i<3;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v = fp.getVar(letter[0]);
        if (v == null) {
            if (label != null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u = fp.getVar(letter[1]);
        if (u != null) return true;
        if (label != null) fp.delVar(label);
        return false;
    }
}

class TRV extends tool {

    public float compute() {
        float x =((ArrayList<Float>)args[1].thing).get(args[1].index);
        return x;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length < 3) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter = new char[2];
        args = new fileProcess.Var[2];
        for (int i=1;i<3;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v = fp.getVar(letter[0]);
        if (v == null) {
            if (label != null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u = fp.getVar(letter[1]);
        if (u != null) return true;
        if (label != null) fp.delVar(label);
        return false;
    }
}

class SMTH extends tool {

    Float pc=new Float(0f);
    Float rest=new Float(1f);

    public float compute() {
        float x =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float y =((ArrayList<Float>)args[1].thing).get(args[1].index);
        return (x*pc)+(y*rest);
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<4) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { pc=pc.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        if (pc<0.01f || pc>0.99f) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        rest=1f-pc;
        letter = new char[2];
        args = new fileProcess.Var[2];
        for (int i=2;i<4;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v = fp.getVar(letter[0]);
        if (v == null) {
            if (label != null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u = fp.getVar(letter[1]);
        if (u != null) return true;
        if (label != null) fp.delVar(label);
        return false;
    }
}

class CUMP extends tool {

    public float compute() {
        float x =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float y =((ArrayList<Float>)args[1].thing).get(args[1].index);
        if (x>0.0) return x+y;
        return y;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<3) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter = new char[2];
        args = new fileProcess.Var[2];
        for (int i=1;i<3;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v = fp.getVar(letter[0]);
        if (v == null) {
            if (label != null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u = fp.getVar(letter[1]);
        if (u != null) return true;
        if (label != null) fp.delVar(label);
        return false;
    }
}

class CUMN extends tool {

    public float compute() {
        float x =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float y =((ArrayList<Float>)args[1].thing).get(args[1].index);
        if (x<0.0) return y-x;
        return y;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<3) {
            if (label != null) fp.delVar(label);
            return false;
        }
        letter = new char[2];
        args = new fileProcess.Var[2];
        for (int i=1;i<3;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v = fp.getVar(letter[0]);
        if (v == null) {
            if (label != null) fp.delVar(label);
            return false;
        }
        fileProcess.Var u = fp.getVar(letter[1]);
        if (u != null) return true;
        if (label != null) fp.delVar(label);
        return false;
    }
}

class GLR extends tool {

    Float minDist=new Float(0.1);
    Float topHeight=new Float(0f);
    Float topDist=new Float(0f);
    Float lastHeight=new Float(0f);
    Float glideRatio=new Float(0f);
    Float lastDist=new Float(0f);

    public float compute(){
        float dist=((ArrayList<Float>) args[0].thing).get(args[0].index);
        float height=((ArrayList<Float>) args[1].thing).get(args[1].index);
        if (dist-lastDist<0.03f) return glideRatio;
        lastDist=dist;
        float diffDist=dist-topDist;
        if (height>=lastHeight){
            topHeight=height;
            topDist=dist;
            lastHeight=height;
            glideRatio=0f;
            return glideRatio;
        }
        lastHeight=height;
        if (diffDist<minDist) glideRatio=0f;
        else glideRatio=diffDist*1000f/(topHeight-height);
        return glideRatio;
    }

    public boolean check(fileProcess f, String fields[], Character l){
        fp=f;
        if (l != null) label = l;
        if (fields.length<4) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { minDist = minDist.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        if (minDist<0.03f) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[2];
        args=new fileProcess.Var[2];
        for (int i=2;i<4;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore(){
        fileProcess.Var v;
        for (int i=0;i<2;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

}

class NRJ extends tool {

    public float compute() {
        float watt =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float deltaT=((ArrayList<Float>)args[1].thing).get(args[1].index);
        float energy=((ArrayList<Float>)args[2].thing).get(args[2].index);
        return energy+watt*deltaT/60.0f;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<4) {
            if (label != null) fp.delVar(label);
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=1;i<4;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        for (int i=0;i<3;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }
}

class MOT extends tool {

    Float tresh=new Float(0f);

    public float compute() {
        float current =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float deltaT=((ArrayList<Float>)args[1].thing).get(args[1].index);
        float mot=((ArrayList<Float>)args[2].thing).get(args[2].index);
        if (current>tresh) return mot+deltaT;
        return mot;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<5) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { tresh=tresh.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=2;i<5;i++){
            if (fields[i].length()<2 ||!fields[i].startsWith("$")) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        for (int i=0;i<3;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }
}

class COL extends tool {

    Float offset=new Float(0f);
    Float factor=new Float(1f);

    public float compute() {
        float vario =((ArrayList<Float>)args[0].thing).get(args[0].index);
        return (vario-offset)*factor;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        if (fields.length<4) return false;
        fp = f;
        if (l != null) label = l;
        if (fields.length<4) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        Float min=new Float(0f);
        Float max=new Float(100f);
        try { min=min.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        try {
            max = max.parseFloat(fields[2]);
        } catch (NumberFormatException e) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        if (min.compareTo(max)>=0) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[1];
        args=new fileProcess.Var[1];
        if (fields[3].length()<2 || !fields[3].startsWith("$")) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter[0]=fields[3].charAt(1);
        args[0]=fp.getVar(letter[0]);
        if (args[0]==null) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        offset=min;
        factor=100f/(max-min);
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        v=fp.getVar(letter[0]);
        if (v==null){
            if (label != null) fp.delVar(label);
            return false;
        }
        return true;
    }
}

class SOA extends tool {

    Float tresh=new Float(0f);

    public float compute() {
        float current =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float deltaT=((ArrayList<Float>)args[1].thing).get(args[1].index);
        float soa=((ArrayList<Float>)args[2].thing).get(args[2].index);
        if (current<tresh) return soa+deltaT;
        return soa;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<5) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { tresh=tresh.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=2;i<5;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        for (int i=0;i<3;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }
}

class HVL extends tool {

    Float tresh=new Float(0f);

    public float compute() {
        float current =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float volt=((ArrayList<Float>)args[1].thing).get(args[1].index);
        float voltH=((ArrayList<Float>)args[2].thing).get(args[2].index);
        if (current<tresh) return volt;
        return voltH;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<5){
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { tresh=tresh.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=2;i<5;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$") ) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        for (int i=0;i<3;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }
}

class BIR extends tool {

    Float tresh=new Float(0f);

    public float compute() {
        ArrayList<Float> zz = (ArrayList<Float>) args[0].thing;
        float current =((ArrayList<Float>)args[0].thing).get(args[0].index);
        float voltH=((ArrayList<Float>)args[1].thing).get(args[1].index);
        float volt=((ArrayList<Float>)args[2].thing).get(args[2].index);
        if (current<tresh) return 0f;
        return 1000f*(voltH-volt)/current;
    }

    public boolean check(fileProcess f, String fields[], Character l) {
        fp = f;
        if (l != null) label = l;
        if (fields.length<5) {
            if (label!=null) fp.delVar(label);
            return false;
        }
        try { tresh=tresh.parseFloat(fields[1]);
        } catch (NumberFormatException e){
            if (label!=null) fp.delVar(label);
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=2;i<5;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")) {
                if (label!=null) fp.delVar(label);
                return false;
            }
            letter[i-2]=fields[i].charAt(1);
            args[i-2]=fp.getVar(letter[i-2]);
            if (args[i-2]==null) {
                if (label!=null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }

    public boolean checkMore() {
        fileProcess.Var v;
        for (int i=0;i<3;i++){
            v=fp.getVar(letter[i]);
            if (v==null){
                if (label != null) fp.delVar(label);
                return false;
            }
        }
        return true;
    }
}

class GPS extends tool{

    public float compute(){
        Float azimuth=((ArrayList<Float>)args[0].thing).get(args[0].index);
        Float distance=((ArrayList<Float>)args[1].thing).get(args[1].index);
        Float altitude=((ArrayList<Float>)args[2].thing).get(args[2].index);
        Location loc=fp.haver.invHaver(fp.startLoc,distance,azimuth,altitude);
        Exception e=fp.addPoint(loc);
        if (e==null) return 1.0f;
        else return 0.0f;
    }

    public boolean check(fileProcess f, String fields[], Character l){
        if (l!=null) label=l;
        fp=f;
        if (fields.length<4 || fp.startLoc==null){
            if (label!=null) fp.delVar(label);
            fp.startLoc=null;
            return false;
        }
        letter=new char[3];
        args=new fileProcess.Var[3];
        for (int i=1;i<4;i++){
            if (fields[i].length()<2 || !fields[i].startsWith("$")){
               if (label!=null) fp.delVar(label);
               fp.startLoc=null;
               return false;
            }
            letter[i-1]=fields[i].charAt(1);
            args[i-1]=fp.getVar(letter[i-1]);
            if (args[i-1]==null){
                if (label!=null) fp.delVar(label);
                fp.startLoc=null;
                return false;
            }
        }
        return true;
    }

    public boolean checkMore(){
        fileProcess.Var Azim=fp.getVar(letter[0]);
        fileProcess.Var Dist=fp.getVar(letter[1]);
        fileProcess.Var Alti=fp.getVar(letter[2]);
        if (fp.startLoc==null || Azim==null || Dist==null || Alti==null){
            if (label!=null) fp.delVar(label);
            fp.startLoc=null;
            return false;
        }
        return true;
    }
}

class tb{

    public tool toolBox(fileProcess f, String expr, Character l){
        String fields[]=expr.split(",");
        if (fields.length<2) return null;
        if (fields[0].matches("=MEM")){
            tool t=new MEM();
            if (t.check(f,fields,l)) return t;
            return null;
        } else if (fields[0].matches("=DIF")){
            tool t=new DIF();
            if (t.check(f,fields,l)) return t;
            return null;
        } else if (fields[0].matches("=PROD")) {
            tool t = new PROD();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=NRJ")){
            tool t=new NRJ();
            if (t.check(f, fields,l)) return t;
            return null;
        } else if (fields[0].matches("=SMTH")) {
            tool t = new SMTH();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=CUMP")) {
            tool t = new CUMP();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=CUMN")) {
            tool t = new CUMN();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=GLR")) {
            tool t=new GLR();
            if (t.check(f,fields,l)) return t;
            return null;
        } else if (fields[0].matches("=MOT")) {
            tool t = new MOT();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=SOA")) {
            tool t = new SOA();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=HVL")) {
            tool t = new HVL();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=BIR")) {
            tool t = new BIR();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=TRV")) {
            tool t = new TRV();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=COL")) {
            tool t = new COL();
            if (t.check(f, fields, l)) return t;
            return null;
        } else if (fields[0].matches("=GPS")){
            tool t=new GPS();
            if (t.check(f,fields,l)) return t;
            return null;
        } else return null;
    }
}
