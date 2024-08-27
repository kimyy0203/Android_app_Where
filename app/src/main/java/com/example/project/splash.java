package com.example.project;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {
/* splash 클래스는 AppCompatActivity를 상속받는다. AppCompatActivity는
 안드로이드의 기본 액티비티 클래스 중 하나로, 액티비티에서 사용할 수 있는 여러 기능들을 제공한다.*/
    Animation anim;
    //anim : 애니메이션 객체를 저장하기 위한 변수이다. 이 변수에 fade_in 애니메이션이 할당될 예정
    LinearLayout linearLayout;
    //linearLayout : 스플래시 화면의 루트 레이아웃을 참조하는 변수이다.
    private long backKeyPressedTime = 0;
    /* backKeyPressedTime: 뒤로 가기 버튼이 마지막으로 눌린 시간을 저장하는 변수이다.
    이 변수는 뒤로 가기 버튼을 두 번 누르는 것을 감지하는 데 사용된다.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    //onCreate 메소드는 액티비티가 처음 생성될 때 호출된다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /* setContentView(R.layout.activity_splash) : 스플래시화면의 레이아웃을 설정한다.
        여기서는 activity_splash.xml 레이아웃 파일을 사용합니다.*/
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        // 액션바 숨김. 스플래시 화면에는 액션바를 보통 숨김.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT); }
        /*이 블록은 안드로이드 롤리팝(Lollipop) 버전 이상에서 상태 표시줄(Status Bar)을 투명하게 설정하고,
         전체 화면 레이아웃을 적용하기 위한 설정이다.
         setStatusBarColor(Color.TRANSPARENT): 상태 표시줄의 배경색을 투명으로 설정한다.*/
        linearLayout=(LinearLayout)findViewById(R.id.activity_splash);
        //linearLayout:  스플래시 화면의 루트 레이아웃(activity_splash)을 찾는다.
        anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        //anim: fade_in 애니메이션을 로드한다. 애니메이션은 R.anim.fade_in XML 파일에서 정의된 페이드인 효과를 적용한다.
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(splash.this,Title.class));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            /*onAnimationStart: 애니메이션이 시작될 때 호출된다. 현재는 아무런 동작도 정의되어 있지 않는다.
              onAnimationEnd: 애니메이션이 끝났을 때 호출된다. 여기서는 스플래시 화면이 끝나면 메인 화면(Title 액티비티)으로 이동한다.
              startActivity(new Intent(splash.this, Title.class));는 Intent를 사용해 Title 액티비티를 시작한다.
              onAnimationRepeat: 애니메이션이 반복될 때 호출된다. 현재는 아무런 동작도 정의되어 있지 않는다.*/
        });
        linearLayout.startAnimation(anim);
    }


}