package com.ingamedeo.eiriewebtext.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.ingamedeo.eiriewebtext.Constants;
import com.ingamedeo.eiriewebtext.Utils;
import com.ingamedeo.eiriewebtext.WebTextAdapter;
import com.ingamedeo.eiriewebtext.utils.DatabaseUtils;

import java.util.ArrayList;

/**
 * Created by ingamedeo on 09/10/14.
 */
public class DbAdapter {

    private Context c;
    private Uri webTextsUri = null;
    private Uri accountsUri = null;

    public DbAdapter(Context c) {
        this.c = c;
        webTextsUri = DatabaseUtils.generateContentUri(Constants.TableSelect.WEBTEXT);
        accountsUri = DatabaseUtils.generateContentUri(Constants.TableSelect.ACCOUNTS);
    }

    private ContentValues createWebTextContentValues(String fromUsr, String toUsr, byte[] content, long timestamp) {
        ContentValues values = new ContentValues();
        values.put(WebTextsTable.FROMUSER, fromUsr);
        values.put(WebTextsTable.TOUSER, toUsr);
        values.put(WebTextsTable.CONTENT, content);
        values.put(WebTextsTable.TIMESTAMP, timestamp);
        return values;
    }

    private ContentValues createAccountContentValues(String fullname, String email, String password, String phone) {
        ContentValues values = new ContentValues();
        values.put(AccountsTable.FULLNAME, fullname);
        values.put(AccountsTable.EMAIL, email);
        values.put(AccountsTable.PASSWORD, password);
        values.put(AccountsTable.PHONE, phone);
        return values;
    }

    /* Inserts new record, Return -1 if error, or new record ID if successful */
    public long addWebText(String fromUsr, String toUsr, String content) {

        byte[] bytesArray = Utils.fromStringToBytesUTF8(content);

        ContentValues contentValues = createWebTextContentValues(fromUsr, toUsr, bytesArray, Utils.genWebTextTimestamp());

        Uri insertUri = c.getContentResolver().insert(webTextsUri, contentValues);
        return ContentUris.parseId(insertUri);
    }

    public long addAccount(String fullname, String email, String password, String phone) {

        ContentValues contentValues = createAccountContentValues(fullname, email, password, phone);

        Uri insertUri = c.getContentResolver().insert(accountsUri, contentValues);
        return ContentUris.parseId(insertUri);
    }

    public String[] accountsToPhoneStringArray() {

        ArrayList<String> phoneNumbers = new ArrayList<>();

        Cursor cursor = c.getContentResolver().query(accountsUri, null,null, null, null);

        if(cursor!=null && cursor.moveToFirst() && cursor.getCount()>0){
            do{
               phoneNumbers.add(cursor.getString(cursor.getColumnIndex(AccountsTable.PHONE)));

            }while(cursor.moveToNext());
        }

        return phoneNumbers.toArray(new String[phoneNumbers.size()]);
    }

    public Pair<String, String> getEmailAndPassFromLine(String line) {

        Cursor cursor = c.getContentResolver().query(accountsUri, new String[] {AccountsTable.EMAIL, AccountsTable.PASSWORD}, AccountsTable.PHONE+" = ? ", new String[] {line}, null);

        //Only one result should be returned, if more than 1 we just use the 1st one. This should work.
        if(cursor!=null && cursor.moveToFirst() && cursor.getCount()>0){
          return new Pair<>(cursor.getString(cursor.getColumnIndex(AccountsTable.EMAIL)), cursor.getString(cursor.getColumnIndex(AccountsTable.PASSWORD)));
        }

        return null;
    }

    public int countAccounts() {
        Cursor cursor = c.getContentResolver().query(accountsUri, null, null, null, null);
        int count = DatabaseUtils.getCursorDataCount(cursor);
        //cursor.close();
        Log.i(Constants.TAG, "N accounts: " + count);
        return count;
    }

    public int deleteWebtext(String webTextId) {
        return c.getContentResolver().delete(webTextsUri, WebTextsTable._ID+" = ?", new String[] {webTextId});
    }

    /*

    public boolean updateMessageStatus(int msgId, Constants.Status msgStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageTable.STATUS, msgStatus.ordinal());
        return c.getContentResolver().update(messagesUri, contentValues, MessageTable.MSGID + "=" + msgId, null) > 0;
    }

    public boolean updateMultipleMessageStatus(String to, String from, Constants.Status msgStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MessageTable.STATUS, msgStatus.ordinal());
        //Log.i(Constants.TAG, "updateMultipleMessageStatus: from="+from+" - to="+to+ " -> "+msgStatus.toString());
        return c.getContentResolver().update(messagesUri, contentValues, "(" + MessageTable.TOUSER + "=" + DatabaseUtils.sqlEscapeString(from) + " AND " + MessageTable.FROMUSER + "=" + DatabaseUtils.sqlEscapeString(to) + ") AND " + MessageTable.STATUS + " <> " + msgStatus.ordinal(), null) > 0;
    }

    public int countUnreadMessages(String clientID) {
        Cursor cursor = c.getContentResolver().query(messagesUri, null, MessageTable.STATUS + "<>" + Constants.Status.READ.ordinal() + " AND " + MessageTable.TOUSER + "=" + DatabaseUtils.sqlEscapeString(clientID), null, MessageTable.TIMESTAMP + " ASC");
        int count = Utils.getCursorDataCount(cursor);
        //cursor.close();
        Log.i(Constants.TAG, "generateMsgNotification() unread messages count=" + count);
        return count;
    }

    public int countUnreadMessagesPerUser(String clientID, String fromUserID) {

        Cursor cursor = c.getContentResolver().query(messagesUri, null, MessageTable.STATUS + "<>" + Constants.Status.READ.ordinal()+ " AND " + MessageTable.TOUSER + "=" + DatabaseUtils.sqlEscapeString(clientID) + " AND " + MessageTable.FROMUSER + "=" + DatabaseUtils.sqlEscapeString(fromUserID), null, MessageTable.TIMESTAMP + " ASC");

        int count = Utils.getCursorDataCount(cursor);
        //cursor.close();

        Log.i(Constants.TAG, "generateMsgNotification() unread messages count="+count);

        return count;
    }

    */

}
