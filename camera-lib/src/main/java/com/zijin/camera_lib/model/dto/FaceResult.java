package com.zijin.camera_lib.model.dto;

/**
 * Description:
 * Date: 11/26/20
 *
 * @author wangke
 */
public class FaceResult {
    /**
     * retCode : 000000
     * retMsg : 成功
     * tid : 943.16068905947050000
     * access_token : eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsImF1dGhvcml0aWVzIjpbIlJPTEVfMTAwMDEiXSwianRpIjoiYWE3NzkwNzMtNWU1Yi00ODEwLWJlOTYtNjA5YjlkNzk2YWM1IiwiY2xpZW50X2lkIjoic2NyZWVuIiwic2NvcGUiOlsiYWxsIl19.VAjb5zwAKJs0U9TzezRrK7l92sIUYdKNS1jPfQgfuec
     * jti : aa779073-5e5b-4810-be96-609b9d796ac5
     * refresh_token : eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJhZG1pbiIsImF1dGhvcml0aWVzIjpbIlJPTEVfMTAwMDEiXSwianRpIjoiMTliZmIzOWMtZTc2Ny00ZDQ1LThkOTYtYjc4NGJkZjYyY2YwIiwiY2xpZW50X2lkIjoic2NyZWVuIiwic2NvcGUiOlsiYWxsIl0sImF0aSI6ImFhNzc5MDczLTVlNWItNDgxMC1iZTk2LTYwOWI5ZDc5NmFjNSJ9.VeMg_3Iu490XpenBi0Fx8lYobj6nqs0FZcjqNvHN44I
     * scope : all
     * token_type : bearer
     */

    public FaceResult() {
    }

    private String retCode;
    private String retMsg;
    private String tid;
    private String access_token;
    private String jti;
    private String refresh_token;
    private String scope;
    private String token_type;

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    @Override
    public String toString() {
        return "FaceResult{" +
                "retCode='" + retCode + '\'' +
                ", retMsg='" + retMsg + '\'' +
                ", tid='" + tid + '\'' +
                ", access_token='" + access_token + '\'' +
                ", jti='" + jti + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", scope='" + scope + '\'' +
                ", token_type='" + token_type + '\'' +
                '}';
    }

    public boolean isVerifySuccess() {
        return "000000".equals(retCode);
    }
}
