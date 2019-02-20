package com.Hongleilibs.IM;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMSdkConfig;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;

import tencent.tls.platform.TLSLoginHelper;

/**
 * This class echoes a string called from JavaScript.
 */
public class Cloudcomment extends CordovaPlugin {
    public static final String TAG = "CordovaPlugin";
    public static final String APPID_PROPERTY_KEY = "sdkappid";
    protected String appId;
    private JSONArray args;
    private static CallbackContext callbackContext;
    private  static CallbackContext callbackContexts;
    private  static CallbackContext cbContext;
    private static String actions;

    public void Cloudcomment(){

    }
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        TLSLoginHelper tlsLoginHelper;
        super.initialize(cordova, webView);
        System.out.println( "initialize: "+ TIMManager.getInstance().getVersion() );
        //初始化SDK基本配置
        TIMSdkConfig config = new TIMSdkConfig(Config.SdkAppId)
                .setAccoutType(String.valueOf(Config.accountType))
                .enableCrashReport(false)
                .enableLogPrint(true)
                .setLogLevel(TIMLogLevel.ERROR)
                .setLogPath(Environment.getExternalStorageDirectory().getPath() + "/petpet/");
        Context context =  this.cordova.getActivity();
        //初始化SDK
        boolean init=TIMManager.getInstance().init(context, config);
        if(init){
            Log.i(TAG, "init success");
        }else{
            Log.i(TAG, "init fail");
        }
        tlsLoginHelper = TLSLoginHelper.getInstance().init( context,Config.SdkAppId,Config.accountType,"1.0" );
        //TimeOut
        tlsLoginHelper.setTimeOut( 2000 );
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        System.out.println("--------execute----------execute------------execute--------execute-------");
        this.args = args;
        actions = action;

        this.callbackContext = callbackContext;
        if (action.equals("login") || action.equals("logout") || action.equals("getLoginUser")) {
            this.loginActivity(action);
            sendNoResultPluginResult(callbackContext);
            return true;
        }else if(action.equals( "getConversation" ) || action.equals( "sendMessage" ) || action.equals( "sendImageMessage" ) || action.equals( "TIMSoundMessage" )) {
            this.messagingActivity(action);
            sendNoResultPluginResult(callbackContext);
            return true;
        }else if (action.equals( "TIMMessageListener" )){
            this.callbackContexts = callbackContext;
            sendNoResultPluginResult(this.callbackContexts);
            this.messagingActivity(action);
            sendNoResultPluginResult(callbackContext);
            return true;
        }else if (action.equals( "getConversationList" )){
            this.cbContext = callbackContext;
            sendNoResultPluginResult(this.callbackContexts);
            this.messagingActivity(action);
            sendNoResultPluginResult(callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        System.out.println("********************************************"+"requestCode: "+requestCode +
                "    :   "+Activity.RESULT_OK);
          //根据resultCode判断处理结果
        String spot = "";
        long ctime = 0;
            if(resultCode == Activity.RESULT_OK){
                if (intent.hasExtra( "ctime" )){
                    ctime = intent.getLongExtra( "ctime",ctime );
                    Log.d( TAG, "ctime: "+ctime );
                }
                spot=intent.getStringExtra("item");
                Log.d( TAG, "spot: "+spot );
                if (ctime !=0){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                    String res = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                    spot = spot + " : "+res;
                }
                this.pushData(spot,actions);
        }else if(resultCode == Activity.RESULT_CANCELED){
            System.out.println("Activity.RESULT_CANCELED:  "+actions);
            spot=intent.getStringExtra("item");
            this.callbackContext.error(spot);
        }
     }

     //消息模块
    protected void messagingActivity(String action) throws JSONException {
        Intent i = new Intent(this.cordova.getActivity(), messagingActivity.class);
        i.putExtra("action", action);
        if (action.equals( "getConversation" )){
            i.putExtra("type", this.args.getString(0));
            i.putExtra("peer", this.args.getString(1));
        }else if(action.equals( "sendMessage" )){
            i.putExtra("message", this.args.getString(0));
        }else if(action.equals( "sendImageMessage" ) || action.equals( "TIMSoundMessage" )){
            i.putExtra( "path",this.args.getString( 0 ));
        }
        this.cordova.getActivity().startActivity(i);
    }

    //登录模块
    protected void loginActivity(String action) throws JSONException {
        Intent i = new Intent(this.cordova.getActivity(), loginActivity.class);
        i.putExtra("action", action);
        if (action.equals( "login" )){
            Log.d( TAG, "identifier: "+ this.args.getString(0));
            Log.d( TAG, "UserSig: "+ this.args.getString(1));
            i.putExtra("identifier", this.args.getString(0));
            i.putExtra("UserSig", this.args.getString(1));
        }
        this.cordova.getActivity().startActivity(i);
    }

    private void sendNoResultPluginResult(CallbackContext callbackContext) {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    /**
     * 接收推送内容并返回给前端JS
     *
     * @param msg 消息对象
     */
    public static void pushData(final String msg , String actions) {
        if(callbackContexts == null || callbackContext == null) {
            return;
        }

        if (actions.equals( "TIMMessageListener" )){
            PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
            result.setKeepCallback(true);
            callbackContexts.sendPluginResult(result);
        }else {
            PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    public static void pushData(final JSONObject msg , String actions) {
        if(callbackContexts == null || callbackContext == null) {
            return;
        }

        if (actions.equals( "TIMMessageListener" )){
            PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
            result.setKeepCallback(true);
            callbackContexts.sendPluginResult(result);
        }else {
            PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    public static void pushData(final JSONArray msg,String name) {
        if(callbackContexts == null || callbackContext == null) {
            return;
        }
        PluginResult result = new PluginResult(PluginResult.Status.OK, msg);
        result.setKeepCallback(true);
        callbackContexts.sendPluginResult(result);
        }

    /**
     * 接收推送内容并返回给前端JS
     *
     * @param msg 错误yuanyin
     */
    public static void pushError(final String msg , String actions) {
        if(callbackContexts == null || callbackContext == null) {
            return;
        }

        if (actions.equals( "TIMMessageListener" )){
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
            result.setKeepCallback(true);
            callbackContexts.sendPluginResult(result);
        }else if (actions.equals( "getConversationList" )){
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
            result.setKeepCallback(true);
            cbContext.sendPluginResult(result);
        }else {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }
}
