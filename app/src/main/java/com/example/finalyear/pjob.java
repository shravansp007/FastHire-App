package com.example.finalyear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class pjob extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pjob);

        Button btnJob = findViewById(R.id.button11);
        Button btnDashboard = findViewById(R.id.button10);
        Button btnProfile = findViewById(R.id.button13);


        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pjob.this, pprofile.class);
                startActivity(intent);
            }
        });

        btnJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pjob.this, pdashboard.class);
                startActivity(intent);
            }
        });

        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pjob.this, pcreatejob.class);
                startActivity(intent);
            }
        });
    }
}
