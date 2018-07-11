package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-5-31.
 */

public class TeamMemberBean {

    private int origin;
    private String time;
    private String headIconUrl;
    private String remark;
    private int state;

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getOrigin() {
        return origin;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return remark;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setHeadIconUrl(String headIconUrl) {
        this.headIconUrl = headIconUrl;
    }

    public String getHeadIconUrl() {
        return headIconUrl;
    }

    @Override
    public String toString() {
        return "[origin=" + origin + ", time=" + time + ", headIconUrl=" + headIconUrl + ", remark=" + remark + ", state=" + state + "]";
    }
}
