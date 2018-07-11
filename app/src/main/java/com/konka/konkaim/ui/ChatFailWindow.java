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

/**
 * Created by HP on 2018-5-10.
 */

public class ChatFailWindow extends PopupWindow implements View.OnClickListener{
    private final String TAG = "ChatFailWindow";
    private Context mContext;
    private View mView;
    private Button btn_sure;

    public ChatFailWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
    }

    private void initView(Context context){
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.chat_fail, null);
        setContentView(mView);
        btn_sure = (Button)mView.findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(this);
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
                dismiss();
                break;
        }
    }
}
