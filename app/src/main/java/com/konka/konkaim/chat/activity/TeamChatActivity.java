package com.konka.konkaim.chat.activity;

import android.Manifest;
import android.media.AudioManager;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.konka.konkaim.MainApplication;
import com.konka.konkaim.adapter.ActivityManyChatAdapter;
//import com.konka.konkaim.adapter.ManyChatAdapter;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.RoomRequestMessage;
import com.konka.konkaim.bean.TeamBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatExitCode;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatSoundPlayer;
import com.konka.konkaim.chat.SimpleAVChatStateObserver;
import com.konka.konkaim.chat.StateUtil;
import com.konka.konkaim.chat.permission.MPermission;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionDenied;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionGranted;
import com.konka.konkaim.chat.permission.annotation.OnMPermissionNeverAskAgain;
import com.konka.konkaim.chat.team.TeamAVChatItem;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.ui.ManyChattingAddContactWindow;
import com.konka.konkaim.ui.RecycleViewDivider;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatNetDetectCallback;
import com.netease.nimlib.sdk.avchat.AVChatNetDetector;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.AVChatStateObserverLite;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCropRatio;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.avchat.model.AVChatControlEvent;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nrtc.video.render.IVideoRender;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.konka.konkaim.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 多人音视频界面：包含音视频通话界面和接受拒绝界面
 * Created by huangjun on 2017/5/3.
 * <p>互动直播/多人会议视频通话流程示例
 * <ol>
 * <li>主播或者管理员创建房间 {@link AVChatManager#createRoom(String, String, AVChatCallback)}。 创建房间仅仅是在服务器预留一个房间名，房间未使用时有效期为30天，使用后的房间在所有用户退出后回收。</li>
 * <li>注册音视频模块监听 {@link AVChatManager#observeAVChatState(AVChatStateObserverLite, boolean)} 。</li>
 * <li>开启音视频引擎， {@link AVChatManager#enableRtc()}。 </li>
 * <li>设置互动直播模式，设置互动直播推流地址 [仅限互动直播] {@link AVChatParameters#KEY_SESSION_LIVE_MODE}, {@link AVChatParameters#KEY_SESSION_LIVE_URL}。</li>
 * <li>打开视频模块 {@link AVChatManager#enableVideo()}。</li>
 * <li>设置本地预览画布 {@link AVChatManager#setupLocalVideoRender(IVideoRender, boolean, int)} 。</li>
 * <li>设置视频通话可选参数[可以不设置] {@link AVChatManager#setParameter(AVChatParameters.Key, Object)}, {@link AVChatManager#setParameters(AVChatParameters)}。</li>
 * <li>创建并设置本地视频预览源 {@link AVChatVideoCapturerFactory#createCameraCapturer()}, {@link AVChatManager#setupVideoCapturer(AVChatVideoCapturer)}</li>
 * <li>打开本地视频预览 {@link AVChatManager#startVideoPreview()}。</li>
 * <li>加入房间 {@link AVChatManager#joinRoom2(String, AVChatType, AVChatCallback)}。</li>
 * <li>开始多人会议或者互动直播，以及各种音视频操作。</li>
 * <li>关闭本地预览 {@link AVChatManager#stopVideoPreview()} 。</li>
 * <li>关闭本地预览 {@link AVChatManager#disableVideo()} ()} 。</li>
 * <li>离开会话 {@link AVChatManager#leaveRoom2(String, AVChatCallback)}。</li>
 * <li>关闭音视频引擎, {@link AVChatManager#disableRtc()}。</li>
 * </ol></p>
 */

public class TeamChatActivity extends AppCompatActivity implements View.OnClickListener {

    // CONST
    private static final String TAG = "TeamAVChat";
    private static final String KEY_RECEIVED_CALL = "call";
    private static final String KEY_TEAM_ID = "teamid";
    private static final String KEY_ROOM_ID = "roomid";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_TNAME = "teamName";
    private static final int AUTO_REJECT_CALL_TIMEOUT = 45 * 1000;
    private static final int CHECK_RECEIVED_CALL_TIMEOUT = 45 * 1000;
    private static final int MAX_SUPPORT_ROOM_USERS_COUNT = 9;
    private static final int BASIC_PERMISSION_REQUEST_CODE = 0x100;
    // DATA
    private String teamId;
    private String roomId;
    private long chatId;
    private ArrayList<String> accounts = new ArrayList<>();
    private boolean receivedCall;
    private boolean destroyRTC;
    private String teamName;

    // CONTEXT
    private Handler mainHandler;

    private List<TeamAVChatItem> teamAVChatItemList = new ArrayList<>();
    private List<NimUserInfo> contactBeens;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView many_chat_recyclerView;
    private ActivityManyChatAdapter adapter;
    private TextView chat_total_time;
    private TextView show_chat_number;
    private ImageView img_hangup; //挂断
    private ImageView img_add; //添加成员

    // TIMER
    private Timer timer;
    private int seconds;
    private TextView timerText;
    private Runnable autoRejectTask;


    // AVCAHT OBSERVER
    private AVChatStateObserver stateObserver;
    private Observer<AVChatControlEvent> notificationObserver;
    private AVChatCameraCapturer mVideoCapturer;
    private static boolean needFinish = true;

    //private TeamAVChatNotification notifier;

    private ManyChattingAddContactWindow manyChattingAddContactWindow;

    public int total_account = 0;

    public static void startActivity(Context context, boolean receivedCall, String teamId, String roomId, ArrayList<String> mAccounts, String teamName) {
        needFinish = false;
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClass(context, TeamChatActivity.class);
        intent.putExtra(KEY_RECEIVED_CALL, receivedCall);
        intent.putExtra(KEY_ROOM_ID, roomId);
        intent.putExtra(KEY_TEAM_ID, teamId);
        intent.putExtra(KEY_ACCOUNTS, mAccounts);
        intent.putExtra(KEY_TNAME, teamName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (needFinish) {
            finish();
            return;
        }

        LogUtil.LogI(TAG, "TeamAVChatActivity onCreate, savedInstanceState=" + savedInstanceState);

        dismissKeyguard();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_many_chat);

        //startDetect();//开始网络检测

        EventBus.getDefault().register(this);//订阅

        //NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, true);
        //registerMsgReceiveObserver();

        ActivityHelper.getInstance().addActivity(this);

        initView(); //初始化控件

        onInit();
        onIntent();
//        initNotification();
//        findLayouts();

        initData();
        showViews();
        setChatting(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
/*        // 取消通知栏
        activeCallingNotifier(false);*/

        // 禁止自动锁屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_OUT, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(msg_event); //更新记录列表

        check();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        activeCallingNotifier(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.LogI(TAG, "TeamAVChatActivity onDestroy");

        EventBus.getDefault().unregister(this);//解除订阅

        needFinish = true;
        if (timer != null) {
            timer.cancel();
        }

        if (stateObserver != null) {
            AVChatManager.getInstance().observeAVChatState(stateObserver, false);
        }

/*        if (incomingMessageObserver != null) {
            NIMClient.getService(MsgServiceObserve.class)
                    .observeReceiveMessage(incomingMessageObserver, true);
        }*/

/*        if (notificationObserver != null) {
            AVChatManager.getInstance().observeControlNotification(notificationObserver, false);
        }*/
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        hangup(); // 页面销毁的时候要保证离开房间，rtc释放。

//        activeCallingNotifier(false);

        setChatting(false);

        //NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        LogUtil.LogI(TAG, "TeamAVChatActivity onSaveInstanceState");
    }

    //订阅事件处理
    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onDataSynEvent(DataSynEvent event) {
        System.out.println("TeamChatActivity: " + event.toString());
        if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MSG_REJECT)) {
            String reject_account = event.getAccount(); //获取拒绝方的account
            int index = getItemIndex(reject_account);
            System.out.println("teamChatActivity, 拒绝方的account=" + reject_account + ", 对应的index=" + index);
            if (index >= 0) {
                TeamAVChatItem avChatItem = teamAVChatItemList.get(index);
                avChatItem.setState(StateUtil.CHAT_FAIL);
                adapter.notifyItemChanged(index);
            }
        } else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE)) {
            List<String> tempAccounts = event.getAccounts();
            String FromAccount = event.getFromAccount();
            String teamId = event.getTeamId();
            System.out.println("teamChatActivity, FromAccount=" + FromAccount + ", teamId=" + teamId);

            for (int i = 0; tempAccounts != null && i < tempAccounts.size(); i++) {
                System.out.println("account=" + tempAccounts.get(i));
                accounts.add(tempAccounts.get(i));
                teamAVChatItemList.add(new TeamAVChatItem(StateUtil.WAIT_TO_CONNECT, teamId, tempAccounts.get(i)));
            }

            NIMClient.getService(UserService.class).fetchUserInfo(tempAccounts).setCallback(new RequestCallback<List<NimUserInfo>>() {
                @Override
                public void onSuccess(List<NimUserInfo> param) {
                    //List<NimUserInfo> selectItems = NIMClient.getService(UserService.class).getUserInfoList(accounts);
                    for (int i = 0; param != null && i < param.size(); i++) {
                        System.out.println("param=" + param.get(i).getAccount());
                    }

                    if (param != null) {
                        contactBeens.addAll(param);
                        myHandler.sendEmptyMessage(LOAD_OK);
                    }
                }

                @Override
                public void onFailed(int code) {
                    System.out.println("onFailed, code=" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    exception.printStackTrace();
                }
            });
/*            List<NimUserInfo> selectItems = NIMClient.getService(UserService.class).getUserInfoList(accounts);
            if (selectItems != null) {
                contactBeens.addAll(selectItems);
                adapter.notifyDataSetChanged();
                show_chat_number.setText("已邀请" + contactBeens.size() + "人通话");
            }*/
        }
    }


    /**
     * ************************************ 初始化 ***************************************
     */

    // 设置窗口flag，亮屏并且解锁/覆盖在锁屏界面上
    private void dismissKeyguard() {
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    private void onInit() {
        mainHandler = new Handler(this.getMainLooper());
    }

    private void onIntent() {
        Intent intent = getIntent();
        receivedCall = intent.getBooleanExtra(KEY_RECEIVED_CALL, false);
        roomId = intent.getStringExtra(KEY_ROOM_ID);
        teamId = intent.getStringExtra(KEY_TEAM_ID);


        //accounts = (ArrayList<String>) intent.getSerializableExtra(KEY_ACCOUNTS); //获取通话的人的账号


        teamName = intent.getStringExtra(KEY_TNAME);

        getTeamMember(teamId);
        LogUtil.LogI(TAG, "onIntent, roomId=" + roomId + ", teamId=" + teamId
                + ", receivedCall=" + receivedCall + ", accounts=" + accounts.size() + ", teamName = " + teamName);
        System.out.println("onIntent, roomId=" + roomId + ", teamId=" + teamId
                + ", receivedCall=" + receivedCall + ", accounts=" + accounts.size() + ", teamName = " + teamName);
    }

    /**
     * 一分钟还没有接听，则显示其通话状态
     */
    private void check() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 60);

                    int count = 0;
                    int total = contactBeens != null ? contactBeens.size() : 0;
                    for (int i = 0; contactBeens != null && i < contactBeens.size(); i++) {
                        String account = contactBeens.get(i).getAccount();
                        if (account.equals(UserInfoUtil.getAccid())) {
                            count++;
                            continue;
                        }
                        int index = getItemIndex(account);
                        System.out.println("check, 被邀请方的account=" + account + ", 对应的index=" + index);
                        if (index >= 0) {
                            TeamAVChatItem avChatItem = teamAVChatItemList.get(index);
                            if (avChatItem.getState() == StateUtil.WAIT_TO_CONNECT) {
                                count++;
                                avChatItem.setState(StateUtil.CONNECT_FAIL);
                            }
                            //adapter.notifyItemChanged(index);
                            Message msg = new Message();
                            msg.what = UPDATE_USER_STATE;
                            msg.arg1 = index;
                            myHandler.sendMessage(msg);
                        }
                    }
                    System.out.println("count=" + count);
                    if (count == total) {
                        hangup();
                        hasHangup = true;
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

/*        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);

                    int count = 0;
                    int total = contactBeens != null ? contactBeens.size() : 0;
                    for (int i = 0; contactBeens != null && i < contactBeens.size(); i++) {
                        String account = contactBeens.get(i).getAccount();
                        if (account.equals(UserInfoUtil.getAccid())) {
                            count++;
                            continue;
                        }
                        int index = getItemIndex(account);
                        System.out.println("check, 被邀请方的account=" + account + ", 对应的index=" + index);
                        if (index >= 0) {
                            TeamAVChatItem avChatItem = teamAVChatItemList.get(index);
                            if (avChatItem.getState() == StateUtil.WAIT_TO_CONNECT) {
                                count++;
                                avChatItem.setState(StateUtil.CHAT_FAIL);
                            }
                            //adapter.notifyItemChanged(index);
                            Message msg = new Message();
                            msg.what = UPDATE_USER_STATE;
                            msg.arg1 = index;
                            myHandler.sendMessage(msg);
                        }
                    }
                    System.out.println("count=" + count);
                    if (count == total) {
                        hangup();
                        hasHangup = true;
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, 1000);*/
    }

    private void findLayouts() {
/*        callLayout = findView(R.id.team_avchat_call_layout);
        surfaceLayout = findView(R.id.team_avchat_surface_layout);
        voiceMuteButton = findView(R.id.avchat_shield_user);*/
    }

    private void initNotification() {
/*        notifier = new TeamAVChatNotification(this);
        notifier.init(teamId, teamName);*/
    }

    /**
     * ************************************ 主流程 ***************************************
     */

    private void showViews() {
        if (receivedCall) {
            showReceivedCallLayout();
        } else {
            showSurfaceLayout();
        }
    }

    /*
     * 设置通话状态
     */
    private void setChatting(boolean isChatting) {
        TeamAVChatProfile.sharedInstance().setTeamAVChatting(isChatting);
    }

    /*
     * 接听界面
     */
    private void showReceivedCallLayout() {
/*        callLayout.setVisibility(View.VISIBLE);
        // 提示
        TextView textView = (TextView) callLayout.findViewById(R.id.received_call_tip);

        textView.setText(teamName + " 的视频通话");*/

        // 播放铃声
        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);

/*        // 拒绝
        callLayout.findViewById(R.id.refuse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVChatSoundPlayer.instance().stop();
                cancelAutoRejectTask();
                finish();
            }
        });

        // 接听
        callLayout.findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AVChatSoundPlayer.instance().stop();
                cancelAutoRejectTask();
                callLayout.setVisibility(View.GONE);
                showSurfaceLayout();
            }
        });*/

        //startAutoRejectTask();
    }

    // 拒绝
    public void reject() {
        setChatting(false);

        AVChatSoundPlayer.instance().stop();
        //cancelAutoRejectTask();
        finish();
    }

    // 接听
    public void receive() {
        AVChatSoundPlayer.instance().stop();
        //cancelAutoRejectTask();
    }

    /*
     * 通话界面
     */
    private void showSurfaceLayout() {
        //startRtc();

/*        // 列表
        surfaceLayout.setVisibility(View.VISIBLE);
        recyclerView = (RecyclerView) surfaceLayout.findViewById(R.id.recycler_view);
        initRecyclerView();

        // 通话计时
        timerText = (TextView) surfaceLayout.findViewById(R.id.timer_text);

        // 控制按钮
        ViewGroup settingLayout = (ViewGroup) surfaceLayout.findViewById(R.id.avchat_setting_layout);
        for (int i = 0; i < settingLayout.getChildCount(); i++) {
            View v = settingLayout.getChildAt(i);
            if (v instanceof RelativeLayout) {
                ViewGroup vp = (ViewGroup) v;
                if (vp.getChildCount() == 1) {
                    vp.getChildAt(0).setOnClickListener(settingBtnClickListener);
                }
            }
        }*/

        // 音视频权限检查
        checkPermission();
    }

    private void onPermissionChecked() {
        startRtc(); // 启动音视频
    }

    /**
     * ************************************ 音视频事件 ***************************************
     */

    private void startRtc() {
        // rtc init
        AVChatManager.getInstance().enableRtc();

        //AVChatManager.getInstance().enableVideo();
        LogUtil.LogI(TAG, "start rtc done");
        System.out.println("start rtc done");

//        mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
//        AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);

        // state observer
        if (stateObserver != null) {
            AVChatManager.getInstance().observeAVChatState(stateObserver, true);
        }
        stateObserver = new SimpleAVChatStateObserver() {
            @Override
            public void onJoinedChannel(int code, String audioFile, String videoFile, int i) {
                if (code == 200) {
                    onJoinRoomSuccess();
                } else {
                    onJoinRoomFailed(code, null);
                }
            }

            @Override
            public void onUserJoined(String account) {
                onAVChatUserJoined(account);
            }

            @Override
            public void onUserLeave(String account, int event) {
                onAVChatUserLeave(account);
            }

            @Override
            public void onReportSpeaker(Map<String, Integer> speakers, int mixedEnergy) {
//                onAudioVolume(speakers);
            }

            @Override
            public void onNetworkQuality(String account, int quality, AVChatNetworkStats stats) {
                System.out.println("onNetworkQuality account=" + account);
                /*if (quality == 0) {
                    showToast(getApplicationContext(), "通话的网络状况非常好");
                } else if (quality == 1) {
                    showToast(getApplicationContext(), "通话的网络状况好");
                } else*/
                if (quality == 2) {
                    showToast(getApplicationContext(), "通话的网络状况差");
                } else if (quality == 3) {
                    showToast(getApplicationContext(), "通话的网络状况非常差");
                }
            }
        };
        AVChatManager.getInstance().observeAVChatState(stateObserver, true);
        LogUtil.LogI(TAG, "observe rtc state done");
        System.out.println("observe rtc state done");

        // notification observer
/*        if (notificationObserver != null) {
            AVChatManager.getInstance().observeControlNotification(notificationObserver, false);
        }
        notificationObserver = new Observer<AVChatControlEvent>() {

            @Override
            public void onEvent(AVChatControlEvent event) {
                final String account = event.getAccount();
                if (AVChatControlCommand.NOTIFY_VIDEO_ON == event.getControlCommand()) {
                    onVideoLive(account);
                } else if (AVChatControlCommand.NOTIFY_VIDEO_OFF == event.getControlCommand()) {
                    onVideoLiveEnd(account);
                }
            }
        };
        AVChatManager.getInstance().observeControlNotification(notificationObserver, true);*/

        // join
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_AUDIO_REPORT_SPEAKER, true);
        //AVChatManager.getInstance().setParameter(AVChatParameters.KEY_VIDEO_FIXED_CROP_RATIO, AVChatVideoCropRatio.CROP_RATIO_1_1);

        AVChatManager.getInstance().joinRoom2(roomId, AVChatType.AUDIO, new AVChatCallback<AVChatData>() {
            @Override
            public void onSuccess(AVChatData data) {
                chatId = data.getChatId();
                LogUtil.LogI(TAG, "join room success, roomId=" + roomId + ", chatId=" + chatId);
            }

            @Override
            public void onFailed(int code) {
                onJoinRoomFailed(code, null);
                LogUtil.LogI(TAG, "join room failed, code=" + code + ", roomId=" + roomId);
            }

            @Override
            public void onException(Throwable exception) {
                onJoinRoomFailed(-1, exception);
                LogUtil.LogI(TAG, "join room failed, e=" + exception.getMessage() + ", roomId=" + roomId);
            }
        });
        LogUtil.LogI(TAG, "start join room, roomId=" + roomId);
    }

    private void onJoinRoomSuccess() {
        //startTimer();
//        startLocalPreview();
//        startTimerForCheckReceivedCall();
        LogUtil.LogI(TAG, "team avchat running..." + ", roomId=" + roomId);

        System.out.println("team avchat running..." + ", roomId=" + roomId);
    }

    private void onJoinRoomFailed(int code, Throwable e) {
        if (code == ResponseCode.RES_ENONEXIST) {
            //showToast(getString(R.string.t_avchat_join_fail_not_exist));
            //showToast("目标(对象或用户)不存在");
            System.out.println("目标(对象或用户)不存在");
        } else {
            System.out.println("join room failed, code=" + code + ", e=" + (e == null ? "" : e.getMessage()));
            //showToast("join room failed, code=" + code + ", e=" + (e == null ? "" : e.getMessage()));
        }
    }

    public void onAVChatUserJoined(String account) {
        int index = getItemIndex(account);
        if (index >= 0) {
            total_account++;
            TeamAVChatItem avChatItem = teamAVChatItemList.get(index);
            avChatItem.setState(StateUtil.CHATTING);
            adapter.notifyItemChanged(index);
        }

/*        int index = getItemIndex(account);
        if (index >= 0) {
            TeamAVChatItem item = data.get(index);
            AVChatSurfaceViewRenderer surfaceView = adapter.getViewHolderSurfaceView(item);
            if (surfaceView != null) {
                item.state = TeamAVChatItem.STATE.STATE_PLAYING;
                item.videoLive = true;
                adapter.notifyItemChanged(index);
                AVChatManager.getInstance().setupRemoteVideoRender(account, surfaceView, false, AVChatVideoScalingType.SCALE_ASPECT_FIT);
            }
        }
        updateAudioMuteButtonState();*/

        LogUtil.LogI(TAG, "on user joined, account=" + account + ", total_account=" + total_account);
        System.out.println("on user joined, account=" + account + ", total_account=" + total_account);
    }

    public void onAVChatUserLeave(String account) {
        int index = getItemIndex(account);
        if (index >= 0) {
            total_account--;
            if (total_account <= -1) {
                sendMsgByLastOne(teamId, roomId);
            }
            TeamAVChatItem avChatItem = teamAVChatItemList.get(index);
            avChatItem.setState(StateUtil.CHAT_OVER);
            adapter.notifyItemChanged(index);
        }

/*        int index = getItemIndex(account);
        if (index >= 0) {
            TeamAVChatItem item = data.get(index);
            item.state = TeamAVChatItem.STATE.STATE_HANGUP;
            item.volume = 0;
            adapter.notifyItemChanged(index);
        }
        updateAudioMuteButtonState();*/

        LogUtil.LogI(TAG, "on user leave, account=" + account + ", index=" + index + ", total_account=" + total_account);
        System.out.println("on user leave, account=" + account + ", index=" + index + ", total_account=" + total_account);
    }

/*    private void startLocalPreview() {
        if (contactBeens.size() > 1 && contactBeens.get(0).account.equals(AVChatKit.getAccount())) {
            AVChatSurfaceViewRenderer surfaceView = adapter.getViewHolderSurfaceView(data.get(0));
            if (surfaceView != null) {
                AVChatManager.getInstance().setupLocalVideoRender(surfaceView, false, AVChatVideoScalingType.SCALE_ASPECT_FIT);
                AVChatManager.getInstance().startVideoPreview();
                contactBeens.get(0).state = TeamAVChatItem.STATE.STATE_PLAYING;
                contactBeens.get(0).videoLive = true;
                adapter.notifyItemChanged(0);
            }
        }
    }*/

    /**
     * ************************************ 音视频状态 ***************************************
     */

/*    private void onVideoLive(String account) {
        if (account.equals(AVChatKit.getAccount())) {
            return;
        }

        notifyVideoLiveChanged(account, true);
    }

    private void onVideoLiveEnd(String account) {
        if (account.equals(AVChatKit.getAccount())) {
            return;
        }

        notifyVideoLiveChanged(account, false);
    }*/

/*    private void notifyVideoLiveChanged(String account, boolean live) {
        int index = getItemIndex(account);
        if (index >= 0) {
            TeamAVChatItem item = data.get(index);
            item.videoLive = live;
            adapter.notifyItemChanged(index);
        }
    }*/



/*    private void onAudioVolume(Map<String, Integer> speakers) {
        for (TeamAVChatItem item : data) {
            if (speakers.containsKey(item.account)) {
                item.volume = speakers.get(item.account);
                adapter.updateVolumeBar(item);
            }
        }
    }*/



/*    private void updateSelfItemVideoState(boolean live) {
        int index = getItemIndex(AVChatKit.getAccount());
        if (index >= 0) {
            TeamAVChatItem item = data.get(index);
            item.videoLive = live;
            adapter.notifyItemChanged(index);
        }
    }*/

    //离开房间
    private void hangup() {
        if (destroyRTC) {
            return;
        }

        try {
            //AVChatManager.getInstance().stopVideoPreview();
            AVChatManager.getInstance().leaveRoom2(roomId, null);
            AVChatManager.getInstance().disableRtc();
        } catch (Exception e) {
            e.printStackTrace();
        }

        destroyRTC = true;
        hasHangup = true;
        LogUtil.LogI(TAG, "destroy rtc & leave room, roomId=" + roomId);
    }

    /**
     * ************************************ 定时任务 ***************************************
     */

/*    private void startTimer() {
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
        //timerText.setText("00:00");
    }*/

/*    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            seconds++;
            int m = seconds / 60;
            int s = seconds % 60;
            final String time = String.format(Locale.CHINA, "%02d:%02d", m, s);

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    timerText.setText(time);
//                }
//            });
        }
    };*/

/*    private void startTimerForCheckReceivedCall() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int index = 0;
                for (TeamAVChatItem item : data) {
                    if (item.type == TYPE_DATA && item.state == TeamAVChatItem.STATE.STATE_WAITING) {
                        item.state = TeamAVChatItem.STATE.STATE_END;
                        adapter.notifyItemChanged(index);
                    }
                    index++;
                }
                checkAllHangUp();
            }
        }, CHECK_RECEIVED_CALL_TIMEOUT);
    }*/

/*    private void startAutoRejectTask() {
        if (autoRejectTask == null) {
            autoRejectTask = new Runnable() {
                @Override
                public void run() {
                    AVChatSoundPlayer.instance().stop();
                    finish();
                }
            };
        }

        mainHandler.postDelayed(autoRejectTask, AUTO_REJECT_CALL_TIMEOUT);
    }*/

/*    private void cancelAutoRejectTask() {
        if (autoRejectTask != null) {
            mainHandler.removeCallbacks(autoRejectTask);
        }
    }*/

    /*
     * 除了所有人都没接通，其他情况不做自动挂断
     */
/*    private void checkAllHangUp() {
        for (TeamAVChatItem item : data) {
            if (item.account != null &&
                    !item.account.equals(AVChatKit.getAccount()) &&
                    item.state != TeamAVChatItem.STATE.STATE_END) {
                return;
            }
        }
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hangup();
                finish();
            }
        }, 200);
    }*/


//    /**
//     * 通知栏
//     */
//    private void activeCallingNotifier(boolean active) {
//        if (notifier != null) {
//            if (destroyRTC) {
//                notifier.activeCallingNotification(false);
//            } else {
//                notifier.activeCallingNotification(active);
//            }
//        }
//    }

    /**
     * ************************************ 点击事件 ***************************************
     */


/*    private void updateAudioMuteButtonState() {
        boolean enable = false;
        for (TeamAVChatItem item : data) {
            if (item.state == TeamAVChatItem.STATE.STATE_PLAYING &&
                    !AVChatKit.getAccount().equals(item.account)) {
                enable = true;
                break;
            }
        }
        voiceMuteButton.setEnabled(enable);
        voiceMuteButton.invalidate();
    }*/


/*    private void disableUserAudio() {
        List<Pair<String, Boolean>> voiceMutes = new ArrayList<>();
        for (TeamAVChatItem item : data) {
            if (item.state == TeamAVChatItem.STATE.STATE_PLAYING &&
                    !AVChatKit.getAccount().equals(item.account)) {
                voiceMutes.add(new Pair<>(item.account, AVChatManager.getInstance().isRemoteAudioMuted(item.account)));
            }
        }
        TeamAVChatVoiceMuteDialog dialog = new TeamAVChatVoiceMuteDialog(this, teamId, voiceMutes);
        dialog.setTeamVoiceMuteListener(new TeamAVChatVoiceMuteDialog.TeamVoiceMuteListener() {
            @Override
            public void onVoiceMuteChange(List<Pair<String, Boolean>> voiceMuteAccounts) {
                if (voiceMuteAccounts != null) {
                    for (Pair<String, Boolean> voiceMuteAccount : voiceMuteAccounts) {
                        AVChatManager.getInstance().muteRemoteAudio(voiceMuteAccount.first, voiceMuteAccount.second);
                    }
                }
            }
        });
        dialog.show();
    }*/
    @Override
    public void onBackPressed() {
        // 屏蔽BACK
    }

    /**
     * ************************************ 数据源 ***************************************
     */

    private void initView() {
        many_chat_recyclerView = (RecyclerView) findViewById(R.id.many_chat_recyclerView);
        chat_total_time = (TextView) findViewById(R.id.chat_total_time);
        show_chat_number = (TextView) findViewById(R.id.show_chat_number);
        img_hangup = (ImageView) findViewById(R.id.img_hangup);
        img_add = (ImageView) findViewById(R.id.img_add);
        img_hangup.setOnClickListener(this);
        img_add.setOnClickListener(this);
        img_hangup.requestFocus();
    }

    private void initData() {
        final AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        //设置声音模式
        audioManager.setMode(AudioManager.STREAM_MUSIC);
        // 打开扬声器
        audioManager.setSpeakerphoneOn(true);

        // 确认数据源,自己放在首位
        contactBeens = new ArrayList<>();
        teamAVChatItemList = new ArrayList<>();

//        contactBeens = new ArrayList<>(accounts.size() + 1);

/*        for (String account : accounts) {
            if (account.equals(AVChatKit.getAccount())) {
                continue;
            }

            contactBeens.add(new TeamAVChatItem(TYPE_DATA, teamId, account));
        }*/


        adapter = new ActivityManyChatAdapter(TeamChatActivity.this, contactBeens, teamAVChatItemList);

        linearLayoutManager = new LinearLayoutManager(TeamChatActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        many_chat_recyclerView.setLayoutManager(linearLayoutManager);

        many_chat_recyclerView.addItemDecoration(new RecycleViewDivider(TeamChatActivity.this, LinearLayout.HORIZONTAL,
                0, (int) this.getResources().getDimension(R.dimen.team_chat_contact_item_dividerWidth)));

        many_chat_recyclerView.setAdapter(adapter);

/*        TeamAVChatItem selfItem = new TeamAVChatItem(TYPE_DATA, teamId, AVChatKit.getAccount());
        selfItem.state = TeamAVChatItem.STATE.STATE_PLAYING; // 自己直接采集摄像头画面
        data.add(0, selfItem);

        // 补充占位符
        int holderLength = MAX_SUPPORT_ROOM_USERS_COUNT - data.size();
        for (int i = 0; i < holderLength; i++) {
            data.add(new TeamAVChatItem(teamId));
        }

        // RecyclerView
        adapter = new TeamAVChatAdapter(recyclerView, data);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new SpacingDecoration(ScreenUtil.dip2px(1), ScreenUtil.dip2px(1), true));
        */

        last_time = current_time = System.currentTimeMillis();
        System.out.println("进行多人通话 last_time = " + last_time + " current_time" + current_time);
        notifyCurrentChatTime();
        chat_total_time.setText("00:00");

        if (contactBeens != null)
            show_chat_number.setText("已邀请" + contactBeens.size() + "人通话");
    }

    private int getItemIndex(final String account) {
        int index = -1;
        for (int i = 0; contactBeens != null && i < contactBeens.size(); i++) {
            if (account.equals(contactBeens.get(i).getAccount())) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * ************************************ 权限检查 ***************************************
     */
    private void checkPermission() {
        List<String> lackPermissions = AVChatManager.getInstance().checkPermission(TeamChatActivity.this);
        if (lackPermissions.isEmpty()) {
            onBasicPermissionSuccess();
        } else {
            String[] permissions = new String[lackPermissions.size()];
            for (int i = 0; i < lackPermissions.size(); i++) {
                permissions[i] = lackPermissions.get(i);
            }
            MPermission.with(TeamChatActivity.this)
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
        onPermissionChecked();
//        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        System.out.println("授权成功");
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    @OnMPermissionNeverAskAgain(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "音视频通话所需权限未全部授权，可能影响通话", Toast.LENGTH_SHORT).show();
        System.out.println("音视频通话所需权限未全部授权，可能影响通话");
        onPermissionChecked();
    }

    /**
     * ************************************ helper ***************************************
     */

    private void showToast(String content) {
//        Toast.makeText(TeamChatActivity.this, content, Toast.LENGTH_SHORT).show();
    }


    private ManyChattingAddContactWindow.OnAddContactListener onAddContactListener =
            new ManyChattingAddContactWindow.OnAddContactListener() {
                @Override
                public void OnAddContact(List<NimUserInfo> selectItems) {
                    if (selectItems != null && !selectItems.isEmpty()) {
                        List<String> addAccounts = new ArrayList<>();
                        for (int i = 0; i < selectItems.size(); i++) {
                            addAccounts.add(selectItems.get(i).getAccount());
                        }
                        add(teamId, addAccounts, selectItems); //添加成员
                    }
                }
            };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_add:
                System.out.println("accounts.size()="+accounts.size());
                if (accounts.size()>=4){
                    showToast(TeamChatActivity.this, "人数已达上限");
                    return;
                }
                manyChattingAddContactWindow = new ManyChattingAddContactWindow(TeamChatActivity.this, accounts, contactBeens.size());
                //manyChattingAddContactWindow.setCurrent_member(contactBeens.size());
                manyChattingAddContactWindow.setOnAddContactListener(onAddContactListener);
                manyChattingAddContactWindow.show();
                break;

            case R.id.img_hangup:
                total_account--;
                System.out.println("total_account=" + total_account);
                if (total_account <= -1) {
                    sendMsgByLastOne(teamId, roomId);
                }

                //下面把数据写进数据库
                //addToDb();
                //updateTeamToDb();
                DataSynEvent event = new DataSynEvent(DataSynEvent.TYPE_TEAM_HUNGUP, UserInfoUtil.getAccid(), teamId);
                EventBus.getDefault().post(event);

                reject();
                //finish();
                //中断时间的计算
                hasHangup = true;
                break;
        }
    }

    private void addTeamToDb() {
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);

        //no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

        System.out.println("teamId=" + teamId);//获取teamId
        TeamDBUtil.add(TeamChatActivity.this, no_Bean);
    }

    private void updateTeamToDb() {
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(TeamChatActivity.this, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(teamDbBeanList.get(0).getId());
            // no_Bean.setIs_team(teamDbBeanList.get(0).getIs_team());
            no_Bean.setMy_account(UserInfoUtil.getAccid());

            no_Bean.setRecord_time(TimeUtil.getNowTime());

            no_Bean.setTeamId(teamDbBeanList.get(0).getTeamId());

            no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

            System.out.println("update, id=" + teamDbBeanList.get(0).getId() + "teamId=" + teamId);//获取teamId
            TeamDBUtil.update(TeamChatActivity.this, no_Bean);
        } else {
            addTeamToDb();
        }

        DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_OUT, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(msg_event); //更新记录列表
    }

    private void add(final String teamId, final List<String> args_accounts, final List<NimUserInfo> selectItems) {
        if (teamId == null) {
            //showToast("teamId is null");
            System.out.println("teamId is null");
            return;
        }
        NIMClient.getService(TeamService.class).addMembers(teamId, args_accounts)
                .setCallback(new RequestCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> param) {
                        // 返回onSuccess，表示拉人不需要对方同意，且对方已经入群成功了
                        System.out.println("-->add member success, teamId=" + teamId + ", roomId=" + roomId);

                        for (int i = 0; args_accounts != null && i < args_accounts.size(); i++) {
                            System.out.println("-->add all member account " + args_accounts.get(i));
                        }

                        System.out.println("-->----------------------------------------");
                        for (int i = 0; param != null && i < param.size(); i++) {
                            System.out.println("-->add member fail param " + param.get(i));
                            for (int j = 0; j < selectItems.size(); j++) {
                                if (param.get(i).equals(selectItems.get(j).getAccount())) {
                                    selectItems.remove(j);
                                    break;
                                }
                            }
                        }
                        System.out.println("-->----------------------------------------");

                        for (int j = 0; j < selectItems.size(); j++) {
                            System.out.println("-->add member success account is " + selectItems.get(j).getAccount());
                            accounts.add(selectItems.get(j).getAccount());
                            teamAVChatItemList.add(new TeamAVChatItem(StateUtil.WAIT_TO_CONNECT, teamId, selectItems.get(j).getAccount()));
                        }

                        //getTeamMember(teamId); //获取team的成员

                        sendMsgToTeam(teamId, roomId, accounts, TeamConstant.ACTION_TEAM_CHAT_MIDDLE_INVITE);

                        contactBeens.addAll(selectItems);

                        adapter.notifyDataSetChanged();
                        show_chat_number.setText("已邀请" + (contactBeens.size() - 1) + "人通话");

                        check();//检查用户状态
                    }

                    @Override
                    public void onFailed(int code) {
                        // 返回onFailed，并且返回码为810，表示发出邀请成功了，但是还需要对方同意
                        System.out.println("-->add member fail, code=" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public static void sendMsgToTeam(String teamId, String roomName, List<String> accounts, int action) {
        SessionTypeEnum sessionType = SessionTypeEnum.Team;
        String text = "房间相关信息";
        IMMessage textMessage = MessageBuilder.createTextMessage(teamId, sessionType, text);
        Map<String, Object> roomMap = new HashMap<>();
        //roomMap.put("data", new Gson().toJson(new RoomRequestMessage(teamId, "audio")));

        roomMap.put("type", "audio");
        roomMap.put("roomName", roomName);
        roomMap.put("accounts", accounts);
        roomMap.put("callTime", (double) (System.currentTimeMillis() / 1000));
        roomMap.put("action", action);
        textMessage.setRemoteExtension(roomMap);
        textMessage.setPushContent("多人通话请求");

        System.out.println("---> sendMsgToTeam type is audio, roomName is " + roomName + ", teamId is " + teamId);

        // 发送给对方
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                System.out.println("---> sendMsgToTeam code=" + code);
                if (code == ResponseCode.RES_SUCCESS) {
                    System.out.println("---> sendMsgToTeam success ");
                }
            }
        });
    }

    private final int REFRESH_CURRENT_CHAT_TIME = 0x01;
    private final int TIME_OUT = 0x00;
    private final int LOAD_OK = 0x02;
    private final int LOAD_FAIL = 0x03;
    private final int UPDATE_USER_STATE = 0x04;
    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case TIME_OUT:
//                    if (isTimeOut) {
//                        connect_content.setText("对方无响应，结束通话");
//                        avChatController.hangUp(AVChatExitCode.PEER_NO_RESPONSE);
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    Thread.sleep(1000 * 10);
//                                    finish();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
//
//                    }
                    break;

                case REFRESH_CURRENT_CHAT_TIME:
                    System.out.println("---> handler current_time_str" + current_time_str);
//                    if (layout_chat.getVisibility() == View.VISIBLE)
//                        connect_content.setText(current_time_str); //显示当前通话时间
//                    else if (layout_receive.getVisibility() == View.VISIBLE)
//                        tv_chat_request.setText(current_time_str); //显示当前通话时间
                    chat_total_time.setText(current_time_str); //显示当前通话时间
                    break;

                case LOAD_OK:
                    if (contactBeens != null) {
                        if (!contactBeens.isEmpty() && contactBeens.get(0).getAccount().equals(UserInfoUtil.getAccid())) {

                        } else {
                            contactBeens.add(0, NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid())); // 获取所有好友用户资料
                        }
                    }
                    if (teamAVChatItemList != null) {
                        if (!teamAVChatItemList.isEmpty() && teamAVChatItemList.get(0).getAccount().equals(UserInfoUtil.getAccid())) {

                        } else {
                            teamAVChatItemList.add(0, new TeamAVChatItem(StateUtil.CHATTING, teamId, UserInfoUtil.getAccid()));
                        }
                    }

                    if (contactBeens != null)
                        show_chat_number.setText("已邀请" + (contactBeens.size() - 1) + "人通话");
                    adapter.notifyDataSetChanged();
                    break;

                case LOAD_FAIL:

                    break;
                case UPDATE_USER_STATE:
                    int index = msg.arg1;
                    System.out.println("update index=" + index);
                    adapter.notifyItemChanged(index);
                    break;
            }
        }
    };

    private boolean hasHangup = false;
    private long current_time = 0;
    private long last_time = 0;

    public void notifyCurrentChatTime() {
        //String current_time = "";

        new Thread(new Runnable() {
            @Override
            public void run() {
                //System.out.println("notifyCurrentChatTime isCallEstablished is " + avChatController.isCallEstablish.get());
                while (!hasHangup/* && avChatController.isCallEstablish.get()*/) {
                    try {
                        Thread.sleep(1000);
                        current_time_str = computeTime(); //计算时间
                        myHandler.sendEmptyMessage(REFRESH_CURRENT_CHAT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //return current_time;
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

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }


    public void getTeamMember(final String teamId) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {

                System.out.println("queryMemberList, code=" + code + ", members.size=" + (members != null ? members.size() : -1));
                if (code == ResponseCode.RES_SUCCESS && members != null) {
                    for (int i = 0; i < members.size(); i++) {
                        System.out.println("queryMemberList,teamId" + members.get(i).getTid() + ", account=" + members.get(i).getAccount()
                                + ", teamNick=" + members.get(i).getTeamNick() + ", Extension:" + members.get(i).getExtension());

                        accounts.add(members.get(i).getAccount());
                    }
                    //myTeamMembers.addAll(members);

                    //teamBeanList.add(new TeamBean(teamId, members));
                    //manyChatAdapter.notifyDataSetChanged();//刷新数据
                    contactBeens.clear();
                    teamAVChatItemList.clear();

                    System.out.println("--- 所有好友用户资料信息 ---");
                    accounts.remove(UserInfoUtil.getAccid()); //移除自己的账号

                    contactBeens.addAll(NIMClient.getService(UserService.class).getUserInfoList(accounts)); // 获取所有好友用户资料
                    for (int i = 0; contactBeens != null && i < contactBeens.size(); i++) {
                        System.out.println("mobile=" + contactBeens.get(i).getMobile() + ", name="
                                + contactBeens.get(i).getName() + ", account=" + contactBeens.get(i).getAccount());

                        boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(contactBeens.get(i).getAccount());
                        if (!isMyFriend) { //非对方好友，已经被对方删除
                            if (!contactBeens.get(i).getAccount().equals(UserInfoUtil.getAccid())) {
                                System.out.println("--- 非对方好友，已经被对方删除 --- account=" + contactBeens.get(i).getAccount());
                                teamAVChatItemList.add(new TeamAVChatItem(StateUtil.CHAT_NOT_FRIEND_FAIL, teamId, contactBeens.get(i).getAccount()));
                            } else {
                                System.out.println("--- 本人的 account=" + contactBeens.get(i).getAccount());
                                //teamAVChatItemList.add(new TeamAVChatItem(StateUtil.CHAT_NOT_FRIEND_FAIL, teamId, contactBeens.get(i).getAccount()));
                            }
                        } else
                            teamAVChatItemList.add(new TeamAVChatItem(StateUtil.WAIT_TO_CONNECT, teamId, contactBeens.get(i).getAccount()));
                    }

                    myHandler.sendEmptyMessage(LOAD_OK);
                }
            }
        });
    }

/*    private final int LOAD_OK = 0x00;
    private final int LOAD_FAIL = 0x01;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_OK:
                    if (contactBeens != null)
                        show_chat_number.setText("已邀请" + contactBeens.size() + "人通话");
                    adapter.notifyDataSetChanged();
                    break;

                case LOAD_FAIL:

                    break;
            }
        }
    };*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //System.out.println("dispatchKeyEvent " + event.getKeyCode() + ", " + event.getAction());
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //下面把数据写进数据库
            //addToDb();
            //updateTeamToDb();
            DataSynEvent dataSynEvent = new DataSynEvent(DataSynEvent.TYPE_TEAM_HUNGUP, UserInfoUtil.getAccid(), teamId);
            EventBus.getDefault().post(dataSynEvent);

            hasHangup = true;

            reject();
            //finish();
            //中断时间的计算

            return true;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP)) {

            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    private void sendMsgByLastOne(final String teamId, String roomName) {
        //最后一个人离开发送一条群聊结束消息通知
        final IMMessage message = MessageBuilder.createTextMessage(teamId, SessionTypeEnum.Team, "");
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("type", "audio");
        roomMap.put("roomName", roomName);
        roomMap.put("action", TeamConstant.ACTION_TEAM_CHAT_LAST_OUT);
        roomMap.put("callTime", (double) (System.currentTimeMillis() / 1000));
        roomMap.put("finished", true);
        message.setRemoteExtension(roomMap);

        System.out.println("sendMsgByLastOne-->" + message.getRemoteExtension());

        NIMClient.getService(MsgService.class).sendMessage(message, false).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                if (ResponseCode.RES_ECONNECTION == code) {

                    //return;
                }
                if (ResponseCode.RES_SUCCESS == code) {
                    MainApplication.finishedMap.put(teamId, true);
                    MainApplication.myRoomNameMap.put(teamId, null);

                    System.out.println("sendMsgByLastOne-->" + message.getRemoteExtension() + ", code=" + code);
                    //return;
                }
            }
        });
    }
}
