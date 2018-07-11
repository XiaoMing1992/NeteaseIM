package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-7-2.
 */

public class FriendBean {
    private int id;
    private String record_time;
    private String my_account;
    private String friend_account;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setRecord_time(String record_time) {
        this.record_time = record_time;
    }

    public String getRecord_time() {
        return record_time;
    }

    public void setMy_account(String my_account) {
        this.my_account = my_account;
    }

    public String getMy_account() {
        return my_account;
    }

    public void setFriend_account(String friend_account) {
        this.friend_account = friend_account;
    }

    public String getFriend_account() {
        return friend_account;
    }
}
