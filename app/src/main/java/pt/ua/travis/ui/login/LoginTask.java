package pt.ua.travis.ui.login;

import android.content.Intent;
import android.os.AsyncTask;
import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.backend.User;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.login.SignUpActivity;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.utils.CommonKeys;
import pt.ua.travis.utils.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LoginTask extends AsyncTask<Void, Void, Pair<Integer, User>> {

    public interface OnTaskEndedListener {

        void onLoginSuccess();

        void onWrongCredentials();

    }

    private final SherlockActivity parentActivity;
    private final String email;
    private final String password;
    private final List<OnTaskEndedListener> mListeners;

    public LoginTask(SherlockActivity parentActivity, String email, String password) {
        this.parentActivity = parentActivity;
        this.email = email;
        this.password = password;
        this.mListeners = Lists.newArrayList();
    }

    public void execute(OnTaskEndedListener... listeners) {
        mListeners.addAll(Arrays.asList(listeners));
        super.execute();
    }

    @Override
    protected Pair<Integer, User> doInBackground(Void... params) {

        // Doing Asynchronous tests and, in case of success, saving the User
        // object in the backend
        return PersistenceManager.attemptLogin(email, password);
    }

    @Override
    protected void onPostExecute(final Pair<Integer, User> result) {

        if(result == null){
            // Unspecified error occurred
            return;

        } else if(result.first == PersistenceManager.NO_USER_WITH_THAT_EMAIL){

            // Email does not exist, so register account
            for(OnTaskEndedListener l : mListeners) {
                l.onLoginSuccess();
            }
            Intent intent = new Intent(parentActivity, SignUpActivity.class);
            intent.putExtra(CommonKeys.EMAIL, email);
            intent.putExtra(CommonKeys.PASS, password);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            parentActivity.startActivity(intent);
            parentActivity.finish();


        } else if(result.first == PersistenceManager.WRONG_CREDENTIALS){

            // Email exists but the password does not correspond.
            for(OnTaskEndedListener l : mListeners) {
                l.onWrongCredentials();
            }


        } else if(result.first == PersistenceManager.SUCCESSFUL_LOGIN){

            // Email and password match a user
            for(OnTaskEndedListener l : mListeners) {
                l.onLoginSuccess();
            }

            User loggedInUser = result.second;

            Intent activityIntent;
            if(loggedInUser instanceof Client){
                activityIntent = new Intent(parentActivity, MainClientActivity.class);

            } else if(loggedInUser instanceof Taxi){
                activityIntent = new Intent(parentActivity, MainTaxiActivity.class);
                final Taxi t = ((Taxi)loggedInUser);
                TravisApplication app = (TravisApplication) parentActivity.getApplication();
                app.addLocationListener(new TravisApplication.CurrentLocationListener() {
                    @Override
                    public void onCurrentLocationChanged(LatLng latLng) {
                        t.setCurrentLocation(latLng.latitude, latLng.longitude);
                        PersistenceManager.save(t, null);
                    }
                });

            } else {
                return;
            }

            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            parentActivity.startActivity(activityIntent);
            parentActivity.finish();
        }
    }
}