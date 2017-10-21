package com.ingamedeo.eiriewebtext;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.CookieManager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccountsActivity extends AppCompatActivity {

    @BindView(R.id.headerTextView)
    TextView header;

    @BindView(R.id.addAccount)
    Button addAccount;

    @BindView(R.id.emailEditText)
    EditText email;

    @BindView(R.id.passwordEditText)
    EditText password;

    @BindView(R.id.rootLayout)
    android.support.constraint.ConstraintLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        ButterKnife.bind(this);

        initUI(savedInstanceState);
    }

    private void initUI(Bundle savedInstanceState) {
        header.setText(Html.fromHtml("<b>Note:</b> This app currently supports ex Meteor customers only!"));

        addAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.runInputCheck(email) && Utils.runInputCheck(password)) {
                    new VerifyLoginTask().execute(getStringFromEditText(email), getStringFromEditText(password));
                }

            }
        });
    }

    private String getStringFromEditText(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        snackbar.show();
    }

    private class VerifyLoginTask extends AsyncTask<String, Void, Constants.EirLoginResult> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Utils.hideKeyboard(AccountsActivity.this);

            addAccount.setText(R.string.verifying_details);
            addAccount.setClickable(false);
            addAccount.setEnabled(false);
        }

        @Override
        protected Constants.EirLoginResult doInBackground(String... params) {

            CookieManager msCookieManager = new CookieManager();

            Constants.EirLoginResult login = Utils.loginToEir(params[0], params[1], msCookieManager);

            if (login == Constants.EirLoginResult.SUCCESS) {

                String[] customerFullNames = Utils.getCustomerFullName(msCookieManager);
                String[] customerLines = Utils.getCustomerLines(msCookieManager);

                //Fixes crashes (14/10/17)
                if (customerLines==null) {
                    return Constants.EirLoginResult.GENERIC_ERROR;
                }

                //Array to String ( separator ; )
                String customerLinesString = TextUtils.join(";", customerLines);

                for (String fullname : customerFullNames) {
                    Utils.getDbAdapter(AccountsActivity.this).addAccount(fullname, params[0], params[1], customerLinesString);
                }
            }

            return login;
        }

        @Override
        protected void onPostExecute(Constants.EirLoginResult result) {
            super.onPostExecute(result);

            addAccount.setText(R.string.add_account);
            addAccount.setClickable(true);
            addAccount.setEnabled(true);

            switch (result) {
                case SUCCESS:

                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("login", true);
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                    break;
                case WRONG_USER_PASS:
                    email.setText(Constants.EMPTY);
                    password.setText(Constants.EMPTY);
                    email.requestFocus();
                    showSnackBar(getString(R.string.wrong_user_pass_snack));
                    break;
                case LOCKED:
                    email.setText(Constants.EMPTY);
                    password.setText(Constants.EMPTY);
                    email.requestFocus();
                    showSnackBar(getString(R.string.too_many_snack));
                    break;
                case GENERIC_ERROR:
                    showSnackBar(getString(R.string.net_srv_error_snack));
                    break;
            }

        }
    }

}
