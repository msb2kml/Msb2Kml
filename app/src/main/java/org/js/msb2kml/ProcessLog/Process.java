package org.js.msb2kml.ProcessLog;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.Calendar;
import java.lang.CharSequence;



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
    metaData m=new metaData();
    MyHandler mHandler;
    ProgressDialog prog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);
        context = getApplicationContext();
        m.fetchPref(context);
        Directory = m.getDirectory();
        Intent intent = getIntent();
        logPath = intent.getStringExtra("logPath");
        if (logPath != null) checkFile();
        Intent select=new Intent(this,Selector.class);
        select.putExtra("CurrentDir",Directory);
        select.putExtra("WithDir",false);
        select.putExtra("Mask","MSB_\\d{4}+\\.csv");
        startActivityForResult(select,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1 && resultCode==RESULT_OK) {
            logPath = data.getStringExtra("Path");
        } else logPath=null;
        checkFile();
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
            AlertDialog.Builder build=new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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
        }
        startTime=m.getStartTime();
        DatePickerDialog dp=new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen,
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
        AlertDialog.Builder builder=new AlertDialog.Builder(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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
                        paramOut();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
               .setTitle("Flight "+MsbName);
        builder.show();
    }
    

    void paramHour(){
        TimePickerDialog tp=new TimePickerDialog(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen,
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
        final CharSequence ParmList[]={"Decimated processing (1/s) ?", "Use sensors names ?",
                     "Colored Track ?", "Html table ?"};
        final boolean setOptions[]=new boolean[5];
        setOptions[0]=Decimated;
        setOptions[1]=NamedSensors;
        setOptions[2]=Colored;
        setOptions[3]=Html;
        final DialogInterface.OnMultiChoiceClickListener onclick=new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                setOptions[which]=isChecked;
            }
        };
        final AlertDialog.Builder builder=new AlertDialog.Builder(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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
                                 String.format("Extra Setup at line %d", value),
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
            AlertDialog.Builder build=new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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
                                DoIt();
                            }
                            DoIt();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DoIt();
                        }
                    });
            build.show();
        } else DoIt();
    }

    void DoIt(){
        m.set(context,Plane,Comment);
        m.setParam(context,startTime,Directory,Decimated,NamedSensors,Colored,Html,Grapher);
        prog=new ProgressDialog(this,
                android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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
                p.process(mHandler,logPath,m);
                Looper.loop();
            }
        }).start();

    }

}
