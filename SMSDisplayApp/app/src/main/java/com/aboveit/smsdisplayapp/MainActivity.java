package com.aboveit.smsdisplayapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemClickListerner {

    private RecyclerView recyclerView;
    private TextView blankText, senderDetail, messageDetail, timeDetail;
    private LinearLayoutCompat detailLayout;
    private ImageView blankImage, crossImage;
    private SMSAdapter adapter;
    private List<SMSMessage> messages;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = "MainActivity";
    private BroadcastReceiver SMSReceiver;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        blankImage = findViewById(R.id.blankImg);
        blankText = findViewById(R.id.blanktext);
        detailLayout = findViewById(R.id.detail_layout);
        senderDetail = findViewById(R.id.sender);
        messageDetail = findViewById(R.id.message);
        timeDetail = findViewById(R.id.time);
        crossImage = findViewById(R.id.cross);
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        adapter = new SMSAdapter(messages, this);
        recyclerView.setAdapter(adapter);
        if (sharedPref.getBoolean(Constants.INIT_LAUNCH_KEY, true)) {
            editor.putBoolean(Constants.INIT_LAUNCH_KEY, false);
            editor.apply();
            Utility.requestReadContactsPermission(this);
        } else {
            handleFetchCall();
        }
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailLayout.setVisibility(View.GONE);
            }
        });
        detailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing, just block the area
            }
        });
    }

    private void handleFetchCall() {
        Log.d(TAG, "handleFetchCall()");
        if (Utility.checkReadPermission(this)) {
            fetchSMS();
        } else {
            Toast.makeText(this, "Not permitted to access texts", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSwipeFreshLayout() {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "SwipeRefreshLayout >> onRefresh()");
                mSwipeRefreshLayout.setRefreshing(true);
                handleFetchCall();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "permission requeste code: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.READ_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_SMS permission granted");
                Utility.requestReceivePermission(this);
                fetchSMS();
            } else {
                blankImage.setImageResource(R.drawable.no_permission);
                blankImage.setVisibility(View.VISIBLE);
                blankText.setText(R.string.no_permission_text);
                blankText.setVisibility(View.VISIBLE);
                Log.d(TAG, "READ_SMS permission denied");
            }
        } else if (requestCode == Constants.RECEIVE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECEIVE_SMS permission granted");
                initReceiver();
            } else {
                Log.d(TAG, "RECEIVE_SMS permission denied, initiating swipe...");
                initSwipeFreshLayout();
            }
        } else if (requestCode == Constants.READ_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_CONTACTS permission granted");
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Constants.ALLOW_READ_CONTACTS_KEY, true);
                editor.apply();
            } else {
                Log.d(TAG, "READ_CONTACTS permission denied");
            }
            Utility.requestReadPermission(this);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchSMS() {
        Log.d(TAG, "fetchSMS()");
        messages.clear();
        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String sender = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    @SuppressLint("Range") long timestamp = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    messages.add(new SMSMessage(sender, message, timestamp));
                } while (cursor.moveToNext());
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }
        if (adapter.getItemCount() == 0) {
            blankImage.setVisibility(View.VISIBLE);
            blankText.setText(R.string.no_messages);
            blankText.setVisibility(View.VISIBLE);
        } else {
            blankImage.setVisibility(View.GONE);
            blankText.setVisibility(View.GONE);
        }
    }

    private void initReceiver() {
        Log.d(TAG, "InitReceiver()");
        SMSReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "SMSReceiver >> onReceive()");
                if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        if (pdus != null) {
                            for (Object pdu : pdus) {
                                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                                String messageBody = smsMessage.getMessageBody();
                                String sender = Utility.getContactName(smsMessage.getOriginatingAddress(), context, false);
                                Log.d(TAG, "onReceive() >> info: " + sender + ": " + messageBody);
                                Toast.makeText(context, sender + ": " + messageBody, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    handleFetchCall();
                }
            }
        };
        this.registerReceiver(SMSReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() >> allowReadContacts: " + sharedPref.getBoolean(Constants.ALLOW_READ_CONTACTS_KEY, false));
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        if (SMSReceiver != null) {
            this.unregisterReceiver(SMSReceiver);
        }
        super.onStop();
    }

    @Override
    public void onItemClick(String sender, String message, Long time) {
        senderDetail.setText(sender);
        messageDetail.setText(message);
        timeDetail.setText(Utility.formatTimestamp(time, Constants.DETAILED_TIME_PATTERN));
        detailLayout.setVisibility(View.VISIBLE);
    }
}