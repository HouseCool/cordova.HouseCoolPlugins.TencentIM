package com.Hongleilibs.IM;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.util.Log;
import com.Hongleilibs.IM.Cloudcomment;

import com.tencent.imsdk.TIMConversation;
import com.tencent.imsdk.TIMConversationType;
import com.tencent.imsdk.TIMElem;
import com.tencent.imsdk.TIMElemType;
import com.tencent.imsdk.TIMImage;
import com.tencent.imsdk.TIMImageElem;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMMessage;

import com.tencent.imsdk.TIMMessageListener;
import com.tencent.imsdk.TIMSoundElem;
import com.tencent.imsdk.TIMTextElem;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.imsdk.ext.message.TIMConversationExt;
import com.tencent.imsdk.ext.message.TIMManagerExt;
import com.tencent.imsdk.ext.message.TIMMessageExt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class messagingActivity extends Activity  {
    private String TAG = "messagingActivity";
    private static TIMConversation conversation;
    private static Intent intent = new Intent();
    private String action = "";
    int sessionCount = 0;       //获取本地会话条数

    public Cloudcomment cloudcomment = new Cloudcomment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        action = this.getIntent().getStringExtra("action");
        if (action.equals( "sendMessage" )){
            this.sendMessage();
        }else if(action.equals( "getConversation" )){
            this.getConversation();
        }else if(action.equals( "TIMMessageListener" )){
            this.addMessageListener();
        }else if(action.equals( "sendImageMessage" )){
            this.sendImageMessage();
        }else if(action.equals( "TIMSoundMessage" )){
            this.TIMSoundMessage();
        }else if(action.equals( "getConversationList" )){
            this.getConversationList();
        }
    }

    //消息监听器:接收消息
    private void addMessageListener(){
        //设置消息监听器，收到新消息时，通过此监听器回调
        //为了不漏掉消息通知，建议在登录之前注册新消息通知
        TIMManager.getInstance().addMessageListener(new TIMMessageListener() {//消息监听器
            @Override
            public boolean onNewMessages(List<TIMMessage> msgs) {//收到新消息
                //消息的内容解析请参考消息收发文档中的消息解析说明
                JSONObject informationArr = new JSONObject(  );
                for (TIMMessage msg:msgs){
                    for(int i = 0; i < msg.getElementCount(); ++i) {
                        JSONObject information = new JSONObject(  );
                        TIMElem elem = msg.getElement(i);

                        //获取当前元素的类型
                        TIMElemType elemType = elem.getType();
                        Log.d(TAG, "elem type: " + elemType.name());
                        if (elemType == TIMElemType.Text) {
                            //处理文本消息
                            try {
                                TIMTextElem Textmessage = (TIMTextElem)msg.getElement(i);
                                Log.d( TAG, "获取当前元素  onNewMessages: "+Textmessage.getText()+"  条数："+msg.getElementCount() );
                                information.put( "message",Textmessage.getText() );
                                long ctime = msg.timestamp();
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                                String time = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                                information.put( "ctime",time );
                                TIMUserProfile userProfile = msg.getSenderProfile();
                                String identifer = userProfile.getIdentifier();
                                information.put( "identifer",identifer );
                                boolean isSelf = msg.isSelf();
                                information.put( "isSelf",isSelf );
                                informationArr.put( "Text",information );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else if (elemType == TIMElemType.Image) {
                            //图片元素
                            TIMImageElem e = (TIMImageElem) elem;
                            for(TIMImage image : e.getImageList()) {

                                //获取图片类型, 大小, 宽高
                                Log.d(TAG, "image type: " + image.getType() +
                                        " image size " + image.getSize() +
                                        " image height " + image.getHeight() +
                                        " image width " + image.getWidth() +
                                        " image url " + image.getUrl());

                                long ctime = msg.timestamp();
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                                String time = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                                try {
                                    information.put( "ctime",time );
                                    TIMUserProfile userProfile = msg.getSenderProfile();
                                    String identifer = userProfile.getIdentifier();
                                    information.put( "identifer",identifer );
                                    boolean isSelf = msg.isSelf();
                                    information.put( "isSelf",isSelf );
                                    information.put( "image_url",image.getUrl() );
                                    informationArr.put( "Image",information );
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }else if(elemType == TIMElemType.Sound){//语音消息接收
                        }
                    }
                }
                Cloudcomment.pushData( informationArr,"TIMMessageListener" );
                return false; //返回true将终止回调链，不再调用下一个新消息监听器
            }
        });
        finish();
    }

    //获取会话列表
    private void getConversationList() {
        JSONArray arr = new JSONArray(  );
        List<TIMConversation> list = TIMManagerExt.getInstance().getConversationList();
        //网络读取会话
//        if(list == null){
//
//        }else {
        //本地读取会话
        for(TIMConversation con:list) {
            TIMConversationExt conExt = new TIMConversationExt( con );
            //获取此会话的消息
                conExt.getLocalMessage(1, //获取此会话最近的 10 条消息
                        null, //不指定从哪条消息开始获取 - 等同于从最新的消息开始往前
                        new TIMValueCallBack<List<TIMMessage>>() {//回调接口
                            @Override
                            public void onError(int code, String desc) {//获取消息失败
                                //接口返回了错误码 code 和错误描述 desc，可用于定位请求失败原因
                                //错误码 code 含义请参见错误码表
                                Log.d(TAG, "get message failed. code: " + code + " errmsg: " + desc);
                                Cloudcomment.pushError("get message failed. code: " + code + " errmsg: " + desc,action);
                                finish();
                            }

                            @Override
                            public void onSuccess(List<TIMMessage> msgs) {//获取消息成功
                                for(int i = 0; i <=msgs.size()-1;i++){
                                    JSONObject jo = new JSONObject(  );
                                    TIMMessage msg = msgs.get( i );
                                    TIMElem elem = msg.getElement( i );
                                    //可以通过 timestamp()获得消息的时间戳, isSelf()是否为自己发送的消息
                                    Log.e(TAG, "get msg: " + msg.timestamp() + " self: " + msg.isSelf() + " seq: " + msg.getSeq());
                                    //获取当前元素的类型
                                    TIMElemType elemType = elem.getType();
                                    if (elemType == TIMElemType.Text){
                                        TIMTextElem text =  (TIMTextElem)msg.getElement(0);
                                        try {
                                            jo.put( "messge",text.getText());
                                            long ctime = msg.timestamp();
                                            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                                            String time = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                                            jo.put( "ctime",time );
                                            String identifer = msg.getSender();
                                            jo.put( "identifer",identifer );
                                            arr.put( jo );
                                            //未读消息计数
                                            //                                    TIMMessageExt msgExt = new TIMMessageExt(msg);
                                            //                                    msgExt.isRead();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }else if(elemType == TIMElemType.Image){
                                        try {
                                            jo.put( "messge","[图片]" );
                                            long ctime = msg.timestamp();
                                            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                                            String time = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                                            jo.put( "ctime",time );
                                            TIMUserProfile userProfile = msg.getSenderProfile();
                                            String identifer = userProfile.getIdentifier();
                                            jo.put( "identifer",identifer );
                                            arr.put( jo );
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }else if(elemType == TIMElemType.Sound){
                                        try {
                                            jo.put( "messge","[语音消息]" );
                                            long ctime = msg.timestamp();
                                            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                                            String time = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
                                            jo.put( "ctime",time );
                                            TIMUserProfile userProfile = msg.getSenderProfile();
                                            String identifer = userProfile.getIdentifier();
                                            jo.put( "identifer",identifer );
                                            arr.put( jo );
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }}
                            }
                        });
                if(sessionCount == list.size() - 1){
                    Cloudcomment.pushData( arr,action );
                    sessionCount = 0;
                }
            }
            Cloudcomment.pushData( "GetLocal success!",action );
            finish();
//        }
    }

    //语音消息发送
       private void TIMSoundMessage(){
//        if (conversation == null){
//            return;
//        }
//        //构造一条消息
//        TIMMessage msg = new TIMMessage();
//
//        //添加语音
//        TIMSoundElem elem = new TIMSoundElem();
//        String filePath = this.getIntent().getStringExtra("path");
//        elem.setPath(filePath); //填写语音文件路径
//        long speech = elem.getDuration(); //获取语音时长
//        Log.d( TAG, "获取语音时长: "+speech );
//
//        //将 elem 添加到消息
//        if(msg.addElement(elem) != 0) {
//            Log.d(TAG, "addElement failed");
//            return;
//        }
//        //发送消息
//        conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
//            @Override
//            public void onError(int code, String desc) {//发送消息失败
//                //错误码code和错误描述desc，可用于定位请求失败原因
//                //错误码code含义请参见错误码表
//                Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
//                intent.putExtra("item","send message failed. code: " + code + " errmsg: " + desc);
//                setResult(RESULT_CANCELED,intent);
//                finish();
//            }
//
//            @Override
//            public void onSuccess(TIMMessage msg) {//发送消息成功
//                Log.e(TAG, "SendMsg ok");
//                intent.putExtra("item","SendMsg ok");
//                setResult(RESULT_OK,intent);
//                finish();
//            }
//        });
    }

    //文本消息发送
    private void sendMessage(){
        if (conversation == null){
            Cloudcomment.pushError( "Unfetched session",action );
            finish();
            return;
        }
        //构造一条消息
        TIMMessage messages = new TIMMessage();
        //添加文本内容
        TIMTextElem elem = new TIMTextElem();
        String message = this.getIntent().getStringExtra("message");
        elem.setText(message);
        //将elem添加到消息
        if(messages.addElement(elem) != 0) {
            Log.d(TAG, "addElement failed");
            return;
        }

        //发送消息
        conversation.sendMessage(messages, new TIMValueCallBack<TIMMessage>() {//发送消息回调
        @Override
        public void onError(int code, String desc) {
            //发送消息失败
            //错误码 code 和错误描述 desc，可用于定位请求失败原因
            //错误码 code 含义请参见错误码表
            Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
            Cloudcomment.pushError( "send message failed. code: " + code + " errmsg: " + desc,action );
            finish();
        }

        @Override
        public void onSuccess(TIMMessage msg) {//发送消息成功
            Log.d(TAG, "SendMsg ok");
            long ctime = msg.timestamp();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
            String res = sdf.format(new Date(ctime*1000));   // 时间戳转换成时间
            long Seq = msg.getSeq();
            boolean isSelf =  msg.isSelf();
            String identifer = msg.getSender();
            JSONObject jo = new JSONObject(  );
            try {
                //消息是否已读
                jo.put( "isSelf",isSelf );
                //消息发送者
                jo.put( "identifer",identifer );
                //消息发送状态
                jo.put( "status","SendMsg ok");
                //消息发送时间
                jo.put( "ctime",res );
                //消息序列码
                jo.put( "Seq",Seq );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Cloudcomment.pushData( jo,action );
            finish();
        }
        });
    }

    //图片消息发送
    public void sendImageMessage(){
//        if (conversation == null){
//            return;
//        }
//        //构造一条消息
//        TIMMessage msg = new TIMMessage();
//
//        //添加图片
//        TIMImageElem elem = new TIMImageElem();
//        String path = this.getIntent().getStringExtra("path");
//        elem.setPath(path);
//
//        //将 elem 添加到消息
//        if(msg.addElement(elem) != 0) {
//            Log.d(TAG, "addElement failed");
//            return;
//        }
//
//        //发送消息
//        conversation.sendMessage(msg, new TIMValueCallBack<TIMMessage>() {//发送消息回调
//            @Override
//            public void onError(int code, String desc) {//发送消息失败
//                //错误码 code 和错误描述 desc，可用于定位请求失败原因
//                //错误码 code 列表请参见错误码表
//                Log.d(TAG, "send message failed. code: " + code + " errmsg: " + desc);
//                intent.putExtra("item","send message failed. code: " + code + " errmsg: " + desc);
//                setResult(RESULT_CANCELED,intent);
//                finish();
//            }
//
//            @Override
//            public void onSuccess(TIMMessage msg) {//发送消息成功
//                Log.e(TAG, "SendMsg ok");
//                intent.putExtra("item","SendMsg ok");
//                setResult(RESULT_OK,intent);
//                finish();
//            }
//        });
    }

    //会话获取
    private void getConversation(){
        intent = new Intent();
        String type = this.getIntent().getStringExtra("type");
        Log.d( TAG, "type: "+type );
        String peer  = this.getIntent().getStringExtra("peer");
        Log.d( TAG, "peer : "+peer  );
        Log.d( TAG,"conversation: "+conversation );
        if (type.equals( "C2C" )){
            //获取单聊会话
            conversation = TIMManager.getInstance().getConversation(
                    TIMConversationType.C2C,    //会话类型：单聊
                    peer);                      //会话对方用户帐号//对方ID
        }else if(type == "Group"){
            //获取群聊会话
            conversation = TIMManager.getInstance().getConversation(
                    TIMConversationType.Group,      //会话类型：群组
                    peer);                       //群组 ID
        }
        if (conversation != null){
            Cloudcomment.pushData( "Obtain session success",action );
        }else{
            Cloudcomment.pushError( "Session fetch exception",action );
        }
        finish();
    }
}
