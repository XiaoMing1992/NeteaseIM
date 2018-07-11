package com.konka.konkaim.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.bean.BaseBean;
import com.konka.konkaim.bean.DataSynEvent;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.user.UserInfoUtil;
import com.konka.konkaim.util.Utils;

import com.konka.konkaim.R;
import com.konka.konkaim.util.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.NIMSDK;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;
import com.netease.nimlib.sdk.uinfo.UserService;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP on 2018-5-10.
 */

public class EditContactWindow extends PopupWindow {
    private final String TAG = "EditContactWindow";
    private Context mContext;
    private View mView;
    private TextView remark;
    private TextView nickname;
    private TextView mobile;
    private CircleImageView head_icon;
    private EditText et_remark;

    private String friendAccount;

    public EditContactWindow(Context context, String friendAccount) {
        super(context);
        this.friendAccount = friendAccount;
        initView(context);
        initSetting();
        initData();
    }

    private void initView(Context context) {
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.edit_contact, null);
        setContentView(mView);
        remark = (TextView) mView.findViewById(R.id.remark);
        nickname = (TextView) mView.findViewById(R.id.nickname);
        mobile = (TextView) mView.findViewById(R.id.mobile);
        head_icon = (CircleImageView) mView.findViewById(R.id.head_icon);
        et_remark = (EditText) mView.findViewById(R.id.et_remark);
        et_remark.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (Utils.length(String.valueOf(s))>15){
                    //Toast.makeText(mContext, "字数限制为15", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

/*        et_remark.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    Toast.makeText(mContext, "FriendAccount is " + getFriendAccount() + ", 备注是" + et_remark.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                    if (isMyFriend(getFriendAccount())) {
                        updateFriend(getFriendAccount(), et_remark.getText().toString().trim());
                    } else {
                        Toast.makeText(mContext, "非好友", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });*/

        et_remark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                System.out.println("EditManyChatWindow --->actionId is "+actionId);
                //当actionId == XX_SEND 或者 XX_DONE时都触发
                //或者event.getKeyCode == ENTER 且 event.getAction == ACTION_DOWN时也触发
                //注意，这是一定要判断event != null。因为在某些输入法上会返回null。
                if (actionId == EditorInfo.IME_ACTION_SEND
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction())) {
                    if (et_remark.getText().toString().trim().isEmpty()){
                        Toast.makeText(mContext, "你还没有输入内容", Toast.LENGTH_SHORT).show();
                    }else if (Utils.length(et_remark.getText().toString().trim())>15){
                        Toast.makeText(mContext, "字数限制为15", Toast.LENGTH_SHORT).show();
                    }else {
                        //处理事件
                        //Toast.makeText(mContext, "FriendAccount is " + getFriendAccount() + ", 备注是" + et_remark.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                        if (isMyFriend(getFriendAccount())) {
                            updateFriend(getFriendAccount(), et_remark.getText().toString().trim());
                        } else {
                            Toast.makeText(mContext, "非好友", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
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

    private void initData() {
        System.out.println("-->friendAccount="+friendAccount);
        HttpHelper.setHttpListener(httpListener);
        getMobile(friendAccount);

        HttpHelper.downloadPicture(mContext, NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null
                        ? null : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, head_icon); //头像

        //优先显示备注名
        String aliasName = NIMSDK.getFriendService().getFriendByAccount(friendAccount) == null
                ? null : NIMSDK.getFriendService().getFriendByAccount(friendAccount).getAlias(); //获取备注
        //String resultName = !TextUtils.isEmpty(aliasName) ? aliasName : contactBeens.get(position).getName();
        String name = NIMClient.getService(UserService.class).getUserInfo(friendAccount) == null
                ? null : NIMClient.getService(UserService.class).getUserInfo(friendAccount).getName(); //获取昵称
        String remarkStr = !TextUtils.isEmpty(aliasName) ? aliasName : name;
        remark.setText(aliasName);
        nickname.setText("("+name+")");
        if (remarkStr != null) {
            System.out.println("remarkStr="+remarkStr);
            if (Utils.length(remarkStr) >15) {
                et_remark.setText("" + Utils.getStrByLength(remarkStr, 11) + "...");
                //et_remark.setSelection(15);
            }else {
                et_remark.setText(remarkStr);
                //et_remark.setSelection(Utils.length(remarkStr));
            }
        }
    }

    //获取手机号
    private void getMobile(final String account){
        HttpHelper.getMobileByAccid(account);
    }

    private HttpListener<BaseBean> httpListener = new HttpListener<BaseBean>() {
        @Override
        public void fail(Throwable e, String type) {
            if (type.equals(HttpHelper.GET_MOBILE_BY_ACCID_TYPE)){
                System.out.println("type=" + type);
                e.printStackTrace();
                Toast.makeText(mContext, "获取信息失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void success(BaseBean baseBean, String type) {
            if (baseBean != null) {
                System.out.println("" + baseBean.toString());
            }
            if (type.equals(HttpHelper.GET_MOBILE_BY_ACCID_TYPE)){
                if (baseBean != null) {
                    mobile.setText(baseBean.getMobile());
                }
            }
        }
    };

    //更新备注
    public void updateFriend(final String friendAccount, final String remark) {
        // 更新备注名
        Map<FriendFieldEnum, Object> map = new HashMap<>();
        map.put(FriendFieldEnum.ALIAS, remark);
        NIMClient.getService(FriendService.class).updateFriendFields(friendAccount, map)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        System.out.println("update friend success param=" + param);
                        Toast.makeText(mContext, "成功修改好友备注", Toast.LENGTH_SHORT).show();
                        onEditFriendListener.onEditFriend(position, friendAccount, true);
                        dismiss();
                    }

                    @Override
                    public void onFailed(int code) {
                        System.out.println("update friend fail code=" + code);
                        onEditFriendListener.onEditFriend(position, friendAccount, false);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public boolean isMyFriend(final String friendAccount) {
        boolean result = NIMClient.getService(FriendService.class).isMyFriend(friendAccount);
        return result;
    }

    private int position;

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setFriendAccount(String friendAccount) {
        this.friendAccount = friendAccount;
    }

    public String getFriendAccount() {
        return friendAccount;
    }


    public OnEditFriendListener onEditFriendListener;

    public void setOnEditFriendListener(OnEditFriendListener onEditFriendListener) {
        this.onEditFriendListener = onEditFriendListener;
    }

    public interface OnEditFriendListener{
        void onEditFriend(int position, String friendAccount, boolean success);
    }
}
