package com.example.finalyear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class cdashboard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cdashboard);

        Button btnNotification = findViewById(R.id.button17);
        Button btnJobs = findViewById(R.id.button16);
        Button btnProfile = findViewById(R.id.button18);


        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cdashboard.this, cprofile.class);
                startActivity(intent);
            }
        });

        btnJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cdashboard.this, cjobsavai.class);
                startActivity(intent);
            }
        });

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(cdashboard.this, cnotifications.class);
                startActivity(intent);
            }
        });
    }
}
