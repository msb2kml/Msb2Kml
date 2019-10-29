package org.js.msb2kml.DisplayLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.js.msb2kml.Common.listing;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Vtrk extends AppCompatActivity {

    Context context;
    String MsbName=null;
    String pathMSBlog;
    metaData m;
    listing l;
    Track track=null;
    Csv csv=null;
    TextView tTitle;
    CheckBox ckSG;
    Button bcolBy;
    EditText etBlue;
    EditText etRed;
    TextView tTime;
    ProgressBar pBar;
    Button bEntire;
    RadioGroup rSpeed;
    RadioButton sp1;
    RadioButton sp2;
    RadioButton sp10;
    Button bSkp0;
    Button bSkp2;
    Button bSkp10;
    Button bStop;

    Integer[] lineColor={ Color.rgb(0x00,0x00,0xFF),
                          Color.rgb(0x00,0x63,0xF3),
                          Color.rgb(0x00,0x92,0xDE),
                          Color.rgb(0x00,0xB7,0xC2),
                          Color.rgb(0x00,0xD6,0xA0),
                          Color.rgb(0x54,0xDD,0x74),
                          Color.rgb(0x85,0xE0,0x46),
                          Color.rgb(0xAD,0xE1,0x00),
                          Color.rgb(0xD9,0xC6,0x00),
                          Color.rgb(0xFF,0xA5,0x00),
                          Color.rgb(0xFF,0x78,0x00),
                          Color.rgb(0xFF,0x00,0x00)};
    int nColor=lineColor.length;

    String gpxPath;
    String csvPath;
    Intent intentMap=null;
    Double zoom=17.0;
    Boolean wthStart=false;
    Long size;
    boolean running=false;
    Long lastDis=null;
    Long lastTrk=null;
    Long divisor=1L;
    Long toSkip=0L;
    Long startTime=null;
    Calendar startCal=null;
    Boolean runningMap=false;
    Boolean setStart=true;
    Location startLoc=null;
    Double prevAlt=null;
    Location dispLoc=null;
    Boolean Tail=true;
    Double minVal=null;
    Double maxVal=null;
    Integer nbCol;
    Integer srcCol=null;
    Float valBlue=null;
    Float valRed=null;
    ArrayList<String> colHead=new ArrayList<>();
    ArrayList<String> colMin=new ArrayList<>();
    ArrayList<String> colMax=new ArrayList<>();
    String patrnExtrm="^([^:]+):{1}([^;]+);{1}([^;]+)$";
    IntentFilter filter=new IntentFilter("org.js.ACK");
    private Handler mHandler=new Handler();
    private Runnable timerTask=new Runnable() {
        @Override
        public void run() {
            Vnext();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vtrk);
        this.setTitle(getString(R.string.app_name)+": GPX map");
        context=getApplicationContext();
        PackageManager Pm=getPackageManager();
        List<PackageInfo> allPack=Pm.getInstalledPackages(0);
        for (PackageInfo AI :allPack) {
            String zz=AI.packageName;
            if (zz.matches("org.js.Msb2Map")){
                intentMap=Pm.getLaunchIntentForPackage(zz);
                break;
            }
        }
        if (intentMap==null){
            Toast.makeText(context,"Missing Msb2Map application",Toast.LENGTH_LONG).show();
            finish();
        }
        Intent intent=getIntent();
        MsbName=intent.getStringExtra("MsbName");
        pathMSBlog=intent.getStringExtra("MSBlog");
        m=new metaData(pathMSBlog);
        if (MsbName == null) finish();
        l=new listing();
        l.set(context,pathMSBlog);
        l.unique(MsbName);
        m.fetchPref(context);
        if (!m.extract(context, MsbName)) finish();
        gpxPath=m.getPathGpx();
        csvPath=l.getCsv(0);
        startCal=m.getStartTime();
        tTitle=(TextView) findViewById(R.id.title_vt);
        ckSG=(CheckBox) findViewById(R.id.ckSG_vt);
        bcolBy=(Button) findViewById(R.id.colBy_vt);
        etBlue=(EditText) findViewById(R.id.blueVal_vt);
        Integer signNum=InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|
                InputType.TYPE_NUMBER_FLAG_SIGNED;
        etBlue.setInputType(signNum);
        etRed=(EditText) findViewById(R.id.redVal_vt);
        etRed.setInputType(signNum);
        tTime=(TextView) findViewById(R.id.timeTrack_vt);
        pBar=(ProgressBar) findViewById(R.id.progress_vt);
        bEntire=(Button) findViewById(R.id.entire);
        bSkp0=(Button) findViewById(R.id.skp0);
        bSkp2=(Button) findViewById(R.id.skp2);
        bSkp10=(Button) findViewById(R.id.skp10);
        rSpeed=(RadioGroup) findViewById(R.id.speed);
        sp1=(RadioButton) findViewById(R.id.sp1);
        sp2=(RadioButton) findViewById(R.id.sp2);
        sp10=(RadioButton) findViewById(R.id.sp10);
        String line=MsbName+" / "+m.getDay()+" / "+m.getPlane()+" / "+m.getComment();
        tTitle.setText(line);
        colHead.add("- (none)");
        colMin.add("0.0");
        colMax.add("0.0");
        ArrayList<String> extrmString=m.getExtrmString();
        if (extrmString==null){
            nbCol=null;
            bcolBy.setEnabled(false);
        } else {
            nbCol = extrmString.size();
            Pattern pExtrm=Pattern.compile(patrnExtrm);
            for (int i=0;i<nbCol;i++){
                Matcher ma=pExtrm.matcher(extrmString.get(i));
                if (ma.find()){
                    colHead.add(ma.group(1).trim());
                    colMin.add(ma.group(2).trim());
                    colMax.add(ma.group(3).trim());
                }
            }
            bcolBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectCol();
                }
            });
        }
        etBlue.setEnabled(false);
        etRed.setEnabled(false);
        bStop=(Button) findViewById(R.id.stop_vt);
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bEntire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entireTrack();
            }
        });
        bSkp0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running=true;
                getSpeed();
                toSkip=0L;
                mHandler.postDelayed(timerTask,300L);
            }
        });
        bSkp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running=true;
                getSpeed();
                toSkip=120000L;
                mHandler.postDelayed(timerTask,300L);
            }
        });
        bSkp10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running=true;
                getSpeed();
                toSkip=600000L;
                mHandler.postDelayed(timerTask,300L);
            }
        });
    }

    @Override
    protected void onResume(){

        super.onResume();
        fromMap();
    }

    void selectCol(){
        String[] head=colHead.toArray(new String[0]);
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle("Select the column for coloring the track")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setColrz(null);
                    }
                })
           .setItems(head, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setColrz(which);
                    }
                });
        build.show();
    }

    void setColrz(Integer which){
        if (which==null) return;
        bcolBy.setText("Color by: "+colHead.get(which));
        etBlue.setText(colMin.get(which));
        etRed.setText(colMax.get(which));
        if (which==0){
            etBlue.setEnabled(false);
            etRed.setEnabled(false);
            srcCol=null;
        } else {
            etBlue.setEnabled(true);
            etRed.setEnabled(true);
            srcCol=which-1;
        }
    }

    public Location readTrk(){
        Location loc=null;
        Long position;
        if (track==null) return null;
        loc=track.nextPt();
        if (loc==null) return loc;
        position = track.getPos();
        Float prog = (100.0f * Float.valueOf(position)) / Float.valueOf(size);
        pBar.setProgress(prog.intValue());
        if (startTime!=null) {
            Long sec = (loc.getTime() - startTime) / 1000L;
            Long hour=sec/3600L;
            Long min=(sec-hour*3600L)/60L;
            Long s=(sec-hour*3600L-min*60L);
            tTime.setText(String.format("%02d:%02d:%02d",hour,min,s));
        }
        return loc;
    }

    public void getSpeed(){
        int id=rSpeed.getCheckedRadioButtonId();
        switch (id){
            case R.id.sp1:
                divisor=1l;
                break;
            case R.id.sp2:
                divisor=2l;
                break;
            case R.id.sp10:
                divisor=10l;
                break;
        }
    }

    Boolean getValCol(){
        String field;
        NumberFormat nfe=NumberFormat.getInstance(Locale.ENGLISH);
        Number num;
        valBlue=null;
        valRed=null;
        field=etBlue.getText().toString();
        if (field!=null) field=field.trim();
        if (field==null || field.isEmpty()){ return false; }
        try {
            num=nfe.parse(field);
            valBlue=num.floatValue();
        } catch (ParseException e) {return false;}
        field=etRed.getText().toString();
        if (field!=null) field=field.trim();
        if (field==null || field.isEmpty()){ return false; }
        try {
            num=nfe.parse(field);
            valRed=num.floatValue();
        } catch (ParseException e) {return false; }
        if (Math.abs(valBlue-valRed)<0.001f) return false;
        return true;
    }

    int colorz(Float val){
        if (val==null) return Color.BLACK;
        float norm=(val-valBlue)/(valRed-valBlue);
        int v=Math.round(norm*nColor);
        v=Math.max(1,Math.min(nColor,v))-1;
        return lineColor[v];
    }

    public Location skip(Location currentLoc){
        Location loc;
        if (toSkip==0l) return currentLoc;
        loc=currentLoc;
        Long target=currentLoc.getTime()+toSkip;
        toSkip=0L;
        while (loc.getTime()<target){
            loc=readTrk();
            if (loc==null) return loc;
        }
        return loc;
    }

    public void Vnext(){
        String bubbleMap;
        if (!Tail){
            fillTrack();
            return;
        }
        if (!running) return;
        Long now=System.currentTimeMillis();
        if (track==null){

            track=new Track();
            size=track.open(gpxPath);
            dispLoc=readTrk();
            if (dispLoc==null){
                eof();
                finish();
                return;
            } else {
                startLoc = dispLoc;
                startTime = startLoc.getTime();
                prevAlt = null;
                if (csv==null) {
                    minVal = dispLoc.getAltitude();
                    maxVal = minVal;
                } else {
                   Float val=csv.nextPt(startTime,srcCol);
                   if (val!=null) {
                       minVal = val.doubleValue();
                       maxVal = minVal;
                   }
                }
            }
        }
        if (srcCol!=null){
            if (!getValCol()){
                Toast.makeText(context,"Please check the Blue and Red values.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (csv!=null) csv.close();
            csv=new Csv();
            Integer nFields=csv.open(csvPath,startCal);
            if (nFields==null || srcCol>=nFields){
                Toast.makeText(context,"CSV file not available.",
                        Toast.LENGTH_LONG).show();
                csv.close();
                csv=null;
            }
        } else {
            if (csv!=null) csv.close();
            csv=null;
        }
        if (!runningMap){
            Intent nt=(Intent) intentMap.clone();
            nt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            nt.putExtra("CALLER",context.getString(R.string.app_name));
            wthStart=ckSG.isChecked();
            setStart=!wthStart;
            nt.putExtra("CENTER",startLoc);
            nt.putExtra("StartGPS",wthStart);
            nt.putExtra("Tail",Tail);
            if (zoom!=null) nt.putExtra("ZOOM",zoom);
            runningMap=true;
            zoom=null;
            startActivity(nt);
            if (toSkip>0){
                dispLoc=skip(dispLoc);
                if (dispLoc==null){
                    eof();
                    return;
                }
                prevAlt=null;
            }
            registerReceiver(mReceiver,filter);
            return;
        }
        Double alt=dispLoc.getAltitude();
        Intent nt=new Intent();
        nt.setAction("org.js.LOC");
        nt.putExtra("LOC",dispLoc);
        if (csv!=null){
            Long sometime=dispLoc.getTime();
            Float val=csv.nextPt(sometime,srcCol);
            if (val!=null) {
                if (minVal==null){
                    minVal=val.doubleValue();
                    maxVal=minVal;
                } else {
                    minVal = Math.min(minVal, val.doubleValue());
                    maxVal = Math.max(maxVal, val.doubleValue());
                }
                Integer col = colorz(val);
                nt.putExtra("COLOR", col);
                bubbleMap = String.format(Locale.ENGLISH, "%s %.1f",
                        colHead.get(srcCol + 1), val);
            } else {
                bubbleMap=" - ";
            }
        } else {
            minVal=Math.min(minVal,alt);
            maxVal=Math.max(maxVal,alt);
            bubbleMap=String.format(Locale.ENGLISH,"Alt %.1f",alt);
            if (prevAlt == null || alt > prevAlt) {
                nt.putExtra("COLOR", Color.rgb(0xFF, 0x00, 0x00));
            } else {
                nt.putExtra("COLOR", Color.rgb(0x00, 0x00, 0xFF));
            }
        }
        nt.putExtra("BUBBLE",bubbleMap);
        prevAlt=alt;
        sendBroadcast(nt);
        if (setStart){
            nt=new Intent();
            nt.setAction("org.js.LOC");
            nt.putExtra("WPT",startLoc);
            nt.putExtra("WPT_NAME",MsbName);
            sendBroadcast(nt);
            setStart=false;
        }
        lastDis=now;
        lastTrk=dispLoc.getTime();
        Long toWait=0L;
        while (toWait<300L){
            dispLoc=readTrk();
            if (dispLoc==null){
                eof();
                return;
            }
            toWait=(dispLoc.getTime()-lastTrk)/divisor;
        }
        mHandler.postDelayed(timerTask,toWait);
    }


    public void fromMap(){
        if (runningMap) {
            running = false;
            runningMap = false;
//            Toast.makeText(context, "Return from map", Toast.LENGTH_LONG).show();
        }
    }

    void eof(){
        track.close();
        running=false;
        Toast.makeText(context,"END OF FILE",Toast.LENGTH_LONG).show();
        bSkp0.setEnabled(false);
        bSkp2.setEnabled(false);
        bSkp10.setEnabled(false);
        track=null;
        if (csv!=null) {
            csv.close();
            csv = null;
        }
    }

    void entireTrack(){
        if (srcCol!=null){
            if (!getValCol()){
                Toast.makeText(context,"Please check the Blue and Red values.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (csv!=null) csv.close();
            csv=new Csv();
            Integer nFields=csv.open(csvPath,startCal);
            if (nFields==null || srcCol>=nFields){
                Toast.makeText(context,"CSV file not available.",
                        Toast.LENGTH_LONG).show();
                csv.close();
                csv=null;
            }
        } else {
            if (csv!=null) csv.close();
            csv=null;
        }
        if (track!=null) track.close();
        track=new Track();
        size=track.open(gpxPath);
        dispLoc=readTrk();
        if (dispLoc==null){
            eof();
            bEntire.setEnabled(false);
            finish();
            return;
        } else {
            startLoc = dispLoc;
            startTime = startLoc.getTime();
            prevAlt = null;
            if (csv==null) {
                minVal = dispLoc.getAltitude();
                maxVal = minVal;
            } else {
                Float val=csv.nextPt(startTime,srcCol);
                if (val!=null) {
                    minVal = val.doubleValue();
                    maxVal = minVal;
                }
            }
        }
        Tail=false;
        Intent nt=(Intent) intentMap.clone();
        nt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        nt.putExtra("CALLER",context.getString(R.string.app_name));
        nt.putExtra("CENTER",startLoc);
        wthStart=ckSG.isChecked();
        setStart=!wthStart;
        nt.putExtra("StartGPS",wthStart);
        nt.putExtra("Tail",Tail);
        if (zoom!=null) nt.putExtra("ZOOM",zoom);
        runningMap=true;
        zoom=null;
        startActivity(nt);
        registerReceiver(mReceiver,filter);
    }

    void fillTrack(){
        Intent nt;
        String bubbleMap;

        while (dispLoc!=null) {
            dispLoc = readTrk();
            if (dispLoc == null) {
                if (setStart){
                    nt=new Intent();
                    nt.setAction("org.js.LOC");
                    nt.putExtra("WPT",startLoc);
                    nt.putExtra("WPT_NAME",MsbName);
                    sendBroadcast(nt);
                    setStart=false;
                }
                eof();
                bSkp0.setEnabled(true);
                bSkp2.setEnabled(true);
                bSkp10.setEnabled(true);
                Tail=true;
                return;
            }
            Double alt = dispLoc.getAltitude();
            nt = new Intent();
            nt.setAction("org.js.LOC");
            nt.putExtra("LOC", dispLoc);
            if (csv!=null){
                Long sometime=dispLoc.getTime();
                Float val=csv.nextPt(sometime,srcCol);
                if (val==null){
                    nt.putExtra("COLOR",Color.BLACK);
                    bubbleMap=" - ";
                } else {
                    if (minVal==null) {
                        minVal=val.doubleValue();
                        maxVal=minVal;
                    } else {
                        minVal = Math.min(minVal, val.doubleValue());
                        maxVal = Math.max(maxVal, val.doubleValue());
                    }
                    Integer col = colorz(val);
                    nt.putExtra("COLOR", col);
                    bubbleMap=String.format(Locale.ENGLISH,"%s %.1f to %.1f",
                            colHead.get(srcCol+1),minVal,maxVal);
                }
            } else {
                minVal=Math.min(minVal,alt);
                maxVal=Math.max(maxVal,alt);
                bubbleMap=String.format(Locale.ENGLISH,"Alt %.1f to %.1f",minVal,maxVal);
                if (prevAlt == null || alt > prevAlt) {
                    nt.putExtra("COLOR", Color.rgb(0xFF, 0x00, 0x00));
                } else {
                    nt.putExtra("COLOR", Color.rgb(0x00, 0x00, 0xFF));
                }
            }
            nt.putExtra("BUBBLE", bubbleMap);
            prevAlt = alt;
            sendBroadcast(nt);
        }
    }

    private final BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String origin=intent.getStringExtra("NAME");
            unregisterReceiver(mReceiver);
            Vnext();
        }
    };




}
