package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class ConversationActivity extends Activity implements AdapterView.OnItemClickListener {

    Bundle b;
    String bundle;
    private static ConversationActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;


    Button sendSmsBtn;
    EditText smsMessageET;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            checkIfEmpty();
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, smsMessageList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);

        b = getIntent().getExtras();
        bundle = b.getString("phoneNum");

        Toast.makeText(this, bundle, Toast.LENGTH_SHORT).show();

        refreshSmsInbox();


        sendSmsBtn = (Button) findViewById(R.id.btnSendSMS);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);

        smsMessageET.addTextChangedListener(textWatcher);

        checkIfEmpty();

        //OnClick listener for the send button
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });
    }

    public void refreshSmsInbox() {
        Calendar cal = Calendar.getInstance();
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, " address = \'" + bundle + "\'", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexType = smsInboxCursor.getColumnIndex("type");
        int timeMillis = smsInboxCursor.getColumnIndex("date");

        cal.set(1969, 12, 31, 17, 0);
        long baseTime = cal.getTimeInMillis();



        String name = getContactDisplayNameByNumber(bundle);

        if(indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        arrayAdapter.clear();
        do {
            String str;
            String dateString = smsInboxCursor.getString(timeMillis);
            long dateTime = Long.valueOf(dateString);
            long finalTime = dateTime + baseTime;
            Date date = new Date(finalTime);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd h:mm aa");
            String dateText = format.format(date);
            if(smsInboxCursor.getInt(indexType)==2)
                str = "Me" + "\n" + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";
            else
                str = name + "\n" + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";
            arrayAdapter.add(str);
        } while(smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

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
            smsMessageStr += decode(smsMessage);
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void goToReceive(View v) {
        finish();
    }

    /**
     * Decodes a string encoded by @encode
     *
     * @param input
     *            --The string to be decoded
     * @return --The decoded string
     */
    public static String decode(String input)
    {
        Random rand = new Random(42);
        char[] letters = new char[input.length()];
        String result = "";

        for (int i = 0; i < input.length(); i++)
            letters[i] = input.charAt(i);

        // modifies the Char[]

        for (int j = 0; j < input.length(); j++)
        {
            letters[j] -= rand.nextInt(100);
            // Appends the chars from letters to the end of the blank string result
            // to achieve the encoded string
            if (j % 3 == 0 || j % 3 == 2)
            {
                letters[j] /= 2;
                letters[j] -= 1;
            }
            // modifies the ASCII value of each char by adding 47 then
            // multiplies it by two
            letters[j] = (char) ((letters[j] / 2) - 47);
        }

        for (int k = 0; k < input.length(); k++)
            result += letters[k];

        // returns the encoded string

        return result;
    }


    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "" + number;

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    private void sendSms() {

        String toPhone = bundle;
        String smsMessage = encode(smsMessageET.getText().toString());

        if(toPhone.isEmpty() || smsMessage.isEmpty())
            return;

        try {
            //SmsManager is used to send sms messages
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(toPhone, null, smsMessage, null, null);

            //notify user that the sms was sent
            Toast.makeText(this, "SMS sent", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        smsMessageET.setText("");

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        refreshSmsInbox();
    }

    private void checkIfEmpty() {
        Button b = (Button) findViewById(R.id.btnSendSMS);

        String s2 = smsMessageET.getText().toString();

        if(s2.trim().isEmpty())
            b.setEnabled(false);
        else
            b.setEnabled(true);
    }

    /**
     * Encodes a string.
     *
     * @param input
     *            --The string to be encoded
     * @return --Encoded version of the string
     */
    private static String encode(String input)
    {
        Random rand = new Random(42);
        char[] letters = new char[input.length()];
        String result = "";

        // Copies over the characters of the string input to the char[] letters

        for(int i = 0; i < input.length(); i++)
            letters[i] = input.charAt(i);

        // modifies the Char[]

        for(int j = 0; j < input.length(); j++)
        {
            // modifies the ASCII value of each char by adding 47 then
            // multiplies it by two

            letters[j] = (char) ((letters[j] + 47) * 2);
            if(j % 3 == 0 || j % 3 == 2)
            {
                letters[j] += 1;
                letters[j] *= 2;
            }
            letters[j] += rand.nextInt(100);
        }

        // Appends the chars from letters to the end of the blank string result
        // to achieve the encoded string

        for (int k = 0; k < input.length(); k++)
            result += letters[k];

        // returns the encoded string

        return result;
    }
}
