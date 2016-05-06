package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ConversationActivity extends Activity implements AdapterView.OnItemClickListener {

    Bundle conversationBundle;
    String phoneNo;
    private static ConversationActivity inst;
    ArrayList<String> smsMessageList = new ArrayList<>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;


    ImageButton sendSmsBtn;
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
    protected void onResume() {
        super.onResume();
        refreshSmsInbox();
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

        conversationBundle = getIntent().getExtras();
        phoneNo = conversationBundle.getString("phoneNum");

        refreshSmsInbox();


        sendSmsBtn = (ImageButton) findViewById(R.id.btnSendSMS);
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
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms"), null, " address = \'" + phoneNo + "\'", null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexType = smsInboxCursor.getColumnIndex("type");
        int timeMillis = smsInboxCursor.getColumnIndex("date");

        cal.set(1969, 12, 31, 17, 0);
        long baseTime = cal.getTimeInMillis();



        String name = ContactLookup.getContactDisplayNameByNumber(phoneNo, this);

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

        smsInboxCursor.close();
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, arrayAdapter.getCount()-1);
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
            smsMessageStr += Encryption.decode(smsMessage);

            MyToast.show(this, smsMessageStr, true);
            //Toast.makeText(this, smsMessageStr, Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void goToReceive(View v) {
        finish();
    }

    private void sendSms() {

        String toPhone = phoneNo;
        String smsMessage = Encryption.encode(smsMessageET.getText().toString());

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
        ImageButton b = (ImageButton) findViewById(R.id.btnSendSMS);

        if(smsMessageET.getText().toString().trim().isEmpty()) {
            setImageButtonEnabled(this, false, b, R.drawable.ic_send_white);
//            b.setEnabled(false);
        }
        else {
            setImageButtonEnabled(this, true, b, R.drawable.ic_send_white);
//            b.setEnabled(true);
        }
    }

    /**
     * Sets the specified image buttonto the given state, while modifying or
     * "graying-out" the icon as well
     *
     * @param enabled The state of the menu item
     * @param item The menu item to modify
     * @param iconResId The icon ID
     */
    public static void setImageButtonEnabled(Context ctxt, boolean enabled, ImageButton item,
                                             int iconResId) {
        item.setEnabled(enabled);
        Drawable originalIcon = ResourcesCompat.getDrawable(ctxt.getResources(), iconResId, null);
        Drawable icon = enabled ? originalIcon : convertDrawableToGrayScale(originalIcon);
        item.setImageDrawable(icon);
    }

    /**
     * Mutates and applies a filter that converts the given drawable to a Gray
     * image. This method may be used to simulate the color of disable icons in
     * Honeycomb's ActionBar.
     *
     * @return a mutated version of the given drawable with a color filter
     *         applied.
     */
    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

}
