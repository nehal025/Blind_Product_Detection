package com.example.blindproductdetection.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Cost {
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("img")
    @Expose
    private String img;
    @SerializedName("cash")
    @Expose
    private String cash;
    @SerializedName("bookNow")
    @Expose
    private String bookNow;



    public Cost(String title, String img, String cash,String bookNow) {
        super();
        this.title = title;
        this.img = img;
        this.cash = cash;
        this.bookNow=bookNow;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }
    public String getBookNow() {
        return bookNow;
    }

    public void setBookNow(String bookNow) {
        this.bookNow = bookNow;
    }
}
