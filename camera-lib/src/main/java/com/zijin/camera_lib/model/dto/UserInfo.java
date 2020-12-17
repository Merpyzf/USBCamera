package com.zijin.camera_lib.model.dto;

/**
 * Description: 用户信息
 * Date: 12/9/20
 *
 * @author wangke
 */
public class UserInfo {
    public UserInfo() {
    }

    private String retCode;
    private String retMsg;
    private String tid;
    private String userNo;
    private String userName;
    private String postName;
    private String faceBase64;

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

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public String getFaceBase64() {
        return faceBase64;
    }

    public void setFaceBase64(String faceBase64) {
        this.faceBase64 = faceBase64;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "retCode='" + retCode + '\'' +
                ", retMsg='" + retMsg + '\'' +
                ", tid='" + tid + '\'' +
                ", userNo='" + userNo + '\'' +
                ", userName='" + userName + '\'' +
                ", postName='" + postName + '\'' +
                '}';
    }

    public boolean isVerifySuccess() {
        return "000000".equals(retCode);
    }
}
