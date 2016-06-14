package com.example.cmpickle.basicsms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReceiveActivity extends Activity implements AdapterView.OnItemClickListener {

    private static ReceiveActivity inst;
    ArrayList<Sms> contactMessageList = new ArrayList<>();
    ListView smsListView;
    ContactAdapter contactAdapter;
    ArrayList<String> phoneNum = new ArrayList<>();

    ImageButton btnCompose;

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

        setDefault();

        refreshSmsInbox();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        smsListView = (ListView) findViewById(R.id.SMSList);
        contactAdapter = new ContactAdapter(this, R.layout.recieve_conversation_item, contactMessageList);
        smsListView.setAdapter(contactAdapter);
        smsListView.setOnItemClickListener(this);

        btnCompose = (ImageButton) findViewById(R.id.btnCompose);
        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ReceiveActivity.this, SendSMSActivity.class);
                startActivity(intent);
            }
        });

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

        contactAdapter.clear();
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
            contactAdapter.add(Contact.openPhoto(Contact.getContactIDFromNumber(smsInboxCursor.getString(indexAddress), this), this), ContactLookup.getContactDisplayNameByNumber(smsInboxCursor.getString(indexAddress), this), smsInboxCursor.getString(indexBody), dateText);
        } while(smsInboxCursor.moveToNext());

        smsInboxCursor.close();
    }

    public void updateList(final Sms smsMessage, String toPhone)
    {
        contactAdapter.insert(smsMessage, 0);
        phoneNum.add(0, toPhone);
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Intent intent = new Intent(ReceiveActivity.this, ConversationActivity.class);
        intent.putExtra("phoneNum", phoneNum.get(position));
        startActivity(intent);
    }

    @TargetApi(19)
    private void setDefault() {
        final String myPackageName = getPackageName();
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
            // App is not default.
            // Show the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.VISIBLE);

            // Set up a button that allows the user to change the default SMS app
            Button button = (Button) findViewById(R.id.change_default_app);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent =
                            new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                            myPackageName);
                    startActivity(intent);
                }
            });
        } else {
            // App is the default.
            // Hide the "not currently set as the default SMS app" interface
            View viewGroup = findViewById(R.id.not_default_app);
            viewGroup.setVisibility(View.GONE);
        }
    }


}
