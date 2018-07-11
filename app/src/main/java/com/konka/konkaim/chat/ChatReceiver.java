package com.konka.konkaim.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by HP on 2018-5-15.
 */

public class ChatReceiver extends BroadcastReceiver {
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    public static boolean IS_BOOT_START = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.konka.im.action.CHAT_RECEIVER")){
            MyWindowManager.getInstance().createChatTinyView(context, false);
        }else if (action.equals("android.konka.im.team.action.CHAT_RECEIVER")){
            MyWindowManager.getInstance().createChatTinyView(context, true);
        }else if (intent.getAction().equals(ACTION_BOOT)) { //开机启动完成后，要做的事情
            System.out.println("BootBroadcastReceiver onReceive(), Do thing!");
            IS_BOOT_START = true;
        }
    }
}
