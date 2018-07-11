package com.konka.konkaim.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.konka.konkaim.util.Utils;

import com.konka.konkaim.R;
import com.konka.konkaim.util.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;

/**
 * Created by HP on 2018-5-10.
 */

public class DeleteManyChatWindow extends PopupWindow implements View.OnClickListener{
    private final String TAG = "DeleteManyChatWindow";
    private Context mContext;
    private View mView;
    private Button btn_sure;
    private Button btn_cancel;
    private String teamId;
    private int position;

    private boolean isTeamCreator;
    public DeleteManyChatWindow(Context context, String teamId, int position, boolean isTeamCreator) {
        super(context);
        this.teamId = teamId;
        this.position = position;
        this.isTeamCreator = isTeamCreator;
        initView(context);
        initSetting();
    }

    private void initView(Context context){
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.delete_many_chat, null);
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
                System.out.println("click sure to quit teamId="+teamId);
                if (isTeamCreator){
                    dismissTeam(teamId);
                    dismiss();
                }else {
                    quit(teamId);
                    dismiss();
                }
                break;
            case R.id.btn_cancel:
                System.out.println("click cancel to quit teamId="+teamId);
                dismiss();
//                if (isTeamCreator){
//                    onQuitListener.onDismissTeam(position, false);
//                    dismiss();
//                }else {
//                    onQuitListener.onQuit(position, false);
//                    dismiss();
//                }
                break;
        }
    }

    public void quit(final String teamId){
        NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                // 退群成功
                System.out.println("quitTeam success");
                onQuitListener.onQuit(position, true);
            }

            @Override
            public void onFailed(int code) {
                // 退群失败
                System.out.println("quitTeam fail, code="+code);
                onQuitListener.onQuit(position, false);
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                exception.printStackTrace();
                System.out.println("quitTeam fail, exception");
                onQuitListener.onQuit(position, false);
            }
        });
    }

    public void dismissTeam(final String teamId) {
        NIMClient.getService(TeamService.class).dismissTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                // 解散群成功
                System.out.println("dismissTeam success");
                onQuitListener.onDismissTeam(position, true);
            }

            @Override
            public void onFailed(int code) {
                // 解散群失败
                System.out.println("dismissTeam code="+code);
                onQuitListener.onDismissTeam(position, false);
            }

            @Override
            public void onException(Throwable exception) {
                // 错误
                exception.printStackTrace();
                System.out.println("dismissTeam onException");
                onQuitListener.onDismissTeam(position, false);
            }
        });
    }

    public OnQuitListener onQuitListener;

    public void setOnQuitListener(DeleteManyChatWindow.OnQuitListener onQuitListener) {
        this.onQuitListener = onQuitListener;
    }

    public interface OnQuitListener{
        void onQuit(int position, boolean quit);
        void onDismissTeam(int position, boolean dismiss);
    }
}
