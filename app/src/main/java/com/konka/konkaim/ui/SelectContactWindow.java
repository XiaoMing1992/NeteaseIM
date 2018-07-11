package com.konka.konkaim.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.adapter.SelectContactItemAdapter;
import com.konka.konkaim.bean.ContactBean;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.TeamBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.activity.TeamChatActivity;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.chat.team.TeamCreateHelper;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.TimeUtil;
import com.konka.konkaim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.konka.konkaim.R;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by HP on 2018-5-10.
 */

public class SelectContactWindow extends PopupWindow implements View.OnClickListener {
    private final String TAG = "SelectContactWindow";
    private Context mContext;
    private View mView;
    private Button btn_chat;
    private TextView show_chat_number;
    private ArrayList<String> accounts;
    private List<NimUserInfo> contactBeens;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView contact_recyclerView;
    private SelectContactItemAdapter adapter;

    public SelectContactWindow(Context context, Map<String, String> onlineStateMap) {
        super(context);
        this.onlineStateMap = onlineStateMap;
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.select_contact, null);
        setContentView(mView);
        btn_chat = (Button) mView.findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(this);
        contact_recyclerView = (RecyclerView) mView.findViewById(R.id.select_contact_recyclerView);
        show_chat_number = (TextView) mView.findViewById(R.id.show_chat_number);
    }

    private void initSetting() {
        Utils.computeScreenSize(mContext);
        LogUtil.LogD(TAG, "ScreenWidth = " + Utils.getScreenWidth() + " ScreenHeight = " + Utils.getScreenHeight());
        setWidth(Utils.getScreenWidth());
        setHeight(Utils.getScreenHeight());
        setFocusable(true);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));
    }

    private void initData() {
        linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        contact_recyclerView.setLayoutManager(linearLayoutManager);
        contactBeens = new ArrayList<>();
        //demo();
        getData();//获取所有好友
        adapter = new SelectContactItemAdapter(mContext, contactBeens, onlineStateMap);
        adapter.setOnItemClickListener(onItemClickListener);
//        adapter.setOnlineStateMap(onlineStateMap);
        adapter.setOnSelectItemListener(onSelectItemListener); //监听选择联系人过程

        contact_recyclerView.addItemDecoration(new RecycleViewDivider(mContext, LinearLayout.HORIZONTAL,
                0, (int) mContext.getResources().getDimension(R.dimen.team_chat_select_contact_item_dividerWidth)));

        contact_recyclerView.setAdapter(adapter);

        show_chat_number.setText("已邀请" + 0 + "人通话");
    }

    private void getData() {
        accounts = new ArrayList<>();

        System.out.println("--- 所有好友帐号信息 ---");
        List<String> temp = NIMClient.getService(FriendService.class).getFriendAccounts(); // 获取所有好友帐号
        for (int i = 0; temp != null && i < temp.size(); i++) {
            System.out.println("-----" + temp.get(i));
            accounts.add(temp.get(i));
/*            if (onlineStateMap.containsKey(temp.get(i))) {


                String state = onlineStateMap.get(temp.get(i));
                if (state.equals("1"))
                    accounts.add(temp.get(i));
                else {
                    System.out.println("-----" + temp.get(i) + " 不在线");
                }
            }*/
        }
        System.out.println("--- 所有好友用户资料信息 ---");
        contactBeens = NIMClient.getService(UserService.class).getUserInfoList(accounts); // 获取所有好友用户资料
        for (int i = 0; contactBeens != null && i < contactBeens.size(); i++) {
            System.out.println("mobile=" + contactBeens.get(i).getMobile() + ", name=" + contactBeens.get(i).getName());
        }
    }

    private List<NimUserInfo> selectItems;
    private ArrayList<String> selectedAccounts = new ArrayList<>();

    private SelectContactItemAdapter.OnSelectItemListener onSelectItemListener
            = new SelectContactItemAdapter.OnSelectItemListener() {
        @Override
        public void OnSelectItem(List<NimUserInfo> selectItemList) {
            if (selectItemList != null) {
                if (selectItemList.size() > 4) {
                    showToast(mContext, "人数已达上限");
                } else {
                    selectItems = selectItemList;
                    show_chat_number.setText("已邀请" + selectItemList.size() + "人通话");
                }
            }
        }
    };

    private SelectContactItemAdapter.OnItemClickListener onItemClickListener = new SelectContactItemAdapter.OnItemClickListener() {
        @Override
        public void OnItemClick(int position) {
            System.out.println("click position is " + position);
        }
    };

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    public void show() {
        showAtLocation(mView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private long team_last_time = 0;
    private long team_current_time = System.currentTimeMillis();
    private boolean hasShow = false;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_chat: //发起通话
                team_current_time = System.currentTimeMillis();
                if (team_current_time - team_last_time < 1000*5){
                    if (!hasShow) {
                        hasShow = true;
                        showToast(mContext, "点击太频繁，稍后再试");
                    }
                    return;
                }else {
                    hasShow = false;
                    team_last_time = team_current_time;
                }

                if ((selectItems != null && selectItems.size()==0) || selectItems==null) {
                    showToast(mContext, "人数不能为空");
                    return;
                }

                visit(selectItems);
                System.out.println("click btn_chat");
                if (selectItems != null && selectItems.size() > 4) {
                    showToast(mContext, "人数已超上限");
                    return;
                }

                //dismiss();
                //发起多人通话聊天
                //...
                //AVChatKit.outgoingTeamCall(mContext, false, , ,accounts,"11111");
                selectedAccounts.clear();
                for (int i = 0; selectItems != null && i < selectItems.size(); i++) {
                    selectedAccounts.add(selectItems.get(i).getAccount());
                }
                final ArrayList<String> temp = new ArrayList<>();
                temp.clear();
                temp.add(UserInfoUtil.getAccid());
                temp.addAll(selectedAccounts);
                compareTeam(temp);


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (!hasLoadOK) {
/*                            try {
                                //Thread.sleep(1000);
                                System.out.println("total="+total+", num="+num);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        }
                        hasLoadOK = false;

                        while (num < total) {

                        }
                        num = 0;
                        total = 0;

                        System.out.println("go-->");

                        System.out.println("------------------------------");
                        for (int j = 0; j < temp.size(); j++) {
                            System.out.println("accounts.get(j)=" + temp.get(j));
                        }
                        System.out.println("------------------------------");

                        boolean hasCreateTeam = false;
                        int count = 0;
                        for (Map.Entry<String, List<TeamMember>> entry : map.entrySet()) {
                            System.out.println("Key = " + entry.getKey());
                            count = 0;
                            for (int i = 0; entry.getValue() != null && i < entry.getValue().size(); i++) {
                                System.out.println("entry.getValue().get(i).getAccount()=" + entry.getValue().get(i).getAccount());
                                for (int j = 0; j < temp.size(); j++) {
                                    if (temp.get(j).equals(entry.getValue().get(i).getAccount())) {
                                        count++;
                                        break;
                                    }
                                }
                            }
                            System.out.println("count=" + count);
                            if (count == temp.size()) {
                                System.out.println("-------------已经存在 count=" + count+", teamId="+entry.getKey());
                                toChat(temp, entry.getKey());
                                return;
                            }
                        }
                        System.out.println("-- count=" + count);
                        if (count != temp.size()/* && !hasCreateTeam*/) {
                            //if (entry.getValue() != null && count < entry.getValue().size() && !hasCreateTeam) {
                            System.out.println("------------- go to create team");

                            //hasCreateTeam = true;

                            createRoom("kkim" + System.currentTimeMillis(), "康佳音视频", selectedAccounts);
                        }

                        //if (hasCreateTeam) break;

                        //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

                    }
                }).start();

                break;
        }
    }

    //private int
    private void visit(List<NimUserInfo> selectItemList) {
        for (int i = 0; selectItemList != null && i < selectItemList.size(); i++) {
            System.out.println(selectItemList.get(i).getAccount());
        }
    }

    public void createRoom(final String roomName, final String extraMessage, final ArrayList<String> mAccounts) {

        AVChatManager.getInstance().createRoom(roomName, extraMessage, new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                System.out.println("createRoom onSuccess");

                TeamCreateHelper.createNormalTeam(mContext, roomName, mAccounts, false, null);

            }

            @Override
            public void onFailed(int code) {
                System.out.println("createRoom onFailed code" + code);
            }

            @Override
            public void onException(Throwable exception) {
                System.out.println("createRoom onException ");
                exception.printStackTrace();
            }
        });
    }


    private Map<String, String> onlineStateMap;

/*    public void setOnlineStateMap(Map<String, String> onlineStateMap) {
        this.onlineStateMap = onlineStateMap;
    }*/


    public void compareTeam(final List<String> accounts) {
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> teams) {
                System.out.println("--> queryTeamList onSuccess");
                // 获取成功，teams为加入的所有群组

                for (int i = 0; accounts != null && teams != null && i < teams.size(); i++) {
/*                    System.out.println("--> queryTeamList -->teamId=" + teams.get(i).getId()
                            + ", memberCount=" + teams.get(i).getMemberCount()
                            + ", BeInviteMode=" + teams.get(i).getTeamBeInviteMode()
                            + ", InviteMode=" + teams.get(i).getTeamInviteMode()
                            + ", teamName=" + teams.get(i).getName()
                            + ", CreateTime=" + TimeUtil.getNowTime(teams.get(i).getCreateTime())
                            + ", Creator account=" + teams.get(i).getCreator());*/

                    if (accounts.size() == teams.get(i).getMemberCount()) {
                        total++;
                    }
                }
                System.out.println("total=" + total);
                hasLoadOK = true;

                for (int i = 0; accounts != null && teams != null && i < teams.size(); i++) {
                    if (accounts.size() == teams.get(i).getMemberCount()) {
                        getTeamMember(teams.get(i).getId());
                    }
                }

            }

            @Override
            public void onFailed(int i) {
                // 获取失败，具体错误码见i参数
                System.out.println("--> queryTeamList onFailed: " + i);
            }

            @Override
            public void onException(Throwable throwable) {
                // 获取异常
                throwable.printStackTrace();
                System.out.println("--> queryTeamList onException");
            }
        });
    }

    private Map<String, List<TeamMember>> map = new HashMap<String, List<TeamMember>>();
    private boolean hasLoadOK = false;
    private int num = 0;
    private int total = 0;

    public void getTeamMember(final String teamId) {
/*        boolean hasCreateTeam = false;
        List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(mContext, UserInfoUtil.getAccid(), teamId);
        for (int i = 0; teamDbBeanList != null && i < teamDbBeanList.size(); i++) {
            if (!accounts.get(i).equals(teamDbBeanList.get(i).)){
        }
        return hasCreateTeam;*/

        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {

                System.out.println("queryMemberList, code=" + code + ", members.size=" + (members != null ? members.size() : -1));
/*                if (code == ResponseCode.RES_SUCCESS && members != null) {
                    for (int i = 0; i < members.size(); i++) {
                        System.out.println("queryMemberList, account=" + members.get(i).getAccount());
                    }
                }*/
                map.put(teamId, members);
                num++;
            }
        });
    }


    private void toChat(final ArrayList<String>_accounts, final String _teamId){
        final String roomName = "kkim" + System.currentTimeMillis();
        AVChatManager.getInstance().createRoom(roomName, null, new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                System.out.println("createRoom onSuccess, teamId=" + _teamId);
                MainApplication.finishedMap.put(_teamId, false);
                MainApplication.myRoomNameMap.put(_teamId, roomName);

                //写进数据库
                updateTeamToDb(mContext, _teamId, TimeUtil.getNowTime());

                sendMsgToTeam(_teamId, roomName, null, TeamConstant.ACTION_TEAM_CHAT_INVITE);

                //发起多人聊天
                TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                AVChatKit.outgoingTeamCall(mContext, false, _teamId, roomName, _accounts, _teamId);

            }

            @Override
            public void onFailed(int code) {
                System.out.println("createRoom onFailed code" + code);
                showToast(mContext, "创建房间失败，请稍后重试");
            }

            @Override
            public void onException(Throwable exception) {
                System.out.println("createRoom onException ");
                exception.printStackTrace();
                showToast(mContext, "创建房间失败，请稍后重试");
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
        //roomMap.put("members", accounts);
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

    private synchronized void updateTeamToDb(final Context context, final String teamId, final String timeStr) {
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

/*                DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_OUT, UserInfoUtil.getAccid(), null);
                EventBus.getDefault().post(msg_event); //更新记录列表*/

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
