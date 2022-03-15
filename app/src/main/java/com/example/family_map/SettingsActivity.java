package com.example.family_map;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.net.InetSocketAddress;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView logoutView = findViewById(R.id.logoutView);

        logoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FragmentManager fragmentManager = getSupportFragmentManager();
                DataCache.getInstance().userLoggedIn = false;
                Intent logoutIntent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(logoutIntent);
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SwitchPreferenceCompat  lifeStoryLines = findPreference("life_story_lines");
            SwitchPreferenceCompat familyTreeLine = findPreference("family_tree_lines");
            SwitchPreferenceCompat spouseLines = findPreference("spouse_lines");
            SwitchPreferenceCompat fatherSide = findPreference("father_side");
            SwitchPreferenceCompat motherSide = findPreference("mother_side");
            SwitchPreferenceCompat maleEvents = findPreference("male_events");
            SwitchPreferenceCompat femaleEvents = findPreference("female_events");

            lifeStoryLines.setChecked(true);
            familyTreeLine.setChecked(true);
            spouseLines.setChecked(true);
            fatherSide.setChecked(true);
            motherSide.setChecked(true);
            maleEvents.setChecked(true);
            femaleEvents.setChecked(true);
        }
    }
}