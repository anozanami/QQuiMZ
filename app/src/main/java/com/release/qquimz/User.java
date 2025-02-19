package com.release.qquimz;

import java.util.List;

public class User {
    private String token; // Firestore에서 문서 ID
    private int point;
    private List<String> isFriend;
    private String id; // 유저의 실제 ID
    private String rankingIcon;
    private String nickname;
    public User(){
        //기본 생성자
    }

    // 생성자
    public User(String token, String nickname ,int point, List<String> isFriend, String id, String rankingIcon) {
        this.token = token;
        this.nickname = nickname;
        this.point = point;
        this.isFriend = isFriend;
        this.id = id;
        this.rankingIcon = rankingIcon;
    }

    // Getter 및 Setter 메서드

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public List<String> getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(List<String> isFriend) {
        this.isFriend = isFriend;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRankingIcon() {
        return rankingIcon;
    }

    public void setRankingIcon(String rankingIcon) {
        this.rankingIcon = rankingIcon;
    }



    public String getNickname() {
        return nickname;
    }

}
