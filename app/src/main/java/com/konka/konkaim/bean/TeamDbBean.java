package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-6-5.
 */

public class TeamDbBean {
    private int id;
    private String record_time;
    private String teamId;
    private String my_account;
    private String team_name;

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

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setMy_account(String my_account) {
        this.my_account = my_account;
    }

    public String getMy_account() {
        return my_account;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getTeam_name() {
        return team_name;
    }
}
