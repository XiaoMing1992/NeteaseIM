package com.konka.konkaim.chat;

import com.konka.konkaim.chat.activity.AVChatActivity;
import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

/**
 * Created by HP on 2018-5-22.
 */

public class AVChatProfile {

    private final String TAG = "AVChatProfile";

    private boolean isAVChatting = false; // 是否正在音视频通话

    public static AVChatProfile getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isAVChatting() {
        return isAVChatting;
    }

    public void setAVChatting(boolean chating) {
        isAVChatting = chating;
    }

    private static class InstanceHolder {
        public final static AVChatProfile instance = new AVChatProfile();
    }

    public void launchActivity(final AVChatData data, final String displayName, final int source) {
        //AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
        //AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);

        OneToOneActivity.incomingCall(AVChatKit.getContext(), data, displayName, source); //收到来电，启动activity来处理

/*        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 启动，如果 task正在启动，则稍等一下
                if (!AVChatKit.isMainTaskLaunching()) {

                    AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
                } else {
                    launchActivity(data, displayName, source);
                }
            }
        };
        Handlers.sharedHandler(AVChatKit.getContext()).postDelayed(runnable, 200);*/
    }

    public void launchActivity(final AVChatData data, final String displayName, final int source,
                               final boolean isFromTinyView, final boolean fromTinyViewYesOrNo) {
        //AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
        //AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);

        //收到来电，启动activity来处理
        OneToOneActivity.incomingCall(AVChatKit.getContext(), data, displayName, source, isFromTinyView, fromTinyViewYesOrNo);

/*        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 启动，如果 task正在启动，则稍等一下
                if (!AVChatKit.isMainTaskLaunching()) {

                    AVChatActivity.incomingCall(AVChatKit.getContext(), data, displayName, source);
                } else {
                    launchActivity(data, displayName, source);
                }
            }
        };
        Handlers.sharedHandler(AVChatKit.getContext()).postDelayed(runnable, 200);*/
    }
}
