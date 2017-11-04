package com.ingamedeo.eiriewebtext.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.ingamedeo.eiriewebtext.Constants;
import com.ingamedeo.eiriewebtext.db.AccountsTable;
import com.ingamedeo.eiriewebtext.db.ContentProviderDb;
import com.ingamedeo.eiriewebtext.db.DbAdapter;
import com.ingamedeo.eiriewebtext.db.WebTextsTable;

/**
 * Created by ingamedeo on 04/11/2017.
 */

public class DatabaseUtils {

    private static DbAdapter dbAdapter = null;

    public static Uri generateContentUri(Constants.TableSelect tableSelect) {
        switch (tableSelect) {
            case WEBTEXT:
                return Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, WebTextsTable.TABLE_NAME);
            case ACCOUNTS:
                return Uri.withAppendedPath(ContentProviderDb.CONTENT_URI, AccountsTable.TABLE_NAME);
            default:
                return null;
        }
    }

    public static DbAdapter getDbAdapter(Context context) {
        if (dbAdapter==null) {
            dbAdapter = new DbAdapter(context.getApplicationContext());
        }
        return dbAdapter;
    }

    public static int getCursorDataCount(Cursor cursor) {
        int count = 0;

        if(cursor!=null && cursor.moveToFirst() && cursor.getCount()>0){
            do{
                count++;

            }while(cursor.moveToNext());
        }

        //cursor.close();
        return count;
    }


    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

}
