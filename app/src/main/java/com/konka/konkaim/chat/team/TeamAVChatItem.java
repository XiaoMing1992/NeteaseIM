package com.konka.konkaim.chat.team;

/**
 * Created by HP on 2018-6-12.
 */

public class TeamAVChatItem {
    public int type; // 类型：0 加号；1 正常surface
    public int state; // 当前状态：0 等待 1 正在播放 2 未接通 3 已挂断
    public String teamId;
    public String account;

    public TeamAVChatItem(int state, String teamId, String account){
        this.state = state;
        this.teamId = teamId;
        this.account = account;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
