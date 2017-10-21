package com.ingamedeo.eiriewebtext.db;

import android.provider.BaseColumns;

/**
 * Created by ingamedeo on 09/10/14.
 */

public interface AccountsTable extends BaseColumns {

    String TABLE_NAME = "Accounts";
    String FULLNAME = "fullname";
    String EMAIL = "email";
    String PASSWORD = "password";
    String PHONE = "phone";

    String[] COLUMNS = new String[]{_ID, FULLNAME, EMAIL, PASSWORD, PHONE};
}