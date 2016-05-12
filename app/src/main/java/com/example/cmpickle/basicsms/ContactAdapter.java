package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<String> {

    private Activity context;
    private List<String> userList;
    private int layout;

    public ContactAdapter(Activity context, int layout, List<String> userList) {
        super(context, layout, userList);
        this.context=context;
        this.layout=layout;
        this.userList=userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
    }
}
