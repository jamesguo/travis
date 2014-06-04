package pt.ua.travis.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.beardedhen.androidbootstrap.BootstrapAutoCompleteEditText;
import com.dd.processbutton.iml.ActionProcessButton;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import org.apache.commons.io.output.ByteArrayOutputStream;
import pt.ua.travis.R;
import pt.ua.travis.core.SplashScreenActivity;
import pt.ua.travis.utils.CommonKeys;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LoginActivity extends SherlockActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * Shared Preferences used to remember login credentials and automatically login.
     */
    private SharedPreferences prefs;

    private BootstrapAutoCompleteEditText fieldEmail;
    private EditText fieldPassword;
    private ActionProcessButton normalLoginButton;
    private Button facebookLoginButton;
    private CheckBox autoLoginCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        // Sets the shared preferences to store this devices auto login credentials
        prefs = this.getSharedPreferences("TravisPreferences", MODE_PRIVATE);

        fieldEmail = (BootstrapAutoCompleteEditText) findViewById(R.id.field_email);

        fieldPassword = (EditText) findViewById(R.id.field_password);
        fieldPassword.setTypeface(Typeface.DEFAULT);
        fieldPassword.setTransformationMethod(new PasswordTransformationMethod());

        normalLoginButton = (ActionProcessButton) findViewById(R.id.normal_login_button);
        normalLoginButton.setMode(ActionProcessButton.Mode.ENDLESS);
        normalLoginButton.setColorScheme(
                R.color.travis_color,
                R.color.travis_color_lighter,
                R.color.travis_color_darker,
                R.color.travis_color_lighter);

        autoLoginCheck = (CheckBox) findViewById(R.id.auto_login_check);
        autoLoginCheck.setChecked(false);


        facebookLoginButton = (Button) findViewById(R.id.facebook_login_button);
//        facebookLoginButton.setOnErrorListener(new LoginButton.OnErrorListener() {
//
//            @Override
//            public void onError(FacebookException error) {
//                Log.e("Facebook Login", "Error ", error);
//            }
//        });
//        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_photos"));
        facebookLoginButton.setBackgroundResource(com.facebook.android.R.drawable.com_facebook_button_blue);
        facebookLoginButton.setCompoundDrawablesWithIntrinsicBounds(com.facebook.android.R.drawable.com_facebook_inverse_icon, 0, 0, 0);
        facebookLoginButton.setCompoundDrawablePadding(
                getResources().getDimensionPixelSize(com.facebook.android.R.dimen.com_facebook_loginview_compound_drawable_padding));
        facebookLoginButton.setPadding(getResources().getDimensionPixelSize(com.facebook.android.R.dimen.com_facebook_loginview_padding_left),
                getResources().getDimensionPixelSize(com.facebook.android.R.dimen.com_facebook_loginview_padding_top),
                getResources().getDimensionPixelSize(com.facebook.android.R.dimen.com_facebook_loginview_padding_right),
                getResources().getDimensionPixelSize(com.facebook.android.R.dimen.com_facebook_loginview_padding_bottom));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptNormalLogin(View v) {

        // Set button to loading
        normalLoginButton.setProgress(50);

        // Reset errors.
        fieldEmail.setError(null);
        fieldPassword.setError(null);

        // Make fields unalterable.
        setAllViewsEnabled(false);

        // Store values at the time of the login attempt.
        final String email = fieldEmail.getText().toString();
        final String password = fieldPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            normalLoginButton.setProgress(-1);
            fieldPassword.setError(getString(R.string.error_field_required));
            focusView = fieldPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            normalLoginButton.setProgress(-1);
            fieldPassword.setError(getString(R.string.error_invalid_password));
            focusView = fieldPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            normalLoginButton.setProgress(-1);
            fieldEmail.setError(getString(R.string.error_field_required));
            focusView = fieldEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            normalLoginButton.setProgress(-1);
            fieldEmail.setError(getString(R.string.error_invalid_email));
            focusView = fieldEmail;
            cancel = true;
        }

        if (cancel) {
            setAllViewsEnabled(true);
            // There was an error; don't attempt login and focus the first
            // form field withUser an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            new LoginTask(this, email, password).execute(new LoginTask.OnTaskEndedListener() {
                @Override
                public void onLoginSuccess() {
                    normalLoginButton.setProgress(100);
                    normalLoginButton.setTextColor(getResources().getColor(android.R.color.black));
                    if (autoLoginCheck.isChecked()) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(SplashScreenActivity.DO_AUTO_LOGIN, true);
                        editor.putString(SplashScreenActivity.AUTO_EMAIL, email);
                        editor.putString(SplashScreenActivity.AUTO_PASS, password);
                        editor.commit();
                    }
                }

                @Override
                public void onWrongCredentials() {
                    normalLoginButton.setProgress(-1);
                    fieldPassword.setError(getString(R.string.error_incorrect_password));
                    fieldPassword.requestFocus();
                    setAllViewsEnabled(true);
                }
            });


        }
    }

    /**
     * Attempts to sign in or register the account with facebook authentication
     * mechanisms.
     */
    public void attemptFacebookLogin(View v){
        List<String> permissions = Arrays.asList("public_profile", "email");
        Log.e(TAG, "Logging in with Facebook.");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(final ParseUser parseUser, ParseException err) {
                if (parseUser == null) {
                    Log.d(TAG, "The user cancelled the Facebook login.");
                } else if (parseUser.isNew()) {
                    Log.d(TAG, "User logged in through Facebook and its a new account!");
                    Session session = Session.getActiveSession();
                    Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                try {

                                    URL imageValue = new URL("http://graph.facebook.com/" + user.getId() + "/picture");
                                    Bitmap profilePhoto = BitmapFactory.decodeStream(imageValue.openConnection().getInputStream());
                                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                                    profilePhoto.compress(Bitmap.CompressFormat.PNG, 0, outStream);

                                    String firstName = user.getFirstName();
                                    String lastName = user.getLastName();
                                    String email = user.getProperty("email").toString();
                                    byte[] imageData = outStream.toByteArray();

                                    Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                                    intent.putExtra(CommonKeys.LOGGED_IN_FROM_SOCIAL, true);
                                    intent.putExtra(CommonKeys.FIRST_NAME, firstName);
                                    intent.putExtra(CommonKeys.LAST_NAME, lastName);
                                    intent.putExtra(CommonKeys.EMAIL, email);
                                    intent.putExtra(CommonKeys.IMAGE_BYTES, imageData);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();


                                } catch (IOException ex) {
                                    Log.e(TAG, "Error getting user data from facebook.", ex);
                                }

                            }
                        }

                    });

                } else {
                    Log.d(TAG, "User logged in through Facebook and its an existing account!");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        ParseFacebookUtils.finishAuthentication(requestCode, responseCode, intent);
        super.onActivityResult(requestCode, responseCode, intent);
    }

    private boolean isEmailValid(String email) {
        Pattern p = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    private void setAllViewsEnabled(boolean enabled){
        fieldEmail.setEnabled(enabled);
        fieldPassword.setEnabled(enabled);
        normalLoginButton.setEnabled(enabled);
        facebookLoginButton.setEnabled(enabled);
        autoLoginCheck.setEnabled(enabled);
    }
}



