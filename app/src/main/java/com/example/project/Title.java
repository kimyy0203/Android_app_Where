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
    /* Title class는 AppCompatActivity를 상속 받는다
    * AppCompatActivity는 안드로이드의 기본 액티비티 클래스 중 하나로, 하위 호환성을 제공하며
    * 액티비티를 쉽게 관리할 수 있게 한다.*/
    private long backKeyPressedTime = 0;
    // backKeyPressedTime = 0 : 뒤로 가기 버튼이 마지막으로 눌린 시간을 기록하는 변수
    // 이를 통해 사용자가 특정시간(여기는 2500ms) 내에 뒤로 가기 버튼을 두 번 눌렀는지 판단 가능
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //onCreate : 이 메소드는 액티비티가 처음 생성될 때 호출된다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        //setContentView(R.layout.activity_title) : 레이아웃 XML 파일을 이 액티비티의 UI로 설정
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //actionBar.hide() : 액션바 제거되어 전체 화면을 사용할 수 있다.

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
        LinearLayout button4 = findViewById(R.id.정비소);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Title.this, CarCenter.class);
                startActivity(intent);
            }
        });
        /* 각 버튼 정의  LinearLayout button 으로 정의 되어 있다. findViewById 메소드를
           통해 해당 뷰를 찾아온다. 각 버튼에 대해 setOnClickListener가 설정되어 있어 버튼이 클릭
           되면 Intent를 생성해 새로운 액티비티로 전환한다. (주차장 버튼 누르면 Parking 액티비티 진행)*/

    }
    public void onBackPressed() {
    // onBackPressed() : 사용자가 뒤로 가기 버튼을 눌렀을 때 호출된다.
        //뒤로 가기 버튼이 2.5초 이내에 두 번 눌렸는지 확인
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            //두 번 연속 눌렸다면 종료 대화 상자를 띄우고, 그렇지 않다면 backKeyPressedTime 갱신
            backKeyPressedTime = System.currentTimeMillis();
            AlertDialog.Builder builder = new AlertDialog.Builder(Title.this);
            //AlertDialog.Builder 객체를 생성하고 AlertDialog는 사용자에게 메시지를 표시하고, 선택지 제공하는 대화상자를 생성하는 클래스
            builder.setMessage("정말로 종료하시겠습니까?");
            //대화상자에 표시될 메시지 설정
            builder.setTitle("종료 알림창")
            //대화상자의 제목 설정
                    .setCancelable(false)
                    //이 설정은 사용자가 대화 상자의 외부 영역을 터치하거나, 뒤로 가기 버튼을 눌러 대화 상자를 닫을 수 없도록 만든다.
                    //false로 설정했기 때문에 반드시 버튼을 눌러야 대화 상자를 닫을 수 있다.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            //Yes를 눌렀을 때 정의된 OnClickListener 호출
                            moveTaskToBack(true);
                            //이 메서드는 현재의 액티비티를 백그라운드로 이동시킨다. 즉, 현재 앱이 사용자 화면에서 사라지게 한다.
                            finish();
                            //현재 액티비를 종료한다. 이 코드로 인해 Title 액티비티 종료
                            android.os.Process.killProcess(android.os.Process.myPid());
                            //현재 앱의 프로세스를 완전히 종료한다.
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            //No를 눌렀을 때 정의된 OnClickListener 호출
                            dialog.cancel();
                            // 대화상자를 닫고, 아무런 액션도 수행하지 않음
                        }
                    });
            AlertDialog alert = builder.create();
            //대화상자 객체 생성, 이 객체 통해 대화 상자를 표시할 수 있음
            alert.setTitle("종료 알림창");
            //대화상자 제목을 다시 설정 이 줄은 앞서 builder.setTitle("종료 알림창");과 동일한 동작을 한다.
            //여기서는 굳이 다시 설정할 필요는 없지만, 명시적으로 한 번 더 설정하고 있다.
            alert.show();
            //생성된 대화 상자를 화면에 표시
            return;
        }
    }
}