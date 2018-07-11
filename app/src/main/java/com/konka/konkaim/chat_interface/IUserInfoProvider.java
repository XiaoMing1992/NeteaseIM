package com.konka.konkaim.chat_interface;


import com.netease.nimlib.sdk.uinfo.model.UserInfo;

/**
 * 用户相关资料提供者
 *
 */

public abstract class IUserInfoProvider {

    /**
     * 获取用户资料
     * @param account 用户账号
     * @return UserInfo 用户资料
     */
    public abstract UserInfo getUserInfo(String account);

    /**
     * 获取用户显示名称
     * @param account 用户账号
     * @return 用户显示名称
     */
    public abstract String getUserDisplayName(String account);
}

