package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-5-11.
 */

public class GroupBean{

    private int id;             //群聊记录id
    private String accid;       //网易云通信ID
    private String name;        //群聊备注名称
    private int group_chat_id;  //群聊id
    private String update_time; //最后修改时间
    private String code;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getAccid() {
        return accid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setGroup_chat_id(int group_chat_id) {
        this.group_chat_id = group_chat_id;
    }

    public int getGroup_chat_id() {
        return group_chat_id;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "GroupBean["+"id="+id+", accid="+accid+", name="+name+", group_chat_id="+group_chat_id+", update_time="+update_time+"]";
    }
}
