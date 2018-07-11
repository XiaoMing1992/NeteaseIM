package com.konka.konkaim.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.adapter.ChatRecordAdapter;
import com.konka.konkaim.adapter.ContactAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.konka.konkaim.R;

import com.konka.konkaim.adapter.ManyChatAdapter;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.bean.DbBean;
import com.konka.konkaim.bean.FriendBean;
import com.konka.konkaim.bean.TeamBean;
import com.konka.konkaim.bean.TeamDbBean;
import com.konka.konkaim.chat.AVChatKit;
import com.konka.konkaim.chat.AVChatProfile;
import com.konka.konkaim.chat.PhoneCallStateObserver;
import com.konka.konkaim.chat.activity.AddContactActivity;
import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.chat.team.TeamConstant;
import com.konka.konkaim.db.DBUtil;
import com.konka.konkaim.db.FriendDBUtil;
import com.konka.konkaim.db.TeamDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.ui.DeleteManyChatWindow;
import com.konka.konkaim.ui.ExitWindow;
import com.konka.konkaim.ui.KickoutWindow;
import com.konka.konkaim.ui.RecycleViewDivider;
import com.konka.konkaim.ui.SelectContactWindow;
import com.konka.konkaim.ui.TeamChatReceiveWindow;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.TimeUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatControlCommand;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.EventSubscribeServiceObserver;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest;
import com.netease.nimlib.sdk.event.model.NimOnlineStateEvent;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HomeActivity extends AppCompatActivity {
    private final String TAG = "HomeActivity";

    //此页面是主页面的控件
    private TextView tv_user_state;
    private TextView tv_nick_name;
    private CircleImageView user_logo;
    private ImageView add_icon;
    private ImageView add_request_tip;
    private LinearLayout empty;
    private Button btn_add;
    private RelativeLayout layout_contact;
    private RecyclerView contact_recyclerView;
    private RecyclerView many_chat_recyclerView;
    private ManyChatAdapter manyChatAdapter;
    private LinearLayoutManager manyLinearLayoutManager;

    private LinearLayout layout_toMe;

    //多人通话
    private LinearLayout many_chat_layout;
    private RelativeLayout top_many_chat_layout;

    //private ContactAdapter contactAdapter;
    //private RecentAdapter recentAdapter;
    private ChatRecordAdapter chatRecordAdapter;

    //private List<ContactBean> contactBeens;
    private LinearLayoutManager linearLayoutManager;
//    private boolean isFirstFlag = false;

    private List<String> accounts;
    private List<NimUserInfo> users;
    private List<DbBean> dbBeanList = new ArrayList<>();
    private List<TeamDbBean> teamDbBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.getInstance().addActivity(this);
        setContentView(R.layout.activity_home);

        EventBus.getDefault().register(this);//订阅
        //beKickOutObserve(true);

        System.out.println("onCreate");

        initView();
        initData();

        requestAllPower();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
        System.out.println("onStart:  " + PrefenceUtil.CURRENT_USER_STATE_FILENAME);
        System.out.println("onStart:  " + UserInfoUtil.getAccid());

        String my_state = PrefenceUtil.get(HomeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME + UserInfoUtil.getAccid(), PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY);
        if (my_state == null) {
            my_state = "可通话";
            PrefenceUtil.set(HomeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME + UserInfoUtil.getAccid(), PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY, my_state);
        }
        tv_user_state.setText(my_state);

/*        if (my_state.equals("忙碌")) {
            tv_user_state.setBackgroundResource(R.drawable.user_state_bg);
        } else {
            //tv_user_state.setBackground(null);
            tv_user_state.setBackgroundColor(getResources().getColor(R.color.transparent));
        }*/

        tv_nick_name.setText(NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()) == null ?
                "" : NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName()); //显示当前用户的昵称
    }

    private void initView() {

        //此页面是主页面的控件
        tv_user_state = (TextView) findViewById(R.id.tv_user_state);
        tv_nick_name = (TextView) findViewById(R.id.tv_nick_name);
        user_logo = (CircleImageView) findViewById(R.id.user_logo);
        add_icon = (ImageView) findViewById(R.id.add_icon);
        add_request_tip = (ImageView) findViewById(R.id.add_request_tip);
        btn_add = (Button) findViewById(R.id.btn_add);
        empty = (LinearLayout) findViewById(R.id.empty);
        layout_contact = (RelativeLayout) findViewById(R.id.layout_contact);
        contact_recyclerView = (RecyclerView) findViewById(R.id.contact_recyclerView);
        many_chat_recyclerView = (RecyclerView) findViewById(R.id.many_chat_recyclerView);
        layout_toMe = (LinearLayout) findViewById(R.id.layout_toMe);
        many_chat_layout = (LinearLayout) findViewById(R.id.many_chat_layout);
        top_many_chat_layout = (RelativeLayout) findViewById(R.id.top_many_chat_layout);

        layout_toMe.requestFocus();
        listener();

    }

    private void listener() {
        //添加联系人
        add_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, AddContactActivity.class);
                if (add_request_tip.getVisibility() == View.VISIBLE) {
                    intent.putExtra("show_layout_invite", true);
                    Bundle bundle = new Bundle();
                    ArrayList<String> temp1 = new ArrayList<String>();
                    temp1.addAll(accountsToAddContact);
                    ArrayList<String> temp2 = new ArrayList<String>();
                    temp2.addAll(accountsToAddContactTags);
                    bundle.putStringArrayList("accountsToAddContact", temp1);
                    bundle.putStringArrayList("accountsToAddContactTags", temp2);
                    intent.putExtra("friend_account", bundle);
                    add_request_tip.setVisibility(View.GONE);

                    System.out.println("accountsToAddContact.size=" + accountsToAddContact.size());
                }
                accountsToAddContact.clear();
                accountsToAddContactTags.clear();

                startActivity(intent);
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, AddContactActivity.class);
                if (add_request_tip.getVisibility() == View.VISIBLE) {
                    intent.putExtra("show_layout_invite", true);
                    Bundle bundle = new Bundle();
                    ArrayList<String> temp1 = new ArrayList<String>();
                    temp1.addAll(accountsToAddContact);
                    ArrayList<String> temp2 = new ArrayList<String>();
                    temp2.addAll(accountsToAddContactTags);
                    bundle.putStringArrayList("accountsToAddContact", temp1);
                    bundle.putStringArrayList("accountsToAddContactTags", temp2);
                    intent.putExtra("friend_account", bundle);
                    add_request_tip.setVisibility(View.GONE);
                }
                accountsToAddContact.clear();
                accountsToAddContactTags.clear();

                startActivity(intent);

            }
        });

        layout_toMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, MeActivity.class);
                startActivity(intent);
            }
        });

        many_chat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("--->many_chat_layout, to select contact");
                SelectContactWindow contactWindow = new SelectContactWindow(HomeActivity.this, onlineStateMap);
                //contactWindow.setOnlineStateMap(getOnlineStateMap());
                contactWindow.show();

            }
        });

        top_many_chat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("--->top_many_chat_layout, to select contact");
                SelectContactWindow contactWindow = new SelectContactWindow(HomeActivity.this, onlineStateMap);
                //contactWindow.setOnlineStateMap(getOnlineStateMap());

                contactWindow.show();
            }
        });
    }

    private void initData() {
        linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        contact_recyclerView.setLayoutManager(linearLayoutManager);

        getData();

        //contactAdapter = new ContactAdapter(HomeActivity.this, users);
        //contactAdapter.setOnItemClickListener(onItemClickListener);

        chatRecordAdapter = new ChatRecordAdapter(HomeActivity.this, dbBeanList);
        chatRecordAdapter.setOnItemClickListener(onItemClickListener);
        chatRecordAdapter.setmLayoutMgr(linearLayoutManager);
        chatRecordAdapter.setRecyclerView(contact_recyclerView);

        //recentAdapter = new RecentAdapter(HomeActivity.this, myRecents);
        //recentAdapter.setOnItemClickListener(onItemClickListener);

        contact_recyclerView.addItemDecoration(new RecycleViewDivider(HomeActivity.this, LinearLayout.HORIZONTAL,
                0, (int) this.getResources().getDimension(R.dimen.contact_item_dividerWidth)));

        contact_recyclerView.setAdapter(chatRecordAdapter);


        //contact_recyclerView.setAdapter(contactAdapter);
        //contact_recyclerView.setAdapter(recentAdapter);

        reisterObserver(true); //监听添加好友请求通知

        registerFriendOnlineState(accounts); //订阅在线状态事件
        listenEvent(true); //监听

        //chatRecordAdapter.setOnlineStateMap(getOnlineStateMap());
        //contactAdapter.setOnlineStateMap(getOnlineStateMap());
        //recentAdapter.setOnlineStateMap(getOnlineStateMap());

        loadTeams();
        //getRecentContacts();

        registerRecentContactObserver(true); //监听最近会话变更

//        registerMsgReceiveObserver();

    }

/*    private long friend_last_time = 0;
    private long friend_current_time = System.currentTimeMillis();*/

    private ChatRecordAdapter.OnItemClickListener onItemClickListener = new ChatRecordAdapter.OnItemClickListener() {
        @Override
        public void OnItemClick(int position) {
/*            friend_current_time = System.currentTimeMillis();
            if (friend_current_time - friend_last_time < 1000 * 10) {
                friend_last_time = friend_current_time;
                //showToast(HomeActivity.this, "点击太频繁，稍后再试");
                return;
            } else {
                friend_last_time = friend_current_time;
            }*/

            System.out.println("--> OnItemClick position=" + position + ", friendAccount=" + dbBeanList.get(position).getFriend_account());
            //String aliasName = NIMSDK.getFriendService().getFriendByAccount(users.get(position).getAccount()).getAlias();
            updateToDb(dbBeanList.get(position).getFriend_account(), 1);

            Intent intent = new Intent();
            intent.setClass(HomeActivity.this, OneToOneActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("account", dbBeanList.get(position).getFriend_account());
            //bundle.putInt("", OneToOneActivity.FROM_INTERNAL); //来自发起方
            //bundle.putString("avatar", users.get(position).getAvatar());
            //bundle.putString("remark", !TextUtils.isEmpty(aliasName) ? aliasName : users.get(position).getName());
            intent.putExtra("chat_data", bundle);
            intent.putExtra(OneToOneActivity.KEY_IN_CALLING, false);
            intent.putExtra(OneToOneActivity.KEY_SOURCE, OneToOneActivity.FROM_INTERNAL);
            startActivity(intent);

        }
    };


    private void getData() {

//        tv_nick_name.setText(NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName()); //显示当前用户的昵称
        HttpHelper.downloadPicture(HomeActivity.this,
                NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()) == null ?
                        null : NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, user_logo); //加载头像

        System.out.println("--- 所有好友帐号信息 ---");
        accounts = NIMClient.getService(FriendService.class).getFriendAccounts(); // 获取所有好友帐号
        for (int i = 0; accounts != null && i < accounts.size(); i++) {
            System.out.println(accounts.get(i));
            //updateToDb(accounts.get(i));
        }
        System.out.println("--- 所有好友用户资料信息 ---");
        users = NIMClient.getService(UserService.class).getUserInfoList(accounts); // 获取所有好友用户资料
        for (int i = 0; users != null && i < users.size(); i++) {
            System.out.println("mobile=" + users.get(i).getMobile() + ", name=" + users.get(i).getName());

        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                dbBeanList.clear();
                dbBeanList.addAll(getRecentRecord());
                myHandler.sendEmptyMessage(GET_CHAT_RECORD);
            }
        }).start();

    }

    public static String FRIEND_REJECT_ME_TAG = "friendRejectMe";
    public static String FRIEND_ACCENT_ME_TAG = "friendAccentMe";
    public static String FRIEND_ADD_ME_TAG = "friendAddMe";
    private ArrayList<String> accountsToAddContact = new ArrayList<>();
    private ArrayList<String> accountsToAddContactTags = new ArrayList<>();

    Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage systemMessage) {
            if (systemMessage.getType() == SystemMessageType.AddFriend) {
                AddFriendNotify attachData = (AddFriendNotify) systemMessage.getAttachObject();
                if (attachData != null) {
                    // 针对不同的事件做处理
                    if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_DIRECT) {
                        // 对方直接添加你为好友
                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_AGREE_ADD_FRIEND) {
                        // 对方通过了你的好友验证请求
                        accounts.add(systemMessage.getFromAccount());
                        registerFriendOnlineState(accounts); //订阅在线状态事件

                        add_request_tip.setVisibility(View.VISIBLE);

                        for (int i = 0; accountsToAddContact != null && i < accountsToAddContact.size(); i++) {
                            if (accountsToAddContact.get(i).equals(systemMessage.getFromAccount())) {
                                System.out.println("delete i=" + i);
                                accountsToAddContact.remove(i);
                                accountsToAddContactTags.remove(i);
                                i--;
                            }
                        }

                        accountsToAddContact.add(systemMessage.getFromAccount());
                        accountsToAddContactTags.add(FRIEND_ACCENT_ME_TAG);

                        System.out.println("通过方帐号是:" + systemMessage.getFromAccount());

                        //addToDb(systemMessage.getFromAccount());
                        updateToDb(systemMessage.getFromAccount(), 2);

                        DataSynEvent event = new DataSynEvent(DataSynEvent.TYPE_ADD_FRIEND, UserInfoUtil.getAccid(), null);
                        EventBus.getDefault().post(event); //更新adapter

                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_REJECT_ADD_FRIEND) {
                        // 对方拒绝了你的好友验证请求
                        add_request_tip.setVisibility(View.VISIBLE);
/*                        for (int i=0;accountsToAddContact != null && i<accountsToAddContact.size();i++){
                            if (accountsToAddContact.get(i).equals(systemMessage.getFromAccount())){
                                System.out.println("delete i="+i);
                                accountsToAddContact.remove(i);
                                accountsToAddContactTags.remove(i);
                                i--;
                            }
                        }*/
                        if (!accountsToAddContact.contains(systemMessage.getFromAccount())) {
                            accountsToAddContact.add(systemMessage.getFromAccount());
                            accountsToAddContactTags.add(FRIEND_REJECT_ME_TAG);
                        }
                        System.out.println("拒绝方帐号是:" + systemMessage.getFromAccount() + ", accountsToAddContact.size=" + accountsToAddContact.size());
                    } else if (attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        // 对方请求添加好友，一般场景会让用户选择同意或拒绝对方的好友请求。
                        // 通过message.getContent()获取好友验证请求的附言
                        System.out.println("发起方帐号是:" + systemMessage.getFromAccount());
                        add_request_tip.setVisibility(View.VISIBLE);
                        //accountsToAddContact.add(systemMessage.getFromAccount());
                        //accountsToAddContactTags.add(FRIEND_ADD_ME_TAG);

                        List<FriendBean> friendBeanList = FriendDBUtil.queryOnlyFriend(HomeActivity.this, UserInfoUtil.getAccid(), systemMessage.getFromAccount());
                        if (friendBeanList != null && friendBeanList.size() > 0) {
                            System.out.println("发起方帐号是:" + systemMessage.getFromAccount() + "已经在数据库里面");
                            return;
                        }
                        FriendBean friendBean = new FriendBean();
                        friendBean.setMy_account(UserInfoUtil.getAccid());
                        friendBean.setFriend_account(systemMessage.getFromAccount());
                        friendBean.setRecord_time(TimeUtil.getNowTime());
                        FriendDBUtil.add(HomeActivity.this, friendBean);
                    }
                }
            }
        }
    };

    private synchronized void loadFriendToDb(String friendAccount, int is_out) {
        DbBean friend_Bean = new DbBean();
        //friend_Bean.setIs_team(0);
        friend_Bean.setMy_account(UserInfoUtil.getAccid());
        friend_Bean.setFriend_account(friendAccount);
        friend_Bean.setIs_friend(1);
        friend_Bean.setChat_from(friendAccount);
        friend_Bean.setChat_to(friendAccount);
        if (is_out == 2)
            friend_Bean.setIs_connect(-1);
        else friend_Bean.setIs_connect(0);

        friend_Bean.setIs_out(is_out);
        DBUtil.add(HomeActivity.this, friend_Bean);

        dbBeanList.add(friend_Bean);
    }

    private synchronized void addToDb(String friendAccount, int is_out) {
        DbBean friend_Bean = new DbBean();
        //friend_Bean.setIs_team(0);
        friend_Bean.setMy_account(UserInfoUtil.getAccid());
        friend_Bean.setFriend_account(friendAccount);
        friend_Bean.setIs_friend(1);
        friend_Bean.setChat_from(friendAccount);
        friend_Bean.setChat_to(friendAccount);
        if (is_out == 2)
            friend_Bean.setIs_connect(-1);
        else friend_Bean.setIs_connect(0);

        friend_Bean.setIs_out(is_out);
        DBUtil.add(HomeActivity.this, friend_Bean);
    }

    private synchronized void updateToDb(final String friendAccount, final int is_out) {
/*        new Thread(new Runnable() {
            @Override
            public void run() {*/
        System.out.println("updateToDb, friendAccount=" + friendAccount + ", time is " + TimeUtil.getNowTime());//获取teamId

        List<DbBean> dbBeanList = DBUtil.queryForFriend(HomeActivity.this, UserInfoUtil.getAccid(), friendAccount);
        for (int i = 0; i < dbBeanList.size(); i++) {
            System.out.println("updateToDb, friendAccount=" + dbBeanList.get(i).getFriend_account() + ", id=" + dbBeanList.get(i).getId());//获取teamId
        }
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
            if (is_out == 2)
                friend_Bean.setIs_connect(-1);
            else friend_Bean.setIs_connect(0);

            friend_Bean.setIs_out(is_out);

            System.out.println("update, id=" + dbBeanList.get(0).getId() + " friendAccount=" + friendAccount);//获取teamId
            DBUtil.update(HomeActivity.this, friend_Bean);
        } else {
            addToDb(friendAccount, is_out);
        }


/*            }
        }).start();*/
    }

    private void reisterObserver(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, register);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");

        reisterObserver(false);
        unRegisterFriendOnlineState();
        listenEvent(false);
        unRegisterRecentContactObserver(false);

//        unregisterMsgReceiveObserver();

        EventBus.getDefault().unregister(this);//解除订阅

        //beKickOutObserve(false);
    }

    private EventSubscribeRequest eventSubscribeRequest;

    private void registerFriendOnlineState(List<String> mAccounts) {
        eventSubscribeRequest = new EventSubscribeRequest();
        eventSubscribeRequest.setPublishers(mAccounts);
        eventSubscribeRequest.setEventType(1); //设置订阅在线状态事件
        eventSubscribeRequest.setExpiry(60 * 60 * 24 * 7);  //订阅的有效期，范围为 60s 到 30days，数值单位为秒
        eventSubscribeRequest.setSyncCurrentValue(true);    //订阅后是否立刻同步事件状态值，默认为 false

        NIMClient.getService(EventSubscribeService.class).subscribeEvent(eventSubscribeRequest).setCallback(new RequestCallbackWrapper<List<String>>() {
            @Override
            public void onResult(int code, List<String> result, Throwable exception) {

                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null) {
                        // 部分订阅失败的账号。。。
                        for (int i = 0; i < result.size(); i++) {
                            System.out.println("---> 部分订阅失败的账号:" + result.get(i));
                        }
                    }
                } else {

                }
            }
        });
    }


    private void unRegisterFriendOnlineState() {
        if (eventSubscribeRequest != null)
            NIMClient.getService(EventSubscribeService.class).unSubscribeEvent(eventSubscribeRequest);
    }

    public static Map<String, String> onlineStateMap = new HashMap<>();

    public static Map<String, String> getOnlineStateMap() {
//        for (int i = 0; i < eventSubscribeRequest.getPublishers().size(); i++) {
//            System.out.println(eventSubscribeRequest.getEventType());
//            onlineStateMap.put(eventSubscribeRequest.getPublishers().get(i), "" + eventSubscribeRequest.getEventType());
//        }
        return onlineStateMap;
    }

    public void listenEvent(boolean register) {
        NIMClient.getService(EventSubscribeServiceObserver.class).observeEventChanged(new Observer<List<Event>>() {
            @Override
            public void onEvent(List<Event> events) {
                // 处理
                for (int i = 0; events != null && i < events.size(); i++) {
                    System.out.println("event account is " + events.get(i).getPublisherAccount() + " eventType = " + events.get(i).getEventValue());
                    onlineStateMap.put(events.get(i).getPublisherAccount(), "" + events.get(i).getEventValue());
                }
                //chatRecordAdapter.setOnlineStateMap(getOnlineStateMap()); //收到联系人账号在线状态发布事件
                chatRecordAdapter.notifyDataSetChanged();
            }
        }, register);
    }

    private void loadTeams() {
        manyLinearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        manyLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        many_chat_recyclerView.setLayoutManager(manyLinearLayoutManager);

        many_chat_recyclerView.addItemDecoration(new RecycleViewDivider(HomeActivity.this, LinearLayout.HORIZONTAL,
                0, (int) this.getResources().getDimension(R.dimen.many_contact_item_dividerWidth)));

        manyChatAdapter = new ManyChatAdapter(HomeActivity.this, teamBeanList);
        manyChatAdapter.setOnItemClickListener(manyChatItemClickListener);
        //manyChatAdapter.setOnQuitListener(onQuitListener);
        manyChatAdapter.setmLayoutMgr(manyLinearLayoutManager);
        manyChatAdapter.setRecyclerView(many_chat_recyclerView);
        many_chat_recyclerView.setAdapter(manyChatAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                teamDbBeanList = getTeamRecord();//获取数据库的记录
                myHandler.sendEmptyMessage(GET_TEAM);
            }
        }).start();

    }

    private long team_last_time = 0;
    private long team_current_time = System.currentTimeMillis();
    private boolean hasShow = false;
    private ManyChatAdapter.OnItemClickListener manyChatItemClickListener = new ManyChatAdapter.OnItemClickListener() {
        @Override
        public void OnItemClick(final int position) {
            team_current_time = System.currentTimeMillis();
            if (team_current_time - team_last_time < 1000 * 5) {
                if (!hasShow) {
                    hasShow = true;
                    showToast(HomeActivity.this, "点击太频繁，稍后再试");
                }
                return;
            } else {
                hasShow = false;
                team_last_time = team_current_time;
            }

            System.out.println("-->manyChatItemClickListener position=" + position);
            final ArrayList<String> selectedAccounts = new ArrayList<>();
            for (int i = 0; teamBeanList.get(position).getMemberList() != null && i < teamBeanList.get(position).getMemberList().size(); i++) {
                selectedAccounts.add(teamBeanList.get(position).getMemberList().get(i).getAccount());
            }


            if (MainApplication.finishedMap.containsKey(teamBeanList.get(position).getTeamId())){

                boolean finish = MainApplication.finishedMap.get(teamBeanList.get(position).getTeamId());
                System.out.println("join old room, finish=" + finish+", teamId="+teamBeanList.get(position).getTeamId());

                if (finish){

                }else {
                    if (MainApplication.myRoomNameMap.containsKey(teamBeanList.get(position).getTeamId())){
                        String roomName = MainApplication.myRoomNameMap.get(teamBeanList.get(position).getTeamId());
                        System.out.println("join old room, roomName=" + roomName+", teamId="+teamBeanList.get(position).getTeamId());

                        if (!TextUtils.isEmpty(roomName)){
                            System.out.println("join old room, teamId=" + teamBeanList.get(position).getTeamId()+", room name is "+roomName);
                            //写进数据库
                            updateTeamToDb(HomeActivity.this, teamBeanList.get(position).getTeamId(), TimeUtil.getNowTime());
                            //sendMsgToTeam(teamBeanList.get(position).getTeamId(), MainApplication.myRoomName, null, TeamConstant.ACTION_TEAM_CHAT_INVITE);
                            //发起多人聊天
                            TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                            AVChatKit.outgoingTeamCall(HomeActivity.this, false, teamBeanList.get(position).getTeamId(), roomName, selectedAccounts, teamBeanList.get(position).getTeamId());
                            return;
                        }
                    }
                }
            }

            //final String roomName = teamBeanList.get(position).getTeamId();
            final String roomName = "kkim" + System.currentTimeMillis();

            AVChatManager.getInstance().createRoom(roomName, null, new AVChatCallback<AVChatChannelInfo>() {
                @Override
                public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                    System.out.println("createRoom onSuccess, teamId=" + teamBeanList.get(position).getTeamId());

                    //MainApplication.myRoomName = roomName;
                    //MainApplication.finished = false;
                    //MainApplication.myTeamId = teamBeanList.get(position).getTeamId();
                    MainApplication.finishedMap.put(teamBeanList.get(position).getTeamId(), false);
                    MainApplication.myRoomNameMap.put(teamBeanList.get(position).getTeamId(), roomName);

                    //写进数据库
                    updateTeamToDb(HomeActivity.this, teamBeanList.get(position).getTeamId(), TimeUtil.getNowTime());

                    sendMsgToTeam(teamBeanList.get(position).getTeamId(), roomName, null, TeamConstant.ACTION_TEAM_CHAT_INVITE);

                    //发起多人聊天
                    TeamAVChatProfile.sharedInstance().setTeamAVChatting(true);
                    AVChatKit.outgoingTeamCall(HomeActivity.this, false, teamBeanList.get(position).getTeamId(), roomName, selectedAccounts, teamBeanList.get(position).getTeamId());

                }

                @Override
                public void onFailed(int code) {
                    System.out.println("createRoom onFailed code" + code);
                    showToast(HomeActivity.this, "创建房间失败，请稍后重试");
                }

                @Override
                public void onException(Throwable exception) {
                    System.out.println("createRoom onException ");
                    exception.printStackTrace();
                    showToast(HomeActivity.this, "创建房间失败，请稍后重试");
                }
            });
        }
    };

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

/*                DataSynEvent msg_event = new DataSynEvent(DataSynEvent.TYPE_TEAM_CHAT_OUT, UserInfoUtil.getAccid(), null);
                EventBus.getDefault().post(msg_event); //更新记录列表*/

//            }
//        }).start();
    }

    //private List<Team> myTeams = new ArrayList<>();

    public void getTeam(final List<TeamDbBean> teamDbBeanList) {
        NIMClient.getService(TeamService.class).queryTeamList().setCallback(new RequestCallback<List<Team>>() {
            @Override
            public void onSuccess(List<Team> teams) {
                System.out.println("--> queryTeamList onSuccess");
                // 获取成功，teams为加入的所有群组
                //myTeams.addAll(teams);
                System.out.println("queryTeamList size=" + (teams != null ? teams.size() : null));
                System.out.println("teamDbBeanList size=" + (teamDbBeanList != null ? teamDbBeanList.size() : null));

                //teamBeanList.clear();

                int index = 0;
                for (int j = 0; teamDbBeanList != null && j < teamDbBeanList.size(); j++) {
                    for (int i = 0; teams != null && i < teams.size(); i++) {
                        if (teamDbBeanList.get(j).getTeamId().equals(teams.get(i).getId())) {

                            System.out.println("--> queryTeamList -->teamId=" + teams.get(i).getId()
                                    + ", memberCount=" + teams.get(i).getMemberCount()
                                    + ", BeInviteMode=" + teams.get(i).getTeamBeInviteMode()
                                    + ", InviteMode=" + teams.get(i).getTeamInviteMode()
                                    + ", teamName=" + teams.get(i).getName()
                                    + ", CreateTime=" + TimeUtil.getNowTime(teams.get(i).getCreateTime())
                                    + ", Creator account=" + teams.get(i).getCreator());

                            getTeamMember(teams.get(i).getId(), teams.get(i).getCreator(), index);
                            index++;
                            break;
                        }
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

    private List<TeamBean> teamBeanList = new ArrayList<>();

    public void getTeamMember(final String teamId, final String teamCreatorAccount, final int index) {
        NIMClient.getService(TeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<TeamMember>>() {
            @Override
            public void onResult(int code, final List<TeamMember> members, Throwable exception) {

                System.out.println("queryMemberList, code=" + code + ", members.size=" + (members != null ? members.size() : -1));
                if (code == ResponseCode.RES_SUCCESS && members != null) {
                    for (int i = 0; i < members.size(); i++) {
                        System.out.println("queryMemberList,teamId" + members.get(i).getTid() + ", account=" + members.get(i).getAccount()
                                + ", teamNick=" + members.get(i).getTeamNick() + ", Extension:" + members.get(i).getExtension());
                    }
                    String teamNickName = null;
                    List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(HomeActivity.this, UserInfoUtil.getAccid(), teamId);
                    if (teamDbBeanList != null && !teamDbBeanList.isEmpty()) {
                        teamNickName = teamDbBeanList.get(0).getTeam_name();
                    }
                    //myTeamMembers.addAll(members);
                    System.out.println("index=" + index + ", teamNickName=" + teamNickName);
                    if (teamBeanList.size() < index)
                        teamBeanList.add(new TeamBean(teamId, teamCreatorAccount, members, teamNickName));
                    else
                        teamBeanList.add(index, new TeamBean(teamId, teamCreatorAccount, members, teamNickName));
                    manyChatAdapter.notifyDataSetChanged();//刷新数据
                }
            }
        });
    }

    //  创建观察者对象
    Observer<List<RecentContact>> messageObserver =
            new Observer<List<RecentContact>>() {
                @Override
                public void onEvent(List<RecentContact> messages) {

                }
            };

    private void registerRecentContactObserver(boolean register) {
        //  注册/注销观察者
        NIMClient.getService(MsgServiceObserve.class)
                .observeRecentContact(messageObserver, register);
    }

    private void unRegisterRecentContactObserver(boolean register) {
        if (messageObserver != null)
            //  注销观察者
            NIMClient.getService(MsgServiceObserve.class)
                    .observeRecentContact(messageObserver, register);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("dispatchKeyEvent " + event.getKeyCode() + ", " + event.getAction());
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                /*&& (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)*/) {
            System.out.println("dispatchKeyEvent KEYCODE_BACK");

            if (ChatRecordAdapter.chat_record_flag_edit_has_show && ChatRecordAdapter.chat_record_flag_edit_has_show_position != -1) {
                System.out.println("dispatchKeyEvent KEYCODE_BACK 1");
                RecyclerView.ViewHolder viewHolder = contact_recyclerView.findViewHolderForAdapterPosition(ChatRecordAdapter.chat_record_flag_edit_has_show_position);
                ((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.layout_edit).setVisibility(View.GONE);
                ((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.requestFocus();
                ChatRecordAdapter.chat_record_flag_edit_has_show = false;
                ChatRecordAdapter.chat_record_flag_edit_has_show_position = -1;
            } else if (ManyChatAdapter.many_chat_flag_edit_has_show && ManyChatAdapter.many_chat_flag_edit_has_show_position != -1) {
                System.out.println("dispatchKeyEvent KEYCODE_BACK 2");
                System.out.println("dispatchKeyEvent KEYCODE_BACK 2:" + ManyChatAdapter.many_chat_flag_edit_has_show_position);
                System.out.println("dispatchKeyEvent KEYCODE_BACK 2:" + many_chat_recyclerView.getChildCount());
                System.out.println("dispatchKeyEvent KEYCODE_BACK 2:" + many_chat_recyclerView.getChildAt(ManyChatAdapter.many_chat_flag_edit_has_show_position));

                RecyclerView.ViewHolder viewHolder = many_chat_recyclerView.findViewHolderForAdapterPosition(ManyChatAdapter.many_chat_flag_edit_has_show_position);
                ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.layout_edit).setVisibility(View.GONE);
                ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.requestFocus();
                ManyChatAdapter.many_chat_flag_edit_has_show = false;
                ManyChatAdapter.many_chat_flag_edit_has_show_position = -1;
            } else {
                ActivityHelper.getInstance().finishActivity();
                //finish();
                //Process.killProcess(Process.myPid());
                return super.dispatchKeyEvent(event);
            }

            return true;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT
                || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT)) {
            if (ChatRecordAdapter.chat_record_flag_edit_has_show || ManyChatAdapter.many_chat_flag_edit_has_show) {
                return true;
            }
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (ChatRecordAdapter.chat_record_flag_edit_has_show && ChatRecordAdapter.chat_record_flag_edit_has_show_position != -1) {
                    RecyclerView.ViewHolder viewHolder = contact_recyclerView.findViewHolderForAdapterPosition(ChatRecordAdapter.chat_record_flag_edit_has_show_position);
                    if (((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_delete).isFocusable()) {
                        ((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_edit).requestFocus();
                    }
                    return true;
                } else if (ManyChatAdapter.many_chat_flag_edit_has_show && ManyChatAdapter.many_chat_flag_edit_has_show_position != -1) {
                    RecyclerView.ViewHolder viewHolder = many_chat_recyclerView.findViewHolderForAdapterPosition(ManyChatAdapter.many_chat_flag_edit_has_show_position);
                    if (((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_delete).isFocusable()) {
                        ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_edit).requestFocus();
                    }
                    return true;
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (ChatRecordAdapter.chat_record_flag_edit_has_show && ChatRecordAdapter.chat_record_flag_edit_has_show_position != -1) {
                    RecyclerView.ViewHolder viewHolder = contact_recyclerView.findViewHolderForAdapterPosition(ChatRecordAdapter.chat_record_flag_edit_has_show_position);
                    if (((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_edit).isFocusable()) {
                        ((ChatRecordAdapter.ChatRecordAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_delete).requestFocus();
                    }

                    return true;
                } else if (ManyChatAdapter.many_chat_flag_edit_has_show && ManyChatAdapter.many_chat_flag_edit_has_show_position != -1) {
                    RecyclerView.ViewHolder viewHolder = many_chat_recyclerView.findViewHolderForAdapterPosition(ManyChatAdapter.many_chat_flag_edit_has_show_position);
                    if (((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_edit).isFocusable()) {
                        ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.btn_delete).requestFocus();
                    }
                    return true;
                }
            }

        }
        return super.dispatchKeyEvent(event);
    }

/*    public DeleteManyChatWindow.OnQuitListener onQuitListener = new DeleteManyChatWindow.OnQuitListener() {
        @Override
        public void onQuit(int position, boolean quit) {
            if (quit){
                System.out.println("--->DeleteManyChatWindow position = "+position);
                teamBeanList.remove(position);
                manyChatAdapter.notifyDataSetChanged();
            }else {

            }
        }
    };*/

    TeamChatReceiveWindow teamChatReceiveWindow;

    private List<DbBean> getRecentRecord() {
        System.out.println("getRecentRecord-->accid=" + UserInfoUtil.getAccid());
        if (UserInfoUtil.getAccid() != null) {
            return DBUtil.queryOnlyFriend(HomeActivity.this, UserInfoUtil.getAccid());
        } else {
            return new ArrayList<>();
        }
    }

    private List<TeamDbBean> getTeamRecord() {
        System.out.println("getTeamRecord-->accid=" + UserInfoUtil.getAccid());
        if (UserInfoUtil.getAccid() != null) {
            return TeamDBUtil.queryOnlyTeam(HomeActivity.this, UserInfoUtil.getAccid());
        } else {
            return new ArrayList<>();
        }

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

    public static void sendMsgToFriend(String teamId, String roomName, String friendAccount, int action) {
        SessionTypeEnum sessionType = SessionTypeEnum.P2P;
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

    //订阅事件处理
    @Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
    public void onDataSynEvent(DataSynEvent event) {
        System.out.println("HomeActivity: " + event.toString());
        if (event.getType().equals(DataSynEvent.TYPE_ONE_TO_ONE_HUNGUP)) {  //单人聊天挂断
            refreshChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_ADD_FRIEND)) {  //成功添加好友
            refreshChatRecord();
        }  else if (event.getType().equals(DataSynEvent.TYPE_REFRESH_FRIEND)) {  //成功添加好友
            refreshChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_ACCEPT_FRIEND)) {  //成功添加好友
            String account = event.getAccount();
            accounts.add(account);
            registerFriendOnlineState(accounts); //订阅在线状态事件
            refreshChatRecord();
        }else if (event.getType().equals(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_RECEIVE)) {  //收到来电
            refreshChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_ONE_TO_ONE_CHAT_OUT)) { //去电
            refreshChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_TEAM_HUNGUP)) {  //群聊天挂断
            refreshTeamChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MSG)) {  //群聊天信息通知
            refreshTeamChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_OUT)) { //在原有的群里发起多人聊天
            refreshTeamChatRecord();
        }else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE)) { //在原有的群里发起多人聊天
            refreshTeamChatRecord();
        } else if (event.getType().equals(DataSynEvent.TYPE_KICKOUT)) { //被其他端踢出
            if (HomeActivity.this.isFinishing()) {
                return;
            }
            System.out.println("HomeActivity, wontAutoLogin KICKOUT");
            //showToast(getApplicationContext(), "已在其他设备上登录");
            KickoutWindow kickoutWindow = new KickoutWindow(HomeActivity.this);
            kickoutWindow.show();
        }else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_TINYVIEW)){
            teamChatReceiveWindow = new TeamChatReceiveWindow(HomeActivity.this, event.getFromAccount(),
                    event.getAccounts(), event.getTeamId(), event.getRoomName());
            teamChatReceiveWindow.show();
        }else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_LAST_OUT)){
            if (teamChatReceiveWindow != null && teamChatReceiveWindow.isShowing()) {
                teamChatReceiveWindow.setCancelCall(true);
                //teamChatReceiveWindow.dismiss();
            }
            try {
                Thread.sleep(1000*2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                if (teamChatReceiveWindow != null && teamChatReceiveWindow.isShowing()) {
                    //teamChatReceiveWindow.setCancelCall(true);
                    teamChatReceiveWindow.dismiss();
                }
            }

        }else if (event.getType().equals(DataSynEvent.TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_NORMAL)){
            teamChatReceiveWindow = new TeamChatReceiveWindow(HomeActivity.this, event.getFromAccount(),
                    event.getAccounts(), event.getTeamId(), event.getRoomName());
            teamChatReceiveWindow.show();
        }
    }

    private synchronized void refreshChatRecord() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<DbBean> temp = getRecentRecord();
                dbBeanList.clear();
                dbBeanList.addAll(temp);
                myHandler.sendEmptyMessage(REFRESH_FRIEND);
            }
        }).start();

    }

    private synchronized void refreshTeamChatRecord() {

        //teamBeanList  = new ArrayList<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //List<TeamDbBean> temp = new ArrayList<TeamDbBean>();
                //teamBeanList.clear();
                //temp = getTeamRecord();//获取数据库的记录
                //teamDbBeanList.addAll(temp);
                teamDbBeanList = getTeamRecord();//获取数据库的记录
                myHandler.sendEmptyMessage(REFRESH_TEAM);
            }
        }).start();

    }

    public final int GET_CHAT_RECORD = 0x00;
    public final int GET_TEAM = 0x01;
    public final int REFRESH_FRIEND = 0x02;
    public final int REFRESH_TEAM = 0x03;
    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_CHAT_RECORD:
                    for (int i = 0; users != null && i < users.size(); i++) {
                        boolean hasExist = false;
                        for (int j = 0; j < dbBeanList.size(); j++) {
                            if (dbBeanList.get(j).getMy_account().equals(UserInfoUtil.getAccid())
                                    && users.get(i).getAccount().equals(dbBeanList.get(j).getFriend_account())) {
                                hasExist = true;
                                break;
                            }
                        }
                        if (!hasExist) {
                            System.out.println("load friend " + users.get(i).getAccount());
                            loadFriendToDb(users.get(i).getAccount(), 2);
                        }
                    }
                    if (dbBeanList.isEmpty()) {
                        layout_contact.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        btn_add.requestFocus();
                    } else {
                        layout_contact.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.GONE);
                        chatRecordAdapter.notifyDataSetChanged();
                    }

                    break;

                case GET_TEAM:
                    if (teamDbBeanList == null || teamDbBeanList.isEmpty()) {
                        many_chat_recyclerView.setVisibility(View.GONE);
                        top_many_chat_layout.requestFocus();
                    } else {
                        teamBeanList.clear();
                        many_chat_recyclerView.setVisibility(View.VISIBLE);
                        getTeam(teamDbBeanList);//获取群聊信息
                    }
                    break;

                case REFRESH_FRIEND:
                    if (dbBeanList.isEmpty()) {
                        layout_contact.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                    } else {
                        layout_contact.setVisibility(View.VISIBLE);
                        empty.setVisibility(View.GONE);
                        chatRecordAdapter.notifyDataSetChanged();
                    }
                    break;

                case REFRESH_TEAM:
                    if (teamDbBeanList == null || teamDbBeanList.isEmpty()) {
                        many_chat_recyclerView.setVisibility(View.GONE);
                        top_many_chat_layout.requestFocus();
                    } else {
                        many_chat_recyclerView.setVisibility(View.VISIBLE);
                        teamBeanList.clear();
                        getTeam(teamDbBeanList);//获取群聊信息
                        manyChatAdapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        System.out.println("onWindowFocusChanged hasFocus=" + hasFocus);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            if (requestCode == 1) {
                int position = data.getIntExtra("position", -1);
                String teamId = data.getStringExtra("teamId");
                boolean success = data.getBooleanExtra("success", false);
                //String newName = data.getStringExtra("newName");
                String newName = null;
                List<TeamDbBean> teamDbBeanList = TeamDBUtil.queryByTeamId(HomeActivity.this, UserInfoUtil.getAccid(), teamId);
                if (teamDbBeanList != null && teamDbBeanList.size() > 0) {
                    newName = teamDbBeanList.get(0).getTeam_name();
                }
                System.out.println("onActivityResult, newName="+newName);

                if (success) {
/*                     if (Utils.length(newName) > 7)
                        ((TextView) recyclerView.getChildAt(position).findViewById(R.id.tv_group_name)).setText("" + Utils.getStrByLength(newName, 7) + "...(" + teamBeanList.get(position).getMemberList().size() + ")");
                    else
                        ((TextView) recyclerView.getChildAt(position).findViewById(R.id.tv_group_name)).setText(newName + "(" + teamBeanList.get(position).getMemberList().size() + ")");

                   recyclerView.getChildAt(position).findViewById(R.id.layout_edit).setVisibility(View.GONE);
                    recyclerView.getChildAt(position).requestFocus();*/

                    RecyclerView.ViewHolder viewHolder = many_chat_recyclerView.findViewHolderForAdapterPosition(position);
                    ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.layout_edit).setVisibility(View.GONE);
                    ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.requestFocus();

                    if (Utils.length(newName) > 7)
                        ((TextView) ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.tv_group_name))
                                .setText("" + Utils.getStrByLength(newName, 7) + "...(" + teamBeanList.get(position).getMemberList().size() + ")");
                    else
                        ((TextView) ((ManyChatAdapter.ManyChatAdapterHolder) viewHolder).itemView.findViewById(R.id.tv_group_name))
                                .setText(newName + "(" + teamBeanList.get(position).getMemberList().size() + ")");


                    ManyChatAdapter.many_chat_flag_edit_has_show = false;
                    ManyChatAdapter.many_chat_flag_edit_has_show_position = -1;
                } else {

                }
            }
        }

        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    //Toast.makeText(HomeActivity.this,"not granted",Toast.LENGTH_SHORT);
                    System.out.println("not granted");
                }else {
                    //Toast.makeText(HomeActivity.this,"hava granted",Toast.LENGTH_SHORT);
                    System.out.println("hava granted");
                }
            }
        }
    }

    public void requestAllPower() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (! Settings.canDrawOverlays(HomeActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }
        }

//        if (Build.VERSION.SDK_INT >= 23) {
//            if (ContextCompat.checkSelfPermission(this, "android.permission.SYSTEM_ALERT_WINDOW")
//                    != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.SYSTEM_ALERT_WINDOW")) {
//
//                } else {
//                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.SYSTEM_ALERT_WINDOW"}, 3);
//                }
//            } else {
//                System.out.println("android.permission.SYSTEM_ALERT_WINDOW 成功申请");
//            }
//
//            if (ContextCompat.checkSelfPermission(this, "android.permission.SYSTEM_OVERLAY_WINDOW")
//                    != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.SYSTEM_OVERLAY_WINDOW")) {
//
//                } else {
//                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.SYSTEM_OVERLAY_WINDOW"}, 3);
//                }
//            } else {
//                System.out.println("android.permission.SYSTEM_OVERLAY_WINDOW 成功申请");
//            }
//
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 3) {
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(this, "" + "权限" + permissions[i] + "申请失败", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }


}
