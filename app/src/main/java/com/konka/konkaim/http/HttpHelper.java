package com.konka.konkaim.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.konka.konkaim.bean.GroupBean;
import com.konka.konkaim.util.Utils;
import com.konka.konkaim.api.HttpListener;
import com.konka.konkaim.api.HttpService;
import com.konka.konkaim.bean.BaseBean;
import com.squareup.picasso.Picasso;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by HP on 2018-5-7.
 */

public class HttpHelper {
    //private final static String MOBILE_KEY_NAME = "mobile";
    //private final static String PASSWORD_KEY_NAME = "password";
    private static HttpListener httpListener;
    public static final String SMS_TYPE = "send_sms_code";
    public static final String IS_EXIST_TYPE = "is_exist";
    public static final String CHECK_LOGIN_TYPE = "check_login";
    public static final String ADD_USER_TYPE = "add_user";
    public static final String RESET_PASSWORD_TYPE = "reset_password";
    public static final String GET_ACCID_BY_MOBILE_TYPE = "get_accid_by_mobile";
    public static final String GET_MOBILE_BY_ACCID_TYPE = "get_mobile_by_accid";
    public static final String FIND_GROUP_CHAT_TYPE = "get_group_chat";
    public static final String UPDATE_GROUP_CHAT_TYPE = "update_group_chat";

    //private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void sendSmsCode(final String mobile, final String type) {
        if (mobile == null || mobile.isEmpty()) return;
        if (!Utils.isMobileNO(mobile)) return;

        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.sendSmsCode(mobile, type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, SMS_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, SMS_TYPE);
                    }
                });
    }

    public static void isExist(final String mobile) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.isExist(mobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, IS_EXIST_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, IS_EXIST_TYPE);
                    }
                });
    }

    public static void checkLogin(final String mobile, final String password) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.checkLogin(mobile, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("onError");
                        httpListener.fail(e, CHECK_LOGIN_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        System.out.println("onNext");
                        httpListener.success(baseBean, CHECK_LOGIN_TYPE);
                    }
                });
    }

    public static void addUser(final String mobile, final String password, final String smsCode) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.userAdd(mobile, password, smsCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, ADD_USER_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, ADD_USER_TYPE);
                    }
                });
    }


    public static void resetPassword(final String mobile, final String password, final String smsCode) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.resetPassword(mobile, password, smsCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, RESET_PASSWORD_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, RESET_PASSWORD_TYPE);
                    }
                });
    }

    public static void setHttpListener(HttpListener httpListener) {
        HttpHelper.httpListener = httpListener;
    }

    /*    public static BaseBean getResponseData(final String res) {
        Gson gson = new Gson();
        BaseBean baseBean = gson.fromJson(res, BaseBean.class);
        if (baseBean != null)
            System.out.println("sendSmsCode-->accid=" + baseBean.getAccid() + " code=" + baseBean.getCode()
                    + " desc=" + baseBean.getDesc() + " token=" + baseBean.getToken());
        return baseBean;
    }*/

    public static void getAccidByMobile(final String mobile) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.getAccidByMobile(mobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, GET_ACCID_BY_MOBILE_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, GET_ACCID_BY_MOBILE_TYPE);
                    }
                });
    }

    public static void getMobileByAccid(final String accid) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.getMobileByAccid(accid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, GET_MOBILE_BY_ACCID_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, GET_MOBILE_BY_ACCID_TYPE);
                    }
                });
    }

    public static void getGroupChat(final String accid, final int groupChatId) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.getGroupChat(accid, groupChatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GroupBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, FIND_GROUP_CHAT_TYPE);
                    }

                    @Override
                    public void onNext(GroupBean groupBean) {
                        httpListener.success(groupBean, FIND_GROUP_CHAT_TYPE);
                    }
                });
    }

    public static void updateGroupChat(final String accid, final String name ,final int groupChatId) {
        //获取接口实例
        HttpService httpService = MyRetrofit.getRetrofit().create(HttpService.class);
        Subscription subscription = httpService.updateGroupChat(accid, name, groupChatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        httpListener.fail(e, UPDATE_GROUP_CHAT_TYPE);
                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        httpListener.success(baseBean, UPDATE_GROUP_CHAT_TYPE);
                    }
                });
    }


    public static void downloadPicture(Context context, String url, int loadingRes, int errorRes, ImageView imageView) {
        if(TextUtils.isEmpty(url)) return;
        Log.d("Picasso", "--- Picasso ---"+url);
        Picasso.get().load(url).fit().config(Bitmap.Config.RGB_565)
                .placeholder(loadingRes)
                .error(errorRes)
                .into(imageView);

//        Picasso.with(context).load(url)
//                .fit()
//                .config(Bitmap.Config.RGB_565)
//                .placeholder(loadingRes)
//                .error(errorRes)
//                .into(imageView);
    }

    public static void downloadPictureByGlide(Context context, String url, int loadingRes, int errorRes, ImageView imageView) {
        //if(TextUtils.isEmpty(url)) return;
        Log.d("Glide", "--- Glide ---");
/*        Picasso.with(context).load(url)
                .fit()
                .config(Bitmap.Config.RGB_565)
                .placeholder(loadingRes)
                .error(errorRes)
                .into(imageView);*/

        //先加载缩略图 然后在加载全图
        Glide.with(context)
                .load(url)
                .thumbnail(0.1f)
                .placeholder(loadingRes)
                .error(errorRes)
                .into(imageView);
    }
}
