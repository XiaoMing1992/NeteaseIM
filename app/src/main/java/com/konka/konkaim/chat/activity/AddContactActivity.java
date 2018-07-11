package com.konka.konkaim.chat.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.R;
import com.konka.konkaim.adapter.AddContactInviteAdapter;
import com.konka.konkaim.adapter.AddContactResultAdapter;
import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.bean.BaseBean;
import com.konka.konkaim.bean.FriendBean;
import com.konka.konkaim.db.FriendDBUtil;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static android.R.id.message;

public class AddContactActivity extends AppCompatActivity {

    private TextView empty_tip;
    private LinearLayout layout_result;
    //private RecyclerView search_result_recyclerView;
    private LinearLayout layout_invite;
    private RecyclerView invite_recyclerView;
    private AddContactInviteAdapter addContactInviteAdapter;
    private List<NimUserInfo> nimUserInfoList = new ArrayList<>();

    private EditText edit_mobile;

    //private List<NimUserInfo> nimUserInfoList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    //private AddContactResultAdapter addContactResultAdapter;

    private RelativeLayout search_result;
    private LinearLayout layout_refuse_tip;
    private CircleImageView head_icon;
    private TextView nickname;

    private TextView layout_invite_tip;
    private TextView add_friend_result_tip;


    private List<String> accountsToAddContactTags;
    private ArrayList<String> accountsToAddContact;
    private List<FriendBean> friendBeanList = new ArrayList<>();

    private final int LOAD_FAIL = 0x00;
    private final int LOAD_OK = 0x01;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_OK:
                    ArrayList<String> allAccounts = new ArrayList<>();
                    if (accountsToAddContact != null) {
                        System.out.println("--- 所有好友用户资料信息 ---"+accountsToAddContact.size());
                        allAccounts.addAll(accountsToAddContact);
                    }

                    for (int i = 0; i < friendBeanList.size(); i++) {
                        if (allAccounts.contains(friendBeanList.get(i).getFriend_account()))
                            continue;
                        allAccounts.add(friendBeanList.get(i).getFriend_account());

                        if (accountsToAddContactTags == null) {
                            accountsToAddContactTags = new ArrayList<>();
                        }
                        accountsToAddContactTags.add(HomeActivity.FRIEND_ADD_ME_TAG);
                    }

                    if (!allAccounts.isEmpty()){
                        layout_invite.setVisibility(View.VISIBLE); //显示邀请好友界面
                        layout_result.setVisibility(View.GONE);    //隐藏搜索界面
                    }
                    getAddRequestUserInfo(allAccounts);
                    break;
                case LOAD_FAIL:
                    ArrayList<String> allAccounts_temp = new ArrayList<>();
                    if (accountsToAddContact != null) {
                        System.out.println("--- 所有好友用户资料信息 ---");
                        allAccounts_temp.addAll(accountsToAddContact);
                    }
                    getAddRequestUserInfo(allAccounts_temp);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        ActivityHelper.getInstance().addActivity(this);

        initView();
        initData();
        listener();
    }

    private void initView() {
        edit_mobile = (EditText) findViewById(R.id.edit_mobile);
        //联系人为空的搜索提示
        empty_tip = (TextView) findViewById(R.id.empty_tip);

        //添加联系人的搜索结果
        //search_result_recyclerView = (RecyclerView) findViewById(R.id.search_result_recyclerView);
        layout_result = (LinearLayout) findViewById(R.id.layout_result);
        search_result = (RelativeLayout) findViewById(R.id.search_result);
        layout_refuse_tip = (LinearLayout) findViewById(R.id.layout_refuse_tip);
        head_icon = (CircleImageView) findViewById(R.id.head_icon);
        nickname = (TextView) findViewById(R.id.nickname);
        layout_invite_tip = (TextView) findViewById(R.id.layout_invite_tip);
        add_friend_result_tip = (TextView) findViewById(R.id.add_friend_result_tip);

        //好友邀请
        invite_recyclerView = (RecyclerView) findViewById(R.id.invite_recyclerView);
        layout_invite = (LinearLayout) findViewById(R.id.layout_invite);
    }

    private void loadFriendFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                friendBeanList = FriendDBUtil.queryOnlyFriend(AddContactActivity.this, UserInfoUtil.getAccid());
                mHandler.sendEmptyMessage(LOAD_OK);
            }
        }).start();
    }

    private void initData() {
        HttpHelper.setHttpListener(httpListener);

        loadFriendFromDB();

        Intent intent = getIntent();
        boolean show_layout_invite = intent.getBooleanExtra("show_layout_invite", false);
        if (show_layout_invite) {
            layout_invite.setVisibility(View.VISIBLE); //显示邀请好友界面
            layout_result.setVisibility(View.GONE);    //隐藏搜索界面

            Bundle bundle = intent.getBundleExtra("friend_account");
            accountsToAddContact = bundle.getStringArrayList("accountsToAddContact");
            accountsToAddContactTags = bundle.getStringArrayList("accountsToAddContactTags");
        }
    }

    private void listener() {
        edit_mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11) {
                    //去搜索...
                    System.out.println("--- input mobile is " + s.toString());
                    getAccidByMobile(s.toString());
                } else {
                    add_friend_result_tip.setVisibility(View.GONE);
                    empty_tip.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("afterTextChanged-->" + s);
            }
        });

        edit_mobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lostFocus = true;
                } else {
                    lostFocus = false;
                }
            }
        });
    }

    private boolean lostFocus = false;

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && edit_mobile.getText().toString().trim().length() == 11) {

                if (layout_result.getVisibility() == View.VISIBLE && !lostFocus) {
                    System.out.println("" + edit_mobile.getText().toString().trim());

                    if (TextUtils.isEmpty(friendAccount)) {
                        System.out.println("账号异常");
                        showToast(AddContactActivity.this, "账号异常");
                        return true;
                    }
                    if (friendAccount.equals(UserInfoUtil.getAccid())) {
                        System.out.println("不能添加自己为好友");
                        showToast(AddContactActivity.this, "不能添加自己为好友");
                        return true;
                    } else {
                        boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(friendAccount);
                        if (isMyFriend) {
                            System.out.println("对方已经是好友了");
                            showToast(AddContactActivity.this, "对方已经是好友了");
                        } else {
                            if (add_friend_result_tip.getVisibility() == View.VISIBLE) {
                                System.out.println("已经添加");
                            } else {
                                System.out.println("去添加");
                                addFriend(friendAccount);
                            }
                        }
                        return true;
                    }
                }/*else if (layout_invite.getVisibility() == View.VISIBLE){
                    if (!TextUtils.isEmpty(friendAccount)) {
                        accept(friendAccount);//同意添加对方为好友
                    }
                }
            }else if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                if (layout_invite.getVisibility() == View.VISIBLE) {
                    reject(friendAccount); //拒绝添加对方为好友
                }
            }*/
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private String friendAccount;

    private void getAccidByMobile(final String mobile) {
        HttpHelper.getAccidByMobile(mobile);
    }

    private void query(String account) {
        friendAccount = account;
        List<String> accounts = new ArrayList<>();
        accounts.add(friendAccount);
        getServerUserInfo(accounts);
    }

    private HttpListener<BaseBean> httpListener = new HttpListener<BaseBean>() {
        @Override
        public void fail(Throwable e, String type) {
            if (type.equals(HttpHelper.SMS_TYPE)) {

            } else if (type.equals(HttpHelper.CHECK_LOGIN_TYPE)) {

            } else if (type.equals(HttpHelper.IS_EXIST_TYPE)) {

            } else if (type.equals(HttpHelper.ADD_USER_TYPE)) {

            } else if (type.equals(HttpHelper.GET_ACCID_BY_MOBILE_TYPE)) {
                System.out.println("type=" + type);
                showToast(AddContactActivity.this, "获取用户信息出错");
                e.printStackTrace();
            }
        }

        @Override
        public void success(BaseBean baseBean, String type) {
            if (baseBean != null) {
                System.out.println("" + baseBean.toString());
            }

            if (type.equals(HttpHelper.SMS_TYPE)) {

            } else if (type.equals(HttpHelper.CHECK_LOGIN_TYPE)) {

            } else if (type.equals(HttpHelper.IS_EXIST_TYPE)) {

            } else if (type.equals(HttpHelper.ADD_USER_TYPE)) {

            } else if (type.equals(HttpHelper.GET_ACCID_BY_MOBILE_TYPE)) {
                if (baseBean != null) {
                    query(baseBean.getAccid());
                }
            }
        }
    };

    private void addFriend(final String account) {
        final VerifyType verifyType = VerifyType.VERIFY_REQUEST; // 发起好友验证请求
        String msg = "好友请求附言";
        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        System.out.println("add friend success param=" + param);
                        //showToast(AddContactActivity.this, "已发送添加好友请求");
                        add_friend_result_tip.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("add friend fail code=" + code);
                        showToast(AddContactActivity.this, "失败发送添加好友请求");
                    }

                    @Override
                    public void onException(Throwable exception) {
                        exception.printStackTrace();
                        showToast(AddContactActivity.this, "失败发送添加好友请求");
                    }
                });
    }

    private void getServerUserInfo(List<String> accounts) {
        NIMClient.getService(UserService.class).fetchUserInfo(accounts)
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> param) {
                        if (param != null && !param.isEmpty()) {
                            System.out.println("getServerUserInfo onSuccess-->name=" + param.get(0).getName() + ", avatar=" + param.get(0).getAvatar());

/*                            if (result == null) {
                                showToast(AddContactActivity.this, "该账号不存在");
                                empty_tip.setVisibility(View.VISIBLE);
                                layout_result.setVisibility(View.GONE);
                            } else {*/
                            empty_tip.setVisibility(View.GONE);
                            layout_invite.setVisibility(View.GONE);

                            layout_result.setVisibility(View.VISIBLE);
                            nickname.setText(param.get(0).getName()); //昵称
                            HttpHelper.downloadPicture(AddContactActivity.this, param.get(0).getAvatar(),
                                    R.drawable.img_default, R.drawable.img_default, head_icon); //头像

/*                            showToast(AddContactActivity.this, "该账号存在，昵称是" + param.get(0).getName() + ", 手机号是" + param.get(0).getMobile()
                                    + "， 头像链接地址是" + param.get(0).getAvatar());*/

                            //}
                        } else {
                            //showToast(AddContactActivity.this, "该账号不存在");
                            empty_tip.setVisibility(View.VISIBLE);
                            layout_result.setVisibility(View.GONE);
                            layout_invite.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("getServerUserInfo fail code=" + code);
                        showToast(AddContactActivity.this, "获取用户信息出错");
                    }

                    @Override
                    public void onException(Throwable exception) {
                        System.out.println("getServerUserInfo onException");
                        showToast(AddContactActivity.this, "获取用户信息出错");
                        exception.printStackTrace();
                    }
                });
    }


    private void getAddRequestUserInfo(List<String> accounts) {
        NIMClient.getService(UserService.class).fetchUserInfo(accounts)
                .setCallback(new RequestCallback<List<NimUserInfo>>() {
                    @Override
                    public void onSuccess(List<NimUserInfo> param) {
                        linearLayoutManager = new LinearLayoutManager(AddContactActivity.this);
                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        invite_recyclerView.setLayoutManager(linearLayoutManager);
                        addContactInviteAdapter = new AddContactInviteAdapter(AddContactActivity.this, param, accountsToAddContactTags);
                        invite_recyclerView.setAdapter(addContactInviteAdapter);
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("getAddRequestUserInfo fail code=" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        System.out.println("getAddRequestUserInfo onException");
                        exception.printStackTrace();
                    }
                });
    }

}
