package pt.ua.travis.core;

import android.app.Application;
import android.os.StrictMode;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Utils;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CommonRes.init(this);
//        FilePickerAPI.setKey("AKIepy8VQnykiqorCVkRCz");
        PersistenceManager.init(this);
        LocationLibrary.initialiseLibrary(getBaseContext(), 7000, 60000, "pt.ua.travis");

        // TODO: REMOVE THIS
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        PersistenceManager.attemptLogin("cr7@gmail.com", "123");

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        PersistenceManager.logout();
        TravisLocation.stopTaxiLocationListener();
    }
}
