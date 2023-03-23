package kr.co.ob.obone.android;

import android.os.Build;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Define {


//    운영    url : https://ob-1.ob.co.kr/ams/ob1_mobile/index.html
//    테스트 url : https://ob-1test.ob.co.kr/ams/ob1_mobile/index.html

    String DEV_URL = "http://61.43.143.84:48080";

    String TEST_URL = "https://ob-1test.ob.co.kr";
    String REAL_URL = "https://ob-1.ob.co.kr";

    String BASE_URL = BuildConfig.SERVER;
//    String BASE_URL = BuildConfig.FLAVOR.equals("real")?REAL_URL:BuildConfig.FLAVOR.equals("stg")?TEST_URL:DEV_URL;
//    String BASE_URL = "https://ob-1test.ob.co.kr:48443";
//    String BASE_URL = "http://ob-1test.ob.co.kr:48080";





    String ProjectURL = "/ams/ob1_mobile/";
    String BootstrapURL = "/ams/ob1_mobile/start_android.json";

    String GPS_RUL = BASE_URL + "/ams/restfulApiController/saveGps.do";

    boolean IS_TEST = false;


    interface ApiInterface {

        @POST(GPS_RUL)
        Call<String> requestLocation(@Query("lat") String lat, @Query("lon") String lon, @Query("urkey") String urkey);

    }

    interface REQUEST_PERMISSION {

        int CAMERA = 101;
        int LOCATION = 102;
        int STORAGE = 103;
        int IGNORE_BATTERY = 104;
        int LOCATION_ONE = 105;

        int CALL_PHONE = 106;

    }


}
