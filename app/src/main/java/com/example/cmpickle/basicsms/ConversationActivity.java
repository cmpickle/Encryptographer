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
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ConversationActivity extends Activity implements AdapterView.OnItemClickListener {

    Bundle b;
    String bundle;
    private static ConversationActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    String address = "+18016949546"; //

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
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, " address = \'" + bundle + "\'", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int timeMillis = smsInboxCursor.getColumnIndex("date");
        Date date = new Date(timeMillis);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
        String dateText = format.format(date);

        if(indexBody < 0 || !smsInboxCursor.moveToFirst())
            return;
        arrayAdapter.clear();
        do {
            String str = smsInboxCursor.getString(indexAddress) + "\n"
                    + smsInboxCursor.getString(indexBody) + "\n" + dateText + "\n";
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
        Intent intent = new Intent(ConversationActivity.this, ReceiveActivity.class);
        startActivity(intent);
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
}
