package com.konka.konkaim.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.konka.konkaim.R;
import com.konka.konkaim.chat.AVChatController;
import com.konka.konkaim.chat.AVChatExitCode;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatSoundPlayer;
import com.konka.konkaim.chat.AVChatTimeoutObserver;
import com.konka.konkaim.chat.CallStateEnum;
import com.konka.konkaim.chat.PhoneCallStateObserver;
import com.konka.konkaim.chat.SimpleAVChatStateObserver;
import com.konka.konkaim.chat_interface.AVChatControllerCallback;
import com.konka.konkaim.chat_interface.AVSwitchListener;
import com.konka.konkaim.util.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;

public class AVChatActivity extends AppCompatActivity implements AVSwitchListener {
    // constant
    private static final String TAG = "AVChatActivity";
    private static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_DISPLAY_NAME = "KEY_DISPLAY_NAME";
    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";
    public static final String INTENT_ACTION_AVCHAT = "INTENT_ACTION_AVCHAT";

    public static final int FROM_BROADCASTRECEIVER = 0; // 来自广播
    public static final int FROM_INTERNAL = 1; // 来自发起方
    public static final int FROM_UNKNOWN = -1; // 未知的入口

    // state
    private static boolean needFinish = true; // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
    private boolean mIsInComingCall = false;// is incoming call or outgoing call
    private boolean isCallEstablished = false; // 电话是否接通
    private boolean isUserFinish = false;
    private boolean hasOnPause = false; // 是否暂停音视频

    // data
    private AVChatData avChatData; // config for connect video server
    private int state; // calltype 音频或视频
    private String receiverId; // 对方的account
    private String displayName; // 对方的显示昵称

    // notification
//    private AVChatNotification notifier;

    private AVChatController avChatController;

    // 拨打电话
    public static void outgoingCall(Context context, String account, String displayName, int callType, int source) {
        needFinish = false;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, AVChatActivity.class);
        intent.putExtra(KEY_ACCOUNT, account);
        intent.putExtra(KEY_DISPLAY_NAME, displayName);
        intent.putExtra(KEY_IN_CALLING, false);
        intent.putExtra(KEY_CALL_TYPE, callType);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    // 接听来电
    public static void incomingCall(Context context, AVChatData config, String displayName, int source) {
        needFinish = false;
        Intent intent = new Intent();
        intent.setClass(context, AVChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_DISPLAY_NAME, displayName);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
        if (needFinish) {
            finish();
            return;
        }
        setContentView(R.layout.activity_avchat);

        parseIntent();

        initData();

        show();

        registerObserves(true);
    }

    /**
     * ************************ 初始化 ***************************
     */

    private void parseIntent() {
        mIsInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
        displayName = getIntent().getStringExtra(KEY_DISPLAY_NAME);
        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER: // incoming call
                avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
                state = avChatData.getChatType().getValue();
                break;
            case FROM_INTERNAL: // outgoing call
                receiverId = getIntent().getStringExtra(KEY_ACCOUNT);
                state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
                break;
            default:
                break;
        }
    }

    private void initData() {
        avChatController = new AVChatController(this, avChatData);
    }

    private void registerObserves(boolean register) {
        AVChatManager.getInstance().observeAVChatState(avchatStateObserver, register);
        AVChatManager.getInstance().observeHangUpNotification(callHangupObserver, register);
        AVChatManager.getInstance().observeCalleeAckNotification(callAckObserver, register);
        AVChatManager.getInstance().observeControlNotification(callControlObserver, register);
        //AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, register, mIsInComingCall);
        AVChatManager.getInstance().observeOnlineAckNotification(onlineAckObserver, register);
        PhoneCallStateObserver.getInstance().observeAutoHangUpForLocalPhone(autoHangUpForLocalPhoneObserver, register);
        //放到所有UI的基类里面注册，所有的UI实现onKickOut接口
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    /**
     * ************************ 音视频来电去电入口 ***************************
     */

    private String account;
    //private String displayName;
    private CallStateEnum callingState;

    private void show() {
        if (state == CallStateEnum.AUDIO.getValue()) {
            // 音频
//             audioRoot.setVisibility(View.VISIBLE);
//             videoRoot.setVisibility(View.GONE);
//             surfaceRoot.setVisibility(View.GONE);
            if (mIsInComingCall) {
                // 来电
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
                //avChatAudioUI.showIncomingCall(avChatData);
                showIncomingCall(avChatData);
            } else {
                // 去电
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
                //avChatAudioUI.doOutGoingCall(receiverId);
                doOutGoingCall(receiverId);
            }
        }
    }

    private void showIncomingCall(AVChatData avChatData) {
        // 接听方的数据是AVChatData
        this.avChatData = avChatData;
        this.account = avChatData.getAccount();
        this.callingState = CallStateEnum.INCOMING_AUDIO_CALLING;

        doReceiveCall();

/*        findViews();

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_audio_call_request);
        setMuteSpeakerHangupControl(false);
        setRefuseReceive(true);
        receiveTV.setText(R.string.avchat_pickup);*/
    }

    public void doOutGoingCall(String account) {
        // 拨打方的数据是account
        this.account = account;
        System.out.println("doOutGoingCall account=" + account);

/*        findViews();

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_wait_recieve);
        setWifiUnavailableNotifyTV(true);
        setMuteSpeakerHangupControl(true);
        setRefuseReceive(false);*/

        // 拨打音视频接口调用
        avChatController.doCalling(account, AVChatType.AUDIO, new AVChatControllerCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData mAvChatData) {
                avChatData = mAvChatData;
                avChatController.setAvChatData(avChatData);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                //closeSession();
                finish();
            }
        });
    }

    // 接听来电
    private void doReceiveCall() {
        if (callingState == CallStateEnum.INCOMING_AUDIO_CALLING) {

            avChatController.receive(AVChatType.AUDIO, new AVChatControllerCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("doReceiveCall() success");
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    System.out.println("doReceiveCall() onFailed");
                    finish();
                }
            });
        }
    }

    /**
     * ****************************** 监听器 **********************************
     */

    // 通话过程状态监听
    private SimpleAVChatStateObserver avchatStateObserver = new SimpleAVChatStateObserver() {
        @Override
        public void onAVRecordingCompletion(String account, String filePath) {
            if (account != null && filePath != null && filePath.length() > 0) {
                String msg = "音视频录制已结束, " + "账号：" + account + " 录制文件已保存至：" + filePath;
//                Toast.makeText(AVChatActivity.this, msg, Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(AVChatActivity.this, "录制已结束.", Toast.LENGTH_SHORT).show();
            }

/*            if (state == AVChatType.VIDEO.getValue()) {
                avChatVideoUI.resetRecordTip();
            } else {
                avChatAudioUI.resetRecordTip();
            }*/

        }

        @Override
        public void onAudioRecordingCompletion(String filePath) {
            if (filePath != null && filePath.length() > 0) {
                String msg = "音频录制已结束, 录制文件已保存至：" + filePath;
//                Toast.makeText(AVChatActivity.this, msg, Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(AVChatActivity.this, "录制已结束.", Toast.LENGTH_SHORT).show();
            }

/*            if (state == AVChatType.AUDIO.getValue()) {
                avChatAudioUI.resetRecordTip();
            } else {
                avChatVideoUI.resetRecordTip();
            }*/

        }

        @Override
        public void onLowStorageSpaceWarning(long availableSize) {

/*            if (state == AVChatType.VIDEO.getValue()) {
                avChatVideoUI.showRecordWarning();
            } else {
                avChatAudioUI.showRecordWarning();
            }*/

        }

        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile, int i) {
            LogUtil.LogD(TAG, "audioFile -> " + audioFile + " videoFile -> " + videoFile);
            handleWithConnectServerResult(code);
        }

        @Override
        public void onUserJoined(String account) {
            LogUtil.LogD(TAG, "onUserJoin -> " + account);

/*            if (state == AVChatType.VIDEO.getValue()) {
                avChatVideoUI.initLargeSurfaceView(account);
            }*/

        }

        @Override
        public void onUserLeave(String account, int event) {
            LogUtil.LogD(TAG, "onUserLeave -> " + account);

            avChatController.hangUp(AVChatExitCode.HANGUP);

            finish();
        }

        @Override
        public void onCallEstablished() {
            LogUtil.LogD(TAG, "onCallEstablished");
//            Toast.makeText(AVChatActivity.this, "通话连接建立", Toast.LENGTH_SHORT).show();

            //移除超时监听
            AVChatTimeoutObserver.getInstance().observeTimeoutNotification(timeoutObserver, false, mIsInComingCall);
            if (avChatController.getTimeBase() == 0)
                avChatController.setTimeBase(SystemClock.elapsedRealtime());

/*            if (state == AVChatType.AUDIO.getValue()) {
                avChatAudioUI.showAudioInitLayout();
            } else {
                // 接通以后，自己是小屏幕显示图像，对方是大屏幕显示图像
                avChatVideoUI.initSmallSurfaceView(AVChatKit.getAccount());
                avChatVideoUI.showVideoInitLayout();
            }*/
            isCallEstablished = true;
        }

        @Override
        public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {

/*            if (faceU != null) {
                faceU.effect(frame.data, frame.width, frame.height, FaceU.VIDEO_FRAME_FORMAT.I420);
            }*/

            return true;
        }

        //采集语音数据回调
        @Override
        public boolean onAudioFrameFilter(AVChatAudioFrame frame) {
            return true;
        }

        //音视频设备状态发生改变时，会回调 onDeviceEvent
        @Override
        public void onDeviceEvent(int code, String desc) {
            System.out.println("onDeviceEvent code="+code+", desc="+desc);
        }
    };

    // 通话过程中，收到对方挂断电话
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            avChatData = avChatController.getAvChatData();
            if (avChatData != null && avChatData.getChatId() == avChatHangUpInfo.getChatId()) {
                avChatController.onHangUp(AVChatExitCode.HANGUP);

/*                cancelCallingNotifier();
                // 如果是incoming call主叫方挂断，那么通知栏有通知
                if (mIsInComingCall && !isCallEstablished) {
                    activeMissCallNotifier();
                }*/

            }

        }
    };

    // 呼叫时，被叫方的响应（接听、拒绝、忙）
    Observer<AVChatCalleeAckEvent> callAckObserver = new Observer<AVChatCalleeAckEvent>() {
        @Override
        public void onEvent(AVChatCalleeAckEvent ackInfo) {
            AVChatData info = avChatController.getAvChatData();
            if (info != null && info.getChatId() == ackInfo.getChatId()) {
                AVChatSoundPlayer.instance().stop();

                if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {

                    AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);

                    avChatController.onHangUp(AVChatExitCode.PEER_BUSY);
//                    Toast.makeText(AVChatActivity.this, "对方挂断通话", Toast.LENGTH_SHORT).show();
                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                    avChatController.onHangUp(AVChatExitCode.REJECT);
//                    Toast.makeText(AVChatActivity.this, "对方拒绝通话", Toast.LENGTH_SHORT).show();
                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                    avChatController.isCallEstablish.set(true);
//                    Toast.makeText(AVChatActivity.this, "对方接受通话", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    Observer<Integer> timeoutObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {

            avChatController.hangUp(AVChatExitCode.CANCEL);

            // 来电超时，自己未接听
            if (mIsInComingCall) {
//                activeMissCallNotifier();
            }

            finish();
        }
    };

    // 监听音视频模式切换通知, 对方音视频开关通知
    Observer<AVChatControlEvent> callControlObserver = new Observer<AVChatControlEvent>() {
        @Override
        public void onEvent(AVChatControlEvent netCallControlNotification) {
            handleCallControl(netCallControlNotification);
        }
    };

    // 处理音视频切换请求和对方音视频开关通知
    private void handleCallControl(AVChatControlEvent notification) {
        if (AVChatManager.getInstance().getCurrentChatId() != notification.getChatId()) {
            return;
        }
        switch (notification.getControlCommand()) {
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO:
                //incomingAudioToVideo();
                break;
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_AGREE:
                // 对方同意切成视频啦
                //state = AVChatType.VIDEO.getValue();
                //avChatVideoUI.onAudioToVideoAgree(notification.getAccount());
                break;
            case AVChatControlCommand.SWITCH_AUDIO_TO_VIDEO_REJECT:
                //rejectAudioToVideo();
                //Toast.makeText(AVChatActivity.this, R.string.avchat_switch_video_reject, Toast.LENGTH_SHORT).show();
                break;
            case AVChatControlCommand.SWITCH_VIDEO_TO_AUDIO:
                onVideoToAudio();
                break;
            case AVChatControlCommand.NOTIFY_VIDEO_OFF:
                // 收到对方关闭画面通知
//                if (state == AVChatType.VIDEO.getValue()) {
//                    avChatVideoUI.peerVideoOff();
//                }
                break;
            case AVChatControlCommand.NOTIFY_VIDEO_ON:
                // 收到对方打开画面通知
//                if (state == AVChatType.VIDEO.getValue()) {
//                    avChatVideoUI.peerVideoOn();
//                }
                break;
            default:
//                Toast.makeText(this, "对方发来指令值：" + notification.getControlCommand(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 处理连接服务器的返回值
     *
     * @param auth_result
     */
    protected void handleWithConnectServerResult(int auth_result) {
        LogUtil.LogI(TAG, "result code->" + auth_result);
        if (auth_result == 200) {
            LogUtil.LogD(TAG, "onConnectServer success");
        } else if (auth_result == 101) { // 连接超时
            avChatController.showQuitToast(AVChatExitCode.PEER_NO_RESPONSE);
        } else if (auth_result == 401) { // 验证失败
            avChatController.showQuitToast(AVChatExitCode.CONFIG_ERROR);
        } else if (auth_result == 417) { // 无效的channelId
            avChatController.showQuitToast(AVChatExitCode.INVALIDE_CHANNELID);
        } else { // 连接服务器错误，直接退出
            avChatController.showQuitToast(AVChatExitCode.CONFIG_ERROR);
        }
    }

    /**
     * 注册/注销同时在线的其他端对主叫方的响应
     */
    Observer<AVChatOnlineAckEvent> onlineAckObserver = new Observer<AVChatOnlineAckEvent>() {
        @Override
        public void onEvent(AVChatOnlineAckEvent ackInfo) {

            if (state == AVChatType.AUDIO.getValue()) {
                avChatData = avChatController.getAvChatData();
            } else {
//                avChatData = avChatVideoUI.getAvChatData();
            }

            if (avChatData != null && avChatData.getChatId() == ackInfo.getChatId()) {
                AVChatSoundPlayer.instance().stop();

                String client = null;
                switch (ackInfo.getClientType()) {
                    case ClientType.Web:
                        client = "Web";
                        break;
                    case ClientType.Windows:
                        client = "Windows";
                        break;
                    case ClientType.Android:
                        client = "Android";
                        break;
                    case ClientType.iOS:
                        client = "iOS";
                        break;
                    case ClientType.MAC:
                        client = "Mac";
                        break;
                    default:
                        break;
                }
                if (client != null) {
                    String option = ackInfo.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
//                    Toast.makeText(AVChatActivity.this, "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }
    };

    Observer<Integer> autoHangUpForLocalPhoneObserver = new Observer<Integer>() {
        @Override
        public void onEvent(Integer integer) {
            avChatController.onHangUp(AVChatExitCode.PEER_BUSY);
        }
    };

    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                AVChatSoundPlayer.instance().stop();
                AVChatKit.getAvChatOptions().logout(AVChatActivity.this);
                finish();
            }
        }
    };

    /**
     * ******************** 音视频切换接口 ********************
     */

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasOnPause) {
            avChatController.resumeVideo();
            hasOnPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        avChatController.pauseVideo();
        hasOnPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onVideoToAudio() {

    }

    @Override
    public void onAudioToVideo() {

    }

    @Override
    public void onReceiveAudioToVideoAgree() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
