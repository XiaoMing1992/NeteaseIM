package com.konka.konkaim.user;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.bean.BaseBean;
import com.konka.konkaim.chat.ChatReceiver;
import com.konka.konkaim.chat.activity.OneToOneActivity;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.Constant;
import com.konka.konkaim.util.LogUtil;

import com.konka.konkaim.R;
import com.konka.konkaim.util.NetworkUtil;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;
import com.netease.nimlib.sdk.event.EventSubscribeService;
import com.netease.nimlib.sdk.event.model.Event;
import com.netease.nimlib.sdk.event.model.EventSubscribeRequest;
import com.netease.nimlib.sdk.uinfo.UserService;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "LoginActivity";
    private Button login;
    private Button register;
    private EditText mobile;
    private EditText password;
    private TextView forget_password;
    private TextView password_error_tip;
    private TextView mobile_error_tip;

    private LinearLayout layout_loading;
    private LinearLayout layout_login_main;
    private ImageView auto_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.getInstance().addActivity(this);
        setContentView(R.layout.login);
        initView();
        if (UserInfoUtil.canAutoLogin(getApplicationContext())) {
            layout_login_main.setVisibility(View.GONE);
            auto_logo.setVisibility(View.VISIBLE);
            autoLogin();
        }else {
            auto_logo.setVisibility(View.GONE);
            layout_login_main.setVisibility(View.VISIBLE);
            mobile.requestFocus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

/*    public boolean canAutoLogin() {
        final String accid = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME*//*+ UserInfoUtil.getAccid()*//*,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
        final String token = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME*//*+ UserInfoUtil.getAccid()*//*,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);
        System.out.println("doLogin-->accid=" + accid + ", token=" + token);
        if (accid != null && token != null) {
            return true;
        } else
            return false;
    }*/

    private void autoLogin() {
        final String accid = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
        final String token = PrefenceUtil.get(getApplicationContext(), PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);
        System.out.println("doLogin-->accid=" + accid + ", token=" + token);
        if (accid != null && token != null) {
            System.out.println("ChatReceiver.IS_BOOT_START="+ChatReceiver.IS_BOOT_START);

//            if (ChatReceiver.IS_BOOT_START){
//                ChatReceiver.IS_BOOT_START = false;
//
//                UserInfoUtil.setAccid(accid);
//                UserInfoUtil.setToken(token);
//
//                layout_loading.setVisibility(View.GONE);
//
//                Intent login_intent = new Intent();
//                login_intent.setClass(LoginActivity.this, LoginActivity.class);
//                startActivity(login_intent);
//                return;
//            }

            LoginInfo info = new LoginInfo(accid, token); // config...
            RequestCallback<LoginInfo> callback =
                    new RequestCallback<LoginInfo>() {
                        // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用

                        @Override
                        public void onSuccess(LoginInfo param) {
                            System.out.println("onSuccess param=" + param);

                            UserInfoUtil.setAccid(accid);
                            UserInfoUtil.setToken(token);

                            syncDataObserver(true); //登录后同步数据过程通知
                            setOnlineStatusObsever(); //在线状态变化观察者

                        }

                        @Override
                        public void onFailed(int code) {
                            System.out.println("onFailed code=" + code);

                            auto_logo.setVisibility(View.GONE);
                            layout_login_main.setVisibility(View.VISIBLE);
                            login.requestFocus();
                        }

                        @Override
                        public void onException(Throwable exception) {
                            System.out.println("--- onException ---");
                            exception.printStackTrace();

                            auto_logo.setVisibility(View.GONE);
                            layout_login_main.setVisibility(View.VISIBLE);
                            login.requestFocus();
                        }
                    };
            NIMClient.getService(AuthService.class).login(info)
                    .setCallback(callback);
        }
    }

    private void initData() {
        HttpHelper.setHttpListener(httpListener);
    }

    private void initView() {
        layout_loading = (LinearLayout) findViewById(R.id.layout_loading);
        layout_login_main = (LinearLayout) findViewById(R.id.layout_login_main);
        auto_logo = (ImageView)findViewById(R.id.auto_logo);

        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        mobile = (EditText) findViewById(R.id.mobile);
        password = (EditText) findViewById(R.id.password);
        forget_password = (TextView) findViewById(R.id.forget_password);
        password_error_tip = (TextView) findViewById(R.id.password_error_tip);
        mobile_error_tip = (TextView) findViewById(R.id.mobile_error_tip);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forget_password.setOnClickListener(this);
        mobile.requestFocus();
        listener();
    }

    private void listener() {
        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) login.setEnabled(true);
//                else login.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                }else {
                    mobile_error_tip.setVisibility(View.GONE);
                }
            }
        });


        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                }else {
                    password_error_tip.setVisibility(View.GONE);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() > 0) login.setEnabled(true);
//                else login.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private HttpListener<BaseBean> httpListener = new HttpListener<BaseBean>() {
        @Override
        public void fail(Throwable e, String type) {
            System.out.println("fail type = " + type);
            if (type.equals(HttpHelper.CHECK_LOGIN_TYPE)) {
                layout_loading.setVisibility(View.GONE);
            }
        }

        @Override
        public void success(BaseBean baseBean, String type) {
            if (baseBean != null) {
                System.out.println("success type = " + type);
            }

            if (type.equals(HttpHelper.CHECK_LOGIN_TYPE)) {

                if (baseBean != null) {
                    System.out.println(baseBean.toString());
                    UserInfoUtil.setAccid(baseBean.getAccid());
                    UserInfoUtil.setToken(baseBean.getToken());
                    UserInfoUtil.setCode(baseBean.getCode());
                    UserInfoUtil.setDesc(baseBean.getDesc());

                    if (baseBean.getCode().equals(Constant.USER_NOT_EXIST)) {
                        layout_loading.setVisibility(View.GONE);

                        mobile_error_tip.setVisibility(View.VISIBLE);
                        password_error_tip.setVisibility(View.GONE);


                    } else if (baseBean.getCode().equals(Constant.USERNAME_PASSWORD_ERROR)) {
                        layout_loading.setVisibility(View.GONE);

                        mobile_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.VISIBLE);


                    } else if (baseBean.getCode().equals(Constant.REQUEST_SUCCESS)) {
                        mobile_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);


                        //登陆云信
                        doLogin();
                    }

                    //NIMClient.getService(UserService.class).getUserInfo(baseBean.getAccid());
                }

            }
        }
    };

    public void doLogin() {
        System.out.println("accid=" + UserInfoUtil.getAccid() + ", token=" + UserInfoUtil.getToken());

        LoginInfo info = new LoginInfo(UserInfoUtil.getAccid(), UserInfoUtil.getToken()); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用

                    @Override
                    public void onSuccess(LoginInfo param) {
                        System.out.println("onSuccess param=" + param);

                        syncDataObserver(true); //登录后同步数据过程通知
                        setOnlineStatusObsever(); //在线状态变化观察者

                        PrefenceUtil.set(LoginActivity.this, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID, UserInfoUtil.getAccid());
                        PrefenceUtil.set(LoginActivity.this, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN, UserInfoUtil.getToken());
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("onFailed code=" + code);
                        layout_loading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        System.out.println("--- onException ---");
                        exception.printStackTrace();
                        layout_loading.setVisibility(View.GONE);
                    }
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }

    private void toLogin(final String mobile, final String password) {
        HttpHelper.checkLogin(mobile, password);
    }

    private void syncDataObserver(boolean register) {
        NIMClient.getService(AuthServiceObserver.class).observeLoginSyncDataStatus(new Observer<LoginSyncStatus>() {
            @Override
            public void onEvent(LoginSyncStatus status) {
                if (status == LoginSyncStatus.BEGIN_SYNC) {
                    LogUtil.LogI(TAG, "login sync data begin");
                } else if (status == LoginSyncStatus.SYNC_COMPLETED) { //数据同步完成
                    LogUtil.LogI(TAG, "login sync data completed");
                    System.out.println("login sync data completed");

                    layout_loading.setVisibility(View.GONE);

                    Intent login_intent = new Intent();
                    login_intent.setClass(LoginActivity.this, HomeActivity.class);
                    startActivity(login_intent);
                }
            }
        }, register);
    }

    //在线状态变化观察者
    private void setOnlineStatusObsever() {
/*        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                new Observer<StatusCode>() {
                    public void onEvent(StatusCode status) {
                        if (status.wontAutoLogin()) {  //SDK将停止自动登录
                            // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                            //status.getValue() == StatusCode.LOGINED
                        }
                    }
                }, true);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                if (!NetworkUtil.isNetworkAvailable(LoginActivity.this)) {
                    showToast(LoginActivity.this, "当前网络不可用，请检查网络是否连接");
                    return;
                }
                String mobileStr = mobile.getText().toString().trim();
                String passwordStr = password.getText().toString().trim();
                LogUtil.LogD(TAG, "mobile=" + mobileStr + ", password=" + passwordStr);
                System.out.println("mobile=" + mobileStr + ", password=" + passwordStr);

                if (mobileStr.isEmpty()){
                    System.out.println("mobile is empty");
                    mobile_error_tip.setText("*  手机号不能为空");
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    return;
                }else if (!Utils.isMobileNO(mobileStr)) {
                    System.out.println("mobile is wrong");
                    mobile_error_tip.setText("*  请填写正确的手机号");
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    return;
                } else {
                    mobile_error_tip.setText("*  用户不存在");
                    mobile_error_tip.setVisibility(View.GONE);

                    if (passwordStr.isEmpty()){
                        password_error_tip.setText("*  密码不能为空");
                        password_error_tip.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        password_error_tip.setText("*  密码错误");
                        password_error_tip.setVisibility(View.GONE);
                    }

                    if (passwordStr.length() < 6 || passwordStr.length() > 20
                            || !Utils.isDigitOrLetter(passwordStr)) {
                        password_error_tip.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        password_error_tip.setVisibility(View.GONE);
                    }
                }
                layout_loading.setVisibility(View.VISIBLE);
                toLogin(mobileStr, passwordStr);

/*                Intent login_intent = new Intent();
                login_intent.setClass(this, OneToOneActivity.class);
                //login_intent.setClass(this, HomeActivity.class);
                //login_intent.setClass(this, ManyChatActivity.class);
                startActivity(login_intent);*/
                break;
            case R.id.register:
                Intent register_intent = new Intent();
                register_intent.setClass(this, RegisterActivity.class);
                startActivity(register_intent);
                break;
            case R.id.forget_password:
                Intent toForgetIntent = new Intent();
                toForgetIntent.setClass(this, ForgetPasswordActivity.class);
                startActivity(toForgetIntent);
                break;
        }
    }

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

//    private EventSubscribeRequest eventSubscribeRequest = new EventSubscribeRequest();
//    private void publishOnlineEvent(){
//        NIMClient.getService(EventSubscribeService.class).publishEvent(new Event());
//    }
}
