package com.konka.konkaim.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.konka.konkaim.util.LogUtil;
import com.konka.konkaim.util.Utils;

import com.konka.konkaim.R;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;

/**
 * Created by HP on 2018-5-10.
 */

public class DeleteContactWindow extends PopupWindow implements View.OnClickListener {
    private final String TAG = "DeleteContactWindow";
    private Context mContext;
    private View mView;
    private Button btn_sure;
    private Button btn_cancel;

    private String friendAccount;

    public DeleteContactWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.delete_contact, null);
        setContentView(mView);
        btn_sure = (Button) mView.findViewById(R.id.btn_sure);
        btn_cancel = (Button) mView.findViewById(R.id.btn_cancel);
        btn_sure.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sure:
                System.out.println("click sure");
                delete(getFriendAccount());
                dismiss();
                break;
            case R.id.btn_cancel:
                System.out.println("click cancel");
                dismiss();
                break;
        }
    }

    private void delete(final String account) {
        NIMClient.getService(FriendService.class).deleteFriend(account)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        System.out.println("delete friend success param=" + param);
                        onDeleteListener.onDelete(position, true);
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("delete friend fail code=" + code);
                        onDeleteListener.onDelete(position, false);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        onDeleteListener.onDelete(position, false);
                        exception.printStackTrace();
                    }
                });
    }

    public void setFriendAccount(String friendAccount) {
        this.friendAccount = friendAccount;
    }

    public String getFriendAccount() {
        return friendAccount;
    }


    private int position;
    public void setPosition(int position) {
        this.position = position;
    }
    public int getPosition() {
        return position;
    }

    public OnDeleteListener onDeleteListener;

    public void setOnDeleteListener(OnDeleteListener onDeleteListener) {
        this.onDeleteListener = onDeleteListener;
    }

    public interface OnDeleteListener{
        void onDelete(int position, boolean quit);
    }
}
