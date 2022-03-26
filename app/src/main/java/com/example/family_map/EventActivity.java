package com.example.family_map;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String eventID = getIntent().getStringExtra("eventID");
        String eventInfo = getIntent().getStringExtra("eventInfo");

        FragmentManager fragmentManager = this.getSupportFragmentManager();

        Bundle eventBundle = new Bundle();
        eventBundle.putString("eventID", eventID);
        eventBundle.putString("eventInfo", eventInfo);

        MapsFragment eventMapFragment = new MapsFragment();
        eventMapFragment.setArguments(eventBundle);

        fragmentManager.beginTransaction().add(R.id.eventMap, eventMapFragment).commit();
    }
}