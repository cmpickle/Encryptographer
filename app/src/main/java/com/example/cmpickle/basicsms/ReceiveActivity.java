package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReceiveActivity extends Activity implements AdapterView.OnItemClickListener {

    private static ReceiveActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    ArrayList<String> phoneNum = new ArrayList<>();

    public static ReceiveActivity instance() {
        return inst;
    }

    @Override
    protected void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSmsInbox();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/TerminusTTF-4.40.1.ttf");

        refreshSmsInbox();
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, "address IS NOT NULL) GROUP BY (address", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int timeMillis = smsInboxCursor.getColumnIndex("date");

        Calendar cal = Calendar.getInstance();
        cal.set(1969, 12, 31, 17, 0);
        long baseTime = cal.getTimeInMillis();

        if(indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;

        arrayAdapter.clear();
        phoneNum.clear();

        do {
            String dateString = smsInboxCursor.getString(timeMillis);
            long dateTime = Long.valueOf(dateString);
            long finalTime = dateTime + baseTime;
            Date date = new Date(finalTime);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd h:mm aa");
            String dateText = format.format(date);
            String str = ContactLookup.getContactDisplayNameByNumber(smsInboxCursor.getString(indexAddress), this) + "\n"
                    + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";
            phoneNum.add(smsInboxCursor.getString(indexAddress));
            arrayAdapter.add(str);
        } while(smsInboxCursor.moveToNext());

        smsInboxCursor.close();
    }

    public void updateList(final String smsMessage, String toPhone) {
        arrayAdapter.insert(smsMessage, 0);
        phoneNum.add(0, toPhone);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(ReceiveActivity.this, ConversationActivity.class);
        intent.putExtra("phoneNum", phoneNum.get(position));
        startActivity(intent);
    }

    public void goToCompose(View v) {
        Intent intent = new Intent(ReceiveActivity.this, SendSMSActivity.class);
        startActivity(intent);
    }

}
