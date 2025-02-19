package com.release.qquimz;

public class CommentItem {
    String comment;
    String nickname;
    String date;

    public CommentItem(String comment, String nickname, String date) {
        this.comment = comment;
        this.nickname = nickname;
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public String getNickname() {
        return nickname;
    }

    public String getDate() {
        return date;
    }
}
