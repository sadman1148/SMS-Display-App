package com.aboveit.smsdisplayapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SMSAdapter adapter;
    private List<SMSMessage> messages;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messages = new ArrayList<>();
        adapter = new SMSAdapter(messages);
        recyclerView.setAdapter(adapter);
        if (Utility.isFirstLaunch) {
            Utility.isFirstLaunch = false;
            Utility.requestReadContactsPermission(this);
            Utility.requestReceivePermission(this);
            Utility.requestReadPermission(this);
        } else {
            handleFetchCall();
            if (!Utility.checkReceivePermission(this)) {
                Log.d(TAG, "RECEIVE_SMS permission not found, initiating swipe...");
                initSwipeFreshLayout();
            }
            if (!Utility.checkReadContactsPermission(this)) {
                Log.d(TAG, "READ_CONTACTS permission not found");
                Utility.allowReadContacts = false;
            }
        }
    }

    private void handleFetchCall() {
        Log.d(TAG, "handleFetchCall()");
        if (Utility.checkReadPermission(this)) {
            fetchSMS();
        } else {
            Utility.requestReadPermission(this);
        }
    }

    private void initSwipeFreshLayout() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
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
                fetchSMS();
            } else {
                Toast.makeText(this, "Not Permitted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "READ_SMS permission denied");
            }
        } else if (requestCode == Constants.RECEIVE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RECEIVE_SMS permission granted");
            } else {
                Log.d(TAG, "RECEIVE_SMS permission denied");
            }
        } else if (requestCode == Constants.READ_CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_CONTACTS permission granted");
                Utility.allowReadContacts = true;
            } else {
                Log.d(TAG, "READ_CONTACTS permission denied");
                Utility.allowReadContacts = false;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchSMS() {
        Log.d(TAG, "fetchSMS()");
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
    }

    private final BroadcastReceiver SMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive()");
            if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            handleFetchCall();
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            String messageBody = smsMessage.getMessageBody();
                            String sender = Utility.getContactName(smsMessage.getOriginatingAddress(), context, false);
                            Log.d(TAG, "text received. info: " + sender + ": " + messageBody);
                            Toast.makeText(context, sender + ": " + messageBody, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        this.registerReceiver(SMSReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        this.unregisterReceiver(SMSReceiver);
        super.onStop();
    }
}