package com.konka.konkaim.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.konka.konkaim.util.Utils;

import com.konka.konkaim.R;
import com.konka.konkaim.util.LogUtil;

/**
 * Created by HP on 2018-5-10.
 */

public class AboutWindow extends PopupWindow{
    private final String TAG = "AboutWindow";
    private Context mContext;
    private View mView;
    private TextView version;

    public AboutWindow(Context context) {
        super(context);
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context){
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.about, null);
        setContentView(mView);
        version = (TextView)mView.findViewById(R.id.version);
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

    private void initData(){
        version.setText(Utils.getVersionName(mContext));
    }
}
