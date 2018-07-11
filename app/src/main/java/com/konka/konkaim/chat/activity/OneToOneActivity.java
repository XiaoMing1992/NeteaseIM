package com.konka.konkaim.chat.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.chat.AVChatController;
import com.konka.konkaim.chat.AVChatExitCode;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatProfile;
import com.konka.konkaim.chat.AVChatSoundPlayer;
import com.konka.konkaim.chat.AVChatTimeoutObserver;
import com.konka.konkaim.chat.CallStateEnum;
import com.konka.konkaim.chat.PhoneCallStateObserver;
import com.konka.konkaim.chat.SimpleAVChatStateObserver;
import com.konka.konkaim.chat.permission.MPermission;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionDenied;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionGranted;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionNeverAskAgain;
import com.konka.konkaim.chat_interface.AVChatControllerCallback;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatNetDetectCallback;
import com.netease.nimlib.sdk.avchat.AVChatNetDetector;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatEventType;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCalleeAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatCommonEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatNotifyOption;
import com.netease.nimlib.sdk.avchat.model.AVChatOnlineAckEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class OneToOneActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OneToOneActivity";

    private LinearLayout layout_back;
    private RelativeLayout layout_search;
    private LinearLayout layout_fail;
    private LinearLayout layout_chat;

    private LinearLayout layout_receive;
    private LinearLayout layout_receive_btn;
    private LinearLayout layout_receive_not_btn;
    private ImageView layout_receive_btn_cancel;

    private ImageView btn_cancel;

    private Button btn_reconnect;
    private Button btn_buy;
    private CircleImageView head_icon;
    private TextView remark;
    private TextView connect_content; //正在等待对方接受邀请..., 对方已拒绝通话邀请, 01:30
    private TextView operate_tip;     //取消，挂断

    //来电界面控件
    private CircleImageView from_head_icon;
    private TextView from_remark;
    private ImageView btn_yes;
    private ImageView btn_no;

    private static boolean needFinish = false; // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
    private boolean mIsInComingCall = false;// is incoming call or outgoing call
    private AVChatData avChatData; // config for connect video server
    private int state; // calltype 音频或视频
    //private String receiverId; // 对方的account
    private String displayName; //来电的时候显示来电用户的备注

    public static final int FROM_BROADCASTRECEIVER = 0; // 来自广播
    public static final int FROM_INTERNAL = 1; // 来自发起方
    public static final int FROM_UNKNOWN = -1; // 未知的入口
    private AVChatController avChatController;
    private String accountStr;
    public static final String KEY_IN_CALLING = "KEY_IN_CALLING";
    private static final String KEY_ACCOUNT = "KEY_ACCOUNT";
    private static final String KEY_CALL_TYPE = "KEY_CALL_TYPE";
    public static final String KEY_SOURCE = "source";
    private static final String KEY_CALL_CONFIG = "KEY_CALL_CONFIG";


    private boolean isCallEstablished = false; // 电话是否接通
    private static final String KEY_DISPLAY_NAME = "KEY_DISPLAY_NAME";

    private final int TIME_OUT = 0x00;
    private final int REFRESH_CURRENT_CHAT_TIME = 0x01;
    private final int HAS_MICRO_PHONE = 0x02;
    private final int NOT_HAS_MICRO_PHONE = 0x03;

    private TextView tv_chat_request;

    private ImageView microphone_icon_bg1;
    private ImageView microphone_icon_bg2;

    private boolean isFromTinyView = false;
    private boolean fromTinyViewYesOrNo = false;
    public static final String KEY_IS_FROM_TINYVIEW = "isFromTinyView";
    private static final String KEY_FROM_TINYVIEW_YES_OR_NO = "fromTinyViewYesOrNo";

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case TIME_OUT:
                    if (isTimeOut) {
                        connect_content.setText("对方无人接听，请稍后再拨");
                        connect_content.setTextColor(getResources().getColor(R.color.color17));
                        //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.NO_RESPONSE);

                        timeOutUpdateToDb();
                        DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_OUT, UserInfoUtil.getAccid(), null);
                        EventBus.getDefault().post(msg_event); //更新记录列表

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * 5);
                                    avChatController.hangUp(AVChatExitCode.PEER_NO_RESPONSE);
                                    finish();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                    break;

                case REFRESH_CURRENT_CHAT_TIME:
                    System.out.println("---> handler current_time_str" + current_time_str);
                    if (!isCallEstablished || hasHangup) return;

                    if (layout_chat.getVisibility() == View.VISIBLE)
                        connect_content.setText(current_time_str); //显示当前通话时间
                    else if (layout_receive.getVisibility() == View.VISIBLE)
                        tv_chat_request.setText(current_time_str); //显示当前通话时间
                    break;

                case HAS_MICRO_PHONE:
                    shouldEndAnimation = true;
                    microphone_icon_bg1.setVisibility(View.GONE);
                    microphone_icon_bg2.setVisibility(View.GONE);

                    layout_chat.setVisibility(View.VISIBLE);
                    layout_search.setVisibility(View.GONE);
                    layout_fail.setVisibility(View.GONE);
                    show();
                    break;

                case NOT_HAS_MICRO_PHONE:
                    shouldEndAnimation = true;
                    microphone_icon_bg1.setVisibility(View.GONE);
                    microphone_icon_bg2.setVisibility(View.GONE);

                    layout_search.setVisibility(View.GONE);
                    layout_fail.setVisibility(View.VISIBLE);
                    btn_reconnect.requestFocus();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 若来电或去电未接通时，点击home。另外一方挂断通话。从最近任务列表恢复，则finish
        if (needFinish) {
            finish();
            return;
        }

        setContentView(R.layout.activity_one_to_one);
        ActivityHelper.getInstance().addActivity(this);

        initView();
        listener();


        //startDetect(); //网络检测

        //checkPermission();

        parseIntent();

        avChatController = new AVChatController(this, avChatData);
        avChatController.isCallEstablish.set(false);


        if (!mIsInComingCall) {
            searchAnimation();
            checkMicrophone();
        } else {
            layout_chat.setVisibility(View.VISIBLE);
            layout_search.setVisibility(View.GONE);
            layout_fail.setVisibility(View.GONE);
            show();
        }
        //initData();

        registerObserves(true);
    }

    private void initView() {
        layout_back = (LinearLayout) findViewById(R.id.layout_back);
        layout_search = (RelativeLayout) findViewById(R.id.layout_search);
        layout_fail = (LinearLayout) findViewById(R.id.layout_fail);
        layout_chat = (LinearLayout) findViewById(R.id.layout_chat);

        layout_receive = (LinearLayout) findViewById(R.id.layout_receive);
        layout_receive_btn = (LinearLayout) findViewById(R.id.layout_receive_btn);
        layout_receive_not_btn = (LinearLayout) findViewById(R.id.layout_receive_not_btn);
        layout_receive_btn_cancel = (ImageView) findViewById(R.id.layout_receive_btn_cancel);

        btn_yes = (ImageView) findViewById(R.id.btn_yes);
        btn_no = (ImageView) findViewById(R.id.btn_no);
        btn_cancel = (ImageView) findViewById(R.id.btn_cancel);
        microphone_icon_bg1 = (ImageView) findViewById(R.id.microphone_icon_bg1);
        microphone_icon_bg2 = (ImageView) findViewById(R.id.microphone_icon_bg2);

        btn_reconnect = (Button) findViewById(R.id.btn_reconnect);
        btn_buy = (Button) findViewById(R.id.btn_buy);
        head_icon = (CircleImageView) findViewById(R.id.head_icon);
        remark = (TextView) findViewById(R.id.remark);

        from_head_icon = (CircleImageView) findViewById(R.id.from_head_icon);
        from_remark = (TextView) findViewById(R.id.from_remark);

        tv_chat_request = (TextView) findViewById(R.id.tv_chat_request);

        connect_content = (TextView) findViewById(R.id.connect_content);

        operate_tip = (TextView) findViewById(R.id.operate_tip);
        btn_reconnect.setOnClickListener(this);
        btn_buy.setOnClickListener(this);
        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        layout_receive_btn_cancel.setOnClickListener(this);
    }

    private void initData() {
        if (!mIsInComingCall) { //去电才走这里面的逻辑

            AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);

            layout_receive.setVisibility(View.GONE); //隐藏来电界面
            layout_chat.setVisibility(View.VISIBLE); //显示去电界面

            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("chat_data");
            if (bundle != null) {
                accountStr = bundle.getString("account");
                System.out.println("OneToOneActivity account is " + accountStr);
                //avatarStr = bundle.getString("avatar");
                //remarkStr = bundle.getString("remark");
            }
            String aliasName = NIMSDK.getFriendService().getFriendByAccount(accountStr) == null ?
                    null : NIMSDK.getFriendService().getFriendByAccount(accountStr).getAlias();
            String nickname = NIMClient.getService(UserService.class).getUserInfo(accountStr) == null ?
                    null : NIMClient.getService(UserService.class).getUserInfo(accountStr).getName();
            remark.setText(!TextUtils.isEmpty(aliasName) ? aliasName : nickname); //加载备注

            if (NIMClient.getService(UserService.class).getUserInfo(accountStr) == null){
                head_icon.setImageResource(R.drawable.img_default);
            }else {
                if (!TextUtils.isEmpty(NIMClient.getService(UserService.class).getUserInfo(accountStr).getAvatar())){
                    HttpHelper.downloadPicture(OneToOneActivity.this,
                            NIMClient.getService(UserService.class).getUserInfo(accountStr).getAvatar(),
                            R.drawable.img_default, R.drawable.img_default, head_icon); //加载头像
                }else {
                    head_icon.setImageResource(R.drawable.img_default);
                }
            }


            //outgoingCall(accountStr, remark.getText().toString().trim()); //发起通话
            doOutGoingCall(accountStr);//发起通话
        } else { //来电
            if (!isFromTinyView){
                AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
            }

            layout_receive.setVisibility(View.VISIBLE);//显示来电界面
            layout_chat.setVisibility(View.GONE);//隐藏去电界面

            String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount) == null ?
                    null : NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias();
            String nickname = NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null ?
                    null : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName();
            from_remark.setText(!TextUtils.isEmpty(aliasName) ? aliasName : nickname); //加载备注


            if (NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null){
                from_head_icon.setImageResource(R.drawable.img_default);
            }else {
                if (!TextUtils.isEmpty(NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar())){
                    HttpHelper.downloadPicture(OneToOneActivity.this,
                            NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar(),
                            R.drawable.img_default, R.drawable.img_default, from_head_icon); //加载头像
                }else {
                    from_head_icon.setImageResource(R.drawable.img_default);
                }
            }
        }

        setFocusView(); //控制显示各界面的时候的先获取焦点的控件
    }

    private boolean shouldEndAnimation = false;

    private void searchAnimation() {
        //while (!shouldEndAnimation) {
        searchAnimation1();
        //}
    }

    private void searchAnimation1() {
        microphone_icon_bg1.setVisibility(View.VISIBLE);//先设置显示

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(microphone_icon_bg1, "scaleX", 1f, 3f);
        scaleX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //System.out.println("---"+animation.getAnimatedValue());
                //System.out.println(animation.getCurrentPlayTime());
                //System.out.println("+++++"+animation.getAnimatedFraction());
                if (animation.getAnimatedFraction() >= 0.95) {
                    if (shouldEndAnimation) return;
                    searchAnimation2();
                }
            }
        });
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(microphone_icon_bg1, "scaleY", 1f, 3f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(microphone_icon_bg1, "alpha", 1.0f, 0.0f);

        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        //animatorSet.play(scaleX).with(translationX);//两个动画同时开始
        //animatorSet.playTogether(scaleX, scaleY, translationX, translationY, alpha);
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.start();

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //System.out.println("go to play next");
                microphone_icon_bg1.setVisibility(View.GONE);
/*                if (shouldEndAnimation) return;
                searchAnimation2();*/
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void searchAnimation2() {
        microphone_icon_bg2.setVisibility(View.VISIBLE);//先设置显示

        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(microphone_icon_bg2, "scaleX", 1f, 3f);
        scaleX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //System.out.println("---"+animation.getAnimatedValue());
                //System.out.println(animation.getCurrentPlayTime());
                //System.out.println("+++++"+animation.getAnimatedFraction());
                if (animation.getAnimatedFraction() >= 0.95) {
                    if (shouldEndAnimation) return;
                    searchAnimation1();
                }
            }
        });
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(microphone_icon_bg2, "scaleY", 1f, 3f);

//        ObjectAnimator translationX = ObjectAnimator.ofFloat(microphone_icon_bg1, "translationX", 0f, 600f);
//        ObjectAnimator translationY = ObjectAnimator.ofFloat(microphone_icon_bg1, "translationY", 0f, 600f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(microphone_icon_bg2, "alpha", 1.0f, 0.0f);

        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        //animatorSet.play(scaleX).with(translationX);//两个动画同时开始
        //animatorSet.playTogether(scaleX, scaleY, translationX, translationY, alpha);
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //System.out.println("end");
                microphone_icon_bg2.setVisibility(View.GONE);
                if (shouldEndAnimation) return;
                searchAnimation1();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * ************************ 初始化 ***************************
     */

    private void parseIntent() {
        mIsInComingCall = getIntent().getBooleanExtra(KEY_IN_CALLING, false);
        //mIsInComingCall = false;
        System.out.println("--- mIsInComingCall" + mIsInComingCall);
        if (mIsInComingCall) {
            displayName = getIntent().getStringExtra(KEY_DISPLAY_NAME);
            //
            isFromTinyView = getIntent().getBooleanExtra(KEY_IS_FROM_TINYVIEW, false);
            fromTinyViewYesOrNo = getIntent().getBooleanExtra(KEY_FROM_TINYVIEW_YES_OR_NO, false);
        }

        switch (getIntent().getIntExtra(KEY_SOURCE, FROM_UNKNOWN)) {
            case FROM_BROADCASTRECEIVER: // incoming call
                avChatData = (AVChatData) getIntent().getSerializableExtra(KEY_CALL_CONFIG);
                state = avChatData.getChatType().getValue();
                System.out.println("---KEY_SOURCE is FROM_BROADCASTRECEIVER");
                break;
            case FROM_INTERNAL: // outgoing call
                //receiverId = getIntent().getStringExtra(KEY_ACCOUNT);
                //state = getIntent().getIntExtra(KEY_CALL_TYPE, -1);
                System.out.println("---KEY_SOURCE is FROM_INTERNAL");
                break;
            default:
                System.out.println("---KEY_SOURCE is FROM_UNKNOWN");
                break;
        }
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserves(false); //注销观察者
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        myHandler.removeCallbacksAndMessages(this);
        AVChatSoundPlayer.instance().stop();
        shouldEndAnimation = true;
    }

    /**
     * ************************ 音视频来电去电入口 ***************************
     */

    //private String account;
    //private String displayName;
    private CallStateEnum callingState;

    private void show() {
        //有来电的时候就更新数据库
        updateTimeToDb(-1);

        //test();

        //if (state == CallStateEnum.AUDIO.getValue()) {
        // 音频
//             audioRoot.setVisibility(View.VISIBLE);
//             videoRoot.setVisibility(View.GONE);
//             surfaceRoot.setVisibility(View.GONE);
        if (mIsInComingCall) {
            // 来电
            //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);
            //avChatAudioUI.showIncomingCall(avChatData);
            showIncomingCall(avChatData);

        } else {
            // 去电
            //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.CONNECTING);
            //avChatAudioUI.doOutGoingCall(receiverId);
            //doOutGoingCall(accountStr);

            initData();

        }
        //}
    }

    private String friendAccount; //来电方的账号

    private void showIncomingCall(AVChatData avChatData) {
        // 接听方的数据是AVChatData
        this.avChatData = avChatData;
        this.friendAccount = avChatData.getAccount(); //获取来电方的账号
        this.callingState = CallStateEnum.INCOMING_AUDIO_CALLING;

        //doReceiveCall();

        initData();

/*        findViews();

        setSwitchVideo(false);
        showProfile();//对方的详细信息
        showNotify(R.string.avchat_audio_call_request);
        setMuteSpeakerHangupControl(false);
        setRefuseReceive(true);
        receiveTV.setText(R.string.avchat_pickup);*/
    }

    public void doOutGoingCall(final String account) {
        needFinish = false;
        // 拨打方的数据是account
        //this.account = account;
        System.out.println("doOutGoingCall account=" + account);

        // 拨打音视频接口调用
        avChatController.doCalling(account, AVChatType.AUDIO, new AVChatControllerCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData mAvChatData) {
                avChatData = mAvChatData;
                avChatController.setAvChatData(avChatData);
                System.out.println("outcall onSuccess");
                //开始计算计时，判断是否超时（一分钟）
                TimeOut();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                System.out.println("outcall onFailed code=" + code);
                //closeSession();
                //finish();
                connect_content.setText("呼叫失败");
                if (code == 11001 && account != null) {
                    System.out.println("outcall onFailed, 通话不可达，对方离线状态");
                    HomeActivity.onlineStateMap.put(account, "0");
                }
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
                    AVChatSoundPlayer.instance().stop();
                    avChatController.isCallEstablish.set(true);
                    last_time = current_time = System.currentTimeMillis();

                    System.out.println("接受对方通话 last_time = " + last_time + " current_time" + current_time);
                    notifyCurrentChatTime();

                    if (layout_receive.getVisibility() == View.VISIBLE) {
                        layout_receive_btn.setVisibility(View.GONE); //隐藏接受和拒绝按钮
                        layout_receive_not_btn.setVisibility(View.VISIBLE); //显示挂断等信息
                        layout_receive_btn_cancel.requestFocus();
                        tv_chat_request.setText("00:00");
                    }
                }

                @Override
                public void onFailed(int code, String errorMsg) {
                    System.out.println("doReceiveCall() onFailed");
                    if (layout_receive.getVisibility() == View.VISIBLE) {
                        layout_receive_btn.setVisibility(View.GONE); //隐藏接受和拒绝按钮
                        layout_receive_not_btn.setVisibility(View.VISIBLE); //显示挂断等信息
                        layout_receive_btn_cancel.requestFocus();
                        tv_chat_request.setText("接听失败");
                    }
                    //finish();
                }
            });
        }
    }

    /**
     * 拒绝电话
     */
    public void doReject() {
        avChatController.hangUp(AVChatExitCode.REJECT);
    }

    /**
     * 取消
     */
    public void doCancel() {
        avChatController.hangUp(AVChatExitCode.CANCEL);
    }

    /**
     * 挂断
     */
    public void doHangUp() {
        avChatController.hangUp(AVChatExitCode.HANGUP);
    }

    /**
     * ****************************** 监听器 **********************************
     */

    // 通话过程状态监听
    private SimpleAVChatStateObserver avchatStateObserver = new SimpleAVChatStateObserver() {
        @Override
        public void onAVRecordingCompletion(String account, String filePath) {


/*            if (state == AVChatType.VIDEO.getValue()) {
                avChatVideoUI.resetRecordTip();
            } else {
                avChatAudioUI.resetRecordTip();
            }*/

        }

        @Override
        public void onAudioRecordingCompletion(String filePath) {

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

        /**
         *
         * @param code 返回加入频道是否成功
         * @param audioFile 在开启服务器录制的情况下返回语音录制文件的保存路径。
         * @param videoFile 在开启服务器录制的情况下返回语音视频文件的保存路径。
         * @param i 从开始加入房间关调用处开始计算，到成功加入房间的耗时，单位ms
         */
        @Override
        public void onJoinedChannel(int code, String audioFile, String videoFile, int i) {
            LogUtil.LogD(TAG, "code->" + code + "  audioFile -> " + audioFile + " videoFile -> " + videoFile + " i=" + i);
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

            //avChatController.hangUp(AVChatExitCode.HANGUP);

            //finish();
        }

        @Override
        public void onCallEstablished() {
            LogUtil.LogD(TAG, "onCallEstablished");
            //Toast.makeText(OneToOneActivity.this, "通话连接建立", Toast.LENGTH_SHORT).show();

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
            avChatController.isCallEstablish.set(true);
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
            System.out.println("onDeviceEvent code=" + code + ", desc=" + desc);
            if (desc.equals("closed")) {

            }
        }

        @Override
        public void onNetworkQuality(String user, int quality, AVChatNetworkStats stats) {
            //super.onNetworkQuality(user, quality, stats);

            /*if (quality == 0){
                showToast(OneToOneActivity.this, "网络很好");
            }else if (quality == 1){
                showToast(OneToOneActivity.this, "网络稍好");
            }else*/
            if (quality == 2) {
                showToast(OneToOneActivity.this, "网络稍差");
            } else if (quality == 3) {
                showToast(OneToOneActivity.this, "网络很差");
            }
            super.onNetworkQuality(user, quality, stats);
        }
    };

    // 通话过程中，收到对方挂断电话
    Observer<AVChatCommonEvent> callHangupObserver = new Observer<AVChatCommonEvent>() {
        @Override
        public void onEvent(AVChatCommonEvent avChatHangUpInfo) {
            avChatData = avChatController.getAvChatData();
            System.out.println("avChatHangUpInfo.getEvent()=" + avChatHangUpInfo.getEvent());
            AVChatSoundPlayer.instance().stop();

            if (avChatData != null && avChatData.getChatId() == avChatHangUpInfo.getChatId()) {
                hasHangup = true;
                avChatController.isCallEstablish.set(false);
                isCallEstablished = false;

                myHandler.removeCallbacks(time_out_runnable);
                myHandler.removeCallbacks(compute_runnable);


                connect_content.setText("对方已经挂断电话");
                //connect_content.setTextColor(getResources().getColor(R.color.color17));

                if (mIsInComingCall && isCallEstablished) { //收到来电的一方 收到 发起方 的挂断
                    avChatController.isCallEstablish.set(false);
                    isCallEstablished = false;

                    //showToast(OneToOneActivity.this, "对方已经挂断电话");

                    System.out.println("avChatData.getAccount()=" + avChatData.getAccount() + ", isCallEstablished=" + isCallEstablished);

                } else if (mIsInComingCall && !isCallEstablished) {
                    //activeMissCallNotifier();
                    tv_chat_request.setText("对方已经取消呼叫");
                    //tv_chat_request.setTextColor(getResources().getColor(R.color.color17));

                    System.out.println("avChatData.getAccount()=" + avChatData.getAccount());
                    if (avChatData.getAccount() != null) {
                        updateTimeToDb(-2);

                        DataSynEvent reject_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP, UserInfoUtil.getAccid(), null);
                        EventBus.getDefault().post(reject_event);
                    }
                }

                AVChatProfile.getInstance().setAVChatting(false);
                avChatController.onHangUp(AVChatExitCode.HANGUP);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000 * 3);
                            finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

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
            System.out.println("ackInfo.getEvent()=" + ackInfo.getEvent());

            AVChatSoundPlayer.instance().stop();

            if (info != null && info.getChatId() == ackInfo.getChatId()) {

                if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_BUSY) {
                    //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_BUSY);
                    myHandler.removeCallbacks(time_out_runnable);
                    myHandler.removeCallbacks(compute_runnable);

                    connect_content.setText("对方暂时无法接听，请稍后再拨");
                    connect_content.setTextColor(getResources().getColor(R.color.color17));

                    //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);

                    //finish();
                    avChatController.isCallEstablish.set(false);
                    isCallEstablished = false;
                    avChatController.onHangUp(AVChatExitCode.PEER_BUSY);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000 * 3);
                                finish();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_REJECT) {
                    myHandler.removeCallbacks(time_out_runnable);
                    myHandler.removeCallbacks(compute_runnable);

                    connect_content.setText("对方拒绝通话邀请");
                    //connect_content.setTextColor(getResources().getColor(R.color.color17));

                    //AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.PEER_REJECT);

                    //Toast.makeText(OneToOneActivity.this, "对方拒绝通话", Toast.LENGTH_SHORT).show();
                    //finish();
                    avChatController.isCallEstablish.set(false);
                    isCallEstablished = false;

                    avChatController.onHangUp(AVChatExitCode.REJECT);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000 * 2);
                                finish();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                } else if (ackInfo.getEvent() == AVChatEventType.CALLEE_ACK_AGREE) {
                    myHandler.removeCallbacks(time_out_runnable);

                    AVChatSoundPlayer.instance().stop();

                    avChatController.isCallEstablish.set(true);
                    //Toast.makeText(OneToOneActivity.this, "对方接受通话", Toast.LENGTH_SHORT).show();

                    //connect_content.setText(getCurrentTime());
                    last_time = current_time = System.currentTimeMillis();

                    System.out.println("对方接受通话 last_time = " + last_time + " current_time" + current_time);
                    notifyCurrentChatTime();

                    operate_tip.setText("挂断");
                    connect_content.setText("00:00");

/*                    if (layout_receive.getVisibility() == View.VISIBLE) {
                        layout_receive_btn.setVisibility(View.GONE); //隐藏接受和拒绝按钮
                        layout_receive_not_btn.setVisibility(View.VISIBLE); //显示挂断等信息
                        layout_receive_btn_cancel.requestFocus();
                    } else if (layout_chat.getVisibility() == View.VISIBLE) {
                        operate_tip.setText("挂断");
                    }*/

                } else if (ackInfo.getEvent() == AVChatEventType.PEER_HANG_UP) {
                    myHandler.removeCallbacks(time_out_runnable);
                    myHandler.removeCallbacks(compute_runnable);

                    AVChatSoundPlayer.instance().stop();

                    connect_content.setText("对方已经挂断电话");
                    avChatController.isCallEstablish.set(false);
                    isCallEstablished = false;
                    avChatController.onHangUp(AVChatExitCode.HANGUP);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000 * 5);
                                finish();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }
    };

    private long current_time = 0;
    private long last_time = 0;
    private boolean isTimeOut = false;

    private Runnable time_out_runnable = new Runnable() {
        @Override
        public void run() {
            while (!avChatController.isCallEstablish.get() && !isTimeOut) {
                System.out.println("start time is " + (new Date()));
                try {
                    Thread.sleep(1000 * 60);
                    System.out.println("end time is " + (new Date()));
                    if (!avChatController.isCallEstablish.get())
                        isTimeOut = true;
                    else isTimeOut = false;
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            myHandler.sendEmptyMessage(TIME_OUT);
        }
    };

    private void TimeOut() {
        new Thread(time_out_runnable).start();
    }

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
                // onVideoToAudio();
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
                Toast.makeText(this, "对方发来指令值：" + notification.getControlCommand(), Toast.LENGTH_SHORT).show();
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
                System.out.println("client type is " + client);

                if (client != null) {
                    String option = ackInfo.getEvent() == AVChatEventType.CALLEE_ONLINE_CLIENT_ACK_AGREE ? "接听！" : "拒绝！";
                    Toast.makeText(OneToOneActivity.this, "通话已在" + client + "端被" + option, Toast.LENGTH_SHORT).show();
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
                AVChatKit.getAvChatOptions().logout(OneToOneActivity.this);
                finish();
            }
        }
    };

    // 接听来电
    public static void incomingCall(Context context, AVChatData config, String displayName, int source) {
        needFinish = false;

        Intent intent = new Intent();
        intent.setClass(context, OneToOneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_DISPLAY_NAME, displayName);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        context.startActivity(intent);
    }

    // 接听来电
    public static void incomingCall(Context context, AVChatData config, String displayName, int source,
                                    boolean isFromTinyView, boolean fromTinyViewYesOrNo) {
        needFinish = false;

        Intent intent = new Intent();
        intent.setClass(context, OneToOneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_CALL_CONFIG, config);
        intent.putExtra(KEY_DISPLAY_NAME, displayName);
        intent.putExtra(KEY_IN_CALLING, true);
        intent.putExtra(KEY_SOURCE, source);
        intent.putExtra(KEY_IS_FROM_TINYVIEW, isFromTinyView);
        intent.putExtra(KEY_FROM_TINYVIEW_YES_OR_NO, fromTinyViewYesOrNo);

        context.startActivity(intent);
    }

    private void setFocusView() {
        if (layout_fail.getVisibility() == View.VISIBLE) {
            btn_reconnect.requestFocus();
        } else if (layout_chat.getVisibility() == View.VISIBLE) {
            btn_cancel.requestFocus();
        } else if (layout_receive.getVisibility() == View.VISIBLE) {
            if (!fromTinyViewYesOrNo) {
                btn_yes.requestFocus();
            }else {
                btnToReceiveCall();
            }
        }
    }

    private void listener() {
        btn_reconnect.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btn_reconnect.setAlpha(1.0f);
                } else {
                    btn_reconnect.setAlpha(0.8f);
                }
            }
        });

        btn_buy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btn_buy.setAlpha(1.0f);
                } else {
                    btn_buy.setAlpha(0.8f);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reconnect:
                btnToReconnect();
                break;

            case R.id.btn_buy:
                btnToBuy();
                break;

            case R.id.btn_yes:
                btnToReceiveCall();
                break;

            case R.id.btn_no: //拒绝来电
                btnToRefuseCall();
                break;

            case R.id.btn_cancel: //取消去电
                btnToCancelCall();
                break;

            case R.id.layout_receive_btn_cancel:
                btnToHangUpCall();
                break;
        }
    }

    private void btnToReconnect() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click btn_reconnect");
        finish();
    }

    private void btnToBuy() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click btn_buy");
        showToast(OneToOneActivity.this, "暂时还没有开通商场");
    }

    private void btnToCancelCall() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click btn_cancel");
        //下面把数据写进数据库
        //cancelAddToDb();
        cancelUpdateToDb();

        DataSynEvent cancel_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(cancel_event);

        doCancel();
        finish();
    }

    private void btnToReceiveCall() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click btn_yes");
        //下面把数据写进数据库
        receiveUpdateToDb();

        DataSynEvent receive_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_RECEIVE, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(receive_event);

        doReceiveCall(); //接听来电
    }

    private void btnToRefuseCall() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click btn_no");
        //下面把数据写进数据库
        //rejectAddToDb();
        rejectUpdateToDb();

        DataSynEvent reject_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(reject_event);

        doReject();
        finish();
    }

    private void btnToHangUpCall() {
        shouldEndAnimation = true;
        myHandler.removeCallbacks(time_out_runnable);
        myHandler.removeCallbacks(compute_runnable);

        AVChatSoundPlayer.instance().stop();

        System.out.println("click layout_receive_btn_cancel");
        //下面把数据写进数据库
        //hangupAddToDb();
        hangupUpdateToDb();

        DataSynEvent hangup_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(hangup_event);

        doHangUp();
        finish();
    }

    /**
     * *********************************  数据库 ************************************
     */
    private void receiveAddToDb() {
        DbBean no_Bean = new DbBean();
        //no_Bean.setIs_team(0);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setFriend_account(avChatData.getAccount());

        no_Bean.setRecord_time(TimeUtil.getNowTime());

        System.out.println("avChatData.getAccount()=" + avChatData.getAccount());//获取来电方的账号
        no_Bean.setChat_from(avChatData.getAccount());
        no_Bean.setChat_to(UserInfoUtil.getAccid());

        no_Bean.setIs_connect(1);

        no_Bean.setIs_out(0);
        no_Bean.setIs_friend(1);
        DBUtil.add(OneToOneActivity.this, no_Bean);
    }

    private void rejectAddToDb() {
        DbBean no_Bean = new DbBean();
        //no_Bean.setIs_team(0);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setFriend_account(avChatData.getAccount());

        no_Bean.setRecord_time(TimeUtil.getNowTime());

        System.out.println("avChatData.getAccount()=" + avChatData.getAccount());//获取来电方的账号
        no_Bean.setChat_from(avChatData.getAccount());
        no_Bean.setChat_to(UserInfoUtil.getAccid());

        no_Bean.setIs_connect(-1);

        no_Bean.setIs_out(0);
        no_Bean.setIs_friend(1);
        DBUtil.add(OneToOneActivity.this, no_Bean);
    }

    private void cancelAddToDb() {
        DbBean cancel_bean = new DbBean();
        //cancel_bean.setIs_team(0);
        cancel_bean.setMy_account(UserInfoUtil.getAccid());
        cancel_bean.setFriend_account(accountStr);

        cancel_bean.setRecord_time(TimeUtil.getNowTime());

        System.out.println("call accountStr=" + accountStr);//获取接受方的账号
        cancel_bean.setChat_from(UserInfoUtil.getAccid());
        cancel_bean.setChat_to(accountStr);

        cancel_bean.setIs_connect(0);

        cancel_bean.setIs_out(1);

        cancel_bean.setIs_friend(1);
        DBUtil.add(OneToOneActivity.this, cancel_bean);
    }

    private void hangupAddToDb() {
        DbBean hangup_bean = new DbBean();
        //hangup_bean.setIs_team(0);
        hangup_bean.setMy_account(UserInfoUtil.getAccid());

        hangup_bean.setRecord_time(TimeUtil.getNowTime());
        hangup_bean.setIs_friend(1);

        if (mIsInComingCall) {
            hangup_bean.setFriend_account(avChatData.getAccount());
            System.out.println("avChatData.getAccount()=" + avChatData.getAccount());//获取来电方的账号
            hangup_bean.setChat_from(avChatData.getAccount());
            hangup_bean.setChat_to(UserInfoUtil.getAccid());
            hangup_bean.setIs_out(0);
        } else {
            hangup_bean.setFriend_account(accountStr);
            System.out.println("call accountStr=" + accountStr);//获取接受方的账号
            hangup_bean.setChat_from(UserInfoUtil.getAccid());
            hangup_bean.setChat_to(accountStr);
            hangup_bean.setIs_out(1);
        }
        hangup_bean.setIs_connect(1);

        DBUtil.add(OneToOneActivity.this, hangup_bean);
    }

    private void receiveUpdateToDb() {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), avChatData.getAccount());
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
            //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            //friend_Bean.setRecord_time(TimeUtil.getNowDatetime());  //注意，要用当前的时间去更新
            friend_Bean.setRecord_time(dbBeanList.get(0).getRecord_time());

            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            friend_Bean.setIs_connect(1);

            friend_Bean.setIs_out(0);

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + avChatData.getAccount());//获取teamId
            DBUtil.update(OneToOneActivity.this, friend_Bean);
        } else {
            receiveAddToDb();
        }
    }

    private void rejectUpdateToDb() {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), avChatData.getAccount());
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
            //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            //friend_Bean.setRecord_time(TimeUtil.getNowDatetime());  //注意，要用当前的时间去更新
            friend_Bean.setRecord_time(dbBeanList.get(0).getRecord_time());

            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            friend_Bean.setIs_connect(-1);

            friend_Bean.setIs_out(0);

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + avChatData.getAccount());//获取teamId
            DBUtil.update(OneToOneActivity.this, friend_Bean);
        } else {
            rejectAddToDb();
        }
    }

    private void cancelUpdateToDb() {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), accountStr);
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
            //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            //friend_Bean.setRecord_time(TimeUtil.getNowDatetime());  //注意，要用当前的时间去更新
            friend_Bean.setRecord_time(dbBeanList.get(0).getRecord_time());

            //friend_Bean.setIs_team(dbBeanList.get(0).getIs_team());
            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            //friend_Bean.setIs_connect(dbBeanList.get(0).getIs_connect());
            friend_Bean.setIs_connect(0);

            friend_Bean.setIs_out(dbBeanList.get(0).getIs_out());

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + accountStr);//获取teamId
            DBUtil.update(OneToOneActivity.this, friend_Bean);
        } else {
            cancelAddToDb();
        }
    }

    private void timeOutAddToDb() {
        DbBean timeOut_bean = new DbBean();
        //cancel_bean.setIs_team(0);
        timeOut_bean.setMy_account(UserInfoUtil.getAccid());
        timeOut_bean.setFriend_account(friendAccount);
        timeOut_bean.setRecord_time(TimeUtil.getNowTime());
        System.out.println("call accountStr=" + accountStr);//获取接受方的账号
        timeOut_bean.setChat_from(UserInfoUtil.getAccid());
        timeOut_bean.setChat_to(accountStr);

        timeOut_bean.setIs_connect(-1);
        timeOut_bean.setIs_out(1);

        timeOut_bean.setIs_friend(1);
        DBUtil.add(OneToOneActivity.this, timeOut_bean);
    }

    private void timeOutUpdateToDb() {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), accountStr);
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
            //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            friend_Bean.setRecord_time(TimeUtil.getNowTime());  //注意，要用当前的时间去更新

            //friend_Bean.setIs_team(dbBeanList.get(0).getIs_team());
            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            friend_Bean.setIs_connect(-1); //未接通

            friend_Bean.setIs_out(dbBeanList.get(0).getIs_out());

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + accountStr);//获取teamId
            DBUtil.update(OneToOneActivity.this, friend_Bean);
        } else {
            timeOutAddToDb();
        }
    }

    private void hangupUpdateToDb() {
        String friendAccount = "";
        if (mIsInComingCall) {
            friendAccount = avChatData.getAccount();
        } else {
            friendAccount = accountStr;
        }

        List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), friendAccount);
        if (dbBeanList != null && dbBeanList.size() > 0) {
            DbBean friend_Bean = new DbBean();
            friend_Bean.setId(dbBeanList.get(0).getId());
            //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

            friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
            friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
            friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
            friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

            friend_Bean.setRecord_time(TimeUtil.getNowTime());  //注意，要用当前的时间去更新

            //friend_Bean.setIs_team(dbBeanList.get(0).getIs_team());
            friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

            //friend_Bean.setIs_connect(dbBeanList.get(0).getIs_connect());
            friend_Bean.setIs_connect(1);

            friend_Bean.setIs_out(dbBeanList.get(0).getIs_out());

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + friendAccount);//获取teamId
            DBUtil.update(OneToOneActivity.this, friend_Bean);
        } else {
            hangupAddToDb();
        }
    }


    //有来电的时候就更新数据库
    private synchronized void updateTimeToDb(final int is_connect) {
/*        new Thread(new Runnable() {
            @Override
            public void run() {*/
        if (mIsInComingCall) {
            String friendAccount = avChatData.getAccount();

            System.out.println("updateTimeToDb, mIsInComingCall=" + mIsInComingCall + " friendAccount=" + friendAccount);//获取teamId

            List<DbBean> dbBeanList = DBUtil.queryForFriend(OneToOneActivity.this, UserInfoUtil.getAccid(), friendAccount);
            if (dbBeanList != null && dbBeanList.size() > 0) {
                DbBean friend_Bean = new DbBean();
                friend_Bean.setId(dbBeanList.get(0).getId());
                //friend_Bean.setTeamId(dbBeanList.get(0).getTeamId());

                friend_Bean.setMy_account(dbBeanList.get(0).getMy_account());
                friend_Bean.setFriend_account(dbBeanList.get(0).getFriend_account());
                friend_Bean.setChat_from(dbBeanList.get(0).getChat_from());
                friend_Bean.setChat_to(dbBeanList.get(0).getChat_to());

                friend_Bean.setRecord_time(TimeUtil.getNowTime());  //注意，要用当前的时间去更新

                //friend_Bean.setIs_team(dbBeanList.get(0).getIs_team());
                friend_Bean.setIs_friend(dbBeanList.get(0).getIs_friend());

                friend_Bean.setIs_connect(is_connect); //接通情况

                friend_Bean.setIs_out(0);

                System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + friendAccount);//获取teamId
                DBUtil.update(OneToOneActivity.this, friend_Bean);
            } else {
                updateTimeAddToDb(is_connect);
            }

        }
/*             }
        }).start();*/
    }

    private synchronized void updateTimeAddToDb(final int is_connect) {
        DbBean hangup_bean = new DbBean();

        hangup_bean.setMy_account(UserInfoUtil.getAccid());
        hangup_bean.setFriend_account(avChatData.getAccount());

        hangup_bean.setRecord_time(TimeUtil.getNowTime());

        hangup_bean.setIs_friend(1);

        System.out.println("avChatData.getAccount()=" + avChatData.getAccount());//获取来电方的账号
        hangup_bean.setChat_from(avChatData.getAccount());
        hangup_bean.setChat_to(UserInfoUtil.getAccid());
        hangup_bean.setIs_out(0);

        hangup_bean.setIs_connect(is_connect);

        DBUtil.add(OneToOneActivity.this, hangup_bean);
    }

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
        if (!mIsInComingCall) {
            DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_OUT, UserInfoUtil.getAccid(), null);
            EventBus.getDefault().post(msg_event); //更新记录列表
        } else {
            DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_RECEIVE, UserInfoUtil.getAccid(), null);
            EventBus.getDefault().post(msg_event); //更新记录列表
        }

//        if (hasOnPause) {
//            avChatController.resumeVideo();
//            hasOnPause = false;
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        avChatController.pauseVideo();
//        hasOnPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        registerObserves(false); //注销观察者
        myHandler.removeCallbacksAndMessages(this);
        AVChatSoundPlayer.instance().stop();
    }

    public class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                System.out.println("CONNECTIVITY_ACTION");

                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if (activeNetwork != null) { // connected to the internet
                    if (activeNetwork.isConnected() && activeNetwork.isAvailable()) {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            System.out.println("当前WiFi连接可用 ");
                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                            System.out.println("当前移动网络连接可用 ");
                        }

                    } else {
                        System.out.println("当前没有网络连接，请确保你已经打开网络 ");
                        Toast.makeText(context, "当前网络状态不佳", Toast.LENGTH_SHORT).show();
                    }
                } else {   // not connected to the internet
                    System.out.println("当前没有网络连接，请确保你已经打开网络 ");
                    Toast.makeText(context, "当前网络状态不佳", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean hasHangup = false;

    private Runnable compute_runnable = new Runnable() {
        @Override
        public void run() {
            System.out.println("notifyCurrentChatTime isCallEstablished is " + avChatController.isCallEstablish.get());
            while (!hasHangup && avChatController.isCallEstablish.get()) {
                try {
                    Thread.sleep(1000);
                    current_time_str = computeTime(); //计算时间
                    myHandler.sendEmptyMessage(REFRESH_CURRENT_CHAT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void notifyCurrentChatTime() {
        new Thread(compute_runnable).start();
    }

    private String current_time_str = "";

    private String computeTime() {
        //SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        current_time = System.currentTimeMillis();
        String temp = "";

        Date lastDate = new Date(last_time);
        Date currentDate = new Date(current_time);
        long t = currentDate.getTime() - lastDate.getTime();
        long hour = t / (60 * 60 * 1000);
        long minute = (t - hour * 60 * 60 * 1000) / (60 * 1000);
        long second = (t - hour * 60 * 60 * 1000 - minute * 60 * 1000) / 1000;

        System.out.println("" + hour + ":" + minute + ":" + second);

        String hour_str = hour < 10 ? "0" + hour : "" + hour;
        String minute_str = minute < 10 ? "0" + minute : "" + minute;
        String second_str = second < 10 ? "0" + second : "" + second;
        if (hour <= 0) temp = minute_str + ":" + second_str;
        else
            temp = hour_str + ":" + minute_str + ":" + second_str;
        System.out.println("hour_str=" + hour_str + ", minute_str=" + minute_str + ", second_str=" + second_str);
        return temp;
    }

    //检查麦克风是否已经打开
    private void checkMicrophone() {
        layout_search.setVisibility(View.VISIBLE);

        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //设置声音模式
        audioManager.setMode(AudioManager.STREAM_MUSIC);
        // 打开扬声器
        audioManager.setSpeakerphoneOn(true);
        //实例化一个SoundPool对象
        //SoundPool soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        //加载声音
        //int id = soundPool.load(this, R.raw.msg, 5);
        //播放声音
        //soundPool.play(id, 1, 1, 0, 0, 1);

        //打开麦克风
        audioManager.setMicrophoneMute(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    checkPermission();
//                    System.out.println("-->before isMicrophoneMute is " + audioManager.isMicrophoneMute());
//                    if (audioManager.isMicrophoneMute()) {
//                        myHandler.sendEmptyMessage(HAS_MICRO_PHONE);
//                    } else {
//                        //打开麦克风
//                        audioManager.setMicrophoneMute(true);
//                        myHandler.sendEmptyMessage(NOT_HAS_MICRO_PHONE);
//                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    myHandler.sendEmptyMessage(NOT_HAS_MICRO_PHONE);
                }
            }
        }).start();
    }

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    /**
     * ************************************ 权限检查 ***************************************
     */
    private static final int BASIC_PERMISSION_REQUEST_CODE = 0x100;

    private void checkPermission() {
        List<String> lackPermissions = AVChatManager.getInstance().checkPermission(OneToOneActivity.this);
        if (lackPermissions.isEmpty()) {
            onBasicPermissionSuccess();
        } else {
            String[] permissions = new String[lackPermissions.size()];
            for (int i = 0; i < lackPermissions.size(); i++) {
                permissions[i] = lackPermissions.get(i);
            }
            MPermission.with(OneToOneActivity.this)
                    .setRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                    //.permissions(permissions)
                    .permissions(Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CAMERA)
                    .request();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
        //onPermissionChecked();
        //Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        System.out.println("音视频通话授权成功");
        myHandler.sendEmptyMessage(HAS_MICRO_PHONE);
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        //Toast.makeText(this, "音视频通话所需权限未全部授权，部分功能可能无法正常运行！", Toast.LENGTH_SHORT).show();
        System.out.println("音视频通话所需权限未全部授权，部分功能可能无法正常运行！");
        //onPermissionChecked();
        myHandler.sendEmptyMessage(NOT_HAS_MICRO_PHONE);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && layout_search.getVisibility() != View.VISIBLE && layout_fail.getVisibility() != View.VISIBLE) {
            if (mIsInComingCall && !avChatController.isCallEstablish.get()){
                btnToRefuseCall();  //拒绝来电
            }else if (!mIsInComingCall && !avChatController.isCallEstablish.get()){
                btnToCancelCall(); //取消去电
            }else { //挂断
                btnToHangUpCall();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
