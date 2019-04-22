package com.will_russell.ev3bluetoothclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.bottomappbar.BottomAppBar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        BottomAppBar bar = findViewById(R.id.bar);
        setSupportActionBar(bar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_arrow);
    }

    private void gotoURL (String url) {
        Uri uriUrl = Uri.parse(url);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, uriUrl);
    }

    public void gotoGitHub(View view) {
        gotoURL("https://github.com/wrussell1999");
    }

    public void gotoTwitter(View view) {
        gotoURL("https://twitter.com/WilliamR__");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}