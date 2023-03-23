package kr.co.ob.obone.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.nexacro.NexacroResourceManager;
import com.nexacro.NexacroUpdatorActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends NexacroUpdatorActivity implements View.OnClickListener {

    public MainActivity() {
        super();

        setBootstrapURL(Define.BASE_URL + Define.BootstrapURL);
        setProjectURL(Define.BASE_URL + Define.ProjectURL);

        setStartupClass(NexacroActivityExt.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NexacroResourceManager.createInstance(this);
        NexacroResourceManager.getInstance().setDirect(true);
        Intent intent = getIntent();
        if(intent != null) {
            String bootstrapURL = intent.getStringExtra("bootstrapURL");
            String projectUrl = intent.getStringExtra("projectUrl");
            if(bootstrapURL != null) {
                setBootstrapURL(bootstrapURL);
                setProjectURL(projectUrl);
            }
        }
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_main);
//
//        findViewById(R.id.btn).setOnClickListener(this);


    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void onClick(View v) {

//        if(v.getId() == R.id.btn) {
//
//            int result;
//            List<String> permissions = new ArrayList<>();           //필요한 권한 전체 리스트
//            List<String> requestPermissions = new ArrayList<>();    //실제 권한요청 리스트
//
//            permissions.add( Manifest.permission.WRITE_EXTERNAL_STORAGE );
//
//
//            //실제 요청해야할 권한 체크
//            for (String pm : permissions) {
//                result = ContextCompat.checkSelfPermission(this, pm);
//
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions.add(pm);
//                }
//            }
//
//            if (!requestPermissions.isEmpty()) {    //요청해야할 권한이 있으면 권한요청
//                ActivityCompat.requestPermissions(
//                        this,
//                        requestPermissions.toArray(new String[requestPermissions.size()]), Define.REQUEST_PERMISSION.STORAGE);
//            } else {
//
//                String url = "https://blogfiles.pstatic.net/MjAyMTA1MDZfMjc2/MDAxNjIwMjkwMDQyNjI2.QZ_YuFAm0JwsAX3o4rWMdb0mktENmVbyyuXnw6oRDQog.7iX-uzLW9RgDS3Y02GLc7F0QIYjQRMpEp6yBlmQ4SQ8g.JPEG.bbyyaa8/IMG_0768.JPG?type=w1";
//                new DownloadFileAsync(this).execute(url, "1", "1");
//            }
//
//
//
//
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String url = "https://blogfiles.pstatic.net/MjAyMTA1MDZfMjc2/MDAxNjIwMjkwMDQyNjI2.QZ_YuFAm0JwsAX3o4rWMdb0mktENmVbyyuXnw6oRDQog.7iX-uzLW9RgDS3Y02GLc7F0QIYjQRMpEp6yBlmQ4SQ8g.JPEG.bbyyaa8/IMG_0768.JPG?type=w1";
        new DownloadFileAsync(this).execute(url, "1", "1");
    }
}