package com.konka.konkaim.bean;

import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * Created by HP on 2018-5-31.
 */

public class TeamBean {
    private String teamId;
    private String teamCreator;
    private List<TeamMember> memberList;
    private String teamNickname;

    public TeamBean(String teamId, String teamCreator, List<TeamMember>memberList, String teamNickname){
        this.teamId = teamId;
        this.teamCreator = teamCreator;
        this.memberList = memberList;
        this.teamNickname = teamNickname;
    }

    public void setMemberList(List<TeamMember> memberList) {
        this.memberList = memberList;
    }

    public List<TeamMember> getMemberList() {
        return memberList;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamCreator(String teamCreator) {
        this.teamCreator = teamCreator;
    }

    public String getTeamCreator() {
        return teamCreator;
    }

    public void setTeamNickname(String teamNickname) {
        this.teamNickname = teamNickname;
    }

    public String getTeamNickname() {
        return teamNickname;
    }
}
