package com.example.cmpickle.basicsms;

import android.graphics.Bitmap;

public class Contact {
    public Bitmap photo;
    public String name;

    public Contact() {
        super();
    }

    public Contact(Bitmap photo, String name) {
        super();
        this.photo = photo;
        this.name = name;
    }
}