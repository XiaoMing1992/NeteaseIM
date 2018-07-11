package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-6-5.
 */

public class DbBean {
    private int id;
    private String chat_from;
    private String chat_to;
    private String record_time;
    //private String teamId;
    private String my_account;
    private String friend_account;
    //private int is_team;
    private int is_connect;
    private int is_friend;
    private int is_out;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setChat_from(String chat_from) {
        this.chat_from = chat_from;
    }

    public String getChat_from() {
        return chat_from;
    }

    public void setChat_to(String chat_to) {
        this.chat_to = chat_to;
    }

    public String getChat_to() {
        return chat_to;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getRecord_time() {
        return record_time;
    }

/*    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }*/

    public void setMy_account(String my_account) {
        this.my_account = my_account;
    }

    public String getMy_account() {
        return my_account;
    }

/*    public void setIs_team(int is_team) {
        this.is_team = is_team;
    }

    public int getIs_team() {
        return is_team;
    }*/

    public void setIs_connect(int is_connect) {
        this.is_connect = is_connect;
    }

    public int getIs_connect() {
        return is_connect;
    }

    public void setIs_friend(int is_friend) {
        this.is_friend = is_friend;
    }

    public int getIs_friend() {
        return is_friend;
    }

    public void setFriend_account(String friend_account) {
        this.friend_account = friend_account;
    }

    public String getFriend_account() {
        return friend_account;
    }

    public void setIs_out(int is_out) {
        this.is_out = is_out;
    }

    public int getIs_out() {
        return is_out;
    }
}
