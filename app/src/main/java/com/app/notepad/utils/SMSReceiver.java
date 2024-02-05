package com.app.notepad.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

import java.util.Objects;

public class SMSReceiver  extends BroadcastReceiver {
    public SmsListener smsListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), SmsRetriever.SMS_RETRIEVED_ACTION)){
            Bundle bundle=intent.getExtras();
            Status status= (Status) bundle.get(SmsRetriever.EXTRA_STATUS);

            switch (status.getStatusCode()){
                case CommonStatusCodes.SUCCESS:
                    Intent messageIntent=bundle.getParcelable(SmsRetriever.EXTRA_CONSENT_INTENT);
                    smsListener.onSuccess(messageIntent);
                    break;
                    case CommonStatusCodes.TIMEOUT:
                        smsListener.onFailure();
                        break;
            }
        }

    }

    public interface SmsListener{
        void onSuccess(Intent intent);
        void onFailure();
    }
}
