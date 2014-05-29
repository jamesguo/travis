package pt.ua.travis.ui.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.google.android.gms.common.SignInButton;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.backend.User;
import pt.ua.travis.core.TravisLocation;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.Utils;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************
 * In order for Google+ sign in to work withUser your app, you must first go to:
 * https://developers.google.com/+/mobile/android/getting-started#step_1_enable_the_google_api
 * and follow the steps in "Step 1" to create an OAuth 2.0 client for your package.
 */
public class LoginActivity extends PlusBaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NO_AUTO_LOGIN = "no_auto_login";

    public static final String AUTO_CHECKED = "remember_pass_checked";
    public static final String AUTO_EMAIL = "remember_user";
    public static final String AUTO_PASS = "remember_pass";

//    /**
//     * A dummy authentication store containing known user names and passwords.
//     * TODO: remove after connecting to a real authentication system.
//     */
//    private static final String[] DUMMY_CREDENTIALS = new String[]{
//            "foo@example.com:hello", "bar@example.com:world"
//    };


    /**
     * Shared Preferences used to remember login credentials and automatically login.
     */
    private SharedPreferences prefs;

    /**
     * Keeping track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mEmailLoginFormView;
    private SignInButton mPlusSignInButton;
    private View mSignOutButtons;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        if (!Validate.hasGooglePlayServices(this)) {
            finish();
        }

        // Sets the shared preferences to store this devices auto login credentials
        prefs = this.getSharedPreferences("TravisPreferences", MODE_PRIVATE);

        // Find the Google+ sign in button.
        mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);

        // Set a listener to connect the user when the G+ button is clicked.
        mPlusSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Set up the email form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        // Set up the password form.
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailLoginFormView = findViewById(R.id.email_login_form);
        mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);

//        // TODO: AUTO LOGIN
//
//        // If the auto login skipper flag is not present or if it isn't equal to
//        // the default value, attempt auto login.
//        if(!Validate.argExists(getIntent(), NO_AUTO_LOGIN)) {
//            String email = prefs.getString(AUTO_EMAIL, "");
//            String passwordDigest = prefs.getString(AUTO_PASS, "");
//
//            if(!Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(passwordDigest)){
//                // Credentials stored in the shared preferences are not null nor
//                // empty, which means that they were previously set by the user.
//                mAuthTask = new UserLoginTask(email, passwordDigest);
//                mAuthTask.later((Void) null);
//                return;
//            }
//        }

        populateAutoComplete();

//        PersistenceManager.query().clients().later(new Callback<List<Client>>() {
//            @Override
//            public void onResult(final List<Client> selectedClients) {
//                Log.e("----", selectedClients.toString());
//            }
//        });
//        PersistenceManager.query().taxis().later(new Callback<List<Taxi>>() {
//            @Override
//            public void onResult(final List<Taxi> selectedTaxis) {
//                Log.e("----", selectedTaxis.toString());
//            }
//        });
//        PersistenceManager.query().clients().withEmail("a@b.c").later(new Callback<List<Client>>() {
//            @Override
//            public void onResult(final List<Client> selectedClients) {
//                Log.e("----", selectedClients.get(0).numberOfFavorites() + "");
//            }
//        });
//
//        PersistenceManager.query().rides().later(new Callback<List<Ride>>() {
//            @Override
//            public void onResult(List<Ride> queryResults) {
//                Log.e("", queryResults.toString());
//            }
//        });

    }

    private void populateAutoComplete() {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field withUser an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this withUser your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this withUser your own logic
        return password.length() >= 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView
                    .animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
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
        }
    }

    @Override
    protected void onPlusClientSignIn() {
        //Set up sign out and disconnect buttons.
        Button signOutButton = (Button) findViewById(R.id.plus_sign_out_button);
        signOutButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        Button disconnectButton = (Button) findViewById(R.id.plus_disconnect_button);
        disconnectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                revokeAccess();
            }
        });
    }

    @Override
    protected void onPlusClientBlockingUI(boolean show) {
        showProgress(show);
    }

    @Override
    protected void updateConnectButtonState() {
        //TODO: Update this logic to also handle the user logged in by email.
        boolean connected = getPlusClient().isConnected();

        mSignOutButtons.setVisibility(connected ? View.VISIBLE : View.GONE);
        mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
        mEmailLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onPlusClientRevokeAccess() {
        // TODO: Access to the user's G+ account has been revoked.  Per the developer terms, delete
        // any stored user data here.
    }

    @Override
    protected void onPlusClientSignOut() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "profile"),
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
        List<String> emails = new ArrayList<String>();
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
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Pair<Integer, User>> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Pair<Integer, User> doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//
//            // TODO: register the new account here.
//            return true;

//            // Converting password to SHA1 digest
//            final String passDigest = Utils.generateSHA1DigestFromString(mPassword);

            // Doing Asynchronous tests and, in case of success, saving the User object
            // statically in the Backend Manager
            return PersistenceManager.attemptLogin(mEmail, mPassword);
        }

        @Override
        protected void onPostExecute(final Pair<Integer, User> result) {
            mAuthTask = null;
            showProgress(false);

            if(result == null){
                // Unspecified error occurred
                return;

            } else if(result.first == PersistenceManager.NO_USER_WITH_THAT_EMAIL){
                // Email does not exist, so register account
                Intent intent = new Intent(LoginActivity.this, SignUpLastStepsActivity.class);
                startActivity(intent);
                finish();

            } else if(result.first == PersistenceManager.WRONG_CREDENTIALS){
                // Email exists but the password does not correspond.
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

            } else if(result.first == PersistenceManager.SUCCESSFUL_LOGIN){
                // Email and password match a user
                User loggedInUser = result.second;

                Intent activityIntent;
                if(loggedInUser instanceof Client){
                    activityIntent = new Intent(LoginActivity.this, MainClientActivity.class);

                } else if(loggedInUser instanceof Taxi){
                    activityIntent = new Intent(LoginActivity.this, MainTaxiActivity.class);
                    TravisLocation.startTaxiLocationListener((Taxi)loggedInUser);

                } else {
                    return;
                }

                startActivity(activityIntent);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



