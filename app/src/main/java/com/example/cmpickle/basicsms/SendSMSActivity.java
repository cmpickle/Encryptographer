package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SendSMSActivity extends Activity {

    ImageButton sendSmsBtn;
    EditText toPhoneNumber;
    EditText smsMessageET;

    static final int PICK_CONTACT_REQUEST = 1;

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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        sendSmsBtn = (ImageButton) findViewById(R.id.btnSendSMS);
        toPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNo);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);

        toPhoneNumber.addTextChangedListener(textWatcher);
        smsMessageET.addTextChangedListener(textWatcher);

        checkIfEmpty();

        //OnClick listener for the send button
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms();
            }
        });

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == PICK_CONTACT_REQUEST) {
            if(resultCode == RESULT_OK) {
                Uri pickedPhoneNumber = intent.getData();

                toPhoneNumber.setText(ContactLookup.getNumberByURI(pickedPhoneNumber, this));
            }
        }
    }

    /**
     * Sends a SMS using the phone number in the toPhoneNumber EditText and the
     * message in the smsMessageET EditText
     */
    private void sendSms() {

        String toPhone = toPhoneNumber.getText().toString();
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

        goToInbox(null);
    }

    /**
     * Starts the Receive Activity
     *
     * @param v - The current view
     */
    public void goToInbox(View v) {
        finish();
    }

    private void checkIfEmpty() {
        ImageButton b = (ImageButton) findViewById(R.id.btnSendSMS);

        String s1 = toPhoneNumber.getText().toString();
        String s2 = smsMessageET.getText().toString();

        if(s1.trim().isEmpty() || s2.trim().isEmpty())
            b.setEnabled(false);
        else
            b.setEnabled(true);
    }

    public void doLaunchContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST);
    }
}
