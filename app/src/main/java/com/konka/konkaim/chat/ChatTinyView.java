package com.konka.konkaim.chat;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.R;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.TimeUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.model.AVChatData;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 2018-4-24.
 */

public class ChatTinyView extends FrameLayout {

    private Context mContext;
    private WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    private static WindowManager windowManager;
    private MyWindowManager myWindowManager;
    private View view;
    private CircleImageView head_icon;
    private TextView remark;
    private TextView tip; //个人通话，显示“发起语音聊天”；多人通话，显示“邀请你进行多人通话”
    private boolean isTeamChat = false;

    private String friendAccount; //发起方的account
    private String displayName;
    private AVChatData data;
    private int source;
    private AVChatController avChatController;

    //群聊
    private String teamId; //发起方的account
    private String roomId;
    private String teamName;
    private boolean receivedCall;
    private ArrayList<String> accounts;

    public ChatTinyView(Context context, boolean isTeamChat) {
        super(context);
        this.mContext = context;
        this.isTeamChat = isTeamChat;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(R.layout.chat_tiny_view, this);
        initLayoutParams();

        initView();
        init();
        initData();

    }

    public ChatTinyView(Context context, boolean isTeamChat, AVChatData data, String displayName, int source) {
        super(context);
        this.mContext = context;
        this.isTeamChat = isTeamChat;
        this.data = data;
        this.friendAccount = data.getAccount();
        this.displayName = displayName;
        this.source = source;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(R.layout.chat_tiny_view, this);
        initLayoutParams();

        initView();
        init();
        initData();

    }

    public ChatTinyView(Context context, boolean isTeamChat, boolean receivedCall, String teamId,
                        String roomId, ArrayList<String> accounts, String teamName, String friendAccount) {
        super(context);
        this.mContext = context;
        this.isTeamChat = isTeamChat;
        this.receivedCall = receivedCall;
        this.teamId = teamId;
        this.roomId = roomId;
        this.accounts = accounts;
        this.teamName = teamName;
        this.friendAccount = friendAccount;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        view = LayoutInflater.from(context).inflate(R.layout.chat_tiny_view, this);
        initLayoutParams();

        initView();
        init();
        initData();

    }

    private int getScreenWidth() {
        return windowManager.getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        return windowManager.getDefaultDisplay().getHeight();
    }

    /**
     * 初始化参数
     */
    private void initLayoutParams() {
        //屏幕宽高
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        System.out.println("--- screenWidth=" + screenWidth + " screenHeight=" + screenHeight);

        //总是出现在应用程序窗口之上。
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0+
//            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        }else {
//            lp.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//        }

        // FLAG_NOT_TOUCH_MODAL不阻塞事件传递到后面的窗口
        // FLAG_NOT_FOCUSABLE 悬浮窗口较小时，后面的应用图标由不可长按变为可长按,不设置这个flag的话，home页的划屏会有问题
//        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        //lp.flags = WindowManager.LayoutParams.

        //悬浮窗默认显示的位置
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        //指定位置
/*        lp.x = screenWidth - view.getLayoutParams().width * 2;
        lp.y = screenHeight / 2 + view.getLayoutParams().height * 2;*/
        //悬浮窗的宽高
        //lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        //lp.height = 489;
        lp.width = 510;
        lp.height = 300;
        lp.format = PixelFormat.TRANSPARENT;
        windowManager.addView(this, lp);
    }

    private void initView() {
        head_icon = (CircleImageView) findViewById(R.id.head_icon);
        remark = (TextView) findViewById(R.id.remark);
        tip = (TextView) findViewById(R.id.tip);
        if (isTeamChat) {
            tip.setText("邀请你进行多人通话");
        } else {
            tip.setText("发起语音聊天");
        }
    }

    private void initData() {
        HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, head_icon); //头像

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias(); //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName();
        remark.setText("" + resultName);

        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);

        if (!isTeamChat) {
            avChatController = new AVChatController(mContext, data);
            avChatController.isCallEstablish.set(false);
        }
    }


    private void init() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                AVChatSoundPlayer.instance().stop();
                if (isTeamChat){
                    reject();
                }else {
                    AVChatProfile.getInstance().setAVChatting(false);
                    //AVChatProfile.getInstance().launchActivity(data, displayName, source, true, false);
                    rejectUpdateToDb();

//                    DataSynEvent reject_event = new DataSynEvent(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP, UserInfoUtil.getAccid(), null);
//                    EventBus.getDefault().post(reject_event);
                    doReject();
                }

                MyWindowManager.getInstance().removeChatTinyView(mContext);
                //Process.killProcess(Process.myPid());
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                AVChatSoundPlayer.instance().stop();

                //MyWindowManager.getInstance().createMovieFloatView(mContext);
                //MyWindowManager.getInstance().hideFaceMainView();
                System.out.println("小窗口响应");
                if (isTeamChat){
                    accept(teamId, roomId, /*accounts,*/ teamId);
                }else {
                    AVChatProfile.getInstance().setAVChatting(true);
                    AVChatProfile.getInstance().launchActivity(data, displayName, source, true, true);
                }

                MyWindowManager.getInstance().removeChatTinyView(mContext);
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {

                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void accept(String teamId, String roomId, /*ArrayList<String> accounts,*/ String teamName){
        getTeamMember(teamId, roomId, teamName);
    }

    public void getTeamMember(final String teamId, final String roomId, final String teamName) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {
                ArrayList<String> accounts = new ArrayList<>();

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
                    MainApplication.finishedMap.put(teamId, false);
                    MainApplication.myRoomNameMap.put(teamId, roomId);

                    TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                    AVChatKit.outgoingTeamCall(mContext, false, teamId, roomId, accounts, teamName);
                }
            }
        });
    }

    private void reject(){
        TeamAVChatProfile.sharedInstance().setTeamAVChatting(false);
        sendMsgToTeam(teamId, roomId, null, TeamConstant.ACTION_TEAM_CHAT_REJECT);
    }

    public static void sendMsgToTeam(String teamId, String roomName, List<String> accounts, int action){
        SessionTypeEnum sessionType = SessionTypeEnum.Team;
        String text = "房间相关信息";
        IMMessage textMessage = MessageBuilder.createTextMessage(teamId, sessionType, text);
        Map<String, Object> roomMap = new HashMap<>();
        //roomMap.put("data", new Gson().toJson(new RoomRequestMessage(teamId, "audio")));
        roomMap.put("type","audio");
        roomMap.put("roomName",roomName);
        //roomMap.put("members", accounts);
        roomMap.put("callTime", (double)(System.currentTimeMillis()/1000));
        roomMap.put("action", action);
        textMessage.setRemoteExtension(roomMap);

        System.out.println("---> sendMsgToTeam type is audio, roomName is "+roomName+", teamId is "+teamId+", action="+action);

        // 发送给对方
        NIMClient.getService(MsgService.class).sendMessage(textMessage, true).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {
                System.out.println("---> sendMsgToTeam code="+code);
                if (code == ResponseCode.RES_SUCCESS) {
                    System.out.println("---> sendMsgToTeam success ");
                }
            }
        });
    }

    /**
     * 拒绝电话
     */
    public void doReject() {
        avChatController.hangUp(AVChatExitCode.REJECT);
    }

    private void rejectUpdateToDb() {
        List<DbBean> dbBeanList = DBUtil.queryForFriend(mContext, UserInfoUtil.getAccid(), data.getAccount());
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

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + data.getAccount());//获取teamId
            DBUtil.update(mContext, friend_Bean);
        } else {
            rejectAddToDb();
        }
    }

    private void rejectAddToDb() {
        DbBean no_Bean = new DbBean();
        //no_Bean.setIs_team(0);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setFriend_account(data.getAccount());

        no_Bean.setRecord_time(TimeUtil.getNowTime());

        System.out.println("avChatData.getAccount()=" + data.getAccount());//获取来电方的账号
        no_Bean.setChat_from(data.getAccount());
        no_Bean.setChat_to(UserInfoUtil.getAccid());

        no_Bean.setIs_connect(-1);

        no_Bean.setIs_out(0);
        no_Bean.setIs_friend(1);
        DBUtil.add(mContext, no_Bean);
    }
}
