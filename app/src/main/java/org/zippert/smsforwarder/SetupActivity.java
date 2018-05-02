package org.zippert.smsforwarder;

import android.Manifest;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SetupActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 0;
    static final String KEY_SHARED_PREF_NUMBER = "number";
    private View mPermissionView;
    private Button mPermissionButton;
    private EditText mPhoneNumberEditView;
    private View mSetupView;
    private Button mActivateButton;
    private boolean mIsEnabled = false;
    private SmsReceiver mSmsReceiver = new SmsReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mPermissionView = findViewById(R.id.permissionView);
        mPermissionButton = findViewById(R.id.permissionButton);
        mPhoneNumberEditView = findViewById(R.id.phoneNumber);
        mSetupView = findViewById(R.id.setupView);
        mActivateButton = findViewById(R.id.activateButton);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        } else {
            showMainUI();
        }
    }

    @Override
    protected void onDestroy() {
        if (mIsEnabled) {
            mIsEnabled = false;
            unregisterReceiver(mSmsReceiver);
        }
        super.onDestroy();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showMainUI();
            } else {
                showPermissionErrorUI();
            }
        }
    }

    private void showMainUI() {
        mSetupView.setVisibility(View.VISIBLE);
        mPermissionView.setVisibility(View.GONE);

        mPhoneNumberEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String number = charSequence.toString();
                mActivateButton.setEnabled(!TextUtils.isEmpty(number) && Patterns.PHONE.matcher(charSequence).matches());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mActivateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeNumber(mPhoneNumberEditView.getText().toString());

                if (mIsEnabled) {
                    mIsEnabled = false;
                    unregisterReceiver(mSmsReceiver);
                } else {
                    mIsEnabled = true;
                    registerReceiver(mSmsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
                }

                mActivateButton.setText(mIsEnabled ? R.string.deactivate : R.string.activate);
            }
        });

        mPhoneNumberEditView.setText(getNumber());

    }

    private void showPermissionErrorUI() {
        mSetupView.setVisibility(View.GONE);
        mPermissionView.setVisibility(View.VISIBLE);
        mPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions();
            }
        });
    }

    private void storeNumber(String number) {
        getSharedPreferences("prefs", MODE_PRIVATE).edit().putString(KEY_SHARED_PREF_NUMBER, number).apply();
    }

    private String getNumber() {
        return getSharedPreferences("prefs", MODE_PRIVATE).getString(KEY_SHARED_PREF_NUMBER, "");
    }
}
