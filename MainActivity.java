package com.example.tmaplocationtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;


import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.lang.String;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity<v> extends AppCompatActivity {
    LinearLayout linMapView;
    Button btnZoomIn;
    Button btnZoomOut;
    Button btnMyLocation;
    Button btnSearch;
    EditText edtSearch;
    TMapView tMapView;
    TMapData tMapData;
    // 검색 결과를 출력하기 위해 만든 리스트
    ArrayList<TMapPOIItem> poiResult;
    LocationManager locationManager;
    Bitmap rightButton;
    BitmapFactory.Options options;

    String[] strings;

    ArrayAdapter<String> stringArrayAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initInstance();
        eventListener();
        //setMarker("경기과학기술대학교", 37.3390222, 126.7358078);
    }

    // 위젯 변수 및 필요 객체 등의 멤버 변수 초기화 작업
    private void initView(){
        linMapView = findViewById(R.id.linMapView);
        btnZoomIn=findViewById(R.id.btnZoomIn);
        btnZoomOut=findViewById(R.id.btnZoomOut);
        btnMyLocation=findViewById(R.id.btnMyLocation);
        btnSearch=findViewById(R.id.btnSearch);
        edtSearch=findViewById(R.id.edtSearch);
        options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        rightButton = BitmapFactory.decodeResource(getResources(), R.drawable.right_arrow, options);
    }

    // 필요 객체 변수 인스턴스화 작업
    private void initInstance(){
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey("l7xx620ec84c2fe1415bab858ce4beaec659");
        linMapView.addView(tMapView);
        tMapData = new TMapData();

        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        poiResult = new ArrayList<>();
    }

    // 각 버튼 이벤트 리스너 등록
    private void eventListener(){
        btnZoomIn.setOnClickListener(listener);
        btnZoomOut.setOnClickListener(listener);
        btnMyLocation.setOnClickListener(listener);
        btnSearch.setOnClickListener(listener);
    }

    // 버튼에 사용하는 리스너 인스턴스화
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnSearch:
                    //POI 검색
                    poiResult.clear();
                    String strData = edtSearch.getText().toString();
                    if (!strData.equals("")) {
                        searchPOI(strData);

                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                        strings = new String[poiResult.size()];

                        for(int i=0; i<poiResult.size();i++){
                            strings[i] = poiResult.get(i).getPOIName();
                        }

                        for(int i=0; i<poiResult.size();i++){
                            Log.d("check", strings[i]);
                        }
                        OnClickHandler(adptr(strings));
                    }
                    else
                        Toast.makeText(getApplicationContext(), "검색어를 입력하세요!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnZoomIn:
                    tMapView.MapZoomIn();
                    break;
                case R.id.btnZoomOut:
                    tMapView.MapZoomOut();
                    break;
                case R.id.btnMyLocation:
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    } catch (SecurityException e) {

                    }
                    break;
            }
        }
    };

    // 위치 수신자 인스턴스화
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            tMapView.setCenterPoint(lon, lat);
            tMapView.setLocationPoint(lon,lat);
            tMapView.setIconVisibility(true);
            // tMapView.setLocationPoint(lat, lon);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    // 마커 설정 메서드
    private void setMarker(String name, double lat, double lon){
        TMapMarkerItem markerItem = new TMapMarkerItem();

        TMapPoint tMapPoint = new TMapPoint(lat, lon);

        // Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker_icon);
        // markerItem.setIcon(bitmap); // 마커 아이콘 지정
        markerItem.setPosition(0.5f, 1.0f);
        markerItem.setTMapPoint(tMapPoint);
        markerItem.setName(name); // 마커 타이틀 지정
        markerItem.setCanShowCallout(true);
        markerItem.setCalloutTitle(name);
        tMapView.addMarkerItem("name", markerItem); // 지도에 마커 추가
    }

    // 통합 검색 메서드
    private void searchPOI(String strData){
        tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> arrayList) {
                // 외부에서 사용하기 위한 검색 결과 복사
                poiResult.addAll(arrayList);

                tMapView.setCenterPoint(arrayList.get(0).getPOIPoint().getLongitude(),
                        arrayList.get(0).getPOIPoint().getLatitude(), true);
                for(int i = 0; i < arrayList.size(); i++) {
                    TMapPOIItem item = (TMapPOIItem) arrayList.get(i);
                    Log.d("POI Name:", item.getPOIAddress().toString()+", " +
                            "Address : " + item.getPOIAddress().replace("null", "") + ", " +
                            "Point : " + item.getPOIPoint().toString() + "Contents : " + item.getPOIContent());
                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setTMapPoint(item.getPOIPoint());
                    markerItem.setCalloutTitle(item.getPOIName());
                    markerItem.setCalloutSubTitle(item.getPOIAddress());
                    markerItem.setCanShowCallout(true);
                    // markerItem.setCalloutRightButtonImage(rightButton);

                    tMapView.addMarkerItem(item.getPOIName(), markerItem);
                }
            }
        });
    }

    public ArrayAdapter<String> adptr(String[] stRing1){
        for(int i=0; i<poiResult.size();i++){
            Log.d("check", poiResult.get(i).getPOIName());
        }
        stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stRing1);
        return stringArrayAdapter;
    }

    public void OnClickHandler(final ArrayAdapter<String> stringArrayAdapter){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("결과");

        builder.setAdapter(stringArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final TMapPoint tMapPointStart = new TMapPoint(37.3390222, 126.735807);
                final TMapPoint tMapPointEnd = poiResult.get(i).getPOIPoint();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            TMapPolyLine tMapPolyLine = new TMapData().findPathData(tMapPointStart, tMapPointEnd);
                            tMapPolyLine.setLineColor(Color.BLUE);
                            tMapPolyLine.setLineWidth(10);
                            tMapView.addTMapPolyLine("Line1", tMapPolyLine);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}