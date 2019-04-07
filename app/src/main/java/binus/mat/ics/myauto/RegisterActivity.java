package binus.mat.ics.myauto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    final String registerUrl = "http://wendrian.duckdns.org/stanley/myauto/api/register.php";
    final String loginUrl = "http://wendrian.duckdns.org/stanley/myauto/api/login.php";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mRepeatPasswordView;
    private EditText mNameView;
    private EditText mUsernameView;
    private View mProgressView;
    private View mLoginFormView;

    // OkHttp
    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    String postJson(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the login form.
        mNameView = findViewById(R.id.name);

        mUsernameView = findViewById(R.id.username);

        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);

        mRepeatPasswordView = findViewById(R.id.password_repeat);
        mRepeatPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mNameView.setError(null);
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mRepeatPasswordView.setError(null);


        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String repeatPassword = mRepeatPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for repeat password
        if (TextUtils.isEmpty(repeatPassword)) {
            mRepeatPasswordView.setError(getString(R.string.error_field_required));
            focusView = mRepeatPasswordView;
            cancel = true;
        }

        if (!password.equals(repeatPassword)) {
            mPasswordView.setError(getString(R.string.password_not_same));
            mRepeatPasswordView.setError(getString(R.string.password_not_same));
            focusView = mRepeatPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(name, username, email, password);
            mAuthTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mNameView.setVisibility(show ? View.GONE : View.VISIBLE);
            mUsernameView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRepeatPasswordView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                                                                     .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ReturnData {
        public boolean isSuccess;
        public int errorId;
        public String errorString;
        public String hash;
        public String userId;
    }


    public class UserLoginTask extends AsyncTask<Void, Void, ReturnData> {

        private final String mName;
        private final String mUsername;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String name, String username, String email, String password) {
            mName = name;
            mUsername = username;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected ReturnData doInBackground(Void... params) {
            // TODO: register the new account here.
            Map<String, String> postParam = new HashMap<>();
            postParam.put("user_name", mName);
            postParam.put("user_username", mUsername);
            postParam.put("user_email", mEmail);
            postParam.put("user_pass", mPassword);

            // Convert Map to JSONObject
            JSONObject jObj = new JSONObject(postParam);

            String response = null;

            try {
                response = postJson(registerUrl, jObj.toString());
            } catch (IOException e) {
                Log.e(getApplicationContext().toString(), "Connection error", e);

                ReturnData returnData = new ReturnData();
                returnData.isSuccess = false;
                returnData.errorString = "Connection error";
                return returnData;
            }

            Gson gson = new Gson();
            Map<String,Object> map = new HashMap();

            map = (Map<String,Object>) gson.fromJson(response, map.getClass());

            if (map.get("result").toString().equals("0.0")) {
                ReturnData returnData = new ReturnData();
                returnData.isSuccess = false;
                returnData.errorId = (int) Float.parseFloat(map.get("result").toString());
                returnData.errorString = map.get("errorString").toString();
                return returnData;

            } else if (map.get("result").toString().equals("2.0")) {
                ReturnData returnData = new ReturnData();
                returnData.isSuccess = false;
                returnData.errorId = (int) Float.parseFloat(map.get("result").toString());
                returnData.errorString = map.get("errorString").toString();
                return returnData;

            } else if (map.get("result").toString().equals("1.0")) {

                // Login
                String responseLogin = null;
                try {
                    responseLogin = postJson(loginUrl, jObj.toString());
                } catch (IOException e) {
                    Log.e(getApplicationContext().toString(), "Connection error", e);
                    ReturnData returnData = new ReturnData();
                    returnData.isSuccess = false;
                    returnData.errorId = 3;
                    returnData.errorString = "Connection error";
                    return returnData;
                }

                Map<String,Object> mapLogin = new HashMap();
                mapLogin = (Map<String,Object>) gson.fromJson(responseLogin, map.getClass());

                if (map.get("result").toString().equals("0.0")) {
                    ReturnData returnData = new ReturnData();
                    returnData.isSuccess = false;
                    returnData.errorId = 4;
                    returnData.errorString = "Internal error";
                    return returnData;

                } else if (mapLogin.get("result").toString().equals("1.0")) {
                    ReturnData returnData = new ReturnData();
                    returnData.isSuccess = true;
                    returnData.hash = mapLogin.get("hash").toString();
                    returnData.userId = mapLogin.get("user_id").toString();
                    return returnData;
                }
            }

            ReturnData returnData = new ReturnData();
            returnData.isSuccess = false;
            returnData.errorString = "Internal error";
            return returnData;
        }

        @Override
        protected void onPostExecute(final ReturnData returnData) {
            mAuthTask = null;
            showProgress(false);

            if (returnData.isSuccess) {
                Toast.makeText(RegisterActivity.this, "Registration complete. Attempting login...", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor sp = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE).edit();

                sp.putBoolean("logged_in", true);
                sp.putString("user_hash", returnData.hash);
                sp.putString("user_email", mEmail);
                sp.putString("user_id", returnData.userId);
                sp.apply();

                startActivity(new Intent(RegisterActivity.this, MainMenuActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();

            } else if (returnData.errorId == 2) {
                mUsernameView.setError(getString(R.string.error_user_exist));
                mEmailView.setError(getString(R.string.error_user_exist));
                mUsernameView.requestFocus();
            } else {
                Toast.makeText(RegisterActivity.this, "An error occured. " + returnData.errorString, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

