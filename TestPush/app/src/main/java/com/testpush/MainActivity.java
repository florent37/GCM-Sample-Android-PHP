package com.testpush;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.testpush.notification.GcmInitializer;


public class MainActivity extends ActionBarActivity implements GcmInitializer.GcmInitialiserCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new GcmInitializer(this, this).enregistrerCloudMessaginClient();
    }

    @Override
    public void onGcmInitialised(String token) {
        String shareBody = token;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "GCM Token");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Send GCM Token"));
    }

}
