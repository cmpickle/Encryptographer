package com.example.cmpickle.basicsms;

import android.graphics.Bitmap;

public class Sms {

    public Bitmap photo;
    public String name;
    public String body;
    public String date;

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
}
