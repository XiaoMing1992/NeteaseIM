package com.konka.konkaim.chat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.PrefenceUtil;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatData;

/**
 * Created by HP on 2018-7-2.
 */

public class ChatService extends Service{
    private static final String TAG = ChatService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        registerAVChatIncomingCallObserver(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerAVChatIncomingCallObserver(false);
    }

    /**
     * 注册音视频来电观察者
     * @param register 注册或注销
     */
    private static void registerAVChatIncomingCallObserver(boolean register) {
        AVChatManager.getInstance().observeIncomingCall(inComingCallObserver, register);
    }

    private static Observer<AVChatData> inComingCallObserver = new Observer<AVChatData>() {
        @Override
        public void onEvent(final AVChatData data) {
/*
            String my_state = PrefenceUtil.get(, PrefenceUtil.CURRENT_USER_STATE_FILENAME+ UserInfoUtil.getAccid(), PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY);
            if (my_state == null) my_state = "可通话";


            String extra = data.getExtra();
            Log.e("Extra", "Extra Message->" + extra);
            System.out.println("Extra Message->" + extra);
            if (PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE
                    || AVChatProfile.getInstance().isAVChatting()
                    || TeamAVChatProfile.sharedInstance().isTeamAVChatting()
                    || AVChatManager.getInstance().getCurrentChatId() != 0
                    || !my_state.equals("可通话")) {
                LogUtil.LogI(TAG, "reject incoming call data =" + data.toString() + " as local phone is not idle");
                System.out.println("reject incoming call data =" + data.toString() + " as local phone is not idle");
                System.out.println("reject incoming call data =" + data.toString() +"is avchating="+AVChatProfile.getInstance().isAVChatting()+ " my_state="+my_state);
                AVChatManager.getInstance().sendControlCommand(data.getChatId(), AVChatControlCommand.BUSY, null);
                return;
            }
            MyWindowManager.getInstance().createChatTinyView(context, false, data.getAccount());
*/

            //System.out.println("to launchActivity displayName is "+userInfoProvider.getUserDisplayName(data.getAccount()));
            // 有网络来电打开AVChatActivity
            //AVChatProfile.getInstance().setAVChatting(true);
            //AVChatProfile.getInstance().launchActivity(data, userInfoProvider.getUserDisplayName(data.getAccount()), OneToOneActivity.FROM_BROADCASTRECEIVER);
        }
    };
}
