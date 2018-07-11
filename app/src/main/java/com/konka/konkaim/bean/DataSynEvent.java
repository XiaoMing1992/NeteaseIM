package com.konka.konkaim.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2018-6-12.
 */

public class DataSynEvent {
    public static final String TYPE_TEAM_HUNGUP = "team_hungup";
    public static final String TYPE_ONE_TO_ONE_HUNGUP = "one_to_one_hungup";
    public static final String TYPE_ONE_TO_ONE_CHAT_RECEIVE = "one_to_one_chat_receive";
    public static final String TYPE_ONE_TO_ONE_CHAT_OUT = "one_to_one_chat_out";
    public static final String TYPE_TEAM_CHAT_MSG = "team_chat_msg";
    public static final String TYPE_TEAM_CHAT_MSG_REJECT = "team_chat_msg_reject";
    public static final String TYPE_TEAM_CHAT_OUT = "team_chat_out";
    public static final String TYPE_TEAM_CHAT_MIDDLE_INVITE = "team_chat_middle_invite";
    public static final String TYPE_TEAM_CHAT_NOT_RECEIVE = "team_chat_not_receive";

    public static final String TYPE_ADD_FRIEND = "add_friend";
    public static final String TYPE_ACCEPT_FRIEND = "accept_friend";
    public static final String TYPE_REFRESH_FRIEND = "refresh_friend";

    public static final String TYPE_KICKOUT = "kickout";
    public static final String TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_TINYVIEW = "team_chat_middle_invite_from_tinyview";
    public static final String TYPE_TEAM_CHAT_LAST_OUT = "team_chat_last_out";

    public static final String TYPE_TEAM_CHAT_MIDDLE_INVITE_FROM_NORMAL = "team_chat_middle_invite_from_normal";

    private String type;
    private String account;
    private String teamId;
    private List<String> accounts;
    private String fromAccount;
    private int position;
    private String roomName;

    public DataSynEvent(String type, String account, String teamId){
        this.type = type;
        this.account = account;
        this.teamId = teamId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount() {
        return account;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    @Override
    public String toString() {
        return "DataSynEvent[type="+type+", account="+account+", teamId="+teamId+", position="+position+"]";
    }
}
