package com.konka.konkaim.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by HP on 2018-6-28.
 */

public class MyToast {
    private Context mContext;
    private Toast mToast;

    public MyToast(Context context){
        this.mContext = context;
        init(mContext);
    }

    private void init(Context context){
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public void Show(String content){
        if (mToast != null){
            mToast.cancel();

            mToast.setText(content);
            mToast.show();
        }
    }

    public void Close(){
        if (mToast != null){
            mToast.cancel();
        }
    }
}
