package org.crazyit.baidumapdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private Button mMapCommon;
    private Button mMapSite;
    private Button mMapTraffic;
    private Button mMyLocation;
    private boolean isFirstIn=true;
    private double mLatitude;
    private double mLongtitude;
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;

    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        //获取地图控件引用
      initView();
        // after andrioid m,must request Permiision on runtime
        getPersimmions();
        initLocation();
    }
    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             *
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
        //这四个权限需要运行时申请
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    private void initView(){
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        MapStatusUpdate mapStatusUpdateFactory=MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(mapStatusUpdateFactory);
        mMapCommon=(Button) findViewById(R.id.map_common);
        mMapSite=(Button) findViewById(R.id.map_site);
        mMapTraffic=(Button) findViewById(R.id.map_traffic);
        mMyLocation=(Button) findViewById(R.id.map_mylocation);

        mMapCommon.setOnClickListener( this);
        mMapSite.setOnClickListener(this);
        mMapTraffic.setOnClickListener(this);
        mMyLocation.setOnClickListener(this);

    }
     private void initLocation(){
         mLocationClient=new LocationClient(this);
         myLocationListener=new MyLocationListener();
         mLocationClient.registerLocationListener(myLocationListener);
         LocationClientOption locationClientOption=new LocationClientOption();
         locationClientOption.setCoorType("bd09ll");
         locationClientOption.setIsNeedAddress(true);
         locationClientOption.setOpenGps(true);
         locationClientOption.setScanSpan(1000);
         mLocationClient.setLocOption(locationClientOption);


     }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.map_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case  R.id.map_traffic:
                if (mBaiduMap.isTrafficEnabled()){
                    mBaiduMap.setTrafficEnabled(false);
                    mMapTraffic.setText("实时交通on");
                }else{
                    mBaiduMap.setTrafficEnabled(true);
                    mMapTraffic.setText("实时交通off");
                }
                break;
            case R.id.map_mylocation:
                LatLng latLng=new LatLng(mLatitude, mLongtitude);
                MapStatusUpdate mapStatusUpdate=MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //此时的位置
            MyLocationData data=new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    .latitude( bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);
            mLatitude=bdLocation.getLatitude();
            mLongtitude=bdLocation.getLongitude();
            if(isFirstIn){
                LatLng latLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate mapStatusUpdate=MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
                isFirstIn=false;
            }

        }
    }

}
