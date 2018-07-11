package com.konka.konkaim.chat;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;

/**
 * Created by HP on 2018-5-15.
 */

public class StateUtil {
    public static final int CHAT_NOT_FRIEND_FAIL = -3; //非对方好友，通话失败
    public static final int CONNECT_FAIL = -1; //接通失败
    public static final int CHAT_FAIL = -2; //通话失败
    public static final int WAIT_TO_CONNECT = 0; //等待接通...
    public static final int CHATTING = 1; //聊天中...
    public static final int CHAT_OVER = 2; //聊天结束，已经挂断
    public static final int CHAT_SELF = 3; //本人自己

    public static final String STATE_ONLINE = "在线"; //在线
    public static final String STATE_OFFLINE = "离线"; //离线

    public static String getCurrentChatState(){
        StatusCode status = NIMClient.getStatus();
        if (status == StatusCode.LOGINED) return STATE_ONLINE;
        else return STATE_OFFLINE;
    }
}
