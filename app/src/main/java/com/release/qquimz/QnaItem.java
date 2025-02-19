package com.release.qquimz;

public class QnaItem {

    String date;
    String title;
    String content;
    String result;
    String category;
    String likeCnt;
    long commentCnt;
    String checked;
    String nickname;
    String quizTitle;

    public QnaItem(String date, String title, String content, String likeCnt, long commentCnt, String checked, String nickname, String quizTitle) {
        this.date = date;
        this.title = title;
        this.content = content;
        this.likeCnt = likeCnt;
        this.commentCnt = commentCnt;
        this.checked = checked;
        this.nickname = nickname;
        this.quizTitle = quizTitle;
    }


    public void setDate(String date) {
        this.date = date;
    }

    public void settitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLikeCnt(String likeCnt) {
        this.likeCnt = likeCnt;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }


    public void setCommentCnt(long commentCnt) {
        this.commentCnt = commentCnt;
    }

    public String getChecked() {
        return checked;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {return date.toString(); }

    public String getResult() {
        return result;
    }

    public String getCategory() {
        return category;
    }

    public String getLikeCnt() {
        return (likeCnt);
    }

    public String getCommentCnt() {
        return String.valueOf(commentCnt);
    }

    public String getNickname() {
        return nickname;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

}
