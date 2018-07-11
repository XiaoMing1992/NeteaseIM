package com.konka.konkaim.ui;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.konka.konkaim.R;
import com.konka.konkaim.user.LoginActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

/**
 * Created by HP on 2018-5-10.
 */

public class KickoutWindow extends PopupWindow implements View.OnClickListener{
    private final String TAG = "KickoutWindow";
    private Context mContext;
    private View mView;
    private Button btn_sure;
    private Button btn_cancel;

    public KickoutWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
    }

    private void initView(Context context){
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.kickout, null);
        setContentView(mView);
        btn_sure = (Button)mView.findViewById(R.id.btn_sure);
        btn_cancel = (Button)mView.findViewById(R.id.btn_cancel);
        btn_sure.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    private void initSetting(){
        Utils.computeScreenSize(mContext);
        LogUtil.LogD(TAG, "ScreenWidth = "+Utils.getScreenWidth()+" ScreenHeight = "+Utils.getScreenHeight());
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
        UserInfoUtil.clearUserInfo();
        PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
        PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);

        logout();
        toLogin();
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure:
                System.out.println("click sure");
                dismiss();
                break;
            case R.id.btn_cancel:
                System.out.println("click cancel");
                UserInfoUtil.clearUserInfo();
                PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                        PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
                PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                        PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);

                logout();
                dismiss();
                break;
        }
    }

    private void logout(){
        ActivityHelper.getInstance().finishActivity();
        NIMClient.getService(AuthService.class).logout();
    }

    private void toLogin(){
        Intent intent = new Intent();
        intent.setClass(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }
}
