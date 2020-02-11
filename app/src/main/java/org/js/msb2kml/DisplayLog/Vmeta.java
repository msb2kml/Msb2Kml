package org.js.msb2kml.DisplayLog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.js.msb2kml.Common.StartGPS;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Vmeta extends AppCompatActivity {

    TextView vName;
    TextView vDate;
    TextView vHour;
    TextView vPlane;
    TextView vComment;
    TextView vStartName;
    Button bLoc;
    Button bQuit;
    Button bAddr;
    ListView list;
    View header;
    Context context;

    String MsbName=null;
    String pathMSBlog;
    metaData m;
    List<String> extrmString=new ArrayList<>();
    MetaListAdapt mAdapt;
    ArrayList<String > addrSens=new ArrayList<>();
    String pathStartGPS=null;
    Map<String, Location> startPoints=new HashMap<>();
    Location startLoc=null;
    Intent intentMap=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vmeta);
        context=getApplicationContext();
        vName=(TextView) findViewById(R.id.name);
        vDate=(TextView) findViewById(R.id.date);
        vHour=(TextView) findViewById(R.id.hour);
        vPlane=(TextView) findViewById(R.id.plane);
        vComment=(TextView) findViewById(R.id.comment);
        vStartName=(TextView) findViewById(R.id.startname);
        bLoc=(Button) findViewById(R.id.location);
        list=(ListView) findViewById(R.id.list);
        bQuit=(Button) findViewById(R.id.quit);
        bAddr=(Button) findViewById(R.id.addrsens);
        bQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        bAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddr();
            }
        });
        bLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMap();
            }
        });
        PackageManager Pm=getPackageManager();
        List<PackageInfo> allPack=Pm.getInstalledPackages(0);
        for (PackageInfo AI : allPack){
            String zz=AI.packageName;
            if (zz.matches("org.js.Msb2Map")){
                intentMap=Pm.getLaunchIntentForPackage(zz);
                break;
            }
        }
        Intent intent=getIntent();
        MsbName=intent.getStringExtra("MsbName");
        pathMSBlog=intent.getStringExtra("MSBlog");
        m=new metaData(pathMSBlog);
        if (!m.extract(context,MsbName)) finish();
        vName.setText(MsbName);
        String date=m.getDate();
        Pattern pat=Pattern.compile("[ ]");
        String fields[]=pat.split(date);
        vDate.setText   ("Date:         "+fields[0]);
        vHour.setText("Hour: "+fields[1]);
        vPlane.setText  ("Plane:        "+m.getPlane());
        vComment.setText("Comment:  "+m.getComment());
        String startName=m.getStartName();
        if (startName==null) {
            vStartName.setText("Start Name: <none>");
            bLoc.setEnabled(false);
            bLoc.setText("<none>");
        } else {
            vStartName.setText("Start Name: "+startName);
            pathStartGPS=m.getPathStartGPS();
            startLoc=nameToLoc(startName);
            if (startLoc!=null){
                String sLoc=String.format(Locale.ENGLISH,"%.6f",startLoc.getLatitude())+
                        ", "+String.format(Locale.ENGLISH,"%.6f",startLoc.getLongitude())+
                        ", "+String.format(Locale.ENGLISH,"%.2f",startLoc.getAltitude());
                bLoc.setText(sLoc);
                bLoc.setEnabled(intentMap!=null);
            }
        }
        addrSens=m.getAddrSens();
        if (addrSens.size()==0) bAddr.setEnabled(false);
        extrmString.addAll(m.getExtrmString());
        if (extrmString.isEmpty()){
            Toast.makeText(context,"No measure!", Toast.LENGTH_LONG).show();
        } else {
            header= getLayoutInflater().inflate(R.layout.list_item,null);
            list.addHeaderView(header);
            mAdapt=new MetaListAdapt(context,extrmString);
            list.setAdapter(mAdapt);
        }
    }

    public Location nameToLoc(String pylone) {
        Location loc = null;
        if (pathStartGPS == null) return null;
        StartGPS sGPS = new StartGPS(pathStartGPS);
        Map<String, Location> startPoints = sGPS.readSG();
        if (startPoints.isEmpty()) return null;
        if (!startPoints.containsKey(pylone)) return null;
        loc = startPoints.get(pylone);
        return loc;
    }

    void showAddr(){
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        StringBuilder sb=new StringBuilder();
        for (int i=0;i<addrSens.size();i++){
            sb.append(addrSens.get(i)+"\n");
        }
        build.setMessage(sb.toString());
        build.setTitle("AddrSens that has been used")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        build.show();
    }

    void showMap(){
        if (startLoc==null || intentMap==null) return;
        Intent nt=(Intent) intentMap.clone();
        nt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        nt.putExtra("CALLER",context.getString(R.string.app_name));
        nt.putExtra("StartGPS",true);
        nt.putExtra("CENTER",startLoc);
        startActivity(nt);
    }
}
