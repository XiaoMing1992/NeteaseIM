package com.konka.konkaim;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatProfile;
import com.konka.konkaim.chat.MyWindowManager;
import com.konka.konkaim.chat.PhoneCallStateObserver;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.chat_interface.AVChatOptions;
import com.konka.konkaim.chat_interface.ITeamDataProvider;
import com.konka.konkaim.chat_interface.IUserInfoProvider;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.ui.KickoutWindow;
import com.konka.konkaim.ui.TeamChatReceiveWindow;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.LoginActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.SDKOptions;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.OnlineClient;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatNetworkQuality;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 2018-5-8.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将完成自动登录）
        NIMClient.init(this, loginInfo(), options());

        // ... your codes
        if (NIMUtil.isMainProcess(this)) {
            // 注意：以下操作必须在主进程中进行
            // 1、UI相关初始化操作
            // 2、相关Service调用

            // 云信sdk相关业务初始化
            //NIMInitManager.getInstance().init(true);

            // 初始化音视频模块
            initAVChatKit();

            //registerMsgReceiveObserver();//注册信息接收
            observeOtherClient(true); //注册监听

            //beKickOutObserve(true);

            //doLogin();
            instance = this;

            registerActivityLifecycleCallbacks(new MyLifecycleHandler());

            beKickOutObserve(true);

            registerMsgReceiveObserver();
        }
    }

/*    private Observer<List<IMMessage>> incomingMessageObserver;

    private void registerMsgReceiveObserver() {
        incomingMessageObserver =
                new Observer<List<IMMessage>>() {
                    @Override
                    public void onEvent(List<IMMessage> messages) {
                        // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
                        for (int i = 0; messages != null && i < messages.size(); i++) {
                            System.out.println("application register, FromAccount is " + messages.get(i).getFromAccount() + ", "
                                    + messages.get(i).getSessionId() + ", type is " + messages.get(i).getSessionType()
                                    + messages.get(i).getRemoteExtension());
                            if (messages.get(i).getSessionType() == SessionTypeEnum.Team) {
                                String FromAccount = messages.get(i).getFromAccount();
                                List<String> members = new ArrayList<>();
                                //Object obj = messages.get(i).getRemoteExtension().get("members");
                                //String objects = obj.toString();
                                //System.out.println("objects = "+objects);
                                //String roomName = (String) messages.get(i).getRemoteExtension().get("roomName");
                                //String type = (String) messages.get(i).getRemoteExtension().get("type");
                                members.add("0801b314012e4d00be40a5192f3c534e");
                                String roomName = "88812";
                                String type = "audio";
                                System.out.println("--->roomName" + roomName + ", type=" + type);
                                TeamChatReceiveWindow teamChatReceiveWindow = new
                                        TeamChatReceiveWindow(getApplicationContext(), FromAccount, members, messages.get(i).getSessionId(), roomName);
                                teamChatReceiveWindow.show();
                            } else if (messages.get(i).getSessionType() == SessionTypeEnum.P2P) {

                            }
                        }


                    }
                };
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);
    }

    public void unregisterMsgReceiveObserver() {
        if (incomingMessageObserver != null) {
            NIMClient.getService(MsgServiceObserve.class)
                    .observeReceiveMessage(incomingMessageObserver, false);
        }
    }*/

    private static MainApplication instance;

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //unregisterMsgReceiveObserver();
        observeOtherClient(false);

        //beKickOutObserve(false);

        unregisterMsgReceiveObserver();
    }

    private void initAVChatKit() {
        AVChatOptions avChatOptions = new AVChatOptions() {
            @Override
            public void logout(Context context) {
                //MainActivity.logout(context, true);
                NIMClient.getService(AuthService.class).logout();
            }
        };
        //avChatOptions.entranceActivity = HomeActivity.class;
        //avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
        AVChatKit.init(avChatOptions);
        AVChatKit.setContext(getApplicationContext());

        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NIMClient.getService(UserService.class).getUserInfo(account);
                //return NimUIKit.getUserInfoProvider().getUserInfo(account);
            }

            @Override
            public String getUserDisplayName(String account) {
                return NIMClient.getService(UserService.class).getUserInfo(account) == null ?
                        null : NIMClient.getService(UserService.class).getUserInfo(account).getName();
                //return UserInfoHelper.getUserDisplayName(account);
            }
        });
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
            @Override
            public String getDisplayNameWithoutMe(String teamId, String account) {
                return null;
                //return TeamHelper.getDisplayNameWithoutMe(teamId, account);
            }

            @Override
            public String getTeamMemberDisplayName(String teamId, String account) {
                return null;
                //return TeamHelper.getTeamMemberDisplayName(teamId, account);
            }
        });

/*        AVChatOptions avChatOptions = new AVChatOptions(){
            @Override
            public void logout(Context context) {
                MainActivity.logout(context, true);
            }
        };
        avChatOptions.entranceActivity = WelcomeActivity.class;
        avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
        AVChatKit.init(avChatOptions);

        // 初始化日志系统
        LogHelper.init();
        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NimUIKit.getUserInfoProvider().getUserInfo(account);
            }

            @Override
            public String getUserDisplayName(String account) {
                return UserInfoHelper.getUserDisplayName(account);
            }
        });
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
            @Override
            public String getDisplayNameWithoutMe(String teamId, String account) {
                return TeamHelper.getDisplayNameWithoutMe(teamId, account);
            }

            @Override
            public String getTeamMemberDisplayName(String teamId, String account) {
                return TeamHelper.getTeamMemberDisplayName(teamId, account);
            }
        });*/
    }

    // 如果返回值为 null，则全部使用默认参数。
    private SDKOptions options() {
        SDKOptions options = new SDKOptions();
        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用采用默认路径作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        String sdkPath = getAppCacheDir(getApplicationContext()) + "/nim"; // 可以不设置，那么将采用默认路径
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        options.sdkStorageRootPath = sdkPath;

        return options;


/*        SDKOptions options = new SDKOptions();

        // 如果将新消息通知提醒托管给 SDK 完成，需要添加以下配置。否则无需设置。
        StatusBarNotificationConfig config = new StatusBarNotificationConfig();
        config.notificationEntrance = MainActivity.class; // 点击通知栏跳转到该Activity
        config.notificationSmallIconId = R.drawable.img_default;
        // 呼吸灯配置
        config.ledARGB = Color.GREEN;
        config.ledOnMs = 1000;
        config.ledOffMs = 1500;
        // 通知铃声的uri字符串
        config.notificationSound = "android.resource://com.netease.nim.demo/raw/msg";
        options.statusBarNotificationConfig = config;

        // 配置保存图片，文件，log 等数据的目录
        // 如果 options 中没有设置这个值，SDK 会使用采用默认路径作为 SDK 的数据目录。
        // 该目录目前包含 log, file, image, audio, video, thumb 这6个目录。
        String sdkPath = getAppCacheDir(getApplicationContext()) + "/nim"; // 可以不设置，那么将采用默认路径
        // 如果第三方 APP 需要缓存清理功能， 清理这个目录下面个子目录的内容即可。
        options.sdkStorageRootPath = sdkPath;

        // 配置是否需要预下载附件缩略图，默认为 true
        options.preloadAttach = true;

        // 配置附件缩略图的尺寸大小。表示向服务器请求缩略图文件的大小
        // 该值一般应根据屏幕尺寸来确定， 默认值为 Screen.width / 2
        //options.thumbnailSize = ${Screen.width} / 2;

        // 用户资料提供者, 目前主要用于提供用户资料，用于新消息通知栏中显示消息来源的头像和昵称
        options.userInfoProvider = new UserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return null;
            }

*//*            @Override
            public int getDefaultIconResId() {
                return R.drawable.img_default;
            }

            @Override
            public Bitmap getTeamIcon(String tid) {
                return null;
            }

            @Override
            public Bitmap getAvatarForMessageNotifier(String account) {
                return null;
            }*//*

            @Override
            public Bitmap getAvatarForMessageNotifier(SessionTypeEnum sessionType, String sessionId) {
                return null;
            }

            @Override
            public String getDisplayNameForMessageNotifier(String account, String sessionId,
                                                           SessionTypeEnum sessionType) {
                return null;
            }
        };
        return options;*/
    }

    // 如果已经存在用户登录信息，返回LoginInfo，否则返回null即可
    private LoginInfo loginInfo() {
        return null;
    }


    /**
     * 配置 APP 保存图片/语音/文件/log等数据的目录
     * 这里示例用SD卡的应用扩展存储目录
     */
    static String getAppCacheDir(Context context) {
        String storageRootPath = null;
        try {
            // SD卡应用扩展存储区(APP卸载后，该目录下被清除，用户也可以在设置界面中手动清除)，请根据APP对数据缓存的重要性及生命周期来决定是否采用此缓存目录.
            // 该存储区在API 19以上不需要写权限，即可配置 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="18"/>
            if (context.getExternalCacheDir() != null) {
                storageRootPath = context.getExternalCacheDir().getCanonicalPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(storageRootPath)) {
            // SD卡应用公共存储区(APP卸载后，该目录不会被清除，下载安装APP后，缓存数据依然可以被加载。SDK默认使用此目录)，该存储区域需要写权限!
            storageRootPath = Environment.getExternalStorageDirectory() + "/" + context.getPackageName();
        }

        System.out.println(storageRootPath);
        return storageRootPath;
    }

    Observer<List<OnlineClient>> clientsObserver = new Observer<List<OnlineClient>>() {
        @Override
        public void onEvent(List<OnlineClient> onlineClients) {
            if (onlineClients == null || onlineClients.size() == 0) {
                return;
            }
            for (int i = 0; i < onlineClients.size(); i++) {
                OnlineClient client = onlineClients.get(i);
                switch (client.getClientType()) {
                    case ClientType.Windows:
                        // PC端
                        kickClient(client);
                        break;
                    case ClientType.MAC:
                        // MAC端
                        kickClient(client);
                        break;
                    case ClientType.Web:
                        // Web端
                        kickClient(client);
                        break;
                    case ClientType.iOS:
                        // IOS端
                        kickClient(client);
                        break;
                    case ClientType.Android:
                        // Android端
                        kickClient(client);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    public void observeOtherClient(boolean register) {
        /**
         * 注册/注销多端登录状态观察者。当有其他端登录或者注销时，会通过此接口通知到UI。
         * 登录成功后，如果有其他端登录着，也会发出通知。
         *
         * @param clientsObserver 观察者，参数为同时登录的其他端信息。
         *                 如果有其他端注销，参数为剩余的在线端。如果没有剩余在线端了，参数为null。
         * @param register true为注册，false为注销
         */
        NIMClient.getService(AuthServiceObserver.class).observeOtherClients(clientsObserver, register);
    }

    /**
     * 踢掉多端同时在线的其他端
     *
     * @param client 被踢端信息
     */
    private void kickClient(final OnlineClient client) {
        NIMClient.getService(AuthService.class).kickOtherClient(client).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                // 踢出其他端成功
                System.out.println("-->kickClient onSuccess");
            }

            @Override
            public void onFailed(int code) {
                // 踢出其他端失败，返回失败code
                System.out.println("kickClient code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                // 踢出其他端错误
                exception.printStackTrace();
            }
        });
    }

    private void beKickOutObserve(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                new Observer<StatusCode>() {
                    public void onEvent(StatusCode status) {
                        if (status.wontAutoLogin()) {
                            // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                            if (status == StatusCode.KICKOUT) {
                                System.out.println("MainApplication, wontAutoLogin KICKOUT  StatusCode=" + status);

                                if (MyLifecycleHandler.isApplicationInForeground()/* || MyLifecycleHandler.isApplicationVisible()*/) { //应用在前台
                                    System.out.println("MainApplication ApplicationInForeground()");
                                    DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_KICKOUT, UserInfoUtil.getAccid(),
                                            null);
                                    EventBus.getDefault().post(msg_event);

                                } else { //应用不在前台
                                    UserInfoUtil.clearUserInfo();
                                    PrefenceUtil.remove(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                                            PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
                                    PrefenceUtil.remove(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                                            PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);
                                    MainApplication.finishedMap.clear();
                                    MainApplication.myRoomNameMap.clear();

                                    logout();
                                }
                            } else if (status == StatusCode.KICK_BY_OTHER_CLIENT) {
                                System.out.println("KICK_BY_OTHER_CLIENT  StatusCode=" + status);
                                //showToast(getApplicationContext(), "已被同时在线的其他设备主动登出");
                            } else if (status == StatusCode.NET_BROKEN) {
                                System.out.println("NET_BROKEN  StatusCode=" + status);
                                //showToast(getApplicationContext(), "网络连接已断开");
                            } else if (status == StatusCode.FORBIDDEN) {
                                System.out.println("FORBIDDEN  StatusCode=" + status);
                                //showToast(getApplicationContext(), "服务器禁止登录");
                            }
                            /*logout();
                            UserInfoUtil.clearUserInfo();
                            toLogin();*/
                        } else {
                            // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                            if (status == StatusCode.KICKOUT) {
                                System.out.println("AutoLogin KICKOUT  StatusCode=" + status);
                                //showToast(getApplicationContext(), "已在其他设备上登录");
                            } else if (status == StatusCode.KICK_BY_OTHER_CLIENT) {
                                System.out.println("KICK_BY_OTHER_CLIENT  StatusCode=" + status);
                                //showToast(getApplicationContext(), "已被同时在线的其他设备主动登出");
                            } else if (status == StatusCode.NET_BROKEN) {
                                System.out.println("NET_BROKEN  StatusCode=" + status);
                                //showToast(getApplicationContext(), "网络连接已断开");
                            } else if (status == StatusCode.FORBIDDEN) {
                                System.out.println("FORBIDDEN  StatusCode=" + status);
                                //showToast(getApplicationContext(), "服务器禁止登录");
                            }
                        }
                    }
                }, register);
    }

    private void logout() {
        TeamAVChatProfile.sharedInstance().setTeamAVChatting(false);
        AVChatProfile.getInstance().setAVChatting(false);

        ActivityHelper.getInstance().finishActivity();
        NIMClient.getService(AuthService.class).logout();
    }

    private Observer<List<IMMessage>> incomingMessageObserver;
    //TeamChatReceiveWindow teamChatReceiveWindow;

    //public static String myRoomName = null;
    //public static String myTeamId = null;
    //public static boolean finished = false;

    public static Map<String, Boolean> finishedMap = new HashMap<>();
    public static Map<String, String> myRoomNameMap = new HashMap<>();

    private void registerMsgReceiveObserver() {
        incomingMessageObserver =
                new Observer<List<IMMessage>>() {
                    @Override
                    public void onEvent(List<IMMessage> messages) {
                        // 处理新收到的消息，为了上传处理方便，SDK 保证参数 messages 全部来自同一个聊天对象。
                        for (int i = 0; messages != null && i < messages.size(); i++) {
                            System.out.println("HomeActivity register, FromAccount is " + messages.get(i).getFromAccount() + ", "
                                    + messages.get(i).getSessionId() + ", type is " + messages.get(i).getSessionType()
                                    + messages.get(i).getRemoteExtension());
                            long msgTime = messages.get(i).getTime();
                            long currentTime = System.currentTimeMillis();
                            long betweenTime = currentTime - msgTime;
                            boolean isTimeOut = betweenTime > (1000 * 60);
                            System.out.println("msgTime=" + msgTime + ", currentTime=" + currentTime + ", timestamp=" + betweenTime);

                            if (messages.get(i).getSessionType() == SessionTypeEnum.Team && messages.get(i).getRemoteExtension() != null) {
                                String FromAccount = messages.get(i).getFromAccount();
                                List<String> members = new ArrayList<>();



                                //Object obj = messages.get(i).getRemoteExtension().get("members");
                                //String objects = obj.toString();
                                //System.out.println("objects = "+objects);
                                //List<String> members = (List<String>) messages.get(i).getRemoteExtension().get("members");
                                String roomName = "";
                                if (messages.get(i).getRemoteExtension().containsKey("roomName")) {
                                    roomName = (String) messages.get(i).getRemoteExtension().get("roomName");
                                    //myRoomName = roomName;
                                }
                                // String type = (String) messages.get(i).getRemoteExtension().get("type");
                                String type = "audio";
                                int action = (int) messages.get(i).getRemoteExtension().get("action");
                                //long callTime = (long) messages.get(i).getRemoteExtension().get("callTime");
                                long callTime = messages.get(i).getTime();

                                System.out.println("--->roomName" + roomName + ", type=" + type + ", callTime=" + callTime);

                                if (action == TeamConstant.ACTION_TEAM_CHAT_LAST_OUT) {
                                    finishedMap.put(messages.get(i).getSessionId(), true);
                                    myRoomNameMap.put(messages.get(i).getSessionId(), null);
                                }

                                if (isTimeOut) {
                                    System.out.println("msg is time_out, FromAccount is " + messages.get(i).getFromAccount() + ", "
                                            + messages.get(i).getSessionId() + ", type is " + messages.get(i).getSessionType()
                                            + messages.get(i).getRemoteExtension());

                                    //写进数据库
                                    updateTeamToDb(getApplicationContext(), messages.get(i).getSessionId(), TimeUtil.getNowTime(callTime));
                                    return;
                                }

                                if (action == TeamConstant.ACTION_TEAM_CHAT_MIDDLE_INVITE) {
                                    String my_state = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_STATE_FILENAME + UserInfoUtil.getAccid(),
                                            PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY);
                                    if (my_state == null) my_state = "可通话";

                                    //当前通话忙碌，自动拦截
                                    if (/*(teamChatReceiveWindow != null && teamChatReceiveWindow.isShowing())
                                        || */ TeamAVChatProfile.sharedInstance().isTeamAVChatting()
                                            || AVChatProfile.getInstance().isAVChatting()
                                            || !my_state.equals("可通话")
                                            || PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE) {
                                        System.out.println("---> " + UserInfoUtil.getAccid() + " 正在聊天");

                                        //sendMsgToTeam(messages.get(i).getSessionId(), roomName, null, TeamConstant.ACTION_TEAM_CHAT_REJECT); //发送拒绝信息
                                        //AVChatManager.getInstance().sendControlCommand(Long.valueOf(messages.get(i).getSessionId()), AVChatControlCommand.BUSY, null);
                                        return;
                                    }

                                    members = (List<String>) messages.get(i).getRemoteExtension().get("accounts");
                                    if (!members.contains(UserInfoUtil.getAccid())) return;

                                    //写进数据库
                                    updateTeamToDb(getApplicationContext(), messages.get(i).getSessionId(), TimeUtil.getNowTime(callTime));

                                    DataSynEvent middle_invite_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE, UserInfoUtil.getAccid(),
                                                messages.get(i).getSessionId());
                                    middle_invite_event.setAccounts(members);
                                    EventBus.getDefault().post(middle_invite_event);

                                    if (!MyLifecycleHandler.isApplicationInForeground()) { //应用在前台
                                        ArrayList<String> accounts = new ArrayList<>();
                                        accounts.addAll(members);
                                        MyWindowManager.getInstance().createChatTinyView(getApplicationContext(), true, false,
                                                messages.get(i).getSessionId(), roomName, accounts, roomName, messages.get(i).getFromAccount());
                                        return;
                                    }else {
                                        DataSynEvent middle_invite_event2 = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_NORMAL,
                                                UserInfoUtil.getAccid(), messages.get(i).getSessionId());
                                        middle_invite_event2.setFromAccount(FromAccount);
                                        middle_invite_event2.setAccounts(members);
                                        middle_invite_event2.setRoomName(roomName);

                                        EventBus.getDefault().post(middle_invite_event2);

                                        return;
                                    }

                                }

                                //写进数据库
                                updateTeamToDb(getApplicationContext(), messages.get(i).getSessionId(), TimeUtil.getNowTime(callTime));

                                if (action == TeamConstant.ACTION_TEAM_CHAT_REJECT) {
                                    //Toast.makeText(HomeActivity.this, "对方已经拒绝", Toast.LENGTH_SHORT).show();
                                    DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_MSG_REJECT, /*拒绝方的action*/
                                            messages.get(i).getFromAccount(), /*保存拒绝方的账号*/
                                            messages.get(i).getSessionId()); /*team id*/
                                    EventBus.getDefault().post(msg_event);
                                    return;
                                }

                                if (action == TeamConstant.ACTION_TEAM_CHAT_LAST_OUT) {
                                    //myTeamId = messages.get(i).getSessionId();
                                    //finished = true;
                                    //myRoomName = null;
                                    finishedMap.put(messages.get(i).getSessionId(), true);
                                    myRoomNameMap.put(messages.get(i).getSessionId(), null);

                                    System.out.println("--->最后一个人已经离开, finished="+true);

                                    //oldRoomName = roomName;
                                    //is_last_one = true;

                                    //if ()
                                    //finished = (boolean)messages.get(i).getRemoteExtension().get("finished");

                                    //boolean finished = (boolean)messages.get(i).getRemoteExtension().get("finished");
                                    //System.out.println("--->最后一个人已经离开, finished="+finished);
                                    //if (finished){
                                    //showToast(HomeActivity.this, "多人聊天取消");


//                                    if (teamChatReceiveWindow != null && teamChatReceiveWindow.isShowing()) {
//                                        teamChatReceiveWindow.dismiss();
//                                    }

                                    //}

                                    DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_LAST_OUT, UserInfoUtil.getAccid(),
                                            messages.get(i).getSessionId());
                                    EventBus.getDefault().post(msg_event);

                                    return;
                                }

                                DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_MSG, UserInfoUtil.getAccid(),
                                        messages.get(i).getSessionId());
                                EventBus.getDefault().post(msg_event);

                                String my_state = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_STATE_FILENAME + UserInfoUtil.getAccid(),
                                        PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY);
                                if (my_state == null) my_state = "可通话";

                                //当前通话忙碌，自动拦截
                                if (/*(teamChatReceiveWindow != null && teamChatReceiveWindow.isShowing())
                                        || */ TeamAVChatProfile.sharedInstance().isTeamAVChatting()
                                        || AVChatProfile.getInstance().isAVChatting()
                                        || !my_state.equals("可通话")
                                        || PhoneCallStateObserver.getInstance().getPhoneCallState() != PhoneCallStateObserver.PhoneCallStateEnum.IDLE) {
                                    System.out.println("---> " + UserInfoUtil.getAccid() + " 正在聊天");

                                    //sendMsgToTeam(messages.get(i).getSessionId(), roomName, null, TeamConstant.ACTION_TEAM_CHAT_REJECT); //发送拒绝信息
                                    //AVChatManager.getInstance().sendControlCommand(Long.valueOf(messages.get(i).getSessionId()), AVChatControlCommand.BUSY, null);
                                    return;
                                }

                                if (action == TeamConstant.ACTION_TEAM_CHAT_INVITE ) {
/*                                    teamChatReceiveWindow = new
                                            TeamChatReceiveWindow(HomeActivity.this, FromAccount, members, messages.get(i).getSessionId(), roomName);
                                    teamChatReceiveWindow.show();*/

                                    if (!MyLifecycleHandler.isApplicationInForeground()) { //应用在前台
                                        ArrayList<String> accounts = new ArrayList<>();
                                        accounts.addAll(members);
                                        MyWindowManager.getInstance().createChatTinyView(getApplicationContext(), true, false,
                                                messages.get(i).getSessionId(), roomName, accounts, roomName, messages.get(i).getFromAccount());
                                        return;
                                    }

                                    DataSynEvent msg_event_tiny = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_TINYVIEW,
                                            UserInfoUtil.getAccid(), messages.get(i).getSessionId());
                                    msg_event_tiny.setFromAccount(FromAccount);
                                    msg_event_tiny.setAccounts(members);
                                    msg_event_tiny.setRoomName(roomName);

                                    EventBus.getDefault().post(msg_event_tiny);


                                }

                            } /*else if (messages.get(i).getSessionType() == SessionTypeEnum.P2P  && messages.get(i).getRemoteExtension() != null) {
                                List<String> members = (List<String>) messages.get(i).getRemoteExtension().get("members");
                                String roomName = (String) messages.get(i).getRemoteExtension().get("roomName");
                                String type = (String) messages.get(i).getRemoteExtension().get("type");
                                System.out.println("--->roomName" + roomName + ", type=" + type);
                                TeamChatReceiveWindow teamChatReceiveWindow = new
                                        TeamChatReceiveWindow(HomeActivity.this, messages.get(i).getSessionId(), members, null, roomName);
                                teamChatReceiveWindow.show();
                            }*/
                        }

                    }
                };
        NIMClient.getService(MsgServiceObserve.class)
                .observeReceiveMessage(incomingMessageObserver, true);
    }

    public void unregisterMsgReceiveObserver() {
        if (incomingMessageObserver != null) {
            NIMClient.getService(MsgServiceObserve.class)
                    .observeReceiveMessage(incomingMessageObserver, false);
        }
    }

    private synchronized void updateTeamToDb(final Context context, final String teamId, final String timeStr) {
/*        new Thread(new Runnable() {
            @Override
            public void run() {*/
        System.out.println("updateTeamToDb, teamId=" + teamId + ", time is " + TimeUtil.getNowTime());
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(context, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(teamDbBeanList.get(0).getId());
            //no_Bean.setIs_team(dbBeanList.get(0).getIs_team());
            no_Bean.setMy_account(UserInfoUtil.getAccid());

            no_Bean.setRecord_time(timeStr);

            no_Bean.setTeamId(teamDbBeanList.get(0).getTeamId());

            no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

            System.out.println("update, id=" + teamDbBeanList.get(0).getId() + "teamId=" + teamId);//获取teamId
            TeamDBUtil.update(context, no_Bean);
        } else {
            addTeamToDb(context, teamId);
        }
    }

    private synchronized void addTeamToDb(Context context, String teamId) {
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);

        //no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

        System.out.println("teamId=" + teamId);//获取teamId
        TeamDBUtil.add(context, no_Bean);
    }



}
