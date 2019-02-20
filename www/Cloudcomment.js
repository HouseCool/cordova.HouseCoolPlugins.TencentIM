var exec = require('cordova/exec');

//登录
exports.login = function(success, error,identifier,UserSig) {
	if(typeof UserSig == "undefined" || UserSig == null || UserSig == ""){
        throw "UserSig为空";
    }
    if(typeof identifier == "undefined" || identifier == null || identifier == ""){
            throw "identifier为空";
    }
    return exec(success, error, "Cloudcomment", "login", [identifier,UserSig]);
}

//获取登录信息
/**
*return  user:登陆人信息
*/
exports.getLoginUser = function(success, error) {
    return exec(success, error, "Cloudcomment", "getLoginUser", []);
}

//登出
exports.logout = function(success, error) {
    return exec(success, error, "Cloudcomment", "logout", []);
}

//获取会话
exports.getConversation = function(success, error,type,peer) {
    if(typeof type == "undefined" || type == null || type == ""){
        throw "会话类型为空";
    }
    if(typeof peer == "undefined" || peer == null || peer == ""){
            throw "会话对方用户帐号为空";
    }
    return exec(success, error, "Cloudcomment", "getConversation", [type,peer]);
}

//文本消息发送
/*
 *status:消息发送状态
 *ctime:消息发送时间
 *Seq:消息序列码
 *isSelf:消息是否已读
 *identifer:消息发送用户ID
 */
exports.sendMessage = function(success,error,Message) {
    return exec(success, error, "Cloudcomment", "sendMessage", [Message]);
}

//发送图片消息
exports.sendImageMessage = function(success,error,path) {
    return exec(success, error, "Cloudcomment", "sendImageMessage", [path]);
}

//接收消息
/*
*return 返回结果
*TextInformation、ImageInformation、SoundInformation：（文字消息、图片消息、语音消息）
*message：消息  ctime：发送时间  identifer：发送用户ID（唯一标识）  isSelf：是否已读
*/
exports.TIMMessageListener = function(success, error) {
    return exec(success, error,"Cloudcomment", "TIMMessageListener", []);
}

//同步获取会话最后的消息
/**
 * 从 cache 中获取最后几条消息
 * @param count 需要获取的消息数，最多为 20
 * @return 消息列表，第一条为最新消息。会话非法时，返回 null。
 */
exports.getConversationList = function(success, error ) {
    return exec(success, error,"Cloudcomment", "getConversationList", []);
}
