package com.example.cmpickle.basicsms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

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
            SendSMS.checkIfEmpty(getApplicationContext(), sendSmsBtn, toPhoneNumber, smsMessageET);
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

        SendSMS.checkIfEmpty(this, sendSmsBtn, toPhoneNumber, smsMessageET);

        //OnClick listener for the send button
        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSMS.sendSms(getApplicationContext(), toPhoneNumber.getText().toString(), Encryption.encode(smsMessageET.getText().toString()));
                goToInbox(null);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Starts the Receive Activity
     *
     * @param v - The current view
     */
    public void goToInbox(View v) {
        finish();
    }

    public void doLaunchContactPicker(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST);
    }
}
