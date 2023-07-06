package kr.co.ob.obone.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nexacro.NexacroActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import android.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    Class Name      : NexacroActivityExt
    Description     : NexacroActivity 를 상속받아 Nexacro 앱에서 발생되는 이벤트를 받기위한 Class
 */
public class NexacroActivityExt extends NexacroActivity {

    /**
     * NexacroActivityExt를 다른 클래스에서 사용하기 위한 변수
     */
    public static Context context;
    public static Boolean pauseFlag;
    public static Boolean pushMsgFlag;


    private final String LOG_TAG = this.getClass().getSimpleName(); //현재 Class Name 문자열

    private StandardObject standardObj = null;

    private Define.ApiInterface api;

    private Intent mGpsService = null;

    private long mInterval = 10000;

    private String mUrkey = "";

    /**  */
    private PowerManager.WakeLock mWakelock;


    // 전호번호 임시저장(권한 체크)
    private String mCallNum = "";

    private static final int ZXING_CAMERA = 101;    //ZXING 카메라 권한요청 ID

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        api = RetrofitClient.getInstance().create(Define.ApiInterface.class);

        LocalBroadcastManager.getInstance(this).registerReceiver( mMessageReceiver, new IntentFilter("custom-event-name"));
        context = this;

        this.pauseFlag = false;
        this.pushMsgFlag = false;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onResume() {
        if ( pauseFlag && pushMsgFlag ){
            this.callMethod("selectPushList", null);
            this.pushMsgFlag = false;
        }
        this.pauseFlag = false;
        super.onResume();
    }

    @Override
    public void onPause() {
        pauseFlag = true;
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {    super.onNewIntent(intent);  }

    @Override
    public void onDestroy() {

        if(mWakelock != null && mWakelock.isHeld())
            mWakelock.release();

        if(mGpsService != null) {
            stopService(mGpsService);
        }

        if(mMessageReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }


    public void stopGpsService() {
        if(mGpsService != null)
            stopService(mGpsService);
    }

    /*  StandardObject 클래스 사용을 위한   */
    public void setPlugin(StandardObject obj)
    {
        standardObj = obj;
    }

    /*  화면에서 호출된 데이터를 셋팅하고 처리를 위한 메소드   */
    public void callMethod(String mServiceId, JSONObject mParamData)
    {
        try {
            if(mServiceId.equals("obone_qr")) {
                int result;
                List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
                List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트

                permissions.add( Manifest.permission.CAMERA );          //카메라 사용을 위한 권한

                //실제 요청해야할 권한 체크
                for (String pm : permissions) {
                    result = ContextCompat.checkSelfPermission(this, pm);

                    if (result != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions.add(pm);
                    }
                }

                if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
                    ActivityCompat.requestPermissions( this, requestPermissions.toArray(new String[requestPermissions.size()]), Define.REQUEST_PERMISSION.CAMERA);
                } else {                                //요청해야할 권한이 없으면 스캔시작
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setOrientationLocked(false);     //스캔 방향전환을 위한 설정
                    integrator.initiateScan();
                }
            }
            else if(mServiceId.equals("obone_user")) {
                // token

                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()) {
                            // 실패
                            return;
                        }
                        else {
                            String token = task.getResult();
                            standardObj.send(standardObj.CODE_SUCCESS, token, standardObj.getActionString(StandardObject.ON_CALLBACK));

                        }
                    }
                });

            }
            else if(mServiceId.equals("obone_gps")) {
                // gps

                mInterval = Long.parseLong(mParamData.getString("timer"));
                mUrkey = mParamData.getString("urkey");

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if(isCheckedWhiteList()) {
                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        mWakelock= pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
                        mWakelock.acquire();

                        startGpsService();
                    }
                    else
                    {
                        Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:"+ getPackageName()));
                        startActivityForResult(intent, Define.REQUEST_PERMISSION.IGNORE_BATTERY);
                    }
                }
                else
                {
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    mWakelock= pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
                    mWakelock.acquire();

                    startGpsService();
                }

            }
            else if(mServiceId.equals("obone_gps_stop")) {

                if(mWakelock != null && mWakelock.isHeld())
                    mWakelock.release();

                if(mGpsService != null) {
                    stopService(mGpsService);
                }


                standardObj.send(standardObj.CODE_SUCCESS, "obone_gps_stop", standardObj.getActionString(StandardObject.ON_CALLBACK));

            }

            else if(mServiceId.equals("obone_gps_single")) {

                getGpsSingle();
            }
            else if(mServiceId.equals("obone_tel")) {

                String tel = "tel:" + mParamData.getString("tel");


                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(tel));
                startActivity(intent);
            }
            else if(mServiceId.equals("obone_tel_direct")) {

                String tel = "tel:" + mParamData.getString("tel");

                mCallNum = tel;
                callPhone(tel);
            }
            else if(mServiceId.equals("selectPushList")) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if(!task.isSuccessful()) {
                            // 실패
                            return;
                        }
                        else {
                            String token = task.getResult();
                            standardObj.send(standardObj.CODE_SUCCESS, token, standardObj.getActionString(StandardObject.ON_RESUME));

                        }
                    }
                });
            }
            else if(mServiceId.equals("checkVersion")) {
                standardObj.send(standardObj.CODE_SUCCESS, "2.0", standardObj.getActionString(StandardObject.ON_CALLBACK));
            }


        } catch(Exception e) {
            e.printStackTrace();
            standardObj.send(standardObj.CODE_ERROR, standardObj.getActionString("callMethod") + ":" + e.getMessage(), standardObj.getActionString(StandardObject.ON_CALLBACK)
            );
        }
    }

    /**
     * gps 권한 체크
     */
    public void getGpsSingle()
    {

        int result;
        List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
        List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트

        permissions.add( Manifest.permission.ACCESS_FINE_LOCATION );          // 권한



        //실제 요청해야할 권한 체크
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);

            if (result != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(pm);
            }
        }

        if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
            ActivityCompat.requestPermissions(this,requestPermissions.toArray(new String[requestPermissions.size()]), Define.REQUEST_PERMISSION.LOCATION_ONE);
        }

        getLocation();

    }


    /**
     * 현재 위치값
     */
    private void getLocation() {
        runOnUiThread(() -> {

            GPSService gps = new GPSService(NexacroActivityExt.this);

            Location location = gps.getlocation(true);

            if(location != null) {

                JSONObject obj = new JSONObject();
                try {
                    obj.put("lat", location.getLatitude());
                    obj.put("lon", location.getLongitude());
//                            obj.put("urkey", urkey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                standardObj.send(standardObj.CODE_SUCCESS, obj.toString(), standardObj.getActionString(StandardObject.ON_CALLBACK));
            }
            else
            {
                standardObj.send(standardObj.CODE_ERROR, standardObj.getActionString("callMethod") + ":" + "loaction error", standardObj.getActionString(StandardObject.ON_CALLBACK));
            }
        });
    }

    /**
     * 위치 값 가져오는 서비스 실행
     * <br>위치값 주기적으로 반복
     */
    public void startGpsService() {

        int result;
        List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
        List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트

        permissions.add( Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS );
        permissions.add( Manifest.permission.ACCESS_FINE_LOCATION );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            permissions.add( Manifest.permission.ACCESS_BACKGROUND_LOCATION );          // 권한


        //실제 요청해야할 권한 체크
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);

            if (result != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(pm);
            }
        }

        if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
            ActivityCompat.requestPermissions(this, requestPermissions.toArray(new String[requestPermissions.size()]), Define.REQUEST_PERMISSION.LOCATION);
        } else {                                //요청해야할 권한이 없으면 스캔시작

            mGpsService = new Intent(this, GPSService.class);
            mGpsService.putExtra("interval", mInterval);

            startService(mGpsService);
        }
    }

    private void callPhone(String tel) {
        int result;
        List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
        List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트

        permissions.add( Manifest.permission.CALL_PHONE );          // 권한

        //실제 요청해야할 권한 체크
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);

            if (result != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(pm);
            }
        }

        if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
            ActivityCompat.requestPermissions( this, requestPermissions.toArray(new String[requestPermissions.size()]), Define.REQUEST_PERMISSION.CALL_PHONE);
        } else {                                //요청해야할 권한이 없으면 스캔시작

            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(tel));
            startActivity(intent);

        }

    }



    /**
     * 요청한 권한처리 후 호출됨
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        requestPermissionsResult( requestCode, permissions, grantResults );
    }

    /**
     * Activity 의 onRequestPermissionsResult 에서 호출하여 이곳에 로직 처리한다.
     */
    public void requestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch(requestCode) {
            case Define.REQUEST_PERMISSION.CAMERA: {
                //필요권한(CAMERA) 이 승인되었는지 체크
                boolean isPermissionGranted = true;
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isPermissionGranted = false;
                    }
                }
                if(isPermissionGranted) {   //권한승인시 스캔시작
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.setOrientationLocked(false);     //스캔 방향전환을 위한 설정
                    integrator.initiateScan();
                } else {                    //권한거절시 화면으로 리턴
                    standardObj.send(standardObj.CODE_PERMISSION_ERROR, "Camera permission denied" , standardObj.getActionString("_onpermissionresult"));
                }
                break;
            }
            case Define.REQUEST_PERMISSION.LOCATION: {

                boolean isPermissionGranted = true;
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isPermissionGranted = false;
                    }
                }
                if(isPermissionGranted) {   //권한승인시 스캔시작

                    mGpsService = new Intent(this, GPSService.class);
                    mGpsService.putExtra("interval", mInterval);

                    startService(mGpsService);
                } else {                    //권한거절시 화면으로 리턴

                    if(Define.IS_TEST) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(NexacroActivityExt.this);
                        builder.setMessage("OB-1 앱은 이 기기의 위치에 항상 허용 해야 이용할수 있습니다.");
                        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                startGpsService();
                            }
                        });
                        builder.setNegativeButton("앱 종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 종료?
                            }
                        });
                        builder.setCancelable(false);
                        builder.create();
                        builder.show();


                    }
                    else
                        standardObj.send(standardObj.CODE_PERMISSION_ERROR, "Location permission denied" , standardObj.getActionString("_onpermissionresult"));
                }
                break;
            }

            case Define.REQUEST_PERMISSION.LOCATION_ONE : {

                boolean isPermissionGranted = true;
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isPermissionGranted = false;
                    }
                }
                if(isPermissionGranted) {
                    getLocation();
                }
                else {
                    standardObj.send(standardObj.CODE_PERMISSION_ERROR, "Location permission denied" , standardObj.getActionString("_onpermissionresult"));
                }
                break;
            }

            case Define.REQUEST_PERMISSION.CALL_PHONE : {

                boolean isPermissionGranted = true;
                for(int i = 0; i < permissions.length; i++) {
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        isPermissionGranted = false;
                    }
                }
                if(isPermissionGranted) {
                    callPhone(mCallNum);
                    mCallNum = "";
                }
                else {
                    standardObj.send(standardObj.CODE_PERMISSION_ERROR, "CALL permission denied" , standardObj.getActionString("_onpermissionresult"));
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        if(requestCode == Define.REQUEST_PERMISSION.IGNORE_BATTERY) {

            // 배터리 최적화 예외
            if(resultCode == RESULT_OK) {

                // gps 시작 하라.
                startGpsService();
            }
            else {

                if(Define.IS_TEST) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(NexacroActivityExt.this);
                    builder.setMessage("OB-1 앱은 배터리 최적화 제외 허용을 해야 이용할수 있습니다.");
                    builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent  = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:"+ getPackageName()));
                            startActivityForResult(intent, Define.REQUEST_PERMISSION.IGNORE_BATTERY);
                        }
                    });
                    builder.setNegativeButton("앱 종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 종료?
                        }
                    });
                    builder.setCancelable(false);
                    builder.create();
                    builder.show();

                }

                standardObj.send(standardObj.CODE_PERMISSION_ERROR, "Location permission denied", standardObj.getActionString("_onpermissionresult"));
            }
            return;
        }
        else {

            // QR 스캔일때
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

            if (result != null) {
                if (result.getContents() == null) { //스캔 취소시
                    Log.d(LOG_TAG, "Canceled scan");
                    standardObj.send(standardObj.CODE_ERROR, "User Canceled", standardObj.getActionString(StandardObject.ON_CALLBACK));
                } else {                        //스캔 완료시
                    Log.d(LOG_TAG, "Scanned");
                    standardObj.send(standardObj.CODE_SUCCESS, result.getContents(), standardObj.getActionString(StandardObject.ON_CALLBACK));
                }
            } else {
                super.onActivityResult(requestCode, resultCode, intent);
            }
        }
    }

    /**
     * restful api 전송
     * @param lat
     * @param lon
     */
    private void requestLocation(String lat, String lon) {

        Call<String> call = api.requestLocation(lat, lon, mUrkey);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
//                        logpermission();
                        TraceLog.WW("response", response.toString());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
//                        logpermission();
                        TraceLog.WW("response", t.toString());
                    }
                });
    }

    /**
     * 안드로이드 6.0 이상 (API23) 부터는 Doze모드가 추가됨.
     * 일정시간 화면이꺼진 상태로 디바이스를 이용하지 않을 시 일부 백그라운드 서비스 및 알림서비스가 제한됨.
     * 6.0이상의 버전이라면 화이트리스트에 등록이 됐는지 Check
     */
    public boolean isCheckedWhiteList(){

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean WhiteCheck = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            /**
             * 등록이 되어있따면 TRUE
             * 등록이 안되있다면 FALSE
             */
            WhiteCheck = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            if(WhiteCheck){
                return true;
            }
            else
                return false;
        }
        else
            return true;
    }

    /**
     * GPSService에서 메세지 받아온다.
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
             String latitude = intent.getStringExtra("latitude");
             String longitude = intent.getStringExtra("longitude");

//            Toast.makeText(NexacroActivityExt.this, "lat="+latitude +" , lot="+ longitude, Toast.LENGTH_SHORT).show();

            requestLocation(latitude, longitude);
        }
    };




    public void logpermission() {

        int result;
        List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
        List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트

        permissions.add( Manifest.permission.WRITE_EXTERNAL_STORAGE );          // 권한

        //실제 요청해야할 권한 체크
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);

            if (result != PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(pm);
            }
        }

        if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
            ActivityCompat.requestPermissions( this, requestPermissions.toArray(new String[requestPermissions.size()]), 55555);
        } else {                                //요청해야할 권한이 없으면 스캔시작

        }


    }



    public void setNotification2(String title, String msg) {

        String NOTIFICATION_CHANNEL_ID = "121212";

        int notiId = (int) System.currentTimeMillis();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, NexacroActivityExt.class);
//        notificationIntent.putExtra("notificationId", 111111); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK ) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon)) //BitMap 이미지 요구
                .setContentTitle(title)
                .setContentText(msg)
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.mipmap.icon); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.icon); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(notiId, builder.build()); // 고유숫자로 노티피케이션 동작시킴

    }

}

