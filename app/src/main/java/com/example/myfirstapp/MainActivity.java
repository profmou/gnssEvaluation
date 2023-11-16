package com.example.myfirstapp;

//import java.util.Iterator;
//import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
//import android.location.GpsSatellite;
//import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;

import android.view.View;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
//import java.net.InetAddress;
import java.net.InetAddress;
import java.net.Socket;

//时间戳转换工具
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private static final String TAG = "MainActivity";

    private LocationManager lm;
    private LocationListener locationListener;

    private double Latitude_p;
    private double Longitude_p;
    private double time_p;

    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Context context;

    //存储实时数据，保留最近10条数据
    private StringBuilder recentData = new StringBuilder(); //保存最近的10条数据信息

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        lm.removeUpdates(locationListener);

        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        SDKInitializer.setCoordType(CoordType.BD09LL);
        this.context = this;
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();

        String stringWriteToFile = "lat,lon,alt\n" ;
        FileUtils.writeTxtToFile(stringWriteToFile, "sdcard/gnssEvaluation/", "myGPSDataLog");


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }
        //位置监听 private
         LocationListener locationListener = new LocationListener() {

             /**
              * 位置信息变化时触发
              */

             public void onLocationChanged(Location location) {
                 //updateView(location);

                 long timeStamp = location.getTime();

                 // 将时间戳转换为日期对象
                 Date date = new Date(timeStamp);

                 // 设置日期格式
                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH点mm分", Locale.getDefault());
                 sdf.setTimeZone(TimeZone.getDefault()); // 设置时区为本地时区

                 // 格式化日期对象并输出到Log
                 String formattedDate = sdf.format(date);
                 Log.i(TAG, "时间：" + formattedDate);
//                 Log.i(TAG, "时间：" + location.getTime());
                 Log.i(TAG, "经度：" + location.getLongitude());
                 Log.i(TAG, "纬度：" + location.getLatitude());
                 Log.i(TAG, "海拔：" + location.getAltitude());

//                 TextView textView = findViewById(R.id.textView2);
                 TextView textView = findViewById(R.id.textView2);
/*                 textView.setText("时间：" + location.getTime() + "\n" +
                         "经度" + location.getLongitude() + "\n" +
                         "纬度：" + location.getLatitude() + "\n" +
                         "海拔：" + location.getAltitude());*/

//                 textView.append( "\n" +"时间：" + location.getTime() + "\n" +
//                         "经度" + location.getLongitude() + "\n" +
//                         "纬度：" + location.getLatitude() + "\n" +
//                         "海拔：" + location.getAltitude() + "\n" );
                 String newEntry = "时间：" + formattedDate + "\n" +
                         "经度：" + location.getLongitude() + "\n" +
                         "纬度：" + location.getLatitude() + "\n" +
                         "海拔：" + location.getAltitude() + "\n\n";

                 // 将新数据添加到字符串变量中
                 recentData.append(newEntry);

                 // 检查最近的数据条数是否超过10条，如果超过则删除最旧的数据
                 int lineCount = textView.getLayout().getLineCount();
                 if (lineCount > 10) {
                     int startOffset = textView.getLayout().getLineStart(0);
                     int endOffset = textView.getLayout().getLineEnd(lineCount - 10);
                     recentData.delete(startOffset, endOffset);
                 }

                 // 更新 TextView 中的文本内容
                 textView.setText(recentData.toString());

                 final TextView finalTextView = textView;
                 textView.post(new Runnable() {
                     @Override
                     public void run() {
                         int scrollAmount = finalTextView.getLayout().getLineTop(finalTextView.getLineCount()) - finalTextView.getHeight();
                         if (scrollAmount > 0) {
                             finalTextView.scrollTo(0, scrollAmount);
                         } else {
                             finalTextView.scrollTo(0, 0);
                         }
                     }
                 });

                 String stringWriteToFile =location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "\n";
                  ///sdcard/gnssEvaluation/
                 //FileUtils.writeTxtToFile(idPASideBase64, "Environment.getExternalStorageDirectory().getPath()", "gnssEvaluation.txt");
                 FileUtils.writeTxtToFile(stringWriteToFile, "sdcard/gnssEvaluation/", "gnssEvaluation.txt");
                 Latitude_p = location.getLatitude();
                 Longitude_p = location.getLongitude();
                 time_p = location.getTime();
//                 throws IOException {
//                     //1.创建客户端Socket，指定服务器地址和端口
//                     Socket socket = new Socket("106.13.21.39", 9999);
//                     //2.获取输出流，向服务器端发送信息
//                     OutputStream os = socket.getOutputStream();//字节输出流
//                     PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
//                     //获取客户端的IP地址
//                     //InetAddress address = InetAddress.getLocalHost();
//                     //String ip = address.getHostAddress();
//                     pw.write("客户端：CXY~" + "测试网络" + "~ 接入服务器！！" + "\n" + "时间：" + location.getTime() + "\n" +
//                             "经度" + location.getLongitude() + "\n" +
//                             "纬度：" + location.getLatitude() + "\n" +
//                             "海拔：" + location.getAltitude() + "\n");
//                     pw.flush();
//                     socket.shutdownOutput();//关闭输出流
//                     socket.close();
//                 }

                 double latitude = location.getLatitude(); //纬度
                 double longitude = location.getLongitude(); //经度

                 //将GPS设备采集的原始GPS坐标转换成百度坐标
                 LatLng sourceLatLng = new LatLng(latitude,longitude);

                 //初始化坐标转换工具类，指定源坐标类型和坐标数据
                 // sourceLatLng待转换坐标
                 CoordinateConverter converter  = new CoordinateConverter()
                         .from(CoordinateConverter.CoordType.GPS)
                         .coord(sourceLatLng);
                 //desLatLng 转换后的坐标
                 LatLng desLatLog = converter.convert();

                 MapStatusUpdate centerMapStatus = MapStatusUpdateFactory.newLatLng(desLatLog);
                 mBaiduMap.setMapStatus(centerMapStatus);


             }

             /**
              * GPS状态变化时触发
              */
             public void onStatusChanged(String provider, int status, Bundle extras) {
                 switch (status) {
                     //GPS状态为可见时
                     case LocationProvider.AVAILABLE:
                         Log.i(TAG, "当前GPS状态为可见状态");
                         break;
                     //GPS状态为服务区外时
                     case LocationProvider.OUT_OF_SERVICE:
                         Log.i(TAG, "当前GPS状态为服务区外状态");
                         break;
                     //GPS状态为暂停服务时
                     case LocationProvider.TEMPORARILY_UNAVAILABLE:
                         Log.i(TAG, "当前GPS状态为暂停服务状态");
                         break;
                 }
             }

             /**
              * GPS开启时触发
              */
             public void onProviderEnabled(String provider) {
                 @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(provider);
                 //updateView(location);
             }

             /**
              * GPS禁用时触发
              */
             public void onProviderDisabled(String provider) {
                 //updateView(null);
             }
         };

        //为获取地理位置信息时设置查询条件
        Criteria criteria = new Criteria();
        String bestProvider = lm.getBestProvider(criteria, true);

        //获取位置信息
        //如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(bestProvider);
        //updateView(location);
        //监听状态
        //lm.addGpsStatusListener(listener);
        //lm.addGpsStatusListener(Listener listener);
        //绑定监听，有4个参数
        //参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        //参数2，位置信息更新周期，单位毫秒
        //参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        //参数4，监听
        //备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        //注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        lm.requestLocationUpdates(lm.GPS_PROVIDER, 1000, 1, locationListener);
    }

    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    //点击按键网络连接相关代码
    public void onClick(View v) {
        new Thread() {
            @Override
            public void run() {
                try {
                    acceptServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

        private void acceptServer() throws IOException {
            //1.创建客户端Socket，指定服务器地址和端口
            Socket socket = new Socket("106.13.21.39", 9999);
            //2.获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流
            //获取客户端的IP地址
            //InetAddress address = InetAddress.getLocalHost();
            //String ip = address.getHostAddress();
            pw.write("客户端："  + " 接入服务器！！" + "\n" +
                    "时间：" + time_p + "\n" +
                    "经度: " + Longitude_p + "\n" +
                    "纬度：" + Latitude_p + "\n");

            pw.write("exit");

            pw.flush();
            socket.shutdownOutput();//关闭输出流
            socket.close();
        }

     /*   public void sendMessage(View view) {
                    //Do something in response to button
                    //Intent intent = new Intent(this, DisplayMessageActivity.class);
                    EditText editText = (EditText) findViewById(R.id.editText);
                    String message = editText.getText().toString();

                    //intent.putExtra(EXTRA_MESSAGE, message);
                    //Criteria criteria = new Criteria();
                    //String bestProvider = lm.getBestProvider(criteria, true);
                    //Location location = lm.getLastKnownLocation(bestProvider);
                    //intent.putExtra(EXTRA_MESSAGE,location.getLongitude());
                    //startActivity(intent);
            }
      */
}

