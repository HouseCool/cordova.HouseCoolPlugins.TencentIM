package com.Hongleilibs.IM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;
import com.tencent.imsdk.TIMMessageListener;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class loginActivity extends Activity {
    private String tag = "loginActivity";
    Intent intent=new Intent();
    String action = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String TAG = "loginActivity";
        super.onCreate( savedInstanceState );
        action = this.getIntent().getStringExtra("action");
        if (action.equals( "login" )){
            this.Login();
        }else if(action.equals( "logout" )){
            this.Logout();
        }else if(action.equals( "getLoginUser" )){
            this.GetLoginUser();
        }
    }

    //如用户主动注销或需要进行用户的切换，则需要调用注销操作。
    private void Logout(){
        TIMManager.getInstance().logout(new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                Log.d(tag, "logout failed. code: " + code + " errmsg: " + desc);

                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                //错误码 code 列表请参见错误码表
                Cloudcomment.pushError( "logout failed. code: " + code + " errmsg: " + desc ,action );
                finish();
            }

            @Override
            public void onSuccess() {
                  //登出成功
                Log.d( tag, "onSuccess: "+"Logout success" );
                Cloudcomment.pushData( "Logout success",action );
                finish();
            }
        });
    }

    //登录需要用户提供 identifier、userSig 等信息
    private void  Login(){

        String identifier = this.getIntent().getStringExtra("identifier");
        Log.d( tag, "identifier: "+identifier );
        String UserSig = this.getIntent().getStringExtra("UserSig");
        Log.d( tag, "UserSig: "+UserSig );
        TIMManager.getInstance().login(identifier,UserSig, new TIMCallBack() {
            @Override
            public void onError(int code, String desc) {
                Cloudcomment.pushError( "login failed. code: " + code + " errmsg: " + desc,action );
                finish();
            }

            @Override
            public void onSuccess() {
                //通过setResult绑定返回值
                JSONObject jo = new JSONObject(  );
                try {
                    jo.put( "user",identifier );
                    jo.put( "status", "Login success");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Cloudcomment.pushData( jo,action );
                finish();
            }
        });
    }

    //获取当前登录用户
    private void GetLoginUser()  {
        String user =  TIMManager.getInstance().getLoginUser();
        Log.d( tag, "GetLoginUser: " +user);
        Cloudcomment.pushData( user,action );
        finish();
    }
}
