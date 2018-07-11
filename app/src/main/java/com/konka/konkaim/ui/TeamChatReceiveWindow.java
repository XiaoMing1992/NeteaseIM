package com.konka.konkaim.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.R;
import com.konka.konkaim.adapter.TeamChatReceiveAdapter;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatExitCode;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatSoundPlayer;
import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.konka.konkaim.chat.activity.TeamChatActivity;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.TimeUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HP on 2018-5-10.
 */

public class TeamChatReceiveWindow extends PopupWindow implements View.OnClickListener {
    private final String TAG = "TeamChatReceiveWindow";
    private Context mContext;
    private View mView;
    private CircleImageView from_head_icon;
    private TextView from_remark;
    private RecyclerView team_member_recyclerView;
    private ImageView btn_yes;
    private ImageView btn_no;
    private TextView tv_chat_request;

    private String friendAccount;

    private List<String> memberAccounts;
    private String teamId;
    private String roomId;
    private TeamChatReceiveAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    private boolean cancelCall = false;

    public TeamChatReceiveWindow(Context context, String friendAccount, List<String> memberAccounts, String teamId, String roomId) {
        super(context);
        this.friendAccount = friendAccount;
        this.memberAccounts = memberAccounts;
        this.teamId = teamId;
        this.roomId = roomId;
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.team_chat_receive, null);
        setContentView(mView);
        from_head_icon = (CircleImageView)mView.findViewById(R.id.from_head_icon);
        from_remark = (TextView)mView.findViewById(R.id.from_remark);
        team_member_recyclerView = (RecyclerView)mView.findViewById(R.id.team_member_recyclerView);
        btn_yes = (ImageView)mView.findViewById(R.id.btn_yes);
        btn_no = (ImageView)mView.findViewById(R.id.btn_no);
        tv_chat_request = (TextView)mView.findViewById(R.id.tv_chat_request);
        btn_yes.setOnClickListener(this);
        btn_no.setOnClickListener(this);
        btn_yes.requestFocus();
    }

    private void initSetting() {
        Utils.computeScreenSize(mContext);
        LogUtil.LogD(TAG, "ScreenWidth = " + Utils.getScreenWidth() + " ScreenHeight = " + Utils.getScreenHeight());
        setWidth(Utils.getScreenWidth());
        setHeight(Utils.getScreenHeight());
        setFocusable(true);
        //setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));
    }

    private void initData(){
        HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(friendAccount)==null?
                        null:NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar(),
                R.drawable.img_default, R.drawable.img_default ,from_head_icon); //头像

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount) == null ?
                null : NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias(); //获取备注
        String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName();
        from_remark.setText(""+resultName);

        //memberAccounts = new ArrayList<>();
        for (int i=0;memberAccounts!=null && i<memberAccounts.size();i++){
            System.out.println("---> memberAccount is "+memberAccounts.get(i));
        }

        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        team_member_recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new TeamChatReceiveAdapter(mContext, memberAccounts);
        team_member_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL,
                0, (int) mContext.getResources().getDimension(R.dimen.team_chat_select_contact_item_dividerWidth)));

        team_member_recyclerView.setAdapter(adapter);

        AVChatSoundPlayer.instance().play(AVChatSoundPlayer.RingerTypeEnum.RING);

        TimeOut();
    }

    public void show() {
        showAtLocation(mView, Gravity.CENTER, 0, 0);
    }

    public void setCancelCall(boolean cancelCall){
        this.cancelCall = cancelCall;
        if (this.cancelCall){
            this.cancelCall = false;
            tv_chat_request.setText("对方取消多人通话");
        }
    }

    @Override
    public void dismiss() {
        AVChatSoundPlayer.instance().stop();
        myHandler.removeCallbacks(time_out_runnable);

        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                System.out.println("click yes");
                hasClick = true;
                //下面把数据写进数据库
                //addToDb();
                //updateToDb();

                //ArrayList<String> accounts = new ArrayList<>();
                //accounts.addAll(memberAccounts);
                accept(teamId, roomId, /*accounts,*/ teamId);
                dismiss();
                break;
            case R.id.btn_no:
                System.out.println("click no");
                hasClick = true;
                //下面把数据写进数据库
                //addToDb();
                //updateToDb();

                reject();
                dismiss();
                break;
        }
    }

    private void addToDb(){
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);
        System.out.println("teamId="+teamId);//获取teamId
        TeamDBUtil.add(mContext, no_Bean);
    }

    private void updateToDb(){
        List<TeamDbBean> dbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        if (dbBeanList != null && dbBeanList.size()>0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(dbBeanList.get(0).getId());
            //no_Bean.setIs_team(1);
            no_Bean.setMy_account(UserInfoUtil.getAccid());
            no_Bean.setRecord_time(TimeUtil.getNowTime());
            no_Bean.setTeamId(teamId);
            System.out.println("update, id=" + dbBeanList.get(0).getId()+"teamId=" + teamId);//获取teamId
            TeamDBUtil.update(mContext, no_Bean);
        }else {
            addToDb();
        }
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

    private void accept(String teamId, String roomId, /*ArrayList<String> accounts,*/ String teamName){
        getTeamMember(teamId, roomId, teamName);
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

    private final int TIME_OUT = 0x00;
    private final int DISMISS = 0x01;
    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case TIME_OUT:
                    if (isTimeOut) {

                        updateTeamToDb();
                        DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_NOT_RECEIVE, UserInfoUtil.getAccid(), null);
                        EventBus.getDefault().post(msg_event); //更新记录列表

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000 * 3);
                                    myHandler.sendEmptyMessage(DISMISS);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                    }
                    break;

                case DISMISS:
                    dismiss();
                    break;
            }
        }
    };

    private long current_time = 0;
    private long last_time = 0;
    private boolean isTimeOut = false;
    private boolean hasClick = false;
    private Runnable time_out_runnable = new Runnable() {
        @Override
        public void run() {
            while (!hasClick && !isTimeOut) {
                System.out.println("start time is " + (new Date()));
                try {
                    Thread.sleep(1000 * 60);
                    System.out.println("end time is " + (new Date()));
                    if (!hasClick)
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


    private void addTeamToDb() {
        TeamDbBean no_Bean = new TeamDbBean();
        //no_Bean.setIs_team(1);
        no_Bean.setMy_account(UserInfoUtil.getAccid());
        no_Bean.setRecord_time(TimeUtil.getNowTime());
        no_Bean.setTeamId(teamId);

        //no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

        System.out.println("teamId=" + teamId);//获取teamId
        TeamDBUtil.add(mContext, no_Bean);
    }

    private void updateTeamToDb() {
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
            TeamDbBean no_Bean = new TeamDbBean();
            no_Bean.setId(teamDbBeanList.get(0).getId());
            // no_Bean.setIs_team(teamDbBeanList.get(0).getIs_team());
            no_Bean.setMy_account(UserInfoUtil.getAccid());

            no_Bean.setRecord_time(TimeUtil.getNowTime());

            no_Bean.setTeamId(teamDbBeanList.get(0).getTeamId());

            no_Bean.setTeam_name(teamDbBeanList.get(0).getTeam_name());

            System.out.println("update, id=" + teamDbBeanList.get(0).getId() + "teamId=" + teamId);//获取teamId
            TeamDBUtil.update(mContext, no_Bean);
        } else {
            addTeamToDb();
        }

/*        DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_OUT, UserInfoUtil.getAccid(), null);
        EventBus.getDefault().post(msg_event); //更新记录列表*/
    }

    private void TimeOut() {
        new Thread(time_out_runnable).start();
    }
}
