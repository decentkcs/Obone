package kr.co.ob.obone.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.nexacro.NexacroResourceManager;
import com.nexacro.NexacroUpdatorActivity;

import device.common.DecodeResult;
import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.sdk.ScanManager;
import kr.co.ob.obone.android.common.CommonConstants;
import kr.co.ob.obone.android.download.DownloadFileAsync;
import kr.co.ob.obone.android.nexacro.NexacroActivityExt;

public class MainActivity extends NexacroUpdatorActivity implements View.OnClickListener {




    public MainActivity() {
        super();

        setBootstrapURL(CommonConstants.BASE_URL + CommonConstants.MOBILE_PATH + CommonConstants.START_FILE);
        setProjectURL(CommonConstants.BASE_URL + CommonConstants.MOBILE_PATH);

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

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @Override
    public void onClick(View v) {
    }



}