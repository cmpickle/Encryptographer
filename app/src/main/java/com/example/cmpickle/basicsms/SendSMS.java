package com.example.cmpickle.basicsms;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;

public class SendSMS {
    public static boolean encrypted = true;

    public static void sendSms(Context context, String toPhone, String smsMessage) {
        if(toPhone.isEmpty() || smsMessage.isEmpty())
            return;

        smsMessage = (encrypted)? Encryption.encode(smsMessage):smsMessage;

        try {
            //SmsManager is used to send sms messages
            SmsManager smsManager = SmsManager.getDefault();
            ContentValues values = new ContentValues();

            if(smsMessage.length()<154) {
                smsManager.sendTextMessage(toPhone, null, smsMessage, null, null);
            } else {
                smsManager.sendMultipartTextMessage(toPhone, null, smsManager.divideMessage(smsMessage), null, null);
            }
            values.put("address", toPhone);
            values.put("body", smsMessage);
            values.put("date", Calendar.getInstance().getTimeInMillis());
            values.put("type", 2);
            values.put("read", true);
            context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);

            //notify user that the sms was sent
            Toast.makeText(context, "SMS sent", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkIfEmpty(Context context, ImageButton b, EditText smsMessageET) {
        if(smsMessageET.getText().toString().trim().isEmpty()) {
            setImageButtonEnabled(context, false, b, R.drawable.ic_send_blue);
        }
        else {
            setImageButtonEnabled(context, true, b, R.drawable.ic_send_blue);
        }
    }

    public static void checkIfEmpty(Context context, ImageButton b, EditText toPhone, EditText smsMessageET) {
        if(smsMessageET.getText().toString().trim().isEmpty() || toPhone.getText().toString().trim().isEmpty()) {
            setImageButtonEnabled(context, false, b, R.drawable.ic_send_blue);
        }
        else {
            setImageButtonEnabled(context, true, b, R.drawable.ic_send_blue);
        }
    }

    /**
     * Sets the specified image button to the given state, while modifying or
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
