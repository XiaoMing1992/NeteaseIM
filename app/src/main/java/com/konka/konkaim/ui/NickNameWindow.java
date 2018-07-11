package com.konka.konkaim.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.konkaim.R;
import com.konka.konkaim.user.HomeActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP on 2018-5-10.
 */

public class NickNameWindow extends PopupWindow {
    private final String TAG = "NickNameWindow";
    private Context mContext;
    private View mView;
    //private TextView version;

    //下面是昵称填写页的控件
    //private RelativeLayout layout_nickname;
    private EditText edit_nick_name;
    private TextView tv_nick_name_count;
    private TextView nick_name_error_tip;
    private Button btn_save;

    public NickNameWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.nickname, null);
        setContentView(mView);
        //version = (TextView)mView.findViewById(R.id.version);

        //下面是昵称填写页的控件
        //layout_nickname = (RelativeLayout) mView.findViewById(R.id.layout_nickname);
        edit_nick_name = (EditText) mView.findViewById(R.id.edit_nick_name);
        tv_nick_name_count = (TextView) mView.findViewById(R.id.tv_nick_name_count);
        nick_name_error_tip = (TextView) mView.findViewById(R.id.nick_name_error_tip);
        btn_save = (Button) mView.findViewById(R.id.btn_save);
        listener();
    }

    private void initSetting() {
        Utils.computeScreenSize(mContext);
        LogUtil.LogD(TAG, "ScreenWidth = " + Utils.getScreenWidth() + " ScreenHeight = " + Utils.getScreenHeight());
        setWidth(Utils.getScreenWidth());
        setHeight(Utils.getScreenHeight());
        setFocusable(true);
        setBackgroundDrawable(mContext.getResources().getDrawable(R.color.transparent));
    }

    public void show() {
        showAtLocation(mView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        doLogin();
    }

    private void initData() {

    }

    private void listener() {
        edit_nick_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv_nick_name_count.setText("" + s.length() + "/15");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nicknameStr = edit_nick_name.getText().toString().trim();
                if (nicknameStr.isEmpty()) nick_name_error_tip.setVisibility(View.VISIBLE);
                else {
                    nick_name_error_tip.setVisibility(View.GONE);
                    //layout_nickname.setVisibility(View.GONE);
                    updateUserInfo(nicknameStr);
                }
            }
        });
    }

    public void updateUserInfo(final String nickname) {
        Map<UserInfoFieldEnum, Object> fields = new HashMap<>(1);
        fields.put(UserInfoFieldEnum.Name, nickname);//更新用户本人的名称
        NIMClient.getService(UserService.class).updateUserInfo(fields)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        System.out.println("updateUserInfo, code=" + code);
/*                        Intent intent = new Intent();
                        intent.setClass(mContext, HomeActivity.class);
                        mContext.startActivity(intent);*/
                        dismiss();
                    }
                });
    }

    public void doLogin() {
        System.out.println("accid="+ UserInfoUtil.getAccid()+", token="+UserInfoUtil.getToken());

        LoginInfo info = new LoginInfo(UserInfoUtil.getAccid(), UserInfoUtil.getToken()); // config...
        RequestCallback<LoginInfo> callback =
                new RequestCallback<LoginInfo>() {
                    // 可以在此保存LoginInfo到本地，下次启动APP做自动登录用

                    @Override
                    public void onSuccess(LoginInfo param) {
                        System.out.println("onSuccess param="+param);

                        syncDataObserver(true); //登录后同步数据过程通知
                        setOnlineStatusObsever(); //在线状态变化观察者

                        Intent intent = new Intent();
                        intent.setClass(mContext, HomeActivity.class);
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("onFailed code="+code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        System.out.println("--- onException ---");
                        exception.printStackTrace();
                    }
                };
        NIMClient.getService(AuthService.class).login(info)
                .setCallback(callback);
    }

    private void syncDataObserver(boolean register){
        NIMClient.getService(AuthServiceObserver.class).observeLoginSyncDataStatus(new Observer<LoginSyncStatus>() {
            @Override
            public void onEvent(LoginSyncStatus status) {
                if (status == LoginSyncStatus.BEGIN_SYNC) {
                    LogUtil.LogI(TAG, "login sync data begin");
                } else if (status == LoginSyncStatus.SYNC_COMPLETED) { //数据同步完成
                    LogUtil.LogI(TAG, "login sync data completed");

                }
            }
        }, register);
    }

    //在线状态变化观察者
    private void setOnlineStatusObsever(){
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(
                new Observer<StatusCode> () {
                    public void onEvent(StatusCode status) {
                        if (status.wontAutoLogin()) {  //SDK将停止自动登录
                            // 被踢出、账号被禁用、密码错误等情况，自动登录失败，需要返回到登录界面进行重新登录操作
                            //status.getValue() == StatusCode.LOGINED
                        }
                    }
                }, true);
    }
}
