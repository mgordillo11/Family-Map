package com.example.family_map;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class PersonActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        TextView actualFirstName = findViewById(R.id.actualPersonFirstName);
        TextView constantFirstName = findViewById(R.id.personFirstNameConstant);

        actualFirstName.setText(getIntent().getStringExtra("personID"));
    }
}