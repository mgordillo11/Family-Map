package com.example.family_map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        String eventID = getIntent().getStringExtra("eventID");
        String eventInfo = getIntent().getStringExtra("eventInfo");

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        //Fragment eventMapFragment = fragmentManager.findFragmentById(R.id.map);

        Bundle bundle = new Bundle();
        bundle.putString("eventID", eventID);
        bundle.putString("eventInfo", eventInfo);
        MapsFragment eventMapFragment = new MapsFragment();
        eventMapFragment.setArguments(bundle);

        fragmentManager.beginTransaction().add(R.id.eventMap, eventMapFragment).commit();
    }
}