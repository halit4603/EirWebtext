package com.ingamedeo.eiriewebtext.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ingamedeo on 09/10/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "meteor.db";
    private static final int DATABASE_VERSION = 1;

    private static final String WEBTEXTS_CREATE_QUERY = "create table "
            + WebTextsTable.TABLE_NAME
            + "("
            + WebTextsTable._ID + " integer primary key autoincrement, " //This is the primary key and autoincrement
            + WebTextsTable.FROMUSER + " text not null, "
            + WebTextsTable.TOUSER + " text not null, "
            + WebTextsTable.CONTENT + " BLOB not null, "
            + WebTextsTable.TIMESTAMP + " integer not null"
            + ");";

    private static final String ACCOUNTS_CREATE_QUERY = "create table "
            + AccountsTable.TABLE_NAME
            + "("
            + AccountsTable._ID + " integer primary key autoincrement, " //This is the primary key and autoincrement
            + AccountsTable.FULLNAME + " text not null, "
            + AccountsTable.EMAIL + " text not null, "
            + AccountsTable.PASSWORD + " text not null, "
            + AccountsTable.PHONE + " text not null"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(WEBTEXTS_CREATE_QUERY);
        sqLiteDatabase.execSQL(ACCOUNTS_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i(DatabaseHelper.class.getName(), " Dropping DB Tables!");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WebTextsTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AccountsTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
