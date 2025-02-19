package com.release.qquimz;

import java.util.List;

public class UserAccount {
    private List<String> isFriend;
    public UserAccount(){

    }


    public String id;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getIsFriend(){
        return isFriend;
    }

    public void setIsFriend(List<String> isFriend){
        this.isFriend = isFriend;
    }

    private String points;

    public String getPoints(){
        return points;
    }
    public void setPoints(String points) {
        this.points = points;
    }

    private String nickname;

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }


}