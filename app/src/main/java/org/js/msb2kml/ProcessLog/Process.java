package org.js.msb2kml.ProcessLog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.js.msb2kml.Common.CopyFix;
import org.js.msb2kml.Common.GetFix;
import org.js.msb2kml.Common.HandFix;
import org.js.msb2kml.Common.StartGPS;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.BrowseLog.Display;
import org.js.msb2kml.R;
import org.js.msb2kml.FileSelect.Selector;
import org.js.msb2kml.Work.fileProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.lang.CharSequence;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class Process extends AppCompatActivity {

    String Plane;
    String Comment;
    Calendar startTime;
    String Directory;
    boolean Decimated;
    boolean NamedSensors;
    boolean Colored;
    boolean Html;
    boolean Grapher;
    String MsbName;
    String logPath;
    Context context;
    String pathMSBlog;
    String startName=null;
    metaData m;
    MyHandler mHandler;
    ProgressDialog prog;
    Map<String,Location> startPoints=new HashMap<>();
    Location loc=null;
    StartGPS sGPS=null;
    public String[] ar=new String[0];
    public Integer preSelect=null;
    private ArrayList<Location> biLoc=null;
    private Integer whichLoc=0;
    private String MSBcomment;
    private String MSBcopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        context = getApplicationContext();
        Intent intent = getIntent();
        logPath = intent.getStringExtra("logPath");
        pathMSBlog=intent.getStringExtra("MSBlog");
        m=new metaData(pathMSBlog);
        m.fetchPref(context);
        Directory = m.getDirectory();
        if (logPath != null) checkFile();
        Intent select=new Intent(this,Selector.class);
        select.putExtra("CurrentDir",Directory);
        select.putExtra("WithDir",false);
        select.putExtra("Mask","MSB_\\d{4}+\\.csv");
        select.putExtra("Title","Read from ");
        startActivityForResult(select,getResources().getInteger(R.integer.SELECTOR));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==getResources().getInteger(R.integer.SELECTOR)) {
            if (resultCode == RESULT_OK) {
                logPath = data.getStringExtra("Path");
            } else logPath = null;
            checkFile();
        } else if (requestCode==getResources().getInteger(R.integer.LOC_HAND)){
            if (resultCode==RESULT_OK){
                startName=data.getStringExtra("Name");
                loc=data.getExtras().getParcelable("Location");
                dupLoc(requestCode);
            } else {
                startName=null;
                loc=null;
                DoIt();
            }
        } else if (requestCode==getResources().getInteger(R.integer.LOC_GPS)){
            if (resultCode==RESULT_OK){
                startName=data.getStringExtra("Name");
                loc=data.getExtras().getParcelable("Location");
                dupLoc(requestCode);
            } else {
                startName=null;
                loc=null;
                DoIt();
            }
        } else if (requestCode==getResources().getInteger(R.integer.COPY)){
            if (resultCode==RESULT_OK){
                startName=data.getStringExtra("Name");
                whichLoc=data.getIntExtra("Which",0);
                loc=biLoc.get(whichLoc);
                dupLoc(requestCode);
            } else {
                startName=null;
                loc=null;
                DoIt();
            }
        } else if (requestCode==getResources().getInteger(R.integer.DISPLAYgpx)){
            if (resultCode==RESULT_OK) {
                MSBcopy = data.getStringExtra("Name");
                MSBcomment = data.getStringExtra("MSBcom");
                getBiLoc(data.getStringExtra("pathGps"));
            } else {
                startName=null;
                loc=null;
                DoIt();
            }
        }
    }

    void checkFile(){
        if (logPath == null){
            finish();
            return;
        }
        File f=new File(logPath);
        Directory=f.getParent();
        MsbName=(f.getName()).replace(".csv","");
        if (m.setName(context,MsbName)){
            AlertDialog.Builder build=new AlertDialog.Builder(this);
//                    android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            build.setMessage("Overwrite kml/gpx files?")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setTitle("Flight "+MsbName)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                         paramDay();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            build.show();
        } else paramDay();
    }


    void paramDay() {
        if (m.extract(context,MsbName)){
            Toast toast = Toast.makeText(this, "Using meta data file", Toast.LENGTH_LONG);
            toast.show();
        } else m.fetchPref(context);
        startTime=m.getStartTime();
        DatePickerDialog dp=new DatePickerDialog(this,0,
//                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        startTime.set(year,month,dayOfMonth);
                        paramHour();
                    }
                },
                startTime.get(Calendar.YEAR),
                startTime.get(Calendar.MONTH), startTime.get(Calendar.DAY_OF_MONTH));
        dp.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dp.show();
    }

    void paramPlane(){
        Plane=m.getPlane();
        Comment=m.getComment();
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//                android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        View diagview=View.inflate(this,R.layout.meta,null);
        final EditText pl=diagview.findViewById(R.id.plane);
        pl.setText(Plane);
        final EditText co=diagview.findViewById(R.id.comment);
        co.setText(Comment);
        builder.setView(diagview)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Plane=pl.getText().toString();
                        Comment=co.getText().toString();
                        InputMethodManager imm = (InputMethodManager)
                                context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(pl.getWindowToken(),0);
                        imm.hideSoftInputFromWindow(co.getWindowToken(),0);
                        paramOut();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        InputMethodManager imm = (InputMethodManager)
                                context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(pl.getWindowToken(),0);
                        imm.hideSoftInputFromWindow(co.getWindowToken(),0);
                        finish();
                    }
                })
               .setTitle("Flight "+MsbName);
        builder.show();
    }
    

    void paramHour(){
        TimePickerDialog tp=new TimePickerDialog(this,0,
//                android.R.style.Theme_DeviceDefault_Light_NoActionBar,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        startTime.set(Calendar.MINUTE,minute);
                        startTime.set(Calendar.SECOND,0);
                        paramPlane();
                    }
                },startTime.get(Calendar.HOUR_OF_DAY),startTime.get(Calendar.MINUTE),true);
        tp.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        tp.show();
    }

    void paramOut(){
        Decimated=m.getDecimated();
        NamedSensors=m.getNamedSensors();
        Colored=m.getColored();
        Html=m.getHtml();
        Grapher=m.getGrapher();
        startName=m.getStartName();
        final CharSequence ParmList[]={"Decimated processing (1/s) ?", "Use sensors names ?",
                     "Colored Track ?", "Html table ?", "Use remote GPS ?"};
        final boolean setOptions[]=new boolean[5];
        setOptions[0]=Decimated;
        setOptions[1]=NamedSensors;
        setOptions[2]=Colored;
        setOptions[3]=Html;
        setOptions[4]=(startName!=null);
        final DialogInterface.OnMultiChoiceClickListener onclick=new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                setOptions[which]=isChecked;
            }
        };
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
//                android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        builder.setMultiChoiceItems(ParmList, setOptions, onclick);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setTitle("Flight "+MsbName)
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Decimated=setOptions[0];
                        NamedSensors=setOptions[1];
                        Colored=setOptions[2];
                        Html=setOptions[3];
                        Grapher=true;
                        if (!setOptions[4]) startName=null;
                        else if (startName==null) startName="";
                        paramAddr();
                    }
                });
        builder.show();
    }


    private static class MyHandler extends Handler {

        public final WeakReference<Process> mActivity;

        public MyHandler(Process activity){
            mActivity=new WeakReference<Process>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            int code=msg.what;
            int value;
            switch (code) {
                case 0: {
                    value = msg.arg1;
                    mActivity.get().prog.setMax(value);
                    break;
                }
                case 1: {
                    value = msg.arg1;
                    mActivity.get().prog.setProgress(value);
                    break;
                }
                case 2: {
                    mActivity.get().prog.dismiss();
                    mActivity.get().m.putPref(mActivity.get().context);
                    value = msg.arg1;
                    if (value <= 0) {
                        Toast toast = Toast.makeText(mActivity.get().context,
                                                "No data!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    value = msg.arg2;
                    if (value <= 0) {
                        Toast toast = Toast.makeText(mActivity.get().context,
                                          "No GPS data.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    Intent display = new Intent(mActivity.get().context, Display.class);
                    display.putExtra("MsbName", mActivity.get().MsbName);
                    display.putExtra("MSBlog",mActivity.get().pathMSBlog);
                    mActivity.get().startActivity(display);
                    mActivity.get().finish();
                    break;
                }
                case 5: {
                    mActivity.get().prog.setMessage(
                            "Reading... (processing trigonometric functions)");
                    break;
                }
                case 20: {
                    mActivity.get().prog.dismiss();
                    value = msg.arg1;
                    Toast toast = Toast.makeText(mActivity.get().context,
                              String.format("Wrong format at line  %d", value),
                            Toast.LENGTH_LONG);
                    toast.show();
                    mActivity.get().finish();
                    break;
                }
                case 21: {
                    value = msg.arg1;
                    Toast toast = Toast.makeText(mActivity.get().context,
                                 String.format("New Setup at line %d", value),
                            Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                default: {
                    String message = (String) msg.obj;
                    Toast toast = Toast.makeText(mActivity.get().context, message,
                                                                   Toast.LENGTH_LONG);
                    toast.show();
                    mActivity.get().prog.dismiss();
                    mActivity.get().finish();
                }
            }
        }
    }

    void paramAddr(){
        File addr=new File(m.getPathAddr());
        if (NamedSensors && !addr.exists()){
            AlertDialog.Builder build=new AlertDialog.Builder(this);
//                    android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            build.setMessage("Create default AddrSens.txt?")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setTitle("Flight "+MsbName)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                InputStream model=getResources().openRawResource(R.raw.addrsens);
                                BufferedReader Model=
                                    new BufferedReader(new InputStreamReader(model));
                                FileWriter addr=new FileWriter(m.getPathAddr());
                                String line="";
                                while (line!=null){
                                    line=Model.readLine();
                                    if (line!=null) addr.write(line+"\n");
                                }
                                addr.close();
                                Model.close();
                            } catch (Exception e) {
                                menuGps();
                            }
                            menuGps();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            menuGps();
                        }
                    });
            build.show();
        } else menuGps();
    }

    void menuGps(){
        String[] items;
        Integer sz;
        preSelect=null;
        if (startName!=null){
          sGPS=new StartGPS(m.getPathStartGPS());
          startPoints=sGPS.readSG();
          if (startPoints.isEmpty()){
              ar=new String[0];
              items=new String[3];
              sz=0;
          } else {
              SortedSet<String> keys=new TreeSet<>();
              keys.addAll(startPoints.keySet());
              sz=keys.size();
              Iterator<String> itr=((TreeSet<String>) keys).descendingIterator();
              ar=new String[sz];
              items=new String[sz+3];
              String here;
              for (int i=0;i<sz;i++){
                here=itr.next();
                ar[i]=here;
                if (here.contentEquals(startName)){ preSelect=i; }
                items[i]=ar[i]+": ";
                items[i]+=String.format(Locale.ENGLISH,"lat=%.6f ",
                        startPoints.get(here).getLatitude());
                items[i]+=String.format(Locale.ENGLISH,"lon=%.6f ",
                        startPoints.get(here).getLongitude());
                items[i]+=String.format(Locale.ENGLISH,"alt=%.1f",
                        startPoints.get(here).getAltitude());
              }
          }
          items[sz]="Current GPS location";
          items[sz+1]="Enter location";
          items[sz+2]="Copy from a previous flight";
          if (preSelect==null) preSelect=sz;
          AlertDialog.Builder build=new AlertDialog.Builder(this);
//                  android.R.style.Theme_DeviceDefault_Light_NoActionBar);
          build.setTitle("Start location selection")
                  .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          if (preSelect==null || preSelect<0) {
                              startName=null;
                              DoIt();
                          } else if (preSelect<ar.length){
                              startName=ar[preSelect];
                              loc=startPoints.get(startName);
                              DoIt();
                          } else if (preSelect==ar.length){
                              startLocate();
                          } else if (preSelect==ar.length+1){
                              startEnter();
                          } else {
                              browseGpx();
                          }
                      }
                  })
                  .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                         startName=null;
                         DoIt();
                      }
                  })
                  .setSingleChoiceItems(items, preSelect, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          preSelect=which;
                      }
                  })
                  .setOnCancelListener(new DialogInterface.OnCancelListener() {
                      @Override
                      public void onCancel(DialogInterface dialog) {
                         startName=null;
                         DoIt();
                      }
                  });

          build.show();
        } else {
            DoIt();
        }
    }

    void dupLoc(final Integer requestCode){
        if (sGPS==null) sGPS=new StartGPS(m.getPathStartGPS());
        startPoints=sGPS.readSG();
        if (startPoints.containsKey(startName)){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
//                   android.R.style.Theme_DeviceDefault_Light_NoActionBar);
            builder.setMessage("Duplicate name")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            startName=null;
                            loc=null;
                            DoIt();
                        }
                    })
                    .setTitle(startName)
                    .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startPoints.remove(startName);
                            startPoints.put(startName,loc);
                            sGPS.writeSG(startPoints);
                            DoIt();
                        }
                    })
                    .setNeutralButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (requestCode==getResources().getInteger(R.integer.LOC_GPS)) {
                                startLocate();
                            } else if (requestCode==getResources().getInteger(R.integer.COPY)){
                                startCopy();
                            } else startEnter();
                        }
                    });
            builder.show();
        } else {
            startPoints.put(startName,loc);
            sGPS.writeSG(startPoints);
            DoIt();
        }
    }

    void startLocate(){
        Intent intent=new Intent(this,GetFix.class);
        if (startName!=null) intent.putExtra("Name",startName);
        if (loc!=null) intent.putExtra("Location",loc);
        startActivityForResult(intent,getResources().getInteger(R.integer.LOC_GPS));
    }

    void startEnter(){
        Intent intent=new Intent(this,HandFix.class);
        if (startName!=null) intent.putExtra("Name",startName);
        if (loc!=null) intent.putExtra("Location",loc);
        startActivityForResult(intent,getResources().getInteger(R.integer.LOC_HAND));
    }

    void browseGpx(){
        Intent browse=new Intent(this,Display.class);
        browse.putExtra("MSBlog",pathMSBlog);
        browse.putExtra("Gpx",true);
        startActivityForResult(browse,
                getResources().getInteger(R.integer.DISPLAYgpx));
    }

    void getBiLoc(String pathGps){
        if (sGPS==null) sGPS=new StartGPS(m.getPathStartGPS());
        biLoc=sGPS.readTrack(pathGps);
        whichLoc=0;
        if (biLoc!=null && biLoc.size()==2) startCopy();
        else {
            startName=null;
            DoIt();
        }
    }

    void startCopy(){
        Intent intent=new Intent(this,CopyFix.class);
        intent.putExtra("MSBname",MSBcopy);
        intent.putExtra("MSBcom",MSBcomment);
        intent.putExtra("Location1",biLoc.get(0));
        intent.putExtra("Location2",biLoc.get(1));
        intent.putExtra("Which",whichLoc);
        if (startName!=null) intent.putExtra("Name",startName);
        startActivityForResult(intent,getResources().getInteger(R.integer.COPY));
    }

    void DoIt(){
        if (startName!=null){
            String line=startName+": ";
            line+=String.format(Locale.ENGLISH,"lat=%.6f ", loc.getLatitude());
            line+=String.format(Locale.ENGLISH,"lon=%.6f ", loc.getLongitude());
            line+=String.format(Locale.ENGLISH,"alt=%.1f", loc.getAltitude());
            Toast toast=Toast.makeText(this,line,Toast.LENGTH_LONG);
            toast.show();
        }
        m.set(context,Plane,Comment);
        m.setParam(context,startTime,Directory,Decimated,NamedSensors,Colored,
                Html,Grapher,startName);
        prog=new ProgressDialog(this,
                android.R.style.Theme_Holo_NoActionBar_Fullscreen);
        prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prog.setTitle("Flight "+MsbName);
        prog.setMessage("Reading...");
        prog.setMax(100);
        prog.show();
        mHandler=new MyHandler(Process.this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                fileProcess p=new fileProcess();
                p.process(mHandler,logPath,m,loc);
                Looper.loop();
            }
        }).start();

    }

}
