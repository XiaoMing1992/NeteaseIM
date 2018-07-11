package com.konka.konkaim.util;

/**
 * Created by HP on 2018-5-7.
 */

public class Constant {
    public static final String BASE_URL = "https://test.kkapp.com/wangyi/";
    public static final String ADD_USER = "users/add";                        //用户注册
    public static final String CHECK_MOBILE = "users/checkMobile";           //校验手机号
    public static final String LOGIN = "users/login";                       //用户登录
    public static final String SEND_SMS_CODE = "users/sendSmsCode";         //发送验证码
    public static final String RESET_PASSWORD = "users/resetPws";         //忘记密码
    public static final String GET_ACCID_BY_MOBILE = "users/getAccidByMobile";   //根据手机号获取accid，根据手机号搜索联系人用到
    public static final String GET_MOBILE_BY_ACCID = "users/getMobileByAccid";   //根据accid获取手机号
    public static final String FIND_GROUP_CHAT = "group/findGroupChat";   //查看群聊记录
    public static final String UPDATE_GROUP_CHAT = "group/updateGroupChat";   //修改群聊备注，有记录就更新，没记录就添加

    //状态码
    public static final String USER_NOT_EXIST = "101"; //用户不存在
    public static final String USERNAME_PASSWORD_ERROR = "102"; //用户名或密码错误
    public static final String VERIFYCODE_IS_WRONG = "103"; //短信验证码错误
    public static final String RESET_PASSWORD_FAIL = "104"; //重置密码失败
    public static final String SEND_VERIFYCODE_FAIL = "105"; //发送短信失败
    public static final String MOBILE_IS_EMPTY = "106"; //手机号不能为空
    public static final String MOBILE_HAS_EXIST = "107"; //该手机号已注册
    public static final String MOBILE_IS_WRONG = "108"; //手机号不正确
    public static final String REQUEST_SUCCESS = "200"; //手机号不正确

    public static final String REGISTER_TYPE = "REGISTER"; //注册，发送验证码
    public static final String RESETPSW_TYPE = "RESETPSW"; //重置密码，发送验证码
}
