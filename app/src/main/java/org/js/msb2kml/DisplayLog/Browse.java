package org.js.msb2kml.DisplayLog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import org.js.msb2kml.R;

public class Browse extends AppCompatActivity {

    private WebView browseView;
    Button quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        browseView = (WebView) findViewById(R.id.browse);
        quit=(Button) findViewById(R.id.quitbro);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String url=getIntent().getStringExtra("url");
        browseView.getSettings().setDefaultFontSize(12);
        getSupportActionBar().setTitle(url);
        browseView.loadUrl(url);
    }
}
