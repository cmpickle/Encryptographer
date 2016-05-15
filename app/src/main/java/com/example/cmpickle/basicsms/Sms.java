package com.example.cmpickle.basicsms;

import android.graphics.Bitmap;

public class Sms {

    public Bitmap photo;
    public String name;
    public String body;
    public String date;

    public int viewType = 1;

    public Sms() {
        super();
    }

    public Sms(Bitmap photo, String name, String body, String date) {
        super();
        this.photo=photo;
        this.name=name;
        this.body=body;
        this.date=date;
    }

    public Sms(Bitmap photo, String name, String body, String date, int viewType) {
        super();
        this.photo=photo;
        this.name=name;
        this.body=body;
        this.date=date;
        this.viewType=viewType;
    }
}
