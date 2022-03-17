package com.example.family_map;

import android.content.Intent;
import android.content.SharedPreferences;
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

            SwitchPreferenceCompat lifeStoryLines = findPreference("life_story_lines");
            SwitchPreferenceCompat familyTreeLines = findPreference("family_tree_lines");
            SwitchPreferenceCompat spouseLines = findPreference("spouse_lines");
            SwitchPreferenceCompat fatherSide = findPreference("father_side");
            SwitchPreferenceCompat motherSide = findPreference("mother_side");
            SwitchPreferenceCompat maleEvents = findPreference("male_events");
            SwitchPreferenceCompat femaleEvents = findPreference("female_events");

            lifeStoryLines.setChecked(DataCache.getSettings().lifeStoryLines);
            familyTreeLines.setChecked(DataCache.getSettings().familyTreeLines);
            spouseLines.setChecked(DataCache.getSettings().spouseLines);
            fatherSide.setChecked(DataCache.getSettings().fatherSide);
            motherSide.setChecked(DataCache.getSettings().motherSide);
            maleEvents.setChecked(DataCache.getSettings().maleEvents);
            femaleEvents.setChecked(DataCache.getSettings().femaleEvents);

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    switch (s) {
                        case "life_story_lines":
                            DataCache.getSettings().lifeStoryLines = lifeStoryLines.isChecked();
                            break;
                        case "family_tree_lines":
                            DataCache.getSettings().familyTreeLines = familyTreeLines.isChecked();
                            break;
                        case "spouse_lines":
                            DataCache.getSettings().spouseLines = spouseLines.isChecked();
                            break;
                        case "father_side":
                            DataCache.getSettings().fatherSide = fatherSide.isChecked();
                            break;
                        case "mother_side":
                            DataCache.getSettings().motherSide = motherSide.isChecked();
                            break;
                        case "male_events":
                            DataCache.getSettings().maleEvents = maleEvents.isChecked();
                            break;
                        case "female_events":
                            DataCache.getSettings().femaleEvents = femaleEvents.isChecked();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }
}