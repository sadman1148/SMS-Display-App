package com.aboveit.smsdisplayapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utility {

    private static final String TAG = "Utility";

    static boolean checkReadPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
    }

    static void requestReadPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_SMS}, Constants.READ_PERMISSION_REQUEST_CODE);
    }

    static void requestReceivePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.RECEIVE_SMS}, Constants.RECEIVE_PERMISSION_REQUEST_CODE);
    }

    static boolean checkReceivePermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED);
    }

    static boolean checkReadContactsPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
    }

    static void requestReadContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_CONTACTS}, Constants.READ_CONTACTS_PERMISSION_REQUEST_CODE);
    }

    static String formatTimestamp(long timestampMillis, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(timestampMillis);
    }

    static String getContactName(String phoneNumber, Context context, boolean isSubStringed) {
        if (context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE).getBoolean(Constants.ALLOW_READ_CONTACTS_KEY, false)) {
            if ((int) phoneNumber.charAt(0) == 43 || ((int) phoneNumber.charAt(0) > 47 && (int) phoneNumber.charAt(0) < 58)) {
                Log.d(TAG, "number: " + phoneNumber + " isSubStringed: " + isSubStringed);
                if (!isSubStringed) {
                    // searches for the number without the country code +88 (Bangladesh)
                    return getContactName(phoneNumber.substring(3), context, true);
                }
            } else {
                Log.d(TAG, "number: " + phoneNumber);
                return phoneNumber;
            }
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
            String contactName = "";
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0);
                }
                cursor.close();
            }
            if (contactName.isEmpty()) {
                return phoneNumber;
            }
            return contactName;
        }
        return phoneNumber;
    }
}
