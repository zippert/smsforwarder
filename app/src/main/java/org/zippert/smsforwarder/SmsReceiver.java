package org.zippert.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            String number = context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(SetupActivity.KEY_SHARED_PREF_NUMBER, null);
            if (number != null) {
                for (SmsMessage message : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    String returningMessage = "[" + message.getOriginatingAddress() + "] " + message.getMessageBody();
                    SmsManager.getDefault().sendTextMessage(number, null, returningMessage, null, null);
                }
            }
        }
    }
}
