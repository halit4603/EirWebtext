package com.ingamedeo.eiriewebtext;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.ingamedeo.eiriewebtext.db.WebTextsTable;
import com.ingamedeo.eiriewebtext.utils.DatabaseUtils;
import com.ingamedeo.eiriewebtext.utils.NetworkUtils;
import com.ingamedeo.eiriewebtext.utils.PermissionsUtils;
import com.ingamedeo.eiriewebtext.utils.UIUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.tslamic.prem.Premiumer;
import io.github.tslamic.prem.PremiumerBuilder;
import io.github.tslamic.prem.PremiumerListener;
import io.github.tslamic.prem.Purchase;
import io.github.tslamic.prem.SkuDetails;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, PremiumerListener {

    private static final int LOADER_ID = 1;
    private static final int CONTACTS_LOADER_ID = 2;
    private static final String CONTACTS_LOADER_ARGS_KEY = "text";
    private static final int ACCOUNTS_MANAGER_REQUEST = 3;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.fromNum)
    Spinner fromNumber;

    @BindView(R.id.toEditText)
    AutoCompleteTextView toNumber;

    @BindView(R.id.textField)
    EditText textField;

    @BindView(R.id.charsRemaining)
    TextView charsRemaining;

    @BindView(R.id.fabProgressCircle)
    FABProgressCircle fabProgressCircle;

    @BindView(R.id.floatingActionButton)
    FloatingActionButton sendTextButton;

    @BindView(R.id.sentWebTextList)
    ListView sentWebTextList;

    @BindView(R.id.adView)
    AdView mAdView;

    private WebTextAdapter webTextAdapter = null;
    private ArrayAdapter<String> spinnerAdapter = null;
    private AutoContactsAdapter autoContactsAdapter = null;
    private ContactsLoader contactsLoader = null;

    private Uri contentUri = null;

    private Premiumer premiumer = null;

    private boolean showHideAdsMenuEntry = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        contentUri = DatabaseUtils.generateContentUri(Constants.TableSelect.WEBTEXT);
        contactsLoader = new ContactsLoader();

        initUI();

        if (PermissionsUtils.checkAndRequestPermissions(MainActivity.this)) {
            //Utils.showWhitelistDialog(MainActivity.this);

            //Nothing to init here?
            //appInitRuntime();
        }

        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        //getLoaderManager().initLoader(LOADER_ID, null, this);

        /* Why do you call restartLoader instead of initLoader?
        *
        * I've found out that restart does exactly the same thing as init if our loader doesn't exist yet
        */

        MobileAds.initialize(this, Constants.ADMOB_APP_ID);

        //In-app billing, disable ads
        premiumer = PremiumerBuilder.with(this)
                .sku("noads")
                .listener(this)
                .build();

        //Toast.makeText(this, "> DEBUG VERSION <", Toast.LENGTH_LONG).show();
        //Utils.showDebugDialog(this);
    }

    private void initUI() {

        updateSpinnerData();

            autoContactsAdapter = new AutoContactsAdapter(getApplicationContext(),  null, 0);

            autoContactsAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                public Cursor runQuery(CharSequence constraint) {

                    if (constraint==null) {
                        constraint = "";
                    }

                    Bundle args = new Bundle();
                    args.putString(CONTACTS_LOADER_ARGS_KEY, constraint.toString());

                    if (PermissionsUtils.checkPermissions(MainActivity.this)) {
                        getSupportLoaderManager().restartLoader(CONTACTS_LOADER_ID, args, contactsLoader);
                    }

                    //return getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    return null;
                }
            });

            //Set dropdown adapter
            toNumber.setAdapter(autoContactsAdapter);


        sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                //*** Input validation ***
                if (!UIUtils.runInputCheck(toNumber) && !UIUtils.runInputCheck(textField)) {
                    toNumber.setError(getString(R.string.errorTo));
                    textField.setError(getString(R.string.errorText));
                    return;
                }

                if (!UIUtils.runInputCheck(toNumber)) {
                    toNumber.setError(getString(R.string.errorTo));
                    return;
                }

                if (!UIUtils.runInputCheck(textField)) {
                    textField.setError(getString(R.string.errorText));
                    return;
                }
                //*** End Input validation ***

                //Get user input
                String fromPhoneNumber = fromNumber.getSelectedItem().toString().trim();
                String toPhoneNumber = toNumber.getText().toString().trim();
                String text = textField.getText().toString().trim();

                //Debug
                Log.i(Constants.TAG, "From: " + fromPhoneNumber);
                Log.i(Constants.TAG, "To: " + toPhoneNumber);
                Log.i(Constants.TAG, "Text: " + text);

                new WebTextSendTask().execute(fromPhoneNumber, toPhoneNumber, text);
            }
        });

        webTextAdapter = new WebTextAdapter(getApplicationContext(), null, 0);

        sentWebTextList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        sentWebTextList.setAdapter(webTextAdapter);

        //Register for Context menu for all items in ListView
        registerForContextMenu(sentWebTextList);

        //Go down the ListView
        webTextAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                sentWebTextList.setSelection(webTextAdapter.getCount() - 1);
            }
        });

        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                int webTextsRemaining = (int) Math.ceil((double)s.length()/(double)(Constants.MAX_WEBTEXT_LEN/Constants.MAX_WEBTEXT_NUM));

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(Constants.MAX_WEBTEXT_LEN-s.length())
                        .append("/")
                        .append(Constants.MAX_WEBTEXT_LEN)
                        .append(getString(R.string.chars_remaining))
                        .append("(")
                        .append(webTextsRemaining)
                        .append("/")
                        .append(Constants.MAX_WEBTEXT_LEN/(Constants.MAX_WEBTEXT_LEN/Constants.MAX_WEBTEXT_NUM))
                        .append(")");

                charsRemaining.setText(stringBuilder.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (sentWebTextList==null) {
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.delete_webtext:

                Cursor c = (Cursor) sentWebTextList.getItemAtPosition(info.position);
                DatabaseUtils.getDbAdapter(MainActivity.this).deleteWebtext(String.valueOf(c.getInt(c.getColumnIndex(WebTextsTable._ID))));

                return true;
            case R.id.info_webtext:

                Cursor c1 = (Cursor) sentWebTextList.getItemAtPosition(info.position);

                String fromNumber = c1.getString(c1.getColumnIndex(WebTextsTable.FROMUSER));
                String toNumber = c1.getString(c1.getColumnIndex(WebTextsTable.TOUSER));
                String dateTime = Utils.fromUTCTimestampToString(c1.getInt(c1.getColumnIndex(WebTextsTable.TIMESTAMP)));
                String name = DatabaseUtils.getContactName(this, toNumber);

                String htmlBody = null;

                if (name==null) {
                    htmlBody = "<b>"+getResources().getString(R.string.to)+"</b>"+toNumber+"<br><b>"+getResources().getString(R.string.from)+"</b>"+fromNumber+"<br><b>"+getResources().getString(R.string.sentat)+"</b>"+dateTime;
                } else {
                    htmlBody = "<b>"+getResources().getString(R.string.to)+"</b>"+name+" &lt;"+toNumber+"&gt;<br><b>"+getResources().getString(R.string.from)+"</b>"+fromNumber+"<br><b>"+getResources().getString(R.string.sentat)+"</b>"+dateTime;
                }

                UIUtils.showGenericYesDialog(MainActivity.this, getString(R.string.webtext_info_title), htmlBody, true);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateSpinnerData() {

        Log.i(Constants.TAG, "*** spinnerAdapter update ***");

        //Get customers phone numbers ready for Spinner
        String[] arraySpinner = getCustomersPhoneNumbers();

        if (arraySpinner.length>0) {
            spinnerAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, arraySpinner);
        } else {
            String[] arraySpinnerEmpty = {getString(R.string.no_accounts_spinner)};
            spinnerAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, arraySpinnerEmpty);
        }

        if (fromNumber!=null) {
            fromNumber.setAdapter(spinnerAdapter);
        }
    }

    private void resetWebTextInput() {
        toNumber.setText("");
        textField.setText("");

        View currentFocusView = MainActivity.this.getCurrentFocus();
        if (currentFocusView != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusView.getWindowToken(), 0);
        }
    }

    private void showSnackBar(String message, int length) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, length);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private String[] getCustomersPhoneNumbers() {
        ArrayList<String> arrayListResult = new ArrayList<>();
        String[] customerLines = DatabaseUtils.getDbAdapter(MainActivity.this).accountsToPhoneStringArray();

        for (String lines : customerLines) {
            StringTokenizer tokens = new StringTokenizer(lines, ";");

            while (tokens.hasMoreTokens()) {
                arrayListResult.add(tokens.nextToken());
            }
        }

        return arrayListResult.toArray(new String[arrayListResult.size()]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem item = menu.findItem(R.id.action_hideads);
        if (item!=null) {
            item.setVisible(showHideAdsMenuEntry);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_accounts) {
            Intent accountsIntent = new Intent(MainActivity.this, ManageAccountsActivity.class);
            startActivityForResult(accountsIntent, ACCOUNTS_MANAGER_REQUEST);
            return true;
        } else if (id == R.id.action_hideads) {
            if (premiumer!=null) {
                premiumer.purchase(this);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!premiumer.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);

            //Handle result
            if (requestCode==ACCOUNTS_MANAGER_REQUEST) {
                updateSpinnerData();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(
                this,
                contentUri,
                WebTextsTable.COLUMNS,
                null,
                null,
                WebTextsTable.TIMESTAMP + " ASC"); /* Order messages per timestamp. (Should adjust updated messages automatically) */
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            webTextAdapter.swapCursor(cursor);
        } else {
            webTextAdapter.swapCursor(null);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        webTextAdapter.swapCursor(null);
    }

    private class WebTextSendTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (fabProgressCircle!=null) {
                fabProgressCircle.show();
            }
        }

        @Override
        protected Boolean doInBackground(String... params) {

            //Convert to MSISDN format
            String fromLine = Utils.ieNumberToMSISDN(params[0]);

            Pair<String, String> emailAndPass = DatabaseUtils.getDbAdapter(MainActivity.this).getEmailAndPassFromLine(params[0]);

            //Log.i(Constants.TAG, "emailAndPass: " + emailAndPass.toString());

            if (emailAndPass==null) {
                return false;
            }

            boolean result = NetworkUtils.sendWebText(emailAndPass.first, emailAndPass.second, fromLine, params[1], params[2]);

            if (result) {
                DatabaseUtils.getDbAdapter(MainActivity.this).addWebText(params[0], params[1], params[2]);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            resetWebTextInput(); //Clear text fields

            if (fabProgressCircle==null) {
                return;
            }

            if (result) {
                showSnackBar(getString(R.string.webtext_sent), Snackbar.LENGTH_SHORT);
                fabProgressCircle.beginFinalAnimation();
            } else {
                showSnackBar(getString(R.string.webtext_error), Snackbar.LENGTH_LONG);
                fabProgressCircle.hide();
            }
        }
    }

    @Override
    public void onShowAds() {
        if (mAdView!=null) {
            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    @Override
    public void onHideAds() {
        if (mAdView!=null) {
            mAdView.setVisibility(View.GONE);
        }

        showHideAdsMenuEntry = false;
        invalidateOptionsMenu();
    }

    @Override
    public void onBillingAvailable() {

    }

    @Override
    public void onBillingUnavailable() {

    }

    @Override
    public void onSkuDetails(@Nullable SkuDetails details) {

    }

    @Override
    public void onSkuConsumed() {

    }

    @Override
    public void onFailedToConsumeSku() {

    }

    @Override
    public void onPurchaseRequested(@Nullable String payload) {

    }

    @Override
    public void onPurchaseDetails(@Nullable Purchase purchase) {

    }

    @Override
    public void onPurchaseSuccessful(@NonNull Purchase purchase) {
        Toast.makeText(MainActivity.this, R.string.purchase_ok_toast, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseBadResult(int resultCode, @Nullable Intent data) {

    }

    @Override
    public void onPurchaseBadResponse(@Nullable Intent data) {

    }

    @Override
    public void onPurchaseFailedVerification() {

    }

    @Override protected void onStart() {
        super.onStart();
        if (premiumer!=null) {
            premiumer.bind();
        }
    }

    @Override protected void onStop() {
        super.onStop();
        if (premiumer!=null) {
            premiumer.unbind();
        }
    }

    public static class AutoContactsAdapter extends CursorAdapter implements Filterable {

        private static class ViewHolder {
            public TextView name;
            public TextView number;
        }

        public AutoContactsAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            AutoContactsAdapter.ViewHolder holder = new AutoContactsAdapter.ViewHolder();
            View v = null;

            LayoutInflater inflater = LayoutInflater.from(context);

            v = inflater.inflate(R.layout.dropdown_layout_contact, null);

            holder.name = (TextView) v.findViewById(R.id.name);
            holder.number = (TextView) v.findViewById(R.id.number);

            v.setTag(holder);

            return v;
        }

        @Override
        public void bindView(View v, Context context, Cursor cursor) {

            /* We already have our holder ready at this stage */
            AutoContactsAdapter.ViewHolder holder = (AutoContactsAdapter.ViewHolder) v.getTag();

            String contactDispName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            holder.name.setText(contactDispName);
            holder.number.setText(contactPhone);
            holder.name.setTextColor(context.getResources().getColor(R.color.white));
            holder.number.setTextColor(context.getResources().getColor(R.color.white));
        }

        @Override
        public String convertToString(Cursor cursor) {
            //returns string inserted into textview after item from drop-down list is selected.
            return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        @Override
        public void changeCursor(Cursor cursor) {
            if (cursor!=null) {
                cursor.close();
            }
        }
    }

    //https://stackoverflow.com/questions/14227535/race-condition-with-loadermanager
    private class ContactsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        final String[] PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            String text = args.getString(CONTACTS_LOADER_ARGS_KEY);

            /*
            By using ContactsContract.CommonDataKinds.Phone.CONTENT_URI, the user is presented with a list of contacts, with one entry per phone number.
            The selected contact is guaranteed to have a name and phone number.
            */

            return new CursorLoader(MainActivity.this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    PROJECTION, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" LIKE ?", new String[] {text+"%"}, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            autoContactsAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            autoContactsAdapter.swapCursor(null);
        }
    }
}
