package com.konka.konkaim.ui;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.konka.konkaim.MainApplication;
import com.konka.konkaim.chat.AVChatProfile;
import com.konka.konkaim.chat.team.TeamAVChatProfile;
import com.konka.konkaim.user.LoginActivity;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.Utils;

import com.konka.konkaim.R;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;

/**
 * Created by HP on 2018-5-10.
 */

public class ExitWindow extends PopupWindow implements View.OnClickListener{
    private final String TAG = "ExitWindow";
    private Context mContext;
    private View mView;
    private Button btn_sure;
    private Button btn_cancel;

    public ExitWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
    }

    private void initView(Context context){
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.exit, null);
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
        super.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_sure:
                System.out.println("click sure");
                logout();

                UserInfoUtil.clearUserInfo();
                PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                        PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_ACCID);
                PrefenceUtil.remove(mContext, PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME/*+ UserInfoUtil.getAccid()*/,
                        PrefenceUtil.CURRENT_USER_LOGIN_INFO_FILENAME_KEY_TOKEN);
                MainApplication.finishedMap.clear();
                MainApplication.myRoomNameMap.clear();


                toLogin();
                dismiss();
                break;
            case R.id.btn_cancel:
                System.out.println("click cancel");
                dismiss();
                break;
        }
    }

    private void logout(){
        TeamAVChatProfile.sharedInstance().setTeamAVChatting(false);
        AVChatProfile.getInstance().setAVChatting(false);

        ActivityHelper.getInstance().finishActivity();
        NIMClient.getService(AuthService.class).logout();
    }

    private void toLogin(){
        Intent intent = new Intent();
        intent.setClass(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }
}
