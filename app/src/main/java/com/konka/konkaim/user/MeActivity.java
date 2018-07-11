package com.konka.konkaim.user;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konka.konkaim.R;
import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.bean.BaseBean;
import com.konka.konkaim.http.HttpHelper;
import com.konka.konkaim.ui.AboutWindow;
import com.konka.konkaim.ui.CircleImageView;
import com.konka.konkaim.ui.ExitWindow;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.PrefenceUtil;
import com.konka.konkaim.util.Utils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;

import java.util.HashMap;
import java.util.Map;

public class MeActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout layout_top;
    private RelativeLayout person_layout;
    private RelativeLayout state_layout;
    private RelativeLayout about_layout;
    private LinearLayout layout_other;
    private CircleImageView head_icon;
    private TextView nickname;
    private TextView mobile;
    private TextView tv_state;
    private Button btn_logout;

    //修改昵称界面
    private RelativeLayout layout_nickname_window;
    private EditText edit_nick_name;
    private TextView tv_nick_name_count;
    private TextView nick_name_error_tip;
    private Button btn_save;

    private String[] stateArrays = {"可通话", "忙碌"};
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        ActivityHelper.getInstance().addActivity(this);
        initView();
        initData();
        listener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HttpHelper.setHttpListener(httpListener);
    }


    private void initView() {
        //修改昵称界面
        layout_nickname_window = (RelativeLayout) findViewById(R.id.layout_nickname_window);
        edit_nick_name = (EditText) findViewById(R.id.edit_nick_name);
        tv_nick_name_count = (TextView) findViewById(R.id.tv_nick_name_count);
        nick_name_error_tip = (TextView) findViewById(R.id.nick_name_error_tip);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);

        //个人资料、状态和关于界面
        head_icon = (CircleImageView) findViewById(R.id.head_icon);
        nickname = (TextView) findViewById(R.id.nickname);
        mobile = (TextView) findViewById(R.id.mobile);
        tv_state = (TextView) findViewById(R.id.tv_state);
        btn_logout = (Button)findViewById(R.id.btn_logout);

        layout_other = (LinearLayout) findViewById(R.id.layout_other);
        person_layout = (RelativeLayout) findViewById(R.id.person_layout);
        state_layout = (RelativeLayout) findViewById(R.id.state_layout);
        about_layout = (RelativeLayout) findViewById(R.id.about_layout);
        layout_top = (LinearLayout) findViewById(R.id.layout_top);
        person_layout.setOnClickListener(this);
        state_layout.setOnClickListener(this);
        about_layout.setOnClickListener(this);
        btn_logout.setOnClickListener(this);
    }

    private void initData() {
        String my_state = PrefenceUtil.get(MeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME+UserInfoUtil.getAccid(), PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY);
        if (my_state == null) {
            tv_state.setText(stateArrays[0]);
            PrefenceUtil.set(MeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME +UserInfoUtil.getAccid(),
                    PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY, tv_state.getText().toString().trim());
        }else {
            tv_state.setText(my_state);
        }

        HttpHelper.downloadPicture(MeActivity.this,
                NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getAvatar(),
                R.drawable.img_default, R.drawable.img_default, head_icon); //加载头像

        //昵称
        String _nicknameStr = NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName();
        if (Utils.length(_nicknameStr) > 17)
        {
            nickname.setText("" + Utils.getStrByLength(_nicknameStr, 17) + "...");
        }else {
            nickname.setText(_nicknameStr);
        }

        System.out.println("-->MeActivity to get Phone, accid is "+UserInfoUtil.getAccid());
        getMobile(UserInfoUtil.getAccid());//获取手机号
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

    private void listener() {
        about_layout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    AboutWindow aboutWindow = new AboutWindow(MeActivity.this);
                    aboutWindow.show();
                    return true;
                }
                return false;
            }
        });

        person_layout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    layout_nickname_window.setVisibility(View.VISIBLE);
                    layout_other.setVisibility(View.GONE);
                    layout_top.setVisibility(View.GONE);
                    String nicknameStr = NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName();

                    edit_nick_name.setText(nicknameStr);
                    edit_nick_name.setSelection(nicknameStr.length());
                    return true;
                }
                return false;
            }
        });

        edit_nick_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tv_nick_name_count.setText("" + s.length() + "/15");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv_nick_name_count.setText("" + s.length() + "/15");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        state_layout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        ++count;
                        if (count >= stateArrays.length)
                            count = count % stateArrays.length;
                        tv_state.setText(stateArrays[count]);
                        PrefenceUtil.set(MeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME+ UserInfoUtil.getAccid(),
                                PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY, tv_state.getText().toString().trim());
                        //UserInfoUtil.setCurrent_user_state(tv_state.getText().toString().trim());
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        --count;
                        if (count < 0)
                            count = (count + stateArrays.length);
                        tv_state.setText(stateArrays[count % stateArrays.length]);
                        PrefenceUtil.set(MeActivity.this, PrefenceUtil.CURRENT_USER_STATE_FILENAME+ UserInfoUtil.getAccid(),
                                PrefenceUtil.CURRENT_USER_STATE_FILENAME_KEY, tv_state.getText().toString().trim());
                        //UserInfoUtil.setCurrent_user_state(tv_state.getText().toString().trim());
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void updateUserInfo(final String _nickname) {
        Map<UserInfoFieldEnum, Object> fields = new HashMap<>(1);
        fields.put(UserInfoFieldEnum.Name, _nickname);//更新用户本人的名称
        NIMClient.getService(UserService.class).updateUserInfo(fields)
                .setCallback(new RequestCallbackWrapper<Void>() {
                    @Override
                    public void onResult(int code, Void result, Throwable exception) {
                        System.out.println("updateUserInfo, code=" + code);
/*                        Intent intent = new Intent();
                        intent.setClass(mContext, HomeActivity.class);
                        mContext.startActivity(intent);*/

                        layout_nickname_window.setVisibility(View.GONE);
                        layout_other.setVisibility(View.VISIBLE);
                        layout_top.setVisibility(View.VISIBLE);
                        nickname.setText(NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName()); //昵称
                        person_layout.requestFocus();

                        //setOnUpdatePersonalInfoListener(HomeActivity.class);
                        //onUpdatePersonalInfoListener.OnUpdatePersonalInfo();

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                String newNicknameStr = edit_nick_name.getText().toString().trim();
                if (TextUtils.isEmpty(newNicknameStr)){
                    nick_name_error_tip.setVisibility(View.VISIBLE);
                    return;
                }
                nick_name_error_tip.setVisibility(View.GONE);
                System.out.println("--->newNicknameStr="+newNicknameStr);
                updateUserInfo(newNicknameStr);
                break;
            case R.id.person_layout:
                layout_nickname_window.setVisibility(View.VISIBLE);
                layout_other.setVisibility(View.GONE);
                layout_top.setVisibility(View.GONE);
                String nicknameStr = NIMClient.getService(UserService.class).getUserInfo(UserInfoUtil.getAccid()).getName();

                edit_nick_name.setText(nicknameStr);
                edit_nick_name.setSelection(nicknameStr.length());
                break;
            case R.id.state_layout:

                break;
            case R.id.about_layout:
                AboutWindow aboutWindow = new AboutWindow(MeActivity.this);
                aboutWindow.show();
                break;
            case R.id.btn_logout:
                ExitWindow exitWindow = new ExitWindow(MeActivity.this);
                exitWindow.show();
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (layout_nickname_window.getVisibility() == View.VISIBLE) {
                    layout_nickname_window.setVisibility(View.GONE);
                    nick_name_error_tip.setVisibility(View.GONE);
                    layout_other.setVisibility(View.VISIBLE);
                    layout_top.setVisibility(View.VISIBLE);
                    person_layout.requestFocus();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnUpdatePersonalInfoListener(OnUpdatePersonalInfoListener onUpdatePersonalInfoListener) {
        this.onUpdatePersonalInfoListener = onUpdatePersonalInfoListener;
    }

    private OnUpdatePersonalInfoListener onUpdatePersonalInfoListener;
    public interface OnUpdatePersonalInfoListener{
        void OnUpdatePersonalInfo();
    }
}
