package org.js.msb2kml.DisplayLog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import org.js.msb2kml.R;

public class Browse extends AppCompatActivity {

    private WebView browseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        browseView = (WebView) findViewById(R.id.browse);
        String url=getIntent().getStringExtra("url");
        getSupportActionBar().setTitle(url);
        browseView.loadUrl(url);

    }
}
