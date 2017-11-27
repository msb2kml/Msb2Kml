package org.js.msb2kml;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.js.msb2kml.Common.listing;
import org.js.msb2kml.Common.metaData;
import org.js.msb2kml.BrowseLog.Display;
import org.js.msb2kml.ProcessLog.Process;

public class MainActivity extends AppCompatActivity {

    Context context;
    metaData m=new metaData();
    listing l=new listing();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast toast = Toast.makeText(this,
                        "This application need to read and write files", Toast.LENGTH_LONG);
                toast.show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        }
        checkDir();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            checkDir();
        } else finish();
}

    void checkDir(){
        if (!l.set(context)) {
            AlertDialog.Builder build = new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
            build.setMessage("Create missing destination directory?")
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                    .setTitle(context.getString(R.string.MSBlog))
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
        String jobs[]={"Display processed logs",
                "Process a new log"};
        AlertDialog.Builder build=new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        build.setTitle("Select a job...")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                .setItems(jobs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            part3(which);
                        }
                    });
        build.show();
    }

    void part3 (int which){
        if (which==0){
            Intent browse=new Intent(this,Display.class);
            startActivityForResult(browse,1);
        } else if (which==1){
            Intent process=new Intent(this,Process.class);
            startActivityForResult(process,1);
        } else {
            Toast toast = Toast.makeText(this,"What",Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
    }

@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         part2();
    }
}
