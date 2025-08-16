package com.example.finalyear;
public class cinfo {
    String name, mobile;
    float rating;

    public cinfo() {}

    public cinfo(String name, String mobile, float rating) {
        this.name = name;
        this.mobile = mobile;
        this.rating = rating;
    }

    public String getName() { return name; }
    public String getMobile() { return mobile; }
    public float getRating() { return rating; }
}
