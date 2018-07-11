package com.konka.konkaim.bean;

/**
 * Created by HP on 2018-5-7.
 */

public class BaseBean {

    /**
     * accid : string
     * code : string
     * desc : string
     * token : string
     * mobile : string
     */

    private String accid;
    private String code;
    private String desc;
    private String token;
    private String mobile;

    public String getAccid() {
        return accid;
    }

    public void setAccid(String accid) {
        this.accid = accid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    @Override
    public String toString() {
        return "BaseBean[accid="+accid+", code="+code+", desc="+desc+", token="+token+", mobile"+mobile+"]";
    }
}
