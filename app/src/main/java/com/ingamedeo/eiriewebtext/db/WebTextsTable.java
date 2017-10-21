package com.ingamedeo.eiriewebtext.db;

import android.provider.BaseColumns;

/**
 * Created by ingamedeo on 09/10/14.
 */

public interface WebTextsTable extends BaseColumns {

    String TABLE_NAME = "WebTexts";
    String FROMUSER = "fromusr";
    String TOUSER = "tousr";
    String CONTENT = "content";
    String TIMESTAMP = "timestamp";

    String[] COLUMNS = new String[]{_ID, FROMUSER, TOUSER, CONTENT, TIMESTAMP};
}
