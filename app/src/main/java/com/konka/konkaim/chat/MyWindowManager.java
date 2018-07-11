package com.konka.konkaim.chat;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import com.netease.nimlib.sdk.avchat.model.AVChatData;

import java.util.ArrayList;

/**
 * Created by HP on 2018-4-24.
 */

public class MyWindowManager {

    private ChatTinyView chatTinyView;
    private static volatile MyWindowManager instance;

    private MyWindowManager() {
    }

    public static MyWindowManager getInstance() {
        if (instance == null) {
            synchronized (MyWindowManager.class) {
                if (instance == null)
                    instance = new MyWindowManager();
            }
        }
        return instance;
    }

    /**
     * 创建悬浮窗
     */
    public void createChatTinyView(Context context, boolean isTeamChat) {
        if (chatTinyView == null)
            chatTinyView = new ChatTinyView(context, isTeamChat);
    }

    /**
     * 创建悬浮窗
     */
    public void createChatTinyView(Context context, boolean isTeamChat, AVChatData data, String displayName, int source) {
        if (chatTinyView == null)
            chatTinyView = new ChatTinyView(context, isTeamChat, data, displayName, source);
    }

    /**
     * 创建悬浮窗
     */
    public void createChatTinyView(Context context, boolean isTeamChat, boolean receivedCall, String teamId,
                                   String roomId, ArrayList<String> accounts, String teamName, String friendAccount) {
        if (chatTinyView == null)
            chatTinyView = new ChatTinyView(context, isTeamChat, receivedCall, teamId, roomId, accounts, teamName, friendAccount);
    }

    /**
     * 移除悬浮窗
     *
     * @param context
     */
    public void removeChatTinyView(Context context) {
        if (chatTinyView != null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(chatTinyView);
            chatTinyView = null;
        }
    }

    /**
     * 隐藏悬浮窗
     *
     * @param
     */
    public void hideChatTinyView() {
        if (chatTinyView != null && chatTinyView.getVisibility() == View.VISIBLE) {
            chatTinyView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示悬浮窗
     *
     * @param
     */
    public void showChatTinyView() {
        if (chatTinyView != null && chatTinyView.getVisibility() == View.GONE) {
            chatTinyView.setVisibility(View.VISIBLE);
        }
    }
}
