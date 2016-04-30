package com.example.cmpickle.basicsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Got to onRecieve!", Toast.LENGTH_SHORT).show();

        Bundle intentExtras = intent.getExtras();

        if(intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";

            for(int i = 0; i < sms.length; i++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();
                long timeMillis = smsMessage.getTimestampMillis();

                Date date = new Date(timeMillis);
                AtomicReference<SimpleDateFormat> format = new AtomicReference<>(new SimpleDateFormat("dd/MM/yy"));
                String dateText = format.get().format(date);

//                smsMessageStr += address + " at " + "\t" + dateText + "\n";
//                smsMessageStr += smsBody + "\n";

                smsMessageStr = ContactLookup.getContactDisplayNameByNumber(address, context) + "\n"
                        + smsBody + "\n" + dateText + "\n";
            }

            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            ReceiveActivity inst = ReceiveActivity.instance();
            if(inst != null)
                inst.updateList(smsMessageStr);
        }

        ReceiveActivity.instance().refreshSmsInbox();
    }
}
