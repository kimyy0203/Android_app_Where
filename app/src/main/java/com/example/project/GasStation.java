package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.CompassView;
import com.naver.maps.map.widget.LocationButtonView;
import com.naver.maps.map.widget.ScaleBarView;
import com.naver.maps.map.widget.ZoomControlView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class GasStation extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener, NaverMap.OnLocationChangeListener {
    /* OnMapReadyCallback, Overlay.OnClickListener, NaverMap.OnLocationChangeListener를 통해 해당 클래스가 액티비티로서 동작하며,
    지도 준비, 오버레이 클릭, 위치 변경 등의 이벤트를 처리할 수 있다. */
    private static final String TAG = "GasStation"; // Log 확인용 태그

    GpsTracker gpsTracker; // GpsTracker 라는 객체를 선언하여, 이 객체를 통해 사용자의 GPS 위치를 추적할 수 있다.

    private static final int PERMISSION_REQUEST_CODE = 100; // 위치 정보 액세스 등의 권한 요청 시 사용할 요청 코드이다.
    private static final String[] PERMISSIONS = { // GPS시스템은 위험 권한이므로 실행했을 때 부여받음
            // 위치 권한 요청시 필요한 권한을 정의한다.
            Manifest.permission.ACCESS_FINE_LOCATION, // 정밀한 위치, 네트워크와 GPS를 사용
            Manifest.permission.ACCESS_COARSE_LOCATION // 대략적인 위치, 네트워크만 사용
    };

    LinearLayout linearLayout; // UI에서 레이아웃을 동적으로 조작할 수 있도록 한다.

    Boolean isInitialUpdate = true; //첫번째 업데이트 판단, 초기값은 true로 설정

    Marker[] markers = new Marker[150]; // 주유소 상호
    NowGas[] nowGas = new NowGas[150]; // 주유소 정보
    Button[] navgate_btn = new Button[150]; // 주유소 길찾기 버튼

    int int_now_count = 0; // 현재 주유소 갯수

    String[] str_oilstation_name = new String[150]; // 주유소 이름
    String[] str_price = new String[150]; // 주유소 유가
    String[] str_distance = new String[150]; // 주유소 거리
    String[] str_x = new String[150]; // 주유소 x좌표
    String[] str_y = new String[150]; // 주유소 y좌표
    String[] str_trademark = new String[150]; // 주유소 상표, 계열사
    StringBuffer buffer = new StringBuffer();

    double db_user_latitude, db_user_longitude; // 현재 위치 위도 경도
    double db_station_latitude, db_station_longitude; // 주유소 위치 위도 경도

    int int_navation_option; // 길찾기 옵션 선택  1 : 네이버 맵, 2 : 카카오 맵

    private FusedLocationSource mLocationSource; // 네이버 맵 FusedLocationSource 객체
    NaverMap mNaverMap; // 네이버 맵 객체 선언

    int int_marker_index; // 마커 클릭 시 비교 숫자
    InfoWindow InfoWindow; // 마커 클릭 시 나타나는 정보 창 객체

    LinearLayout.LayoutParams linearLayoutParams;
    LinearLayout.LayoutParams linearLayoutParams1;
    LinearLayout.LayoutParams linearLayoutParams2;
    LinearLayout.LayoutParams linearLayoutParams3;

    LinearLayout linearLayouts [] = new LinearLayout[150];// 추천뷰
    LinearLayout linearLayouts1 [] =new LinearLayout[150];// 추천뷰
    LinearLayout linearLayouts2 [] =new LinearLayout[150];// 추천뷰
    LinearLayout linearLayouts3 [] =new LinearLayout[150];// 추천뷰

    TextView info [] = new TextView[150];// 주유소 상호명
    TextView info1 [] = new TextView[150];// 추천뷰 가격
    TextView info2 [] = new TextView[150];// 추천뷰 km
    TextView info3 [] = new TextView[150];// 추천뷰 상표

    /* 주유소 메인 화면이 시작 되었을 때 초기 설정 */
    int int_sort = 1; // 가격순, 거리순 초기값(가격순으로 초기화)
    String str_oil_option = "B027"; // 기름 종류 초기값(B027, 휘발유로 초기화)
    int int_radius = 1000; // 반경 초기값 단위 : m(1km 반경으로 초기화)
    ImageView imageView[]= new ImageView[150]; // 추천뷰의 마커 그림
    private long backKeyPressedTime = 0; // 뒤로가기 초기값
    private Toast toast;// 뒤로가기 토스트 메시지

    @Override
    protected void onCreate(Bundle savedInstanceState) { // onCreate 메서드는 액티비티가 생성될 때 호출된다. 따라서 앱의 초기화 작업을 수행한다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gas_main); // 주유소 메인 화면 xml를 해당 액티비티의 레이아웃으로 지정한다.

        ActionBar actionBar = getSupportActionBar(); // 액션바 생성
        actionBar.hide(); // 액션바 숨김
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 현재 기기의 안드로이드 버전이 Lollipop (API 21) 이상인지 확인한다.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            /* clearFlags는 투명한 상태 표시줄을 제거한다.
            setSystemUiVisibility는 시스템 UI의 가시성을 설정한다.
            SYSTEM_UI_FLAG_LAYOUT_STABLE과 SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 플래그를 사용하여 레이아웃이 상태바와 네비게이션바 뒤에 위치하도록 한다.
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR는 상태바의 아이콘을 어둡게 만들어 상태바의 배경이 밝을 때도 잘 보이도록 한다.
            setStatusBarColor를 사용하여 상태바의 배경색을 투명을 설정한다. */
        }

        //배열 내 객체 생성
        CreateObjectArray(); // 이는 markers, nowGas 등 여러 객체 배열을 초기화하는 역할이다.

        //마커 정보 확인 버튼
        Button button =(Button)findViewById(R.id.infomation); // infomation이라는 ID를 가진 버튼을 레이아웃에서 찾아서 button 객체에 할당한다.
        button.setOnClickListener(new View.OnClickListener() { // 버튼 클릭 시 실행될 이벤트 리스너 설정
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(GasStation.this); // AlertDialog를 생성하기 위한 빌더 객체 생성
                LayoutInflater gascolor = LayoutInflater.from(GasStation.this);
                final View view = gascolor.inflate(R.layout.gascolor,null); // gascolor 레이아웃을 인플레이트하여 대화 상자의 콘텐츠로 설정한다.
                dlg.setView(view);
                AlertDialog alertDialog = dlg.create(); // 대화 상자 생성
                alertDialog.getWindow().setGravity(Gravity.BOTTOM); // 대화 상자가 화면의 하단에 나타나도록 설정
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(255,62,79,92))); // 대화 상자의 배경을 특정 색상으로 설정
                alertDialog.show(); // 대화 상자를 화면에 표시한다.
            }
        });

        //지도 프레그먼트
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map); // 객체 생성후 R.id.map이라는 View에 추가한다.
        if (mapFragment == null) { // 만약에 R.id.map 에 해당하는 Fragment가 없다면 새로운 MapFragment를 생성하고 추가한다.
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);

        // 위치를 반환하는 구현체인 FusedLocationSource 생성과 초기화, 이를 통해 네이버 맵에서 현재 위치를 추적할 수 있도록 한다.
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        // 어플 시작 위도경도 값 저장
        gpsTracker = new GpsTracker(GasStation.this); // GpsTracker 객체를 초기화하여 현재 사용자의 위치 정보를 가져온다.
        db_user_latitude = gpsTracker.getLatitude(); // 현재 위치의 위도를 가져와 변수에 저장한다.
        db_user_longitude = gpsTracker.getLongitude(); // 현재 위치의 경도를 가져와 변수에 저장한다.

        // 검색 버튼, 원하는 옵션으로 업데이트
        Button now = findViewById(R.id.now); // now ID를 가진 버튼을 레이아웃에서 찾아 now 객체에 할당한다.
        now.setOnClickListener(new View.OnClickListener() {
            // now 버튼 클릭 시 updateNow 메서드를 호출하여 주유소 정보를 최신화하고, 토스트 메시지를 표시한다.
            @Override
            public void onClick(View v) {
                updateNow(int_sort, str_oil_option, int_radius);// 초기화 함수(가격 거리순, 기름종류, 반경 거리를 입력받아 초기화 함수 호출)
                Toast.makeText(GasStation.this, "추천뷰가 최신화 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        /* 해당 레이아웃 구성은 조건을 선택후 검색했을 때 나오는 추천뷰의 레이아웃 구성이다. */
        linearLayout = findViewById(R.id.lin); // 지도 프레그먼트 아래 추천뷰 레이아웃
        linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayoutParams3 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        // 가격 순으로 초기화된 상태에서 반경 1km, 기름 종류 B027(가솔린)을 기준으로 주유소 정보를 업데이트한다.
        updateNow(1, "B027", 1000);

        /* 아래에 있는 3개의 Spinner 객체는 각각 검색 반경, 기름 종류, 추천 기준을 선택하는 스피너 객체에 대한 설정값을 가지고 있다. */
        Spinner spinner = findViewById(R.id.추천);// 반경설정(spinner를 이용하여 1km, 3km, 5km 설정)
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){ // 거리를 나타내는 단위는 m이다.
                    int_radius = 1000; // 1000m = 1km
                } else if(position == 1){
                    int_radius = 3000; // 3000m = 3km
                } else if(position == 2){
                    int_radius = 5000; // 5000m = 5km
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 후처리 부분이지만, 초기값을 설정해 주었기 때문에 구현하지 않는다.
            }
        });

        Spinner spinner2 = findViewById(R.id.spinner2); // 기름 종류 설정(휘발유, 경유, 고급휘발유, 실내등유, 자동차부탄)
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    str_oil_option = "B027"; // 휘발유
                } else if(position == 1){
                    str_oil_option = "D047"; // 경유
                } else if(position == 2){
                    str_oil_option = "B034"; // 고급휘발유
                } else if(position == 3){
                    str_oil_option = "C004"; // 실내등유
                } else if(position == 4){
                    str_oil_option = "K015"; // 자동차부탄
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 후처리 부분이지만, 초기값을 설정해 주었기 때문에 구현하지 않는다.
            }
        });
        Spinner spinner3 = findViewById(R.id.spinner3); // 가격순, 거리순 설정
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    int_sort = 1; // 가격순
                }
                else if(position == 1){
                    int_sort = 2; // 거리순
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때의 후처리 부분이지만, 초기값을 설정해 주었기 때문에 구현하지 않는다.
            }
        });
    }

    /* onMapReady 메서드를 통해 네이버 지도가 준비 됐을 때 수행되는 작업들을 정의한다.
    이 메서드를 통해 지도 설정을 초기화하고, 사용자 인터페이스(UI) 요소를 설장한다. 또한, 위치 권한을 요청하고,
    마커 클릭시 나타날 정보창(InfoWindow)의 내용을 정의하여 사용한다.
    따라서 이 메서드는 주유소 정도를 지도에 표시하고 사용자와 상호작용할 수 있는 UI를 제공하는 역할을 수행한다. */
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) { // 네이버 지도가 준비됐을 때 호출한다. 이때 전달된 naverMap 객체를 사용하여 지도와 관련된 설정이 가능하다.
        Log.d(TAG, "onMapReady"); // log 출력, 디버깅 목적으로 로그를 출력

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        mNaverMap = naverMap; // 전달된 naverMap 객체를 mNaverMap 변수에 저장한다.
        mNaverMap.setLocationSource(mLocationSource); // 위치 소스를 mLocationSource로 설정한다. mLocationSource는 이전에 설정된 사용자 위치를 추적하는 객체이다.
        // UI 컨트롤 재배치
        mNaverMap.addOnLocationChangeListener(this); // 현재 클래스를 OnLocationChangeListener로 등록하여 위치가 변경될 때 이벤트를 처리할 수 있도록 한다.
        // UI 컨트롤 재배치
        UiSettings uiSettings = mNaverMap.getUiSettings(); // uiSettings 객체를 토앻 지도의 UI 요소들을 설정한다.
        uiSettings.setCompassEnabled(false); // 기본값 : true (기본적으로 활성화 된 나침반 비활성화)
        uiSettings.setScaleBarEnabled(false); // 기본값 : true (지도의 축척 막대를 비활성화)
        uiSettings.setZoomControlEnabled(false); // 기본값 : true (줌 컨트롤 비활성화)
        uiSettings.setLocationButtonEnabled(false); // 기본값 : false (기본적을 비활성화된 위치 버튼을 그대로 비활성화)
        uiSettings.setLogoGravity(Gravity.RIGHT | Gravity.BOTTOM); // 네이버 지도 로고의 위치를 오른쪽 하단에 위치하도록 설정한다.

        CompassView compassView = findViewById(R.id.compass);
        compassView.setMap(mNaverMap); // compassView는 나침반 뷰를 나타내며, 이를 지도와 연결한다.
        ScaleBarView scaleBarView = findViewById(R.id.scalebar);
        scaleBarView.setMap(mNaverMap); // scaleBarView는 축적 막대 뷰를 나타낸다.
        ZoomControlView zoomControlView = findViewById(R.id.zoom);
        zoomControlView.setMap(mNaverMap); // zoomControlView는 줌 컨트롤을 위한 뷰이다.
        LocationButtonView locationButtonView = findViewById(R.id.location);
        locationButtonView.setMap(mNaverMap); // locationButtonView는 현재 위치로 지도를 이동시키는 버튼을 위한 뷰이다.

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // 마커 클릭시 표시할 정보창 생성 후 설정
        InfoWindow = new InfoWindow();
        InfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            // InfoWindow.DefaultViewAdapter를 사용하고, item.xml을 해당 레이아웃으로 지정하여 텍스트뷰를 설정한다.
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) { // 마커 클릭시 정보 확인
                Marker marker = infoWindow.getMarker();
                View view = View.inflate(GasStation.this, R.layout.item, null);
                TextView title = (TextView) view.findViewById(R.id.title); // item.xml 에 있는 title 이미지
                TextView money = (TextView) view.findViewById(R.id.money); // iteml.xml에 있는 money 이미지
                title.setText("주유소이름: " + nowGas[int_marker_index].getOS());
                money.setText("가격 :" + nowGas[int_marker_index].getPrice() + "원");
                return view;
            }
        });
    }

    /* onRequestPermissionsResult 메서드는 사용자가 앱에서 요청한 권한을 승인하거나 거부한 후에 호출되는 메서드이다.
    이 메서드는 권한 요청 결과를 처리하며, 특히 위치 권환과 관련된 작업을 수행한다.
    이 코드에서는 위치 권한이 승인된 경우, 네이버 지도에서 사용자의 위치를 추적하도록 설정한다.
    이를 통해 사용자가 앱 내에서 자신의 현재 위치를 지도에서 확인할 수 있도록 한다.*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /* 이 메서드는 Activity나 Fragment에서 권한 요청 결과를 처리할 때 호출된다.
        메서드는 requestCode, permissions, grantResults라는 세 가지 매개변수를 받는다.
        requestCode : 권한 요청을 구별하기 위한 코드이다. 권한 요청 시 지정했던 코드와 동일한다.
        permissions : 요청한 권한들의 배열이다.
        grantResults : 각 권한에 대한 승인 여부가 담긴 결과 배열이다. */

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 첫 번째 if 문에서 requestCode가 이 앱에서 정의한 PERMISSION_REQUEST_CODE와 일치하는지 확인한다. 이는 해당 요청이 위치 권환 요청인지 확인한다.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 두 번째 if 문에서는 grantResults 배열의 첫 번째 요소가 PackageManager.PERMISSION_GRANTED와 일치하는지 확인한다. 이는 사용자가 권한을 승인했는지를 의미한다.
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                /* 만약 권한이 승인되었다면, mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow); 메서드를 호출하여 네이버 지도의 위치 추적 모드를 활성화한다.
                이 모드는 사용자의 현재 위치를 따라가도록 지도의 화면을 자동으로 업데이트한다. */
            }
        }
    }

    /* onClick 메서드는 지도에 표시된 마커(정비소 위치)를 클릭했을 때 호출된다.
    사용자가 마커를 클릭하면 해당 마커의 좌표(위도, 경도)를 추출하여 저장하고, 보여주고 있는 정보창의 상태(창을 계속 표시하거나, 닫는다)를 결정한다. */
    @Override
    // 이 메서드는 Overlay.OnClickListener 인터페이스의 onClick 메서드를 오버라이드한 것이다.
    public boolean onClick(@NonNull Overlay overlay) { // 지도 위의 오버레이(이 경우에는 마커)가 클릭되었을 때 호출된다.
        if (overlay instanceof Marker) {
            // 선택된 해당 마커의 좌표(위도, 경도)정보를 미리 만들어 둔 변수에 저장
            LatLng aa = ((Marker) overlay).getPosition();
            db_station_latitude = aa.latitude;
            db_station_longitude = aa.longitude;
            Marker marker = (Marker) overlay;
            // overlay가 마커인지 확인한 후, 클릭된 마커의 위치(LatLng)를 가져온다. 이 위치 정보를 통해 위도(db_station_latitude)와 경도(db_station_longitude)를 추출한다.
            for (int k = 0; k < int_now_count; k++) {
                if ((nowGas[k].getX() == db_station_latitude) && (nowGas[k].getY() == db_station_longitude)) {
                    int_marker_index = k;
                }
            } // 현재 주유소의 개수(int_now_count)만큼 반복하면서, 클린되 마커의 해당 좌표와 일치하는 주유소 정보를 찾는다.
            if (marker.getInfoWindow() != null) { // 현재 마커에 정보 창이 열려 있는지 확인하고 만약 열려 있으면 닫고, 그렇지 않으면 열도록 처리한다.
                InfoWindow.close();
                Toast.makeText(this.getApplicationContext(), "정보창을 닫습니다.", Toast.LENGTH_LONG).show();
            } else {
                InfoWindow.open(marker);
                Toast.makeText(this.getApplicationContext(), "선택한 주유소입니다..", Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return false;
    }

    /* nowGetXmlData는 주유소 정보 제공 사이트인 오피넷의 API를 통해 주유소 정보를 XML 형식으로 받아 이를 파싱한다.
    각 주유소의 상표, 상호, 판매가격, 현재 위치로부터의 거리, 위도, 경도 등의 정보를 배열에 저장한다.  */
    public void nowGetXmlData(double lat, double lon, int sort, String prodcd, int km) { // 주유소 정보 데이터 파싱
        String queryUrl = "http://www.opinet.co.kr/api/aroundAll.do?code=F960210322&x=" + lat + "&y=" + lon + "&radius="+km+"&sort="+sort+"&prodcd="+prodcd+"&out=xml"; // 파싱 URL
        /* 이때 lat, lon은 GpsTracker를 통해 얻은 현재 사용자의 위치의 위도, 경도 좌표이다.
        또한 km는 검색하고자 하는 반경을 의미하고, sort는 정렬 기준(거리순, 가격순), pordcd는 유종(기름 종류)코드를 의미한다.
        이러한 조건은 데이터 파싱을 할때 조건으로 같이 넣어 API를 통해 정보를 요청하면 해당되는 정보만 받을 수 있다. */
        try {
            URL url = new URL(queryUrl); // 문자열로 된 요청 url을 URL 객체로 생성.
            InputStream is = url.openStream(); // 이 URL로부터 데이터를 읽기 위해 입력 스트림(InputStream)을 연다. InputStream은 데이터를 바이트 단위로 읽는다.

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); // xml파싱을 위한 객체 생성
            XmlPullParser xpp = factory.newPullParser(); // 이를 통해 XmlPullParser 객체를 생성
            xpp.setInput(new InputStreamReader(is, "UTF-8")); // 이 파서에 입력 스트림을 연결하여 XML 데이터를 UTF-8 인코딩을 읽을 수 있다. (inputstream 으로부터 xml 입력받기)

            String tag;

            xpp.next(); // xpp.next()를 호출하여 XML의 첫 번째 이벤트로 이동한다.
            int eventType = xpp.getEventType(); // 해당 메서드를 사용하여 현재 이벤트 타입을 가져온다.
            while (eventType != XmlPullParser.END_DOCUMENT) { // while 문을 통해 XML 문서의 끝까지(END_DOCUMENT) 파싱을 계속한다.
                switch (eventType) { // 이벤트의 타입을 기준으로 switch-case문을 사용한다.
                    case XmlPullParser.START_DOCUMENT:
                        buffer.append("파싱 시작...\n\n"); // 문서의 시작을 나타내는 이벤트가 발생하면, buffer에 파싱 시작이라는 메시지를 추가한다.
                        break;

                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); // 해당 이벤트가 발생하면 현재 태그의 이름을 가져와 tag 변수에 저장한다.

                        if (tag.equals("OIL")) ;// 첫번째 검색결과
                        else if (tag.equals("POLL_DIV_CO")) {
                            xpp.next();
                            str_trademark[int_now_count] = new String(); //상표 저장
                            str_trademark[int_now_count] = xpp.getText();
                        }
                        else if (tag.equals("OS_NM")) {
                            xpp.next();
                            str_oilstation_name[int_now_count] = new String(); // 상호 저장
                            str_oilstation_name[int_now_count] = xpp.getText();
                        } else if (tag.equals("PRICE")) {
                            xpp.next();
                            str_price[int_now_count] = new String(); // 판매가격 저장
                            str_price[int_now_count] = xpp.getText();
                        } else if (tag.equals("DISTANCE")) {
                            xpp.next();
                            str_distance[int_now_count] = new String(); // 현재위치로 부터 거리 저장
                            str_distance[int_now_count] = xpp.getText();
                        } else if (tag.equals("GIS_X_COOR")) {
                            xpp.next();
                            str_x[int_now_count] = new String(); // x좌표 저장
                            str_x[int_now_count] = xpp.getText();
                        } else if (tag.equals("GIS_Y_COOR")) {
                            xpp.next();
                            str_y[int_now_count] = new String(); // y 좌표 저장
                            str_y[int_now_count] = xpp.getText();
                            int_now_count++;
                        }
                        /* 주유소의 정보를 제공해주는 API의 변수 명들을 보고, 내가 얻고자하는 정보를 필터링 하여 미리 만들어 둔 변수에 저장한다.
                        int_now_count 변수를 통해 들어오는 주유소의 수를 세어가며 진행하고, int_now_count를 증가시켜 다음 데이터를 저장할 위치를 지정한다. */
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG: // 해당 이벤트가 발생하면, 태그 이름을 가져온다.
                        tag = xpp.getName(); //테그 이름 얻어오기
                        if (tag.equals("OIL")) buffer.append("\n"); // 만약 태그가 item이라면, 버퍼에 줄바꿈 문자를 추가여 각 정비소 정보를 구분한다. 첫번째 검색결과종료..줄바꿈
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 파싱 중 예외가 발생할 경우, 이를 catch하여 스택 트레이스를 출력한다.
        }

        buffer.append("파싱 끝\n");
        // 파싱이 끝나면 buffer에 파싱 끝 메시지를 추가한다.
    } // getXmlData method....

    /* updatemarkers는 마커를 찍는 메서드이다. 상표명에 따라 해당 이미지도 마커를 생성하도록 한다. */
    public void updatemarkers(int i) { // 마커 찍는 함수
        markers[i] = new Marker(); // amrkeers 배열의 인덱스 i에 새로운 Marker 객체를 생성하여 할당한다.
        markers[i].setPosition(new LatLng(nowGas[i].getX(), nowGas[i].getY())); // nowGas[i]의 좌표를 사용하여 Marker의 위치를 설정한다.
        markers[i].setMap(mNaverMap); // Marker를 mNaverMap에 추가하여 지도에 표시한다.
        markers[i].setWidth(170); // Marker의 폭과 높이를 설정한다.
        markers[i].setHeight(170);
        if (str_trademark[i].equals("SKE") ){ //  상표명에 따라 마커 이미지 설정
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.skfinal2_foreground));
        }else if(str_trademark[i].equals("GSC")) {
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.gsfinal2_foreground));
        }else if(str_trademark[i].equals("HDO")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.hunfinal2_foreground));
        }else if(str_trademark[i].equals("SOL")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.soilfinal2_foreground));
        }else if(str_trademark[i].equals("RTO")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.salefinal2_foreground));
        }else if(str_trademark[i].equals("RTX")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.roadfinal2_foreground));
        }else if(str_trademark[i].equals("NHO")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.nongfinal2_foreground));
        }else if(str_trademark[i].equals("ETC")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.selffinal2_foreground));
        }else if(str_trademark[i].equals("E1G")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.e1final4_foreground));
        }else if(str_trademark[i].equals("SKG")){
            markers[i].setIcon(OverlayImage.fromResource(R.mipmap.skgfinal2_foreground));
        }
        markers[i].setOnClickListener(this); // 마커에 클릭 리스너를 설정하여 클릭 시 특정 행동을 수행하도록 한다.
    }

    /* updateNow 메서드는 추천 뷰와 마커를 초기화 하는 기능을 수행한다.
    이 메서드 사용자가 선택한 정렬 기준, 상품 코드 , 그리고 거리 정보에 따라 주변 주유소 정보를 갱신하고,
    이를 지도와 추천 목록에 표시하는 기능을 구현한다. 초기 업데이트 시에는  기존 마커를 제거하고, 새로 가져온 데이터를 기반으로 마커를 다시 추가한다.
    주유소 정보는 거리 및 가격을 기준으로 정렬되며, UI 스레드에서 동적으로 추천 목록을 구성하여 사용자에게 보여준다.
    또한 사용자가 특정 주유소를 선택하면, 지도에서 해당 위치로 이동하고, 주유소에 대한 상세 정보를 제공한다.
    이때, 사용자가 원한다면 해당 마커의 좌표로 길찾기 기능을 수행할 수 있도록 네비게이션 앱(카카오 맵, 네이버 지도)으로 연동하여 진행할 수 있다. */
    public void updateNow(int sort, String prodcd, int km){ // 추천뷰, 마커 초기화 함수
        if(isInitialUpdate == false) { //초기 업데이트가 아닐 시 마커 초기화
            for (int i = 0; i < int_now_count; i++) { // 마커 초기화
                markers[i].setMap(null);
            }
        }
        else{ //초기 업데이트시
            isInitialUpdate = false;
        }

        // 새 스레드를 생성하여 백그라운드 작업을 수행한다.
        new Thread(new Runnable() {
            @Override
            public void run() {
                int_now_count = 0;
                LatLng latLng = new LatLng(db_user_latitude, db_user_longitude);
                Tm128 tm128 = Tm128.valueOf(latLng);
                nowGetXmlData(tm128.x,tm128.y,sort,prodcd,km);
                /* 이때 사용자 위치를 LatLng 객체로 설정하고, 이를 Tm128로 변환한다.
                이를 통해 현재 사용자 위치를 기준으로 API에 정보를 전달하고, 검색 기준에 맞춘 정보를 받아와 데이터 업데이트를 요청할 수 있다. */

                runOnUiThread(new Runnable() { // UI 스레드에서 실행되어 추천 뷰를 초기화 한다.
                    @Override
                    public void run() {
                        if (int_now_count != 0) {
                            linearLayout.removeAllViews();// 추천뷰 초기화
                            if (sort ==1) { // sort(정렬 기준 : 가격순, 거리순)값이 1(가격순)일 때 가격 기준으로 정렬한다.
                                for (int i = 0; i < int_now_count; i++) { // 이때 동일한 가격의 주유소가 있을 때, 거리가 더 짧은 주유소가 앞에 오도록 한다.
                                    for (int j = 0; j < int_now_count; j++) {
                                        if (Float.parseFloat(str_distance[i]) < Float.parseFloat(str_distance[j]) && Integer.parseInt(str_price[i]) == Integer.parseInt(str_price[j])) {
                                            String temp = str_distance[i]; str_distance[i] = str_distance[j]; str_distance[j] = temp;
                                            temp = str_oilstation_name[i]; str_oilstation_name[i] = str_oilstation_name[j]; str_oilstation_name[j] = temp;
                                            temp = str_price[i]; str_price[i] = str_price[j]; str_price[j] = temp;
                                            temp = str_x[i]; str_x[i] = str_x[j]; str_x[j] = temp;
                                            temp = str_y[i]; str_y[i] = str_y[j]; str_y[j] = temp;
                                            temp = str_trademark[i]; str_trademark[i] = str_trademark[j]; str_trademark[j] = temp;
                                        }
                                    }
                                }
                            }
                            for (int i = 0; i < int_now_count; i++) {
                                int ci = i;
                                nowGas[i] = new NowGas();
                                Tm128 tm128 = new Tm128(Double.parseDouble(str_x[i]), Double.parseDouble(str_y[i]));
                                LatLng latLng = tm128.toLatLng();
                                nowGas[i].setX(latLng.latitude);
                                nowGas[i].setY(latLng.longitude);
                                nowGas[i].setOS(str_oilstation_name[i]);
                                nowGas[i].setPrice(Integer.parseInt(str_price[i]));
                                nowGas[i].setDistance(Float.parseFloat(str_distance[i]));
                                updatemarkers(i);
                                // NowGas 객체를 생성하고, 각 데이터를 설정한다. 이때 upadatemakers 메서드를 호출하여 마커를 업데이트 한다.

                                linearLayouts[i] = new LinearLayout(GasStation.this);
                                linearLayouts[i].setOrientation(linearLayouts[i].HORIZONTAL);
                                linearLayouts1[i] = new LinearLayout(GasStation.this);
                                linearLayouts1[i].setOrientation(linearLayouts[i].HORIZONTAL);
                                linearLayoutParams3.width = 650;  linearLayoutParams3.height = 180;
                                linearLayouts1[i].setLayoutParams(linearLayoutParams3);
                                linearLayouts2[i] = new LinearLayout(GasStation.this);
                                linearLayouts2[i].setOrientation(linearLayouts[i].VERTICAL);
                                linearLayouts3[i] = new LinearLayout(GasStation.this);
                                linearLayouts3[i].setOrientation(linearLayouts[i].VERTICAL);
                                info3[i] = new TextView(GasStation.this);
                                // LinearLayout과 TextView를 생성하여 각 뷰의 속성을 설정한다.

                                if (str_trademark[i].equals("SKE") ){
                                    info3[i].setText("SK에너지");
                                }else if(str_trademark[i].equals("GSC")) {
                                    info3[i].setText("GS칼텍스");
                                }else if(str_trademark[i].equals("HDO")){
                                    info3[i].setText("현대오일");
                                }else if(str_trademark[i].equals("SOL")){
                                    info3[i].setText("S_OIL");
                                }else if(str_trademark[i].equals("RTO")){
                                    info3[i].setText("자영알뜰");
                                }else if(str_trademark[i].equals("RTX")){
                                    info3[i].setText("고속도로알뜰");
                                }else if(str_trademark[i].equals("NHO")){
                                    info3[i].setText("NH알뜰");
                                }else if(str_trademark[i].equals("ETC")){
                                    info3[i].setText("자가상표");
                                }else if(str_trademark[i].equals("E1G")){
                                    info3[i].setText("E1");
                                }else if(str_trademark[i].equals("SKG")){
                                    info3[i].setText("SK가스");
                                }
                                info3[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP,14.0f);
                                info3[i].setLayoutParams(linearLayoutParams);
                                info3[i].setTextColor(Color.WHITE);
                                info3[i].setBackground(ContextCompat.getDrawable(GasStation.this, R.drawable.shape));
                                linearLayouts3[i].addView(info3[i]);
                                info[i] = new TextView(GasStation.this);
                                info[i].setText(nowGas[i].getOS());
                                info[i].setTextColor(Color.BLACK);
                                info[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP,15.0f);
                                info[i].setLayoutParams(linearLayoutParams);
                                navgate_btn[i] = new Button(GasStation.this);
                                linearLayoutParams2.width = 120; linearLayoutParams2.height = 100;
                                linearLayoutParams2.topMargin = 10;
                                navgate_btn[i].setText("길찾기");
                                navgate_btn[i].setTextSize(8);
                                navgate_btn[i].setBackgroundColor(Color.BLACK);
                                navgate_btn[i].setTextColor(Color.WHITE);
                                navgate_btn[i].setLayoutParams(linearLayoutParams2);
                                navgate_btn[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        /* 해당 마커로 길찾기를 누르면 사용자가 원하는 네비게이션 앱(카카오 맵, 네이버 지도)으로 넘어가 길찾는 작업을 수행하도록 한다. */
                                        db_station_latitude = nowGas[ci].getX();
                                        db_station_longitude = nowGas[ci].getY();
                                        AlertDialog.Builder dlg = new AlertDialog.Builder(GasStation.this);
                                        dlg.setTitle("길찾기"); //제목
                                        final String[] versionArray = new String[]{"카카오 맵", "네이버 지도"};

                                        dlg.setSingleChoiceItems(versionArray, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int a) {
                                                int_navation_option = a;
                                            }
                                        });

                                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int a) { //길찾기 버튼 클릭시 주소값을 intent하여 다른 네비게이션 어플(카카오 맵, 네이버 지도)과 연동
                                                //토스트 메시지
                                                /* 사용자가 선택한 네비게이션 앱으로 목적지까지의 길차기를 수행한다. 앱이 설치되어 있지 않은 경우에는 해당 앱의 다운로드 페이지로 연결한다. */
                                                Intent intent;
                                                if (int_navation_option == 0) {
                                                    try {
                                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("kakaomap://route?sp=" + db_user_latitude + "," + db_user_longitude + "&ep=" + db_station_latitude + "," + db_station_longitude + "&by=CAR"));
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    } catch (Exception e) {
                                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko"));
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                }

                                                else if (int_navation_option == 1) {
                                                    try {
                                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("nmap://navigation?dlat=" + db_station_latitude + "&dlng=" + db_station_longitude + "&dname=목적지&appname=com.example.MapProject"));
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    } catch (Exception e) {
                                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.skt.tmap.ku&hl=ko"));
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                    }
                                                }
                                                Toast.makeText(GasStation.this, "확인을 눌르셨습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dlg.show();
                                    }
                                });
                                linearLayouts3[i].addView(navgate_btn[i]);
                                linearLayouts1[i].addView(linearLayouts3[i]);
                                linearLayouts1[i].addView(info[i]);
                                linearLayoutParams1.width = 220; linearLayoutParams1.height = 150;
                                imageView[i] = new ImageView(GasStation.this);
                                if (str_trademark[i].equals("SKE") ){
                                    imageView[i].setImageResource(R.mipmap.sk);
                                }else if(str_trademark[i].equals("GSC")) {
                                    imageView[i].setImageResource(R.mipmap.gs);
                                }else if(str_trademark[i].equals("HDO")){
                                    imageView[i].setImageResource(R.mipmap.hun);
                                }else if(str_trademark[i].equals("SOL")){
                                    imageView[i].setImageResource(R.mipmap.soil);
                                }else if(str_trademark[i].equals("RTO")){
                                    imageView[i].setImageResource(R.mipmap.sale);
                                }else if(str_trademark[i].equals("RTX")){
                                    imageView[i].setImageResource(R.mipmap.road);
                                }else if(str_trademark[i].equals("NHO")){
                                    imageView[i].setImageResource(R.mipmap.nong);
                                }else if(str_trademark[i].equals("ETC")){
                                    imageView[i].setImageResource(R.mipmap.self);
                                }else if(str_trademark[i].equals("E1G")){
                                    imageView[i].setImageResource(R.mipmap.e1);
                                }else if(str_trademark[i].equals("SKG")){
                                    imageView[i].setImageResource(R.mipmap.skg);
                                }
                                imageView[i].setLayoutParams(linearLayoutParams1);
                                info1[i] = new TextView(GasStation.this);
                                info1[i].setText(nowGas[i].price+"원");
                                info1[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP,19.0f);
                                info1[i].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                                info1[i].setTextColor(Color.BLACK);
                                info1[i].setLayoutParams(linearLayoutParams);
                                linearLayouts2[i].addView(info1[i]);
                                info2[i] = new TextView(GasStation.this);
                                float nNumber = nowGas[i].distance/1000;
                                String strNumber = String.format("%.2f", nNumber);
                                info2[i].setText(strNumber+"km");
                                info2[i].setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                                info2[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP,19.0f);
                                info2[i].setTextColor(Color.BLACK);
                                info2[i].setLayoutParams(linearLayoutParams);
                                linearLayouts2[i].addView(info2[i]);
                                linearLayouts[i].setBackground(ContextCompat.getDrawable(GasStation.this, R.drawable.main));
                                linearLayouts[i].addView(imageView[i]);
                                linearLayouts[i].addView(linearLayouts1[i]);
                                linearLayouts[i].addView(linearLayouts2[i]);
                                linearLayouts[i].setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(nowGas[ci].getX(), nowGas[ci].getY())).animate(CameraAnimation.Easing);
                                        mNaverMap.moveCamera(cameraUpdate);
                                        for (int i = 0; i < int_now_count; i++) {
                                            Tm128 tm128 = new Tm128(Double.parseDouble(str_x[i]), Double.parseDouble(str_y[i]));
                                            LatLng latLng = tm128.toLatLng();
                                            if (nowGas[ci].getX() == latLng.latitude && nowGas[ci].getY() == latLng.longitude) {
                                                int_marker_index = i;
                                                db_station_latitude = markers[i].getPosition().latitude;
                                                db_station_longitude = markers[i].getPosition().longitude;
                                                if (markers[i].getInfoWindow() == null) {
                                                    InfoWindow.open(markers[int_marker_index]);
                                                }
                                            }
                                        }
                                    }
                                });
                                linearLayout.addView(linearLayouts[i]);
                            }
                        }
                        else{
                            linearLayout.removeAllViews();
                            TextView asd = new TextView(GasStation.this);
                            asd.setText("주변에 주유소가 없습니다.");
                            asd.setLayoutParams(linearLayoutParams);
                            linearLayout.addView(asd);
                        }
                    }
                });
            }
        }).start();
    }

    /* 해당 메서드는 사용자의 현재 위치가 변경될 때 자동으로 호출되는 메서드이다. */
    @Override
    public void onLocationChange(@NonNull Location location) { // 현재위치 변경 시, 자동 호출 함수
        db_user_latitude = location.getLatitude();
        db_user_longitude = location.getLongitude();
    }

    /* onBackPressed 메서드는 사용자가 '뒤로 가기' 버튼을 눌렀을 때 앱이 바로 종료되지 않도록 하고,
    2.5초 이내에 다시 한 번 '뒤로 가기' 버튼을 누르면 앱을 종료하는 기능을 구현한 것이다.
    이는 사용자가 실수로 앱을 종료하는 것을 방지하기 위한 일반적인 방법이다. */
    public void onBackPressed() { // 뒤로가기 함수
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
            /* 사용자가 '뒤로 가기' 버튼을 눌렀을 때 현재 시간(System.currentTimeMillis())이 backKeyPressedTime에 2.5초를 더한 시간보다 큰지 확인한다.
            이 조건이 참이라면, 사용자가 마지막으로 '뒤로 가기' 버튼을 누른 후 2.5초가 지났다는 뜻이다.
             따라서  backKeyPressedTime을 현재 시간으로 업데이트하고, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다."라는 메시지를 토스트로 화면에 보여준다.
             그리고 return 문을 사용해 메서드를 종료한다. 이는 첫 번째 버튼 클릭 후 앱이 종료되지 않도록 하기 위함이다. */
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
            toast.cancel();
            toast = Toast.makeText(this, "이용해 주셔서 감사합니다.", Toast.LENGTH_LONG);
            toast.show();
            /* 사용자가 2.5초 이내에 다시 '뒤로 가기' 버튼을 누른 경우, 조건문이 참이 되며 앱을 종료하는 finish() 메서드를 호출한다.
            이때, 이전에 보여주던 토스트 메시지를 toast.cancel()로 취소하고, "이용해 주셔서 감사합니다."라는 메시지를 새로운 토스토로 보여준다. */
        }
    }

    /* 이 메서드는 여러 개의 배열을 초기화하고, 각 배열의 요소에 객체를 할당한다.
    총 150개의 배열 요소가 있으며, 이를 통해 UI와 관련된 다양한 요소들이 초기화 된다. */
    public void CreateObjectArray(){
        for(int i = 0 ; i < 150; i++){
            info[i] = new TextView(GasStation.this);
            info1[i] = new TextView(GasStation.this);
            info2[i] = new TextView(GasStation.this);
            info3[i] = new TextView(GasStation.this);
            /* TextView 배열 info, info1, info2, info3의 각 요소를 GasStation 액티비티 컨텍스트를 사용하여 초기화한다.
            각각의 TextView는 추천뷰의 정보를 나타낼 변수이다. */

            linearLayouts [i] = new LinearLayout(GasStation.this);
            linearLayouts1 [i] = new LinearLayout(GasStation.this);
            linearLayouts2 [i] = new LinearLayout(GasStation.this);
            linearLayouts3 [i] = new LinearLayout(GasStation.this);
            /* LinearLayout 배열 linearLayouts, linearLayouts1, linearLayouts2, linearLayouts3의 각 요소를 초기화한다.
            이 레이아웃들은 추천뷰의 UI를 구성하는 데 사용된다. */

            str_oilstation_name[i] = new String(); // 주유소 이름
            str_price[i] = new String(); // 주유소 유가
            str_distance[i] = new String(); // 주유소 거리
            str_x[i] = new String(); // 주유소 x좌표
            str_y[i] = new String(); // 주유소 y좌표
            str_trademark[i] = new String(); // 주유소 상표, 계열사

            markers[i] = new Marker(); // Marker 배열의 각 요소를 초기화한다. 이 마커들은 지도 상에 주유소 위치를 표시한다.
            nowGas[i] = new NowGas(); // NowGas 배열의 각 요소를 초기화한다. 이 객체는 주유소에 대한 상세 정보를 저장한다.
            navgate_btn[i]  = new Button(GasStation.this); // 길찾기 기능을 제공하는 버튼이다.
            imageView[i]= new ImageView(GasStation.this); // ImageView 배열의 각 요소를 초기화한다. 추천뷰의 마커 이미지를 표시하는 데 사용된다.
        }
    }
}


