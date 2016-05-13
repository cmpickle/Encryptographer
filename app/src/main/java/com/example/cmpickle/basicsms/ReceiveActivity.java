package com.example.cmpickle.basicsms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ReceiveActivity extends Activity implements AdapterView.OnItemClickListener {

    private static ReceiveActivity inst;
//    ArrayList<String> smsMessageList = new ArrayList<>();
    ArrayList<Sms> contactMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    ContactAdapter contactAdapter;
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
//        smsListView.setAdapter(arrayAdapter);
        smsListView.setAdapter(contactAdapter);
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

        contactAdapter.clear();
//        arrayAdapter.clear();
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
//            arrayAdapter.add(str);
            contactAdapter.add(openPhoto(getContactIDFromNumber(smsInboxCursor.getString(indexAddress), this)), ContactLookup.getContactDisplayNameByNumber(smsInboxCursor.getString(indexAddress), this), smsInboxCursor.getString(indexBody), dateText);
        } while(smsInboxCursor.moveToNext());

        smsInboxCursor.close();
    }

    public void updateList(final String smsMessage, String toPhone)
    {
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


    public Bitmap openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
//        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd = getContentResolver().openAssetFileDescriptor(photoUri, "r");
            return BitmapFactory.decodeStream(fd.createInputStream());
        } catch(IOException e) {

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.unnamed);

            return bm;
        }
//        Cursor cursor = getContentResolver().query(photoUri,
//                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
//        if (cursor == null) {
//            return null;
//        }
//        try {
//            if (cursor.moveToFirst()) {
//                byte[] data = cursor.getBlob(0);
//                if (data != null) {
//                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
//                }
//            }
//        } finally {
//            cursor.close();
//        }

    }

    public static long getContactIDFromNumber(String contactNumber, Context context) {
        String UriContactNumber = Uri.encode(contactNumber);
        long phoneContactID = 1;
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, UriContactNumber),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while (contactLookupCursor.moveToNext()) {
            phoneContactID = contactLookupCursor.getLong(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }
}
