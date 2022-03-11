package com.example.family_map;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment loginFragment = fragmentManager.findFragmentById(R.id.loginFragment);

        if(loginFragment == null) {
            loginFragment = new LoginFragment();
            fragmentManager.beginTransaction().add(R.id.loginFragment, loginFragment).commit();
        } else {
            if(loginFragment instanceof LoginFragment) {
                ((LoginFragment) loginFragment).registerListener(this);
            }
        }
    }

    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment mapsFragment = new MapsFragment();
        fragmentManager.beginTransaction().replace(R.id.loginFragment, mapsFragment).commit();
    }
}