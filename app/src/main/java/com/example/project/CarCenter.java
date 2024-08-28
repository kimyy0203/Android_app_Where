package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/* 정비소에 대한 정보를 알려주는 메인 화면 동작을 구현한 클래스이다. 해당 CarCenter 클래스는 공공데이터포탈에서 전국 정비소 정보를 API 방식으로
해당 정보를 XML 파일로 받아온다. 이렇게 받아온 XML 파일을 데이터파싱을 통해 앱에서 나타내고자 하는 정보만 추출하고, 앱으로 표시될 수 있도록 가공한다.
이때 지도는 네이버 맵을 사용했으며, API를 통해 받아온 정비소의 정보들을 위도, 경도의 값을 통해 지도 위에 마커로 표시되게끔 했다.
표시된 마커를 클릭시 해당 위치에 있는 정비소의 정보를 볼 수 있으며, 원한다면 길찾기 버튼을 눌러 네비게이션 앱(카카오 맵, 네이버 지도)을 통해
길찾기 서비스를 제공 받을 수 있도록 다른 앱으로 넘어갈 수 있다.
또한, 이러한 방식은 앱이 시작되었을 때 비동기식 작업으로 전국에 있는 정비소 정보를 전부 마커로 표시하고, 네이버 지도 위에 표시 된 마커를 보고
사용자가 원하는 정비소를 찾게 하는 것이다. */
public class CarCenter extends AppCompatActivity implements OnMapReadyCallback, Overlay.OnClickListener {
    /* OnMapReadyCallback과 Overlay.OnClickListener 인터페이스를 구현하여 지도와 오버레이(마커 등)와 관련된 기능을 처리한다. */
    private static final String TAG = "CarCenter"; // 로그 출력을 위해 사용되는 태그이다.
    private static final int PERMISSION_REQUEST_CODE = 100; // 위치 권한을 요청할 때 사용하 요청 코드이다. 권한 요청 결과를 처리할 때 이 코드로 식별 가능
    private static final String[] PERMISSIONS = {
            // 위치 권환 요청시 필요한 권한을 정의한다.
            Manifest.permission.ACCESS_FINE_LOCATION, // 정밀한 위치, 네트워크와 GPS를 사용
            Manifest.permission.ACCESS_COARSE_LOCATION // 대략적인 위치, 네트워크만 사용
    };
    StringBuffer buffer = new StringBuffer();
    String[] name = new String[20000]; // 정비소 명
    String[] lati = new String[20000]; // 정비소 위도
    String[] lontude = new String[20000]; // 정비소 경도
    String[] lnmadr = new String[20000]; // 정비소 주소
    String[] phoneN = new String[20000]; // 정비소 전화번호
    String[] type = new String[20000]; // 정비소 종류
    String[] open = new String[20000]; // 정비소 시작 시간
    String[] close = new String[20000]; // 정비소 닫는 시각
    String[] state = new String[20000]; // 정비소 상태
    Marker[] markers = new Marker[20000]; // 지도 위에 표시될 마커

    int count = 0; // 정비소 정보의 수

    private FusedLocationSource mLocationSource; // 사용자의 현재 위치 제공
    private NaverMap mNaverMap; // 네이버 지도
    private long backKeyPressedTime = 0; // 뒤로 가기 버튼이 마지막으로 눌린 시간
    private Toast toast; // 사용자에게 짧은 메시지를 보여주는 역할, 뒤로 가기 버튼을 두 번 눌러 종료할 때 사용
    com.naver.maps.map.overlay.InfoWindow InfoWindow; // 지도에서 마커를 클릭했을 때 해당 위치의 추가 정보를 보여주는 창을 생성
    double lat, lon; // 선택한 마커의 위도 경도
    int as = 0; //마커클릭시 비교 숫자
    int Mnumber;
    int wash1 = 0;
    ProgressDialog progressDialog; // 긴 작업이 진행 중임을 사용자에게 알리기 위해 표시되는 객체, 다이얼로그

    /* onCreate 메서드는 안드로이드 앱의 메인 액티비티가 생성될 때 수행되는 작업들을 정의한다.
    따라서 먼저 기본 UI 설정과 관련된 작업을 수행하고, 상태바의 색상과 가시성을 조정한다.
    그 후에 지도 객체를 생성하고 이를 통해 네이버 맵을 사용하며, 기기의 위치 정보또한 가져온다.
    이때 비동기 작업을 수행하는 MyAsyncTask를 실행하여 추가적인 백그라운드 작업을 수행한다.*/
    protected void onCreate(Bundle savedInstanceState) { // onCreate는 액티비티가 처음 생성될 때 호출된다.
        // 여기서 Bundle 객체는 이전에 저장된 인스턴스 상태를 전달한다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carcenter_main); // 카센터 메인 화면 xml로 해당 액티비티의 레이아웃을 지정한다.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 현재 기기의 안드로이드 버전이 Lollipop (API 21) 이상인지 확인한다.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            /* clearFlags는 투명한 상태 표시줄을 제거한다.
            setSystemUiVisibility는 시스템 UI의 가시성을 설정한다.
            SYSTEM_UI_FLAG_LAYOUT_STABLE과 SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN 플래그를 사용하여 레이아웃이 상태바와 네비게이션바 뒤에 위치하도록 한다.
            SYSTEM_UI_FLAG_LIGHT_STATUS_BAR는 상태바의 아이콘을 어둡게 만들어 상태바의 배경이 밝을 때도 잘 보이도록 한다.
            setStatusBarColor를 사용하여 상태바의 배경색을 투명을 설정한다. */
        }

        // 지도 객체 생성
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
        CarCenter.MyAsyncTask asyncTask = new CarCenter.MyAsyncTask();
        asyncTask.execute();
        //이 부분 MyAsyncTask 찾아보기
    }

    /* onMapReady 메서드를 통해 네이버 지도가 준비 됐을 때 수행되는 작업들을 정의한다.
        또한 지도와 관련된 UI 설정, 사용자 위치, 마커 클릭 시 표시될 정보 창 등을 처리한다.
        이를 통해 사용자는 지도에서 정비소 위치를 확인하고, 마커를 클릭했을 때 정비소에 대한 세부 정보를 볼 수 있게 된다.*/
    public void onMapReady(@NonNull NaverMap naverMap) { // 네이버 지도가 준비됐을 때 호출한다. 이때 전달된 naverMap 객체를 사용하여 지도와 관련된 설정이 가능하다.
        Log.d(TAG, "onMapReady"); // 디버깅 목적으로 로그를 출력한다.
        // 지도상에 마커 표시
        mNaverMap = naverMap; // 전달된 naverMap 객체를 mNaverMap 변수에 저장한다.
        mNaverMap.setLocationSource(mLocationSource); // 위치 소스를 mLocationSource로 설정한다. mLocationSource는 이전에 설정된 사용자 위치를 추적하는 객체이다.
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
        LatLng initialPosition = new LatLng(37.506855, 127.066242);
        // 앱 시작시 위치한 지도의 화면을 해당 위도, 경도로 설정한다. 이 좌표는 임의로 변경이 가능하며, 현재 설정되 좌표는 '빅히트 엔터테이먼트'의 위치이다.
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition); // CameraUpdate.scrollTo 메서드를 통해 카메라를 해당 위치로 부드럽게 이동시킨다.
        naverMap.moveCamera(cameraUpdate);

        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        InfoWindow = new InfoWindow(); // 마커 클릭시 보여지는 기본정보창
        InfoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            // InfoWindow.DefaultViewAdapter를 사용하고, item2.xml을 해당 레이아웃으로 지정하여 텍스트뷰를 설정한다.
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                View view = View.inflate(com.example.project.CarCenter.this, R.layout.item2, null);
                TextView title = (TextView) view.findViewById(R.id.prkplceNm);
                TextView money = (TextView) view.findViewById(R.id.operday);
                title.setText("정비소이름 : " + name[as]);
                money.setText("정비소주소 : " + lnmadr[as]);
                return view;
            }
        });
    }

    /* getXmlData는 외부 API로부터 전국 정비소 정보를 XML 형식으로 받아와 이를 파싱한다.
    각 정비소의 이름, 종류, 주소, 전화번호, 위도, 경도 영업 시작/종료 시간, 상태 등의 정보를 배열에 저장한다.
    이 과정에서 발생한 데이터를 버퍼에 기록한다. 이 정보들은 앱에서 지도를 표시하거나 정보를 제공하는 사용된다.*/
    private void getXmlData(int q) { //전국정비소 정보 데이터 파싱
        String queryUrl = "http://api.data.go.kr/openapi/tn_pubr_public_auto_maintenance_company_api?serviceKey=28k6dj2VzcV4Bgng3CN931SanEKlVifOCPTFQ%2FaOF%2BLhVB3gH1YztmmiClWwCeFaviTXIRrZvGFGgkYRiIsipQ%3D%3D&pageNo=0&numOfRows=15000&type=xml";
        //String queryUrl = "http://api.data.go.kr/openapi/tn_pubr_public_carwsh_api?serviceKey=d8w2%2FGzcZJPLy8PLdb7OZOuJk1223dqUzF%2BHWvuT3px1t9dbzJ5cJ95h%2Bg%2B7XsW8hG85guyXA%2BfNbfnLaQtuJA%3D%3D&pageNo=0&numOfRows=15000&type=xml";
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

                        if (tag.equals("item")) ;
                        else if (tag.equals("inspofcNm")) { //정비소 명
                            xpp.next();
                            name[count] = xpp.getText();
                        } else if (tag.equals("inspofcType")) { //정비소 종류
                            xpp.next();
                            type[count] = xpp.getText();
                        } else if (tag.equals("rdnmadr")) { //주소
                            xpp.next();
                            lnmadr[count] = xpp.getText();
                        } else if (tag.equals("phoneNumber")) { //정비소 전화 번호
                            xpp.next();
                            phoneN[count] = xpp.getText();
                        } else if(tag.equals("operOpenHm")) { //정비소 시작 시간
                            xpp.next();
                            open[count] = xpp.getText();
                        } else if(tag.equals("operCloseHm")) { //정비소 닫는 시간
                            xpp.next();
                            close[count] = xpp.getText();
                        } else if(tag.equals("bsnSttus")) { //정비소 상태
                            xpp.next();
                            state[count] = xpp.getText();
                        } else if (tag.equals("latitude")) { //정비소 위도
                            xpp.next();
                            lati[count] = xpp.getText();
                        } else if (tag.equals("longitude")) { //정비소 경도
                            xpp.next();
                            lontude[count] = xpp.getText();
                            count++;
                        }
                        /* 정비소의 정보를 제공해주는 API의 변수 명들을 보고, 내가 얻고자하는 정보를 필터링 하여 미리 만들어둔 변수에 저장한다.
                        count 변수를 통해 들어오는 정비소 수를 세어가며 진행하고, count를 증가시켜 다음 데이터를 저장할 위치를 지정한다. */

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG: // 해당 이벤트가 발생하면, 태그 이름을 가져온다.
                        tag = xpp.getName(); //테그 이름 얻어오기
                        if (tag.equals("item")) buffer.append("\n"); // 만약 태그가 item이라면, 버퍼에 줄바꿈 문자를 추가여 각 정비소 정보를 구분한다. 첫번째 검색결과종료..줄바꿈
                        break;
                }
                eventType = xpp.next(); // 다음 이벤트로 이동하여 루프가 계속될 수 있도록 한다.
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 파싱 중 예외가 발생할 경우, 이를 catch하여 스택 트레이스를 출력한다.
        }
        buffer.append("파싱 끝\n");
        // 파싱이 끝나면 buffer에 파싱 끝 메시지를 추가한다.
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
    사용자가 마커를 클릭하면 해당 마커에 대한 정보가 다이어로그와 정보 창(InfoWindow)을 통해 제공되며,
    사용자가 원하는 네비게이션 앱(카카오 맵, 네이버 지도)을 통해 해당 위치로 길찾기 기능을 이용할 수 있는 옵션도 나타난다. */
    @Override
    // 이 메서드는 Overlay.OnClickListener 인터페이스의 onClick 메서드를 오버라이드한 것이다.
    public boolean onClick(@NonNull Overlay overlay) { // 지도 위의 오버레이(이 경우에는 마커)가 클릭되었을 때 호출된다.
        if (overlay instanceof Marker) {
            // 선택된 해당 마커의 좌표(위도, 경도)정보를 미리 만들어 둔 변수에 저장
            LatLng aa = ((Marker) overlay).getPosition();
            lat = aa.latitude;
            lon = aa.longitude;
            Marker marker = (Marker) overlay;
            // overlay가 마커인지 확인한 후, 클릭된 마커의 위치(LatLng)를 가져온다. 이 위치 정보를 통해 위도(lat)와 경도(lon)를 추출한다.
            for (int k = 0; k < count; k++) {
                if ((lati[k] != null) && (lontude[k] != null)) {
                    if ((Double.parseDouble(lati[k]) == lat) && (Double.parseDouble(lontude[k]) == lon)) {
                        as = k;
                        continue;
                    }
                }
            } /* 모든 정비소 정보를 순회하면서 현재 클릭된 마커의 위도와 경도가 정비소 데이터의 위도와 경도와 일치하는지를 확인한다. 일치하는 경우, 해당 인덱스를 as 변수에 저장한다. */

            if (marker.getInfoWindow() != null) { //마커 클릭시 다이어로그를 활용하여 상세정보 표시
                InfoWindow.close();
                Toast.makeText(this.getApplicationContext(), "정보창을 닫습니다.", Toast.LENGTH_LONG).show();
            } else {
                InfoWindow.open(marker);
                /* 마커가 이미 정보 창을 열고 있는지 확인한다. 만약 열려있다면 정보 창을 닫고, 그렇지 않다면 정보를 보여준다. */

                AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.CarCenter.this);
                if(phoneN[as] == null) phoneN[as] = "정보없음";
                if(open[as] == null) open[as] = "정보없음";
                if(close[as] == null) close[as] = "정보없음";
                dlg.setTitle("상세정보"); //제목
                dlg.setMessage("주소 : " + lnmadr[as] + "\n전화번호 : " + phoneN[as]  + "\n운영시간 : " + open[as] + " ~ " + close[as]);
                /* 마커를 클릭하면 AlertDialog가 나타난다. 이 다이어로그에는 해당 정비소의 상세 정보(주소, 전화번호, 운영 시간)가 표시된다. 만약 데이터가 없을 경우, "정보없음"으로 표시된다. */

                dlg.setPositiveButton("길찾기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int a) { //길찾기 버튼 클릭시 주소값을 intent하여 다른 네비게이션 어플(카카오 맵, 네이버 지도)과 연동

                        AlertDialog.Builder dlg = new AlertDialog.Builder(com.example.project.CarCenter.this);
                        dlg.setTitle("길찾기");
                        final String[] versionArray = new String[]{"카카오 맵", "네이버 지도"};
                        dlg.setSingleChoiceItems(versionArray, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int a) {
                                Mnumber = a;
                            }
                        }); // 사용자가 원하는 네비게이션 앱(카카오 맵, 네이버 지도)을 선택할 수 있다.

                        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int a) {
                                //토스트 메시지
                                /* 사용자가 선택한 네비게이션 앱으로 목적지까지의 길차기를 수행한다. 앱이 설치되어 있지 않은 경우에는 해당 앱의 다운로드 페이지로 연결한다. */
                                Intent intent;
                                if (Mnumber == 0) {
                                    try {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("kakaomap://route?ep=" + Double.parseDouble(lati[as]) + "," + Double.parseDouble(lontude[as]) + "&by=CAR"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=net.daum.android.map&hl=ko"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                } else if (Mnumber == 1) {
                                    try {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("nmap://navigation?dlat=" + lat + "&dlng=" + lon + "&dname=목적지&appname=com.example.maptest"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.skt.tmap.ku&hl=ko"));
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }
                                Toast.makeText(com.example.project.CarCenter.this, "확인을 눌렀습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dlg.show();
                    }
                });

                AlertDialog alertDialog = dlg.create();
                alertDialog.getWindow().setGravity(Gravity.BOTTOM);
                alertDialog.show();
                // 다이어로그가 화면 하단에 표시된다.
            }
            return true;
        }
        return false;
    }

    /* onBackPressed 메서드는 사용자가 '뒤로 가기' 버튼을 눌렀을 때 앱이 바로 종료되지 않도록 하고,
    2.5초 이내에 다시 한 번 '뒤로 가기' 버튼을 누르면 앱을 종료하는 기능을 구현한 것이다.
    이는 사용자가 실수로 앱을 종료하는 것을 방지하기 위한 일반적인 방법이다. */
    public void onBackPressed() {
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

    /* MyAsyncTask 클래스는 AsyncTask를 상속받아 비동기 작업을 수행하는데, 이 클래스는 XML 데이터를 파싱하고, 지도 위에 마커를 설정하는 작업을 비동기적으로 수행한다.
    이 과정은 크게 세 부분으로 나뉘어지는데, doInBackground, onPreExecute, onPostExecute 으로 나뉘어 진다. */
    public class MyAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... strings) {
            getXmlData(0); // 파싱 실행
            return true;
            /* 이 메소드는 백그라운드에서 실행될 작업을 정의한다. 여기서는 getXmlData(0) 메서드를 호출하여 XML 데이터 파싱을 한다.
            getXMLData(0) 메서드는 API에서 데이터를 받아와 파싱하여 배열에 저장한다. 이 작업은 UI  스레드와 분리되어 실행되므로 UI 응답성을 유지할 수 있다.
            true를 반환하여 작업이 성공적으로 완료되었음을 나타낸다. */
        }

        @Override
        protected void onPreExecute() { // progressDialog창 구현(파싱값 저장전까지 실행)
            progressDialog = ProgressDialog.show(CarCenter.this, "잠시만 기다려주세요", "진행중입니다.", true);
            super.onPreExecute();
            /* 이 메소드의 역할은 백그라운드 작업이 시작되기 전에 호출되어 ProgressDialog를 화면에 띄워 사용자에게 작업이 진행 중임을 알리는 것이다.
            이 다이얼로그는 백그라운드 작업이 끝날 때까지 유지된다. */
        }

        @Override
        protected void onPostExecute(Boolean s) { // 초기 마커 설정
            super.onPostExecute(s);
            for (int i = 0; i < count; i++) {
                if (name[i] == null || lati[i] == null || lontude[i] == null || lnmadr[i] == null) {
                    continue;
                }
                markers[wash1] = new Marker();
                markers[wash1].setPosition(new LatLng(Double.parseDouble(lati[i]), Double.parseDouble(lontude[i])));
                markers[wash1].setMap(mNaverMap);
                markers[wash1].setWidth(100);
                markers[wash1].setHeight(120);
                markers[wash1].setHideCollidedMarkers(true);
                markers[wash1].setIcon(OverlayImage.fromResource(R.drawable.ic_car_center_icon22));
                markers[wash1].setOnClickListener(com.example.project.CarCenter.this);
                wash1++;
                /* 백그라운드 작업이 끝난 후, 결과를 UI에 반영한다. 여기서는 파싱한 데이터를 바탕으로 지도에 마커를 추가하는 작업이다.
                for 루프를 통해 파싱된 데이터를 순회하면서, 유효한 데이터(이름, 위도, 경도, 주소가 null이 아닌 경우)에 대해 마커를 생성한다.
                생성된 마커는 지도 mNaverMap에 표시된다. 마커의 크기, 아이콘, 충돌처리 등을 설정한다.
                markers[wash1].setOnClickListener(com.example.project.CarCenter.this);을 통해 마커 클릭 이벤트를 처리한다. */
            }
            progressDialog.dismiss(); // 마지막으로 작업이 완료되었음을 알리고, 진행 중이던 ProgressDialog를 닫는다.
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // 해당 메서드는 백그라운드 작업 중 진행 상태를 업데이트할 때 호출된다. 하지만 별도의 사용이 없기 때문에 구현 생략
        }

        @Override
        protected void onCancelled(Boolean s) {
            super.onCancelled(s);
            // 비동기 작업이 취소됐을 때 호출되는 메서드이다. 별도의 구현은 없지만, 필요에 따라 취소 시의 후처리 동작을 지정할 수 있다.
        }
    }
}
