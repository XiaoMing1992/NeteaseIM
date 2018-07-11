package com.konka.konkaim.chat.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAttachment;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit自定义消息界面用法展示类
 */
public class SessionHelper {

    private static final int ACTION_HISTORY_QUERY = 0;
    private static final int ACTION_SEARCH_MESSAGE = 1;
    private static final int ACTION_CLEAR_MESSAGE = 2;

//    private static SessionCustomization p2pCustomization;
//    private static SessionCustomization normalTeamCustomization;
//    private static SessionCustomization advancedTeamCustomization;
//    private static SessionCustomization myP2pCustomization;
//    private static SessionCustomization robotCustomization;
//    private static RecentCustomization recentCustomization;
//
//    private static NIMPopupMenu popupMenu;
//    private static List<PopupMenuItem> menuItemList;

    public static void init() {
        // 注册自定义消息附件解析器
//        NIMClient.getService(MsgService.class).registerCustomAttachmentParser(new CustomAttachParser());

        // 注册各种扩展消息类型的显示ViewHolder
        //registerViewHolders();

        // 设置会话中点击事件响应处理
        //setSessionListener();

        // 注册消息转发过滤器
        //registerMsgForwardFilter();

        // 注册消息撤回过滤器
        //registerMsgRevokeFilter();

        // 注册消息撤回监听器
        //registerMsgRevokeObserver();

//        NimUIKit.setCommonP2PSessionCustomization(getP2pCustomization());
//
//        NimUIKit.setCommonTeamSessionCustomization(getTeamCustomization(null));
//
//        NimUIKit.setRecentCustomization(getRecentCustomization());
    }

    public static void startP2PSession(Context context, String account) {
        startP2PSession(context, account, null);
    }

    public static void startP2PSession(Context context, String account, IMMessage anchor) {
//        if (!DemoCache.getAccount().equals(account)) {
//            if (NimUIKit.getRobotInfoProvider().getRobotByAccount(account) != null) {
//                NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getRobotCustomization(), anchor);
//            } else {
//                NimUIKit.startP2PSession(context, account, anchor);
//            }
//        } else {
//            NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, getMyP2pCustomization(), anchor);
//        }
    }

    public static void startTeamSession(Context context, String tid) {
        //startTeamSession(context, tid, null);

    }

    public static void startTeamSession(Context context, String tid, IMMessage anchor) {
        //NimUIKit.startTeamSession(context, tid, getTeamCustomization(tid), anchor);
    }

    // 打开群聊界面(用于 UIKIT 中部分界面跳转回到指定的页面)
    public static void startTeamSession(Context context, String tid, Class<? extends Activity> backToClass, IMMessage anchor) {
        //NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, getTeamCustomization(tid), backToClass, anchor);
    }

}
