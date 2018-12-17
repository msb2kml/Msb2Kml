package org.js.msb2kml.BrowseLog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.DisplayLog.Browse;
import org.js.msb2kml.DisplayLog.Chart;
import org.js.msb2kml.R;
import org.js.msb2kml.Common.listing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class Display extends AppCompatActivity {

    Context context;
    metaData m;
    listing l=new listing();
    String MsbName=null;
    String pathMSBlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        context=getApplicationContext();
        Intent intent=getIntent();
        MsbName=intent.getStringExtra("MsbName");
        pathMSBlog=intent.getStringExtra("MSBlog");
        m=new metaData(pathMSBlog);
        l.set(context,pathMSBlog);
        if (MsbName==null) diag();
        else {
            l.unique(MsbName);
            disp(0);
        }
    }

    void diag() {
        String[] ar=l.get();
        if (ar.length >0) {
            AlertDialog.Builder build=new AlertDialog.Builder(this,
                        android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
            build.setTitle("Choose a flight")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Intent intent = getIntent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    })
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = getIntent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    })
                .setItems(ar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            disp(which);
                        }
                    });
        build.show();
        } else {
            Toast toast = Toast.makeText(this,"No file in the directory!",Toast.LENGTH_LONG);
            toast.show();
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    ArrayList<String> vMenu=new ArrayList<String>();
    int selFile=0;

    void disp(int which){
        selFile=which;
        vMenu.clear();
        String pathTxt=l.getTxt(selFile);
        vMenu.add(this.getString(R.string.vMeta));
        String pathHtml=l.getHtml(selFile);
        if (pathHtml!=null) {
            vMenu.add(this.getString(R.string.vHtml));
        }
        String pathCsv=l.getCsv(selFile);
        if (pathCsv!=null) {
            vMenu.add(this.getString(R.string.vChart));
        }
        String pathGpx=l.getGpx(selFile);
        if (pathGpx!=null) {
            vMenu.add(this.getString(R.string.vTrack));
            vMenu.add(this.getString(R.string.vOsmAnd));
        }
        String pathKml=l.getKml(selFile);
        if (pathKml!=null) {
            vMenu.add(this.getString(R.string.vEarth)); }
        AlertDialog.Builder build=new AlertDialog.Builder(this,
                        android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        build.setTitle(l.getBase(selFile))
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Intent intent = getIntent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    })
                .setItems(vMenu.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        extDisp(which);
                    }
                })
                .setNeutralButton("Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (MsbName!=null) finish();
                        else diag();
                    }
                });
        build.show();
    }

    boolean Earth=false;
    boolean OsmAnd=false;
    boolean TrackBrowser=false;

    void extDisp(int which){
        PackageManager Pm=getPackageManager();
        List<PackageInfo> allPack=Pm.getInstalledPackages(0);
        for (PackageInfo AI : allPack) {
            String zz=AI.packageName;
            if (zz.contains("earth")) Earth=true;
            if (zz.contains("osmand")) OsmAnd=true;
            if (zz.contains("TrackBrowser")) TrackBrowser=true;
        }
        if (vMenu.get(which).contentEquals(this.getString(R.string.vMeta))){
            Intent nt=new Intent(this,Browse.class);
            nt.putExtra("url","file://"+l.getTxt(selFile));
            startActivity(nt);
        } else if (vMenu.get(which).contentEquals(this.getString(R.string.vHtml))) {
            Intent nt=new Intent(this,Browse.class);
            nt.putExtra("url","file://"+l.getHtml(selFile));
            startActivity(nt);
        } else if (vMenu.get(which).contentEquals(this.getString(R.string.vChart))) {
            Intent nt=new Intent(this,Chart.class);
            nt.putExtra("MsbName",l.getBase(selFile));
            nt.putExtra("MSBlog",pathMSBlog);
            startActivity(nt);
        } else if (vMenu.get(which).contentEquals(this.getString(R.string.vTrack))) {
            if (TrackBrowser) {
                Uri u=Uri.parse("file://"+l.getGpx(selFile));
                Intent nt=new Intent("com.qbedded,TrackBrowser.FileSelectionActivity");
                nt.setClassName("com.qbedded.TrackBrowser","com.qbedded.TrackBrowser.FileSelectionActivity");
                nt.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                nt.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                nt.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                nt.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                nt.setData(u);
                startActivity(nt);
            } else {
                Toast toast=Toast.makeText(this,"Track Browser not installed",Toast.LENGTH_LONG);
                toast.show();
            }
        } else if (vMenu.get(which).contentEquals(this.getString(R.string.vOsmAnd))) {
            if (OsmAnd) {
                File f=new File(l.getGpx(selFile));
                Uri u;
//                if (Build.VERSION.SDK_INT < 24) {
                    u=Uri.fromFile(f);
//                } else {
//                    u=FileProvider.getUriForFile(context,"org.js.msb2kml.provider", f);
//                }
                Intent nt= new Intent(Intent.ACTION_VIEW);
                nt.setClassName("net.osmand.plus",
                        "net.osmand.plus.activities.MapActivity");
                nt.setData(u);
                nt.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                nt.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                nt.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                nt.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                startActivity(nt);
            } else {
                Toast toast=Toast.makeText(this,"OsmAnd not installed",Toast.LENGTH_LONG);
                toast.show();
            }
        }else {
            if (Earth) {
                File f=new File(l.getKml(selFile));
                Uri u;
//                if (Build.VERSION.SDK_INT < 24) {
//                    u=Uri.fromFile(f);
//                } else {
                    u = FileProvider.getUriForFile(context, "org.js.msb2kml.provider", f);
//                }
                Intent nt=new Intent(Intent.ACTION_VIEW);
                nt.setDataAndType(u, "application/vnd.google-earth.kml+xml");
                nt.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                nt.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                nt.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                nt.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                nt.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                nt.setPackage("com.google.earth");
                startActivity(nt);
            } else {
                Toast toast=Toast.makeText(this,"Google Earth not installed",Toast.LENGTH_LONG);
                toast.show();
            }
        }
        disp(selFile);
    }

}
