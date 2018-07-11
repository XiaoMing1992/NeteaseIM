package com.konka.konkaim.chat_interface;

/**
 * 群组相关数据提供者
 *
 */

public abstract class ITeamDataProvider {

    /**
     * 获取显示名称。
     */
    public abstract String getDisplayNameWithoutMe(String teamId, String account);

    /**
     * 获取显示名称。用户本人显示“我”
     */
    public abstract String getTeamMemberDisplayName(String teamId, String account);
}
