package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConversationActivity extends Activity implements AdapterView.OnItemClickListener {

    Bundle conversationBundle;
    String phoneNo;
    String name;
    Bitmap photo;
    Bitmap mePhoto;
    private static ConversationActivity inst;
    ArrayList<Sms> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ContactAdapter contactAdapter;

    ImageButton sendSmsBtn;
    ImageButton encryption;
    EditText smsMessageET;
    TextView title;


    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            SendSMS.checkIfEmpty(getApplicationContext(), sendSmsBtn, smsMessageET);
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    };

    public static ConversationActivity instance() {
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
        setContentView(R.layout.activity_conversation);

        title = (TextView) findViewById(R.id.conversationTitle);

        smsListView = (ListView) findViewById(R.id.SMSList);
        contactAdapter = new ContactAdapter(this, R.layout.conversation_received_item, smsMessageList);
        smsListView.setAdapter(contactAdapter);
        smsListView.setOnItemClickListener(this);

        conversationBundle = getIntent().getExtras();
        phoneNo = conversationBundle.getString("phoneNum");
        photo = Contact.openPhoto(Contact.getContactIDFromNumber(phoneNo, this), this);
        mePhoto = Contact.openPhoto(Contact.getContactIDFromNumber("1", this), this);

        name = ContactLookup.getContactDisplayNameByNumber(phoneNo, this);
        title.setText(name);

        refreshSmsInbox();


        sendSmsBtn = (ImageButton) findViewById(R.id.btnSendSMS);
        encryption = (ImageButton) findViewById(R.id.toggleEncryption);
        if(SendSMS.encrypted)
            encryption.setImageResource(R.drawable.ic_enhanced_encryption_blue);
        else
            encryption.setImageResource(R.drawable.ic_no_encryption_blue);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);
//        registerForContextMenu(smsMessageET);

        smsMessageET.addTextChangedListener(textWatcher);

        SendSMS.checkIfEmpty(this, sendSmsBtn, smsMessageET);

        //OnClick listener for the send button
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSMS.sendSms(getApplicationContext(), phoneNo, smsMessageET.getText().toString());
                reset();
            }
        });

//        smsMessageET.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                openOptionsMenu();
//                return true;
//            }
//        });
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }

    public void refreshSmsInbox() {
        Calendar cal = Calendar.getInstance();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, " address = \'" + phoneNo + "\'", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexType = smsInboxCursor.getColumnIndex("type");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        int readCol = smsInboxCursor.getColumnIndex("read");

        cal.set(1969, 12, 31, 17, 0);
        long baseTime = cal.getTimeInMillis();



        if(indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        contactAdapter.clear();
        do {
            if(smsInboxCursor.getInt(readCol)==0)
                markMessageRead(this, phoneNo, smsInboxCursor.getString(indexBody));
            String str;
            String dateString = smsInboxCursor.getString(timeMillis);
            long dateTime = Long.valueOf(dateString);
            long finalTime = dateTime + baseTime;
            Date date = new Date(finalTime);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd h:mm aa");
            String dateText = format.format(date);
            if(smsInboxCursor.getInt(indexType)==2)
                contactAdapter.add(mePhoto, "Me", smsInboxCursor.getString(indexBody), dateText);
            else
                contactAdapter.add(photo, ContactLookup.getContactDisplayNameByNumber(phoneNo, this), smsInboxCursor.getString(indexBody), dateText);


        } while(smsInboxCursor.moveToNext());

        smsInboxCursor.close();
    }

    public void updateList(final Sms sms) {
        contactAdapter.insert(sms, contactAdapter.getCount()-1);
        contactAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        try {
            String smsMessage = smsMessageList.get(position).body;
//            String address = smsMessages[0];
//            String smsMessage = "";
//
//            for(int i = 1; i < smsMessages.length-1; i++) {
//                smsMessage += smsMessages[i];
//            }

            String smsMessageStr = smsMessageList.get(position).name + "\n";
            smsMessageStr += Encryption.decode(smsMessage);

            MyToast.show(this, smsMessageStr, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void goToReceive(View v) {
        finish();
    }

    private void reset() {
                smsMessageET.setText("");

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                refreshSmsInbox();
    }

    public void toggleEncryption(View v) {
        if(SendSMS.encrypted)
        {
            encryption.setImageResource(R.drawable.ic_no_encryption_blue);
            SendSMS.encrypted = false;
        } else
        {
            encryption.setImageResource(R.drawable.ic_enhanced_encryption_blue);
            SendSMS.encrypted = true;
        }
    }

    private void markMessageRead(Context context, String number, String body) {

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try{

            while (cursor.moveToNext()) {
                if ((cursor.getString(cursor.getColumnIndex("address")).equals(number)) && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
                    if (cursor.getString(cursor.getColumnIndex("body")).startsWith(body)) {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                        return;
                    }
                }
            }
        }catch(Exception e)
        {
            Log.e("Mark Read", "Error in Read: "+e.toString());
        }
    }
}
