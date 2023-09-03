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

import com.aboveit.smsdisplayapp.adapters.SMSAdapter;
import com.aboveit.smsdisplayapp.models.Message;
import com.aboveit.smsdisplayapp.utils.Constants;
import com.aboveit.smsdisplayapp.utils.ItemClickListerner;
import com.aboveit.smsdisplayapp.utils.Utility;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ItemClickListerner {

    private RecyclerView recyclerView;
    private TextView blankText, senderDetail, messageDetail, timeDetail;
    private LinearLayoutCompat detailLayout;
    private ImageView blankImage, crossImage;
    private SMSAdapter adapter;
    private List<Message> messages;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private final String TAG = "MainActivity";
    private BroadcastReceiver SMSReceiver;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
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
            Utility.requestReadContactsPermission(this);
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
            Toast.makeText(this, getString(R.string.not_permitted), Toast.LENGTH_SHORT).show();
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
                initNoReadPermissionUI();
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

    public void initNoReadPermissionUI() {
        blankImage.setImageResource(R.drawable.no_permission);
        blankImage.setVisibility(View.VISIBLE);
        blankText.setText(R.string.no_permission_text);
        blankText.setVisibility(View.VISIBLE);
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
                    messages.add(new Message(sender, message, timestamp));
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
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constants.RECEIVER_REGISTERED_KEY, true);
        editor.apply();
    }

    @Override
    protected void onResume() {
        if (Utility.checkReadContactsPermission(this)) {
            if (!sharedPref.getBoolean(Constants.ALLOW_READ_CONTACTS_KEY, false)) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Constants.ALLOW_READ_CONTACTS_KEY, true);
                editor.apply();
            }
        } else {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.ALLOW_READ_CONTACTS_KEY, false);
            editor.apply();
        }
        if (Utility.checkReadPermission(this) && !sharedPref.getBoolean(Constants.INIT_LAUNCH_KEY, true)) {
            handleFetchCall();
        } else {
            initNoReadPermissionUI();
        }
        if (Utility.checkReceivePermission(this) && !sharedPref.getBoolean(Constants.RECEIVER_REGISTERED_KEY, false)) {
            initReceiver();
        }
        Log.d(TAG, "onResume() >> allowReadContacts: " + sharedPref.getBoolean(Constants.ALLOW_READ_CONTACTS_KEY, false));
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        if (sharedPref.getBoolean(Constants.INIT_LAUNCH_KEY, true)) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.INIT_LAUNCH_KEY, false);
            editor.apply();
        }
        if (SMSReceiver != null) {
            this.unregisterReceiver(SMSReceiver);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.RECEIVER_REGISTERED_KEY, false);
            editor.apply();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onItemClick(String sender, String message, Long time) {
        senderDetail.setText(Utility.getContactName(sender, this, false));
        messageDetail.setText(message);
        timeDetail.setText(Utility.formatTimestamp(time, Constants.DETAILED_TIME_PATTERN));
        detailLayout.setVisibility(View.VISIBLE);
    }
}