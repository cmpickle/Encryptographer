package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ConversationActivity extends Activity implements AdapterView.OnItemClickListener {

    Bundle conversationBundle;
    String phoneNo;
    String name;
//    private static ConversationActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;

    ImageButton sendSmsBtn;
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

//    public static ConversationActivity instance() {
//        return inst;
//    }

    @Override
    protected void onStart() {
        super.onStart();
//        inst = this;
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
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        conversationBundle = getIntent().getExtras();
        phoneNo = conversationBundle.getString("phoneNum");

        name = ContactLookup.getContactDisplayNameByNumber(phoneNo, this);
        title.setText(name);

        refreshSmsInbox();


        sendSmsBtn = (ImageButton) findViewById(R.id.btnSendSMS);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);

        smsMessageET.addTextChangedListener(textWatcher);

        SendSMS.checkIfEmpty(this, sendSmsBtn, smsMessageET);

        //OnClick listener for the send button
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSMS.sendSms(getApplicationContext(), phoneNo, Encryption.encode(smsMessageET.getText().toString()));
                reset();
            }
        });
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, " address = \'" + phoneNo + "\'", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexType = smsInboxCursor.getColumnIndex("type");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        Date date = new Date(timeMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        String dateText = format.format(date);

        if(indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        arrayAdapter.clear();
        do {
            String str;
            if(smsInboxCursor.getInt(indexType)==2)
                str = "Me" + "\n" + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";
            else
                str = name + "\n" + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";

            arrayAdapter.add(str);
        } while(smsInboxCursor.moveToNext());

        smsInboxCursor.close();
    }

//    public void updateList(final String smsMessage) {
//        arrayAdapter.insert(smsMessage, arrayAdapter.getCount()-1);
//        arrayAdapter.notifyDataSetChanged();
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        try {
            String[] smsMessages = smsMessageList.get(position).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";

            for(int i = 1; i < smsMessages.length-1; i++) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
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
}
