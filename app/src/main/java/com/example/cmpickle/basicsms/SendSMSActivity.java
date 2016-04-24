package com.example.cmpickle.basicsms;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendSMSActivity extends ActionBarActivity {

    Button sendSmsBtn;
    EditText toPhoneNumber;
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        sendSmsBtn = (Button) findViewById(R.id.btnSendSMS);
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
        Button b = (Button) findViewById(R.id.btnSendSMS);

        String s1 = toPhoneNumber.getText().toString();
        String s2 = smsMessageET.getText().toString();

        if(s1.trim().isEmpty() || s2.trim().isEmpty())
            b.setEnabled(false);
        else
            b.setEnabled(true);
    }

//    Button buttonPickContact = (Button)findViewById(R.id.selectContact);
//    buttonPickContact.OnClickListener(new Button.OnClickListener(){
//
//        @Override
//        public void onClick(View arg0) {
//            // TODO Auto-generated method stub
//
//
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
//            startActivityForResult(intent, 1);
//
//
//        }});
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // TODO Auto-generated method stub
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == RQS_PICK_CONTACT){
//            if(resultCode == RESULT_OK){
//                Uri contactData = data.getData();
//                Cursor cursor =  managedQuery(contactData, null, null, null, null);
//                cursor.moveToFirst();
//
//                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
//
//                //contactName.setText(name);
//                toPhoneNumber.setText(number);
//                //contactEmail.setText(email);
//            }
//        }
//    }

}
