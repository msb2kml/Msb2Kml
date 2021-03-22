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
import org.js.msb2kml.FileSelect.Selector;
import org.js.msb2kml.R;

import java.io.File;
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
    Button bRef;
    Button bcolBy;
    EditText etBlue;
    EditText etRed;
    TextView tTime;
    ProgressBar pBar;
    Button bEntire;
    CheckBox ckUp;
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
    int HalfMagenta=Color.argb(0x80,0xFF,0x00,0xFF);

    String gpxPath;
    String csvPath;
    String pathStartGps;
    String refPath =null;
    String refDirectory =null;
    Intent intentMap=null;
    Double zoom=17.0;
    Boolean rotMap=false;
    Location arrowOrg=null;
    Long size;
    boolean running=false;
    Long lastTrk=null;
    Long divisor=1L;
    Long toSkip=0L;
    Long startTime=null;
    Calendar startCal=null;
    Boolean runningMap=false;
    Boolean waitMap=false;
    Boolean setStart=true;
    Location startLoc=null;
    Double prevAlt=null;
    Location dispLoc=null;
    Boolean inRef =false;
    Track.enttGpx curEntity= Track.enttGpx.ALIEN;
    String curEntName=null;
    String fileName=null;
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

    class SaveGpx {
        Track track=null;
        Location dispLoc=null;
        Location startLoc=null;
        Track.enttGpx curEntity=null;
        String curEntName=null;
    }
    SaveGpx saved=null;

    private Handler mHandler=new Handler();
    private Runnable timerTask=new Runnable() {
        @Override
        public void run() {
            dispatch(2);
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
        refDirectory =pathMSBlog;
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
        pathStartGps=m.getPathStartGPS();
        tTitle=(TextView) findViewById(R.id.title_vt);
        bRef =(Button) findViewById(R.id.bRef);
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
        ckUp=(CheckBox) findViewById(R.id.upCheck);
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
//        if (refPath ==null) refPath =pathStartGps;
        if (refPath ==null) bRef.setText("-none-");
        else {
            String bGname = (new File(refPath).getName());
            bRef.setText(bGname);
        }
        bRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectRef();
            }
        });

        bEntire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settings()) return;
                Tail=false;
                if (track!=null){
                    track.close();
                    track=null;
                }
                running=true;
                dispatch(0);
            }
        });
        bSkp0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settings()) return;
                getSpeed();
                Tail=true;
                running=true;
                toSkip=0L;
                mHandler.postDelayed(timerTask,300L);
            }
        });
        bSkp2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settings()) return;
                getSpeed();
                Tail=true;
                running=true;
                toSkip=120000L;
                mHandler.postDelayed(timerTask,300L);
            }
        });
        bSkp10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!settings()) return;
                getSpeed();
                Tail=true;
                running=true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case 2:
                if (resultCode==RESULT_OK){
                    refPath =data.getStringExtra("Path");
                    if (refPath ==null || refPath.isEmpty()) refPath =null;
                } else refPath =null;
                if (refPath ==null) bRef.setText("-none-");
                else {
                    File f=new File(refPath);
                    String bGname = f.getName();
                    refDirectory=f.getParent();
                    bRef.setText(bGname);
                }
                break;
        }
    }

    void selectRef(){
        Intent intent=new Intent(Vtrk.this, Selector.class);
        if (refDirectory !=null) intent.putExtra("CurrentDir", refDirectory);
        intent.putExtra("WithDir",false);
        intent.putExtra("Mask","(?i).+\\.gpx");
        intent.putExtra("Title","Reference GPX?      ");
        if (refPath !=null) intent.putExtra("Previous", refPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,2);
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

    Boolean settings(){
        if (srcCol!=null){
            if (!getValCol()){
                Toast.makeText(context,"Please check the Blue and Red values.",
                        Toast.LENGTH_LONG).show();
                return false;
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
        return true;
    }

    Location initTrack(){
        Location firstLoc=null;
        if (track!=null) track.close();
        track=new Track();
        size=track.open(gpxPath);
        firstLoc=readTrk();
        if (firstLoc==null){
            Toast.makeText(context,"No valid item in "+gpxPath,Toast.LENGTH_LONG).show();
        }
        minVal=null;
        maxVal=null;
        setStart=true;
        return firstLoc;
    }

    public Location readTrk(){
        Location loc=null;
        Long position;
        Track.enttGpx entity=null;
        String name;
        String entName;
        if (track==null) return null;
        while (true) {
            loc = track.nextPt();
            if (loc == null) return loc;
            position = track.getPos();
            Float prog = (100.0f * Float.valueOf(position)) / Float.valueOf(size);
            pBar.setProgress(prog.intValue());
            if (startTime != null && startTime>0L && loc.getTime() != 0L) {
                Long sec = (loc.getTime() - startTime) / 1000L;
                Long hour = sec / 3600L;
                Long min = (sec - hour * 3600L) / 60L;
                Long s = (sec - hour * 3600L - min * 60L);
                tTime.setText(String.format("%02d:%02d:%02d", hour, min, s));
            } else tTime.setText("0");
            return loc;
        }
    }

    public void getSpeed(){
        int id=rSpeed.getCheckedRadioButtonId();
        rotMap=ckUp.isChecked();
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
        if (toSkip==0l || currentLoc.getTime()==0L) return currentLoc;
        loc=currentLoc;
        Long target=currentLoc.getTime()+toSkip;
        while (loc.getTime()<target){
            loc=readTrk();
            if (loc==null ||
            loc.getExtras().getSerializable("ENTITY")!=Track.enttGpx.TRKWPT){
                toSkip=0L;
                return loc;
            }
        }
        toSkip=0L;
        return loc;
    }


    public void fromMap(){
        if (runningMap) {
            running = false;
            runningMap = false;
        }
    }

    void eof(){
        if (inRef && saved!=null){
            track.close();
            track=saved.track;
            dispLoc=saved.dispLoc;
            curEntity=saved.curEntity;
            startLoc=saved.startLoc;
            curEntName=saved.curEntName;
            saved=null;
            inRef =false;
            setStart=true;
            dispatch(5);
        } else {
            track.close();
            running = false;
            Toast.makeText(context, "END OF FILE", Toast.LENGTH_LONG).show();
            track = null;
            if (csv != null) {
                csv.close();
                csv = null;
            }
        }
    }

    void launchMap(Location centerLoc){
        Intent nt=(Intent) intentMap.clone();
        nt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        nt.putExtra("CALLER",context.getString(R.string.app_name));
        nt.putExtra("CENTER",centerLoc);
        nt.putExtra("StartGPS",false);
        nt.putExtra("Tail",Tail);
        if (zoom!=null) nt.putExtra("ZOOM",zoom);
        zoom=null;
        runningMap=true;
        startActivity(nt);
        waitMap=true;
        registerReceiver(mReceiver,filter);
        setStart=true;
        minVal=null;
        maxVal=null;
        return;
    }

    private final BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!waitMap) return;
            String origin=intent.getStringExtra("NAME");
            int vc=intent.getIntExtra("VERSION",0);
            unregisterReceiver(mReceiver);
            ckVcMap(vc);
            waitMap=false;
            if (refPath ==null) {
                dispatch(2);
            } else {
                setRef();
            }
        }
    };

    void ckVcMap(int vc){
        if (vc<17){
            Toast.makeText(context,"Msb2Map revision should be at least 1.7",
                    Toast.LENGTH_LONG).show();
        }
    }

    void setRef(){
        Location firstLoc=null;
        Long sizeRef=null;
        saved=new SaveGpx();
        saved.track=track;
        saved.dispLoc=dispLoc;
        saved.curEntity=curEntity;
        saved.startLoc=startLoc;
        saved.curEntName=curEntName;
        track=new Track();
        sizeRef=track.open(refPath);
        firstLoc=readTrk();
        if (firstLoc==null){
            Toast.makeText(context,"No valid item in "+ refPath,Toast.LENGTH_LONG).show();
            track.close();
            track=saved.track;
            dispLoc=saved.dispLoc;
            curEntity=saved.curEntity;
            startLoc=saved.startLoc;
            curEntName=saved.curEntName;
            saved=null;
            inRef =false;
            setStart=true;
            dispatch(5);
        } else {
            dispLoc=firstLoc;
            inRef =true;
            dispatch(6);
        }
    }

    void dispatch(int from){
        if (!running) return;
        if (track==null){
            dispLoc=initTrack();
            if (dispLoc==null){
                eof();
                return;
            }
        }
        while (dispLoc!=null){
            Track.enttGpx entity=(Track.enttGpx) dispLoc.getExtras().getSerializable("ENTITY");
            if (!inRef && Tail && (entity==Track.enttGpx.TRK || entity==Track.enttGpx.TRKWPT)){
                if (withTail()) return;
            } else {
                if (noTail()) return;
            }
        }
        eof();
    }

    Boolean noTail(){
        if (dispLoc==null) return true;
        Float val=null;
        int nbBroadcast=0;
        int typ=0;
        Track.enttGpx entity=(Track.enttGpx)dispLoc.getExtras().getSerializable("ENTITY");
        switch (entity){
            case WPT:
                curEntity=entity;
            case RTEWPT:
            case TRKWPT:
                if (!runningMap) {
                    launchMap(dispLoc);
                    return true;
                }
                break;
            case TRK:
                curEntName="Track "+dispLoc.getExtras().getString("name",null);
                setStart=true;
                startLoc=null;
                minVal=null;
                maxVal=null;
                curEntity=entity;
                dispLoc=readTrk();
                return false;
            case RTE:
                curEntName="Route "+dispLoc.getExtras().getString("name",null);
                setStart=true;
                startLoc=null;
                minVal=null;
                maxVal=null;
                curEntity=entity;
                dispLoc=readTrk();
                return false;
            case ALIEN:
                track.close();
                Toast.makeText(context,"Sorry, "+fileName+" is not compatible.",
                        Toast.LENGTH_LONG).show();
                eof();
                return true;
        }
        while (true){
            nbBroadcast++;
            if (nbBroadcast>100){
                mHandler.postDelayed(timerTask,100L);
                return true;
            }
            entity=(Track.enttGpx) dispLoc.getExtras().getSerializable("ENTITY");
            switch (curEntity){
                case WPT:
                    if (entity!=curEntity) return false;
                    if (inRef) typ=2;
                    else typ=0;
                    dispWpt(dispLoc, null,typ);
                    break;
                case TRKWPT:
                case RTEWPT:
                    if (entity!=curEntity) {
                        return false;
                    }
                    int color=Color.BLACK;
                    val=null;
                    String bubble=" - ";
                    if (inRef) color= HalfMagenta;
                    else {
                        if (csv != null) {
                            val = csv.nextPt(dispLoc.getTime(), srcCol);
                            if (val != null) {
                                color = colorz(val);
                                if (minVal == null) {
                                    minVal = val.doubleValue();
                                    maxVal = minVal;
                                } else {
                                    minVal = Math.min(minVal, val.doubleValue());
                                    maxVal = Math.max(maxVal, val.doubleValue());
                                }
                                bubble = String.format(Locale.ENGLISH, "%s %.1f to %.1f",
                                        colHead.get(srcCol + 1), minVal, maxVal);
                            }
                        } else {
                            Double alt = dispLoc.getAltitude();
                            if (prevAlt != null) {
                                if (alt > prevAlt) color = Color.RED;
                                else color = Color.BLUE;
                            }
                            prevAlt = alt;
                            if (minVal == null) {
                                minVal = alt;
                                maxVal = alt;
                            } else {
                                minVal = Math.min(minVal, alt);
                                maxVal = Math.max(maxVal, alt);
                            }
                            bubble = String.format(Locale.ENGLISH, "Alt. %.1f to %.1f",
                                    minVal, maxVal);
                        }
                    }
                    dispTrk(dispLoc,bubble,color,false,Tail);
                    break;
                case RTE:
                    if (entity!= Track.enttGpx.RTEWPT) return false;
                    if (setStart) {
                        if (startLoc == null) {
                            startLoc = dispLoc;
                            startLoc.getExtras().putString("name", curEntName);
                            if (!inRef) startTime = startLoc.getTime();
                            prevAlt = null;
                            if (!inRef) {
                                if (csv == null) {
                                    minVal = dispLoc.getAltitude();
                                    maxVal = minVal;
                                } else {
                                    val = csv.nextPt(startTime, srcCol);
                                    if (val != null) {
                                        minVal = val.doubleValue();
                                        maxVal = minVal;
                                    }
                                }
                            }
                        }
                    }
                    dispTrk(dispLoc,curEntName,Color.BLACK,true,false);
                    if (setStart){
                        dispWpt(startLoc, curEntName,1);
                        setStart=false;
                    }
                    curEntity=entity;
                    break;
                case TRK:
                    if (entity!= Track.enttGpx.TRKWPT) return false;
                    if (setStart) {
                        if (startLoc == null) {
                            startLoc = dispLoc;
                            startLoc.getExtras().putString("name", curEntName);
                            if (!inRef) startTime = startLoc.getTime();
                            prevAlt = null;
                            if (!inRef) {
                                if (csv == null) {
                                    minVal = dispLoc.getAltitude();
                                    maxVal = minVal;
                                } else {
                                    val = csv.nextPt(startTime, srcCol);
                                    if (val != null) {
                                        minVal = val.doubleValue();
                                        maxVal = minVal;
                                    }
                                }
                            }
                        }
                    }
                    dispTrk(dispLoc,curEntName,Color.BLACK,true,false);
                    if (setStart){
                        dispWpt(startLoc, curEntName,1);
                        setStart=false;
                    }
                    curEntity=entity;
                    break;

            }
            if (!runningMap) return true;
            dispLoc=readTrk();
            if (dispLoc==null) {
                return false;
            }
        }
    }

    Boolean withTail() {
        if (dispLoc == null) return true;
        Long now=System.currentTimeMillis();
        Float val = null;
        Track.enttGpx entity=(Track.enttGpx)dispLoc.getExtras().getSerializable("ENTITY");
        switch (entity){
            case TRKWPT:
                if (setStart) {
                    if (startLoc == null) {
                        if (dispLoc.getTime()==0L || !dispLoc.hasAltitude()){
                            Tail=false;
                            return false;
                        }
                        startLoc = dispLoc;
                        startLoc.getExtras().putString("name", curEntName);
                        startTime = startLoc.getTime();
                        prevAlt = null;
                        if (csv == null) {
                            minVal = dispLoc.getAltitude();
                            maxVal = minVal;
                        } else {
                            val = csv.nextPt(startTime, srcCol);
                            if (val != null) {
                                minVal = val.doubleValue();
                                maxVal = minVal;
                            }
                        }
                    }
                }
                if (toSkip>0L){
                    dispLoc=skip(dispLoc);
                    if (dispLoc==null) return false;
                    entity=(Track.enttGpx) dispLoc.getExtras().getSerializable("ENTITY");
                    if (entity!= Track.enttGpx.TRKWPT) return false;
                }
                if (!runningMap) {
                    launchMap(dispLoc);
                    return true;
                }
                break;
            case TRK:
                curEntName="Track "+dispLoc.getExtras().getString("name",null);
                setStart=true;
                startLoc=null;
                minVal=null;
                maxVal=null;
                curEntity=entity;
                arrowOrg=null;
                dispLoc=readTrk();
                return false;
            default:
                return false;
        }
//        entity=(Track.enttGpx) dispLoc.getExtras().getSerializable("ENTITY");
        if (entity!= Track.enttGpx.TRKWPT) return false;
        int color=Color.BLACK;

        val=null;
        String bubble=" - ";
        if (csv!=null) {
            val=csv.nextPt(dispLoc.getTime(),srcCol);
            if (val!=null) {
                color=colorz(val);
                if (minVal == null) {
                    minVal = val.doubleValue();
                    maxVal = minVal;
                } else {
                    minVal = Math.min(minVal, val.doubleValue());
                    maxVal = Math.max(maxVal, val.doubleValue());
                }
                bubble=String.format(Locale.ENGLISH,"%s %.1f",colHead.get(srcCol+1),val);
//                bubble=String.format(Locale.ENGLISH,"%s %.1f to %.1f",
//                                    colHead.get(srcCol+1),minVal,maxVal);
            }
        } else {
            Double alt=dispLoc.getAltitude();
            if (prevAlt!=null){
                if (alt>prevAlt) color=Color.RED;
                else color=Color.BLUE;
            }
            prevAlt=alt;
            if (minVal==null){
                minVal=alt;
                maxVal=alt;
            } else {
                minVal=Math.min(minVal,alt);
                maxVal=Math.max(maxVal,alt);
            }
            bubble=String.format(Locale.ENGLISH,"Alt. %.1f",alt);
//            bubble=String.format(Locale.ENGLISH,"Alt. %.1f to %.1f",
//                                    minVal,maxVal);
        }
        dispTrk(dispLoc,bubble,color,setStart,true);
        if (setStart){
            dispWpt(startLoc,curEntName,1);
            setStart=false;
        }
        curEntity=entity;
        lastTrk=dispLoc.getTime();
        Long toWait=0L;
        while (toWait<300L){
            dispLoc=readTrk();
            if (dispLoc==null) return false;
            entity=(Track.enttGpx) dispLoc.getExtras().getSerializable("ENTITY");
            if (entity!= Track.enttGpx.TRKWPT) return false;
            if (dispLoc.getTime()>0L) toWait=(dispLoc.getTime()-lastTrk)/divisor;
        }
        mHandler.postDelayed(timerTask,toWait);
        return true;
    }

    void dispWpt(Location loc, String infoBubble,int typ){
        Intent nt = new Intent();
        nt.setAction("org.js.LOC");
        nt.putExtra("WPT",loc);
        String namWpt=loc.getExtras().getString("name", "?");
        if (infoBubble==null) {
            if (loc.hasAltitude()) {
                namWpt = String.format(Locale.ENGLISH, "%s (%.1f)",
                        loc.getExtras().getString("name", "?"), loc.getAltitude());
            }
            nt.putExtra("BUBBLE",namWpt);
        } else {
            nt.putExtra("BUBBLE",infoBubble);
        }
        nt.putExtra("WPT_NAME", namWpt);
        nt.putExtra("TYPE",typ);
        sendBroadcast(nt);
    }

    void dispTrk(Location loc, String bubbleMap, int color, Boolean startLine, Boolean actTail){
        Intent nt=new Intent();
        nt.setAction("org.js.LOC");
        nt.putExtra("LOC",loc);
        nt.putExtra("COLOR",color);
        nt.putExtra("BUBBLE",bubbleMap);
        if (actTail && rotMap){
            if (arrowOrg!=null){
                float dist=arrowOrg.distanceTo(loc);
                if (dist>10.0){
                    float bearing=-arrowOrg.bearingTo(loc);
                    nt.putExtra("ORIENT",bearing);
                    arrowOrg=loc;
                }
            } else arrowOrg=loc;
        }
        if (startLine){
            nt.putExtra("ORIENT",0.0f);
            nt.putExtra("START",startLine);
            nt.putExtra("Tail",actTail);
        }
        sendBroadcast(nt);
    }


}
