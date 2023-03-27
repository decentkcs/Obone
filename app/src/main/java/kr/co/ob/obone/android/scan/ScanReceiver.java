package kr.co.ob.obone.android.scan;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.NoCopySpan;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONObject;

import device.common.DecodeResult;
import device.common.ScanConst;
import device.sdk.ScanManager;
import kr.co.ob.obone.android.common.CommonConstants;
import kr.co.ob.obone.android.nexacro.NexacroActivityExt;
import kr.co.ob.obone.android.nexacro.StandardObject;

public class ScanReceiver extends BroadcastReceiver {

    private static final String TAG = "tScanner";


    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {

                boolean result              = intent.getBooleanExtra(ScanConst.EXTRA_EVENT_DECODE_RESULT, false);
                int decodeBytesLength       = intent.getIntExtra(ScanConst.EXTRA_EVENT_DECODE_LENGTH, 0);
                byte[] decodeBytesValue     = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                String decodeValue          = new String(decodeBytesValue, 0, decodeBytesLength);
                int decodeLength            = decodeValue.length();
                String symbolName           = intent.getStringExtra(ScanConst.EXTRA_EVENT_SYMBOL_NAME);
                byte symbolId               = intent.getByteExtra(ScanConst.EXTRA_EVENT_SYMBOL_ID, (byte) 0);
                int symbolType              = intent.getIntExtra(ScanConst.EXTRA_EVENT_SYMBOL_TYPE, 0);
                byte letter                 = intent.getByteExtra(ScanConst.EXTRA_EVENT_DECODE_LETTER, (byte) 0);
                byte modifier               = intent.getByteExtra(ScanConst.EXTRA_EVENT_DECODE_MODIFIER, (byte) 0);
                int decodingTime            = intent.getIntExtra(ScanConst.EXTRA_EVENT_DECODE_TIME, 0);

                System.out.println(TAG + " == " + "1. result: " + result);
                System.out.println(TAG + " == " + "2. bytes length: " + decodeBytesLength);
                System.out.println(TAG + " == " + "3. bytes value: " + decodeBytesValue);
                System.out.println(TAG + " == " + "4. decoding length: " + decodeLength);
                System.out.println(TAG + " == " + "5. decoding value: " + decodeValue);
                System.out.println(TAG + " == " + "6. symbol name: " + symbolName);
                System.out.println(TAG + " == " + "7. symbol id: " + symbolId);
                System.out.println(TAG + " == " + "8. symbol type: " + symbolType);
                System.out.println(TAG + " == " + "9. decoding letter: " + letter);
                System.out.println(TAG + " == " + "10.decoding modifier: " + modifier);
                System.out.println(TAG + " == " + "11.decoding time: " + decodingTime);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("scanVal", decodeValue);
                ((NexacroActivityExt)NexacroActivityExt.context).callMethod("scan", jsonObject);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
