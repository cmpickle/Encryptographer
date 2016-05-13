package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmpickle.basicsms.Contact;

import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<Sms> {

    Context context;
    int layoutResourceId;
//    Contact data[] = null;
    ArrayList<Sms> contacts;

//    public ContactAdapter(Context context, int layoutResourceId, Contact[] data) {
//        super(context, layoutResourceId, data);
//        this.layoutResourceId = layoutResourceId;
//        this.context = context;
//        this.data = data;
//    }

    public ContactAdapter(Context context, int layoutResourceId, ArrayList<Sms> contacts) {
        super(context, layoutResourceId, contacts);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.contacts = contacts;
    }

    public void add(Bitmap bitmap, String name, String body, String date) {
        contacts.add(new Sms(bitmap, name, body, date));
    }

    public void insert(Sms sms, int position) {
        contacts.add(position,sms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContactHolder holder = null;

        if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContactHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.receiveImage);
            holder.txtTitle = (TextView)row.findViewById(R.id.receiveTitle);
            holder.receiveBody = (TextView)row.findViewById(R.id.receiveBody);
            holder.receiveDate = (TextView)row.findViewById(R.id.receiveDate);

            row.setTag(holder);
        } else {
            holder = (ContactHolder)row.getTag();
        }

//        Contact contact = data[position];
        Sms contact = contacts.get(position);
        holder.txtTitle.setText(contact.name);
        holder.imgIcon.setImageBitmap(contact.photo);
        holder.receiveBody.setText(contact.body);
        holder.receiveDate.setText(contact.date);

        return row;
    }

    static class ContactHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView receiveBody;
        TextView receiveDate;
    }
//
//    @Override
//    public void clear() {
//        super();
//
//    }
}