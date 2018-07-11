package com.konka.konkaim.api;

import com.konka.konkaim.bean.GroupBean;
import com.konka.konkaim.util.Constant;
import com.konka.konkaim.bean.BaseBean;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by HP on 2018-5-8.
 */

public interface HttpService {
    //POST请求
    @FormUrlEncoded
    @POST(Constant.SEND_SMS_CODE)
    Observable<BaseBean> sendSmsCode(@Field("mobile") String mobile, @Field("type") String type); //发送验证码

    //POST请求
    @FormUrlEncoded
    @POST(Constant.LOGIN)
    Observable<BaseBean> checkLogin(@Field("mobile") String mobile, @Field("password") String password); //检查登陆

    //POST请求
    @FormUrlEncoded
    @POST(Constant.CHECK_MOBILE)
    Observable<BaseBean> isExist(@Field("mobile") String mobile); //判断手机号是否存在

    //POST请求
    @FormUrlEncoded
    @POST(Constant.ADD_USER)
    Observable<BaseBean> userAdd(@Field("mobile") String mobile, @Field("password") String password, @Field("smsCode") String smsCode); //增加用户

    //POST请求
    @FormUrlEncoded
    @POST(Constant.RESET_PASSWORD)
    Observable<BaseBean> resetPassword(@Field("mobile") String mobile, @Field("password") String password, @Field("smsCode") String smsCode); //重置密码

    //POST请求
    @FormUrlEncoded
    @POST(Constant.GET_ACCID_BY_MOBILE)
    Observable<BaseBean> getAccidByMobile(@Field("mobile") String mobile); //根据手机号来获取accid

    //POST请求
    @FormUrlEncoded
    @POST(Constant.GET_MOBILE_BY_ACCID)
    Observable<BaseBean> getMobileByAccid(@Field("accid") String accid); //根据accid来获取手机号

    //群聊接口
    //POST请求
    @FormUrlEncoded
    @POST(Constant.FIND_GROUP_CHAT)
    Observable<GroupBean> getGroupChat(@Field("accid") String accid, @Field("groupChatId")int groupChatId); //查看群聊备注

    //POST请求
    @FormUrlEncoded
    @POST(Constant.UPDATE_GROUP_CHAT)
    Observable<BaseBean> updateGroupChat(@Field("accid") String accid, @Field("name") String name, @Field("groupChatId")int groupChatId); //修改群聊备注
}
