package com.release.qquimz;

public class HistoryItem {
    String date;
    String title;
    String content;
    String result;
    String category;
    String id;

    public HistoryItem(String date, String title, String content, String result, String category) {
        this.date = date;
        this.title = title;
        this.content = content;
        this.result = result;
        this.category = category;
        this.id = id;
    }

    public String getDate() {
        return date.toString();
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getResult() {
        return result;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setName(String title) {
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

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "date='" + date + '\'' +
                ", name='" + title + '\'' +
                ", content='" + content + '\'' +
                ", result='" + result + '\'' +
                ", category='" + category + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
