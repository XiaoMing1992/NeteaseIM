package com.konka.konkaim.user;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.http.HttpHelper;

import com.konka.konkaim.R;
import com.konka.konkaim.bean.BaseBean;
import com.konka.konkaim.ui.NickNameWindow;
import com.konka.konkaim.util.ActivityHelper;
import com.konka.konkaim.util.Constant;
import com.konka.konkaim.util.NetworkUtil;
import com.konka.konkaim.util.Utils;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mobile;
    private EditText verifyCode;
    private EditText password;
    private TextView mobile_error_tip;
    private TextView verifyCode_error_tip;
    private TextView password_error_tip;
    private Button register;
    private TextView send_verifyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        ActivityHelper.getInstance().addActivity(this);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initView() {
        mobile = (EditText) findViewById(R.id.mobile);
        verifyCode = (EditText) findViewById(R.id.verifyCode);
        password = (EditText) findViewById(R.id.password);
        mobile_error_tip = (TextView) findViewById(R.id.mobile_error_tip);
        verifyCode_error_tip = (TextView) findViewById(R.id.verifyCode_error_tip);
        password_error_tip = (TextView) findViewById(R.id.password_error_tip);
        register = (Button) findViewById(R.id.register);
        send_verifyCode = (TextView) findViewById(R.id.send_verifyCode);
        register.setOnClickListener(this);
        register.setEnabled(true);

        send_verifyCode.setOnClickListener(this);
        listener();
    }

    private void listener() {
        mobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("onTextChanged-->s=" + s + ", start=" + start + ", before=" + before + ", count=" + count);
/*                if (s.length() == 11)
                    send_verifyCode.setEnabled(true);
                else send_verifyCode.setEnabled(false);*/
            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("afterTextChanged-->" + s);
            }
        });

        mobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                }else {
                    mobile_error_tip.setVisibility(View.GONE);
                }
            }
        });

        verifyCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                }else {
                    verifyCode_error_tip.setVisibility(View.GONE);
                }
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){

                }else {
                    password_error_tip.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initData() {
        HttpHelper.setHttpListener(httpListener);
    }

    private void sendVerifyCode() {
        String mobileStr = mobile.getText().toString().trim();
        System.out.println("mobileStr=" + mobileStr);
        HttpHelper.sendSmsCode(mobileStr, Constant.REGISTER_TYPE);
    }

    private void isExist() {
        String mobileStr = mobile.getText().toString().trim();
        HttpHelper.isExist(mobileStr);
    }

    private void addUser() {
        String mobileStr = mobile.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String smsCodeStr = verifyCode.getText().toString().trim();
        HttpHelper.addUser(mobileStr, passwordStr, smsCodeStr);
    }

    private void showToast(Context context, final String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }

    private HttpListener<BaseBean> httpListener = new HttpListener<BaseBean>() {
        @Override
        public void fail(Throwable e, String type) {
            e.printStackTrace();
            System.out.println("HttpListener error type=" + type);


            if (type.equals(HttpHelper.SMS_TYPE)) {
                showToast(RegisterActivity.this, "发送短信失败");
            } else if (type.equals(HttpHelper.CHECK_LOGIN_TYPE)) {

            } else if (type.equals(HttpHelper.IS_EXIST_TYPE)) {
                showToast(RegisterActivity.this, "检查手机是否已经注册失败");
            } else if (type.equals(HttpHelper.ADD_USER_TYPE)) {
                showToast(RegisterActivity.this, "注册失败");
            }
        }

        @Override
        public void success(BaseBean baseBean, String type) {
            if (baseBean != null) {
                System.out.println("" + baseBean.toString());
            }

            if (type.equals(HttpHelper.SMS_TYPE)) {
                if (baseBean != null) {
                    if (baseBean.getCode().equals(Constant.SEND_VERIFYCODE_FAIL)) {
                        showToast(RegisterActivity.this, "发送短信失败");
                    }
                }
            } else if (type.equals(HttpHelper.IS_EXIST_TYPE)) {

                if (baseBean != null) {

                    if (baseBean.getCode().equals(Constant.MOBILE_HAS_EXIST)) {
                        mobile_error_tip.setText("*  用户已存在");
                        mobile_error_tip.setVisibility(View.VISIBLE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);

                    } else if (baseBean.getCode().equals(Constant.MOBILE_IS_WRONG)) {
                        mobile_error_tip.setText("*  手机号格式不对");
                        mobile_error_tip.setVisibility(View.VISIBLE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);

                    }else if (baseBean.getCode().equals(Constant.REQUEST_SUCCESS)) {
                        mobile_error_tip.setText("*  用户已存在");
                        mobile_error_tip.setVisibility(View.GONE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);

                        sendVerifyCode(); //发送验证码
                        showToast(RegisterActivity.this, "验证码已发");
                        myHandler.sendEmptyMessage(SEND_VERIFYCODE_START);
                        countTime();
                    }
                }

            } else if (type.equals(HttpHelper.ADD_USER_TYPE)) {

                if (baseBean != null) {
                    System.out.println(baseBean.toString());
                    UserInfoUtil.setAccid(baseBean.getAccid());
                    UserInfoUtil.setToken(baseBean.getToken());
                    UserInfoUtil.setCode(baseBean.getCode());
                    UserInfoUtil.setDesc(baseBean.getDesc());

                    if (baseBean.getCode().equals(Constant.MOBILE_HAS_EXIST)) {
                        mobile_error_tip.setText("*  用户已存在");
                        mobile_error_tip.setVisibility(View.VISIBLE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);

                    } else if (baseBean.getCode().equals(Constant.VERIFYCODE_IS_WRONG)) {
                        mobile_error_tip.setText("*  用户已存在");
                        mobile_error_tip.setVisibility(View.GONE);
                        verifyCode_error_tip.setVisibility(View.VISIBLE);
                        password_error_tip.setVisibility(View.GONE);
                    } else if (baseBean.getCode().equals(Constant.MOBILE_IS_WRONG)) {
                        mobile_error_tip.setText("*  手机号格式不对");
                        mobile_error_tip.setVisibility(View.VISIBLE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);

                    } else if (baseBean.getCode().equals(Constant.USERNAME_PASSWORD_ERROR)) {
                        mobile_error_tip.setVisibility(View.GONE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.VISIBLE);
                    } else if (baseBean.getCode().equals(Constant.REQUEST_SUCCESS)) {
                        showToast(RegisterActivity.this, "注册成功");
                        mobile_error_tip.setText("*  用户已存在");
                        mobile_error_tip.setVisibility(View.GONE);
                        verifyCode_error_tip.setVisibility(View.GONE);
                        password_error_tip.setVisibility(View.GONE);
                        //填写昵称
                        NickNameWindow nickNameWindow = new NickNameWindow(RegisterActivity.this);
                        nickNameWindow.show();
                    }
                }

/*                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);*/


            }
        }
    };

    private void countTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i=60;
                    while (i>=1) {
                        Message message = new Message();
                        message.what = SEND_VERIFYCODE_UPDATE;
                        message.arg1 = i;
                        myHandler.sendMessage(message);
                        Thread.sleep(1000);
                        i--;
                    }
                    myHandler.sendEmptyMessage(SEND_VERIFYCODE_END);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_verifyCode:
                if (!NetworkUtil.isNetworkAvailable(RegisterActivity.this)) {
                    showToast(RegisterActivity.this, "当前网络不可用，请检查网络是否连接");
                    return;
                }

                if (mobile.getText().toString().trim().isEmpty()){
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    mobile_error_tip.setText("*  手机号不能为空");
                    return;
                }else if (!Utils.isMobileNO(mobile.getText().toString().trim())) {
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    mobile_error_tip.setText("*  请填写正确的手机号");
                    return;
                }else {
                    mobile_error_tip.setVisibility(View.GONE);
                    mobile_error_tip.setText("*  用户已存在");
                    isExist();
                }

                break;
            case R.id.register:
                if (!NetworkUtil.isNetworkAvailable(RegisterActivity.this)) {
                    showToast(RegisterActivity.this, "当前网络不可用，请检查网络是否连接");
                    return;
                }

                if (mobile.getText().toString().trim().isEmpty()){
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    mobile_error_tip.setText("*  手机号不能为空");
                    return;
                }else if (!Utils.isMobileNO(mobile.getText().toString().trim())) {
                    mobile_error_tip.setVisibility(View.VISIBLE);
                    mobile_error_tip.setText("*  请填写正确的手机号");
                    return;
                }else {
                    mobile_error_tip.setVisibility(View.GONE);
                    mobile_error_tip.setText("*  用户已存在");
                    if (verifyCode.getText().toString().trim().isEmpty()){
                        verifyCode_error_tip.setText("*  验证码不能为空");
                        verifyCode_error_tip.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        verifyCode_error_tip.setText("*  验证码错误");
                        verifyCode_error_tip.setVisibility(View.GONE);
                    }
                    if (password.getText().toString().trim().length()<6 || password.getText().toString().trim().length()>20
                            ||!Utils.isDigitOrLetter(password.getText().toString().trim())){
                        password_error_tip.setVisibility(View.VISIBLE);
                        return;
                    }else {
                        password_error_tip.setVisibility(View.GONE);
                    }
                }

                showToast(RegisterActivity.this, "正在注册");
                addUser();
                break;
        }
    }

    private final int SEND_VERIFYCODE_START = 0x00;
    private final int SEND_VERIFYCODE_UPDATE = 0x01;
    private final int SEND_VERIFYCODE_END = 0x02;
    private Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEND_VERIFYCODE_START:
                    send_verifyCode.setEnabled(false);
                    break;
                case SEND_VERIFYCODE_UPDATE:
                    send_verifyCode.setText(""+msg.arg1+"s后重发");
                    send_verifyCode.setEnabled(false);
                    break;

                case SEND_VERIFYCODE_END:
                    send_verifyCode.setText("发送验证码");
                    send_verifyCode.setEnabled(true);
                    break;
            }
        }
    };
}
