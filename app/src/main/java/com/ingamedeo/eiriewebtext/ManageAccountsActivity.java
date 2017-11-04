package com.ingamedeo.eiriewebtext;

import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ingamedeo.eiriewebtext.db.AccountsTable;
import com.ingamedeo.eiriewebtext.utils.DatabaseUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManageAccountsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    private static final int LOADER_ID = 2;
    private static final int ACCOUNT_REQUEST = 1;

    @BindView(R.id.addNewAccount)
    Button addNewAccount;

    @BindView(R.id.accountsList)
    ListView accountsList;

    @BindView(R.id.noAccountsYet)
    LinearLayout noAccountsYet;

    private CursorAdapter accountsAdapter = null;

    private Uri contentUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_accounts);
        ButterKnife.bind(this);

        contentUri = DatabaseUtils.generateContentUri(Constants.TableSelect.ACCOUNTS);

        initUI(savedInstanceState);

        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        //getLoaderManager().initLoader(LOADER_ID, null, this);

        /* Why do you call restartLoader instead of initLoader?
        *
        * I've found out that restart does exactly the same thing as init if our loader doesn't exist yet
        */
    }

    private void initUI(Bundle savedInstanceState) {
        addNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openAccountsAct = new Intent(ManageAccountsActivity.this, AccountsActivity.class);
                startActivityForResult(openAccountsAct, ACCOUNT_REQUEST);
            }
        });

        accountsAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                null,     // Pass in the cursor to bind to.
                new String[] {AccountsTable.FULLNAME, AccountsTable.EMAIL}, // Array of cursor columns to bind to.
                new int[] {android.R.id.text1, android.R.id.text2}) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setTypeface(null, Typeface.BOLD_ITALIC);
                return view;
            };

        };

        accountsList.setAdapter(accountsAdapter);

        accountsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        accountsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
        super.onChanged();
         accountsList.setSelection(accountsAdapter.getCount() - 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle result
        if (requestCode==ACCOUNT_REQUEST) {

            if(resultCode == Activity.RESULT_OK){
                boolean login = data.getBooleanExtra("login", false);
                Log.i(Constants.TAG, "RESULT_OK and login="+login);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(Constants.TAG, "RESULT_CANCELED");
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                this,
                contentUri,
                new String[] {AccountsTable._ID, AccountsTable.FULLNAME, AccountsTable.EMAIL},
                null,
                null,
                AccountsTable.FULLNAME + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            accountsAdapter.swapCursor(cursor);
            noAccountsYet.setVisibility(View.GONE);
            accountsList.setVisibility(View.VISIBLE);
        } else {
            accountsAdapter.swapCursor(null);
            noAccountsYet.setVisibility(View.VISIBLE);
            accountsList.setVisibility(View.GONE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        accountsAdapter.swapCursor(null);
    }

}
