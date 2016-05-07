package com.example.cmpickle.basicsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SMSBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Got to onRecieve!", Toast.LENGTH_SHORT).show();

        Bundle intentExtras = intent.getExtras();
        String address = "";

        if(intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";

            for(int i = 0; i < sms.length; i++) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                String smsBody = smsMessage.getMessageBody();
                address = smsMessage.getOriginatingAddress();
                long timeMillis = smsMessage.getTimestampMillis();

                Calendar cal = Calendar.getInstance();
                cal.set(1969, 12, 31, 17, 0);
                long baseTime = cal.getTimeInMillis();

                String dateString = ""+timeMillis;
                long dateTime = Long.valueOf(dateString);
                long finalTime = dateTime + baseTime;
                Date date = new Date(finalTime);
                SimpleDateFormat format = new SimpleDateFormat("MM/dd h:mm aa");
                String dateText = format.format(date);

                smsMessageStr = ContactLookup.getContactDisplayNameByNumber(address, context) + "\n"
                        + smsBody + "\n" + dateText + "\n";
            }

            Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            ReceiveActivity inst = ReceiveActivity.instance();
            if(inst != null)
                inst.updateList(smsMessageStr, address);
        }

        ReceiveActivity.instance().refreshSmsInbox();
    }
}
