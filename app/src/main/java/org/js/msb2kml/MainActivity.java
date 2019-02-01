package org.js.msb2kml;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.js.msb2kml.Common.CopyFix;
import org.js.msb2kml.Common.GetFix;
import org.js.msb2kml.Common.HandFix;
import org.js.msb2kml.Common.StartGPS;
import org.js.msb2kml.Common.listing;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.BrowseLog.Display;
import org.js.msb2kml.ProcessLog.Process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Context context;
    listing l=new listing();
    metaData md=null;
    public String pathMSBlog;
    public boolean mountedSD;
    public boolean writeSD;
    public StartGPS sGPS=null;
    public Map<String ,Location> startPoints=new HashMap<>();
    public String Name=null;
    public Location Loc=null;
    public Integer scene;
    private Button bSelectF=null;
    private Button bSelectD=null;
    private Button bMethod=null;
    private Button bQuit=null;
    private ArrayList<Location> biLoc=null;
    private Integer which=0;
    private String MSBcomment;
    private String MSBname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        String exPath=Environment.getExternalStorageDirectory().getAbsolutePath();
        pathMSBlog=exPath+"/"+context.getString(R.string.HomeDir);
        String state=Environment.getExternalStorageState();
        mountedSD=state.contains(Environment.MEDIA_MOUNTED);
        if (!mountedSD) {
            Toast.makeText(context,exPath+" not mounted: aborted!",Toast.LENGTH_LONG).show();
            finish();
        }
        writeSD=!Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        if (!writeSD){
            Toast.makeText(context,exPath+" not writeable: aborted!",Toast.LENGTH_LONG).show();
            finish();
        }
        boolean hasPermission=ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED;
        if (!hasPermission){
            Toast.makeText(context,"This application need to write to "+
            exPath+".",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},10);
        }
        checkDir();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkDir();
            } else finish();
        }
}

    void checkDir(){
        if (!l.set(context,pathMSBlog)) {
            AlertDialog.Builder build = new AlertDialog.Builder(this);
//                    android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
            build.setMessage("Create missing destination directory?")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setTitle(pathMSBlog)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            l.createDir();
                            part2();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            build.show();
        } else part2();
    }

    void part2(){
        if (md==null) md=new metaData(pathMSBlog);
        if (bQuit==null){
            bSelectF=(Button) findViewById(R.id.selectf);
            bSelectD=(Button) findViewById(R.id.selectd);
            bMethod=(Button) findViewById(R.id.method);
            bQuit=(Button) findViewById(R.id.Qbutton);
            bQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            bSelectF.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent process=new Intent(context,Process.class);
                    process.putExtra("MSBlog",pathMSBlog);
                    startActivityForResult(process,
                            getResources().getInteger(R.integer.PROCESS));
                }
            });
            bSelectD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browse=new Intent(context,Display.class);
                    browse.putExtra("MSBlog",pathMSBlog);
                    startActivityForResult(browse,getResources().
                                        getInteger(R.integer.DISPLAY));
                }
            });
            bMethod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    methodGpx();
                }
            });
        }
    }

    void methodGpx(){
        String[] items=new String[3];
        items[0]="Use device GPS";
        items[1]="Enter a known location";
        items[2]="Copy location from a previous flight";
        AlertDialog.Builder build=new AlertDialog.Builder(this);
//                android.R.style.Theme_DeviceDefault_Light_NoActionBar);
        build.setTitle("Select a method to prepare a location");
        build.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                part2();
            }
        })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        part2();
                    }
            })
            .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:{
                                scene=getResources().getInteger(R.integer.LOC_GPS);
                                startLocate();
                                break;
                            }
                            case 1:{
                                scene=getResources().getInteger(R.integer.LOC_HAND);
                                startLocate();
                                break;
                            }
                            case 2:{
                                Intent browse=new Intent(context,Display.class);
                                browse.putExtra("MSBlog",pathMSBlog);
                                browse.putExtra("Gpx",true);
                                startActivityForResult(browse,
                                    getResources().getInteger(R.integer.DISPLAYgpx));
                                break;
                            }
                        }

                    }
            });
        build.show();
    }

    void startLocate(){
        Intent intent;
        if (scene==getResources().getInteger(R.integer.LOC_GPS)) {
            intent = new Intent(context, GetFix.class);
        } else if(scene==getResources().getInteger(R.integer.COPY)){
            intent=new Intent(context,CopyFix.class);
            intent.putExtra("MSBcom",MSBcomment);
            intent.putExtra("MSBname",MSBname);
            intent.putExtra("Location1",biLoc.get(0));
            intent.putExtra("Location2",biLoc.get(1));
            intent.putExtra("Which",which);
        } else {
            intent=new Intent(context,HandFix.class);
        }
        if (Name!=null) intent.putExtra("Name",Name);
        if (Loc!=null) intent.putExtra("Location",Loc);
        startActivityForResult(intent,scene);
    }

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        scene=requestCode;
        if (resultCode!=RESULT_OK) {
            part2();
            return;
        }
        if (requestCode==getResources().getInteger(R.integer.LOC_GPS)){
            Loc=data.getExtras().getParcelable("Location");
            Name=data.getStringExtra("Name");
            dupLoc();
        } else if (requestCode==getResources().getInteger(R.integer.LOC_HAND)) {
            Loc = data.getExtras().getParcelable("Location");
            Name = data.getStringExtra("Name");
            dupLoc();
        } else if (requestCode==getResources().getInteger(R.integer.DISPLAYgpx)) {
            MSBname = data.getStringExtra("Name");
            MSBcomment = data.getStringExtra("MSBcom");
            startCopy(data.getStringExtra("pathGps"));
        } else if (requestCode==getResources().getInteger(R.integer.COPY)){
            Name=data.getStringExtra("Name");
            which=data.getIntExtra("Which",0);
            Loc=biLoc.get(which);
            dupLoc();
        } else part2();
    }

    void dupLoc(){
        if (sGPS==null) sGPS=new StartGPS(md.getPathStartGPS());
        startPoints=sGPS.readSG();
        if (Loc!=null && Name!=null && !Name.isEmpty()){
            if (startPoints.containsKey(Name)){
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
//                        android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen);
                builder.setMessage("Duplicate name")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                part2();
                            }
                        })
                        .setTitle(Name)
                        .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPoints.remove(Name);
                                startPoints.put(Name,Loc);
                                sGPS.writeSG(startPoints);
                                part2();
                            }
                        })
                        .setNegativeButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startLocate();
                            }
                        });
                builder.show();
            }
            else {
                startPoints.put(Name, Loc);
                sGPS.writeSG(startPoints);
                part2();
            }
        } else part2();
    }

    void startCopy(String pathGps){
        if (sGPS==null) sGPS=new StartGPS(md.getPathStartGPS());
        biLoc=sGPS.readTrack(pathGps);
        which=0;
        scene=getResources().getInteger(R.integer.COPY);
        if (biLoc!=null && biLoc.size()==2) startLocate();
        else part2();
    }
}
