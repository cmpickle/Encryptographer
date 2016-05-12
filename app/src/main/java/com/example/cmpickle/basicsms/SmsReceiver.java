package com.example.cmpickle.basicsms;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SmsReceiver extends BroadcastReceiver{

    public static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Got to onRecieve!", Toast.LENGTH_SHORT).show();

        Bundle intentExtras = intent.getExtras();
        String address = "";
        SmsMessage smsMessage = null;

        if(intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";

            for(int i = 0; i < sms.length; i++) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = intentExtras.getString("format");
                    smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);
                } else
                {
                    smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                }

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

                ContentValues values = new ContentValues();
                values.put("address", address);
                values.put("body", smsBody);
                values.put("date", timeMillis);
                values.put("type", 1);
                values.put("read", false);
                context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
            }


            ReceiveActivity inst = ReceiveActivity.instance();
            if(inst != null)
                inst.updateList(smsMessageStr, address);
            ConversationActivity instConv = ConversationActivity.instance();
            if(instConv != null) {
                instConv.refreshSmsInbox();
                Toast.makeText(context, "updating conversation list", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
