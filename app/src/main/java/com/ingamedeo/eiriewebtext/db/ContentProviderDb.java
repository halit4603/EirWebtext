package com.ingamedeo.eiriewebtext.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.ingamedeo.eiriewebtext.Constants;

/**
 * Created by ingamedeo on 09/10/14.
 */
public class ContentProviderDb extends ContentProvider {

    private DatabaseHelper dbHelper;
    public static final String AUTHORITY = Constants.PACKAGE+".contentprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;

        SQLiteDatabase db = dbHelper.getWritableDatabase(); //Get Database rw mode.

        Constants.TableSelect matchTable = Constants.TableSelect.values()[buildUriMatcher().match(uri)];

        switch (matchTable) {
            case WEBTEXT:
                /* last one is LIMIT */
                c = db.query(WebTextsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
            case ACCOUNTS:
                /* last one is LIMIT */
                c = db.query(AccountsTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null);
                break;
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long value = 0;

        SQLiteDatabase db = dbHelper.getWritableDatabase(); //Get Database rw mode.
        Constants.TableSelect matchTable = Constants.TableSelect.values()[buildUriMatcher().match(uri)];

        switch (matchTable) {
            case WEBTEXT:
                value = db.insert(WebTextsTable.TABLE_NAME, null, contentValues);
                break;
            case ACCOUNTS:
                value = db.insert(AccountsTable.TABLE_NAME, null, contentValues);
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(); //Get Database rw mode.
        String table = getTableName(uri);
        int del = db.delete(table, where, args);
        getContext().getContentResolver().notifyChange(uri, null);
        return del;
    }

    /* Unused */
    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        /* We get a URI like: content://com.example.app.provider/table1/3 */

        Log.i(Constants.TAG, "Update URI: " + uri.toString());

        SQLiteDatabase db = dbHelper.getWritableDatabase(); //Get Database rw mode.
        Constants.TableSelect matchTable = Constants.TableSelect.values()[buildUriMatcher().match(uri)];

        int upd = 0;

        switch (matchTable) {
            case WEBTEXT:
                //db.execSQL("UPDATE "+WebTextsTable.TABLE_NAME+" SET "+MessageTable.STATUS+"="+Constants.Status.READ.ordinal()+" WHERE "+whereClause);
                upd = 1;
                //upd = db.updateWithOnConflict(MessageTable.TABLE_NAME, values, whereClause, whereArgs, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case ACCOUNTS:
                //db.execSQL("UPDATE "+AccountsTable.TABLE_NAME+" SET "+AccountsTable.EMAIL+"="+values+" WHERE "+whereClause);
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return upd;
    }

    public static String getTableName(Uri uri) {
        String value = uri.getPath();
        value = value.replace("/", "");
        return value;
    }

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, WebTextsTable.TABLE_NAME, Constants.TableSelect.WEBTEXT.ordinal());
        uriMatcher.addURI(AUTHORITY, AccountsTable.TABLE_NAME, Constants.TableSelect.ACCOUNTS.ordinal());
        return uriMatcher;
    }
}
