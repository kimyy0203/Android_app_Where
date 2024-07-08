package com.example.project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class Title extends AppCompatActivity {
    private long backKeyPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();


        LinearLayout button1 = findViewById(R.id.주차장);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Parking.class);
                startActivity(intent);
            }
        });
        LinearLayout button2 = findViewById(R.id.세차장);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Title.this, Washing.class);
                startActivity(intent);
            }
        });
        LinearLayout button3 = findViewById(R.id.주유소);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Title.this, GasStation.class);
                startActivity(intent);
            }
        });
    }
    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            AlertDialog.Builder builder = new AlertDialog.Builder(Title.this);
            builder.setMessage("정말로 종료하시겠습니까?");
            builder.setTitle("종료 알림창")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            moveTaskToBack(true);
                            finish();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setTitle("종료 알림창");
            alert.show();
            return;
        }
    }
}