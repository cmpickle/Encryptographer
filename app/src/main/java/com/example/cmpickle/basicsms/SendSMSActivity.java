package com.example.cmpickle.basicsms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

public class SendSMSActivity extends ActionBarActivity {

    Button sendSmsBtn;
    EditText toPhoneNumber;
    EditText smsMessageET;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        sendSmsBtn = (Button) findViewById(R.id.btnSendSMS);
        toPhoneNumber = (EditText) findViewById(R.id.editTextPhoneNo);
        smsMessageET = (EditText) findViewById(R.id.editTextSMS);

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
        String smsMessage = encode(smsMessageET.getText().toString());

        try {
            //SmsManager is used to send sms messages
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(toPhone, null, smsMessage, null, null);

            //notify user that the sms was sent
            Toast.makeText(this, "SMS sent", Toast.LENGTH_LONG).show();

            //After sending message clear EditText fields
//            toPhoneNumber.setText("");
//            smsMessageET.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Receive Activity
     *
     * @param v - The current view
     */
    public void goToInbox(View v) {
        Intent intent = new Intent(SendSMSActivity.this, ReceiveActivity.class);
        startActivity(intent);
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
