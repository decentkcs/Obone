package kr.co.ob.obone.android;

import com.google.zxing.common.StringUtils;
import com.nexacro.NexacroActivity;
import com.nexacro.plugin.NexacroPlugin;
import com.nexacro.util.Log;

import org.json.JSONObject;

/*
    Class Name      : StandardObject
    Description     : NexacroPlugin 상속받아 앱과 nexacro 화면의 데이터를 주고 받기위해 재정의한 공통 Class
 */
public class StandardObject extends NexacroPlugin{
    private final String LOG_TAG = this.getClass().getSimpleName(); //현재 Class Name 문자열

    //nexacro 로 반환될 이벤트 속성명
    private static final String SVCID    = "svcid";
    private static final String REASON   = "reason";
    private static final String RETVAL   = "returnvalue";
    private static final String CALLFLAG   = "callFlag";

    public static final int CODE_SUCCESS = 0;                   //처리결과 코드(성공)
    public static final int CODE_ERROR  = -1;                   //처리결과 코드(실패)
    public static final int CODE_PERMISSION_ERROR  = -9;      //앱 권한관련 에러코드

    //처리후 nexacro 에서 호출될 callback 이벤트명 (고정)
    public static final String ON_CALLBACK = "_oncallback";
    //처리후 nexacro 에서 호출될 이벤트명 (고정)
    public static final String ON_RESUME = "_onresume";
    //처리후 nexacro 에서 호출될 permissionresult 이벤트명 (고정)
    public static final String ON_PERMISSION_RESULT = "_onpermissionresult";
    //nexacro 에서 Android 호출한 메소드명
    public static final String METHOD_CALLMETHOD = "callMethod";

    private String mServiceId = "obone_android";         //화면에서 callMethod 호출시 method id
    private JSONObject mParamData = null;   //화면에서 callMethod 호출시 Parameter

    private NexacroActivityExt nexaExtObj = null;

    /* 생성시 NexacroActivity 와 연결을 위한 작업    */
    public StandardObject(String objectId)
    {
        //super Class 생성자 호출
        super(objectId);
        //현재 실행된 nexacro Activity의 Instance
        nexaExtObj = (NexacroActivityExt) NexacroActivity.getInstance();
        //현재 Class를 NexacroActivityExt 에서 참조할 수 있도록 셋팅
        nexaExtObj.setPlugin(this);
    }

    /* Nexacro 화면에서 new nexacro.Standard() 호출시 발생    */
    @Override
    public void init(JSONObject paramObject)
    {
        Log.d(LOG_TAG,"##########################################################################");
        Log.d(LOG_TAG,"#############    StandardObject Class > init 메소드 호출  #################");
        Log.d(LOG_TAG,"##########################################################################");
    }

    /* Nexacro 화면에서 Standard.destroy() 호출시 발생    */
    @Override
    public void release(JSONObject paramObject)
    {
        Log.d(LOG_TAG,"###########################################################################");
        Log.d(LOG_TAG,"#############    StandardObject Class > release 메소드 호출  ###############");
        Log.d(LOG_TAG,"###########################################################################");

        nexaExtObj.stopGpsService();
    }

    /* Nexacro 화면에서 StandardObject 의 callMethod 를 호출시 발생    */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(String method, JSONObject paramObject)
    {
        mServiceId = "";
        mParamData = null;

        //화면에서 호출된 메소드 명이 "callMethod" 일시
        if(method.equals(METHOD_CALLMETHOD))
        {
            try
            {
                JSONObject params = paramObject.getJSONObject("params");   //화면에서 받은 데이터

                mServiceId = params.getString("serviceid");       //화면에서 받은 메소드 ID 문자열
                mParamData = params.getJSONObject("param");               //화면에서 받은 Json 형태 Parameter

                //NexacroActivityExt 의 처리 메소드 호출
                nexaExtObj.callMethod(mServiceId, mParamData);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                send(CODE_ERROR, METHOD_CALLMETHOD + ":" + e.getMessage(), ON_CALLBACK);
            }
        }
    }

    /*
        Nexacro 화면으로 데이터 반환을 위한 메소드

        reason : 화면으로 넘길 반환코드 (CODE_SUCCES, CODE_ERROR, CODE_PERMISSION_ERROR)
        retval : 화면으로 넘길 데이터
        action : 화면에서 호출될 이벤트 (ON_CALLBACK, ON_RESUME, ON_PERMISSION_RESULT)
        */
    public boolean send(int reason, Object retval, String action)
    {
        JSONObject obj = new JSONObject();

        try
        {
            obj.put(SVCID, mServiceId);     //callMethod 호출에 의한 callback 이 아니면 값은 없다.
            obj.put(REASON, reason);
            obj.put(RETVAL, retval);

            callback(action, obj);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            mServiceId = null;
            mParamData = null;
        }

        return true;
    }

    /* 예약된 이벤트명을 반환 */
    public String getActionString (String action)
    {
        if(action.equals(this.ON_CALLBACK)) //_oncallback
        {
            return this.ON_CALLBACK;
        }
        else if(action.equals(this.ON_RESUME))  //_onresume
        {
            return this.ON_RESUME;
        }
        else if(action.equals(this.ON_PERMISSION_RESULT))   //_onpermissionresult
        {
            return this.ON_PERMISSION_RESULT;
        }
        else if(action.equals(this.METHOD_CALLMETHOD))      //화면에서 호출한 메소드 "callMethod"
        {
            return METHOD_CALLMETHOD;
        }

        return "";
    }
}
