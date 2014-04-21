package pt.ua.travis.core;

import android.os.StrictMode;
import com.chute.android.photopickerplus.PhotoPickerPlusApp;
import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.config.PhotoPickerConfiguration;
import com.chute.android.photopickerplus.models.enums.DisplayType;
import com.chute.sdk.v2.api.Chute;
import com.chute.sdk.v2.api.authentication.AuthConstants;
import com.chute.sdk.v2.model.enums.AccountType;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.utils.CommonRes;
import pt.ua.travis.utils.Utils;

import java.util.Map;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisApplication extends PhotoPickerPlusApp {

    private static final String clientId = "5348be83455587319e00002f";
    private static final String clientSecret = "9e8d1f1f480bd28234d38419b5f1bc496dfcccccbdad188d1f4cece00c493faa";


    @Override
    public void onCreate() {
        super.onCreate();

        CommonRes.init(this);
        PersistenceManager.init(this);
        Chute.init(this, new AuthConstants(clientId, clientSecret));

        Map<AccountType, DisplayType> map = Utils.newMap();
        map.put(AccountType.GOOGLE, DisplayType.GRID);


        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // TODO: REMOVE THIS
        PersistenceManager.attemptLogin("a@b.c", Utils.generateSHA1DigestFromString("123"));


        PhotoPickerConfiguration config = new PhotoPickerConfiguration.Builder(getApplicationContext())
                .isMultiPicker(false)
                .supportImages(true)
                .supportVideos(false)
                .defaultAccountDisplayType(DisplayType.GRID)
                .accountDisplayType(map)
                .accountList(
                        AccountType.GOOGLE,
                        AccountType.FACEBOOK,
                        AccountType.TWITTER,
                        AccountType.INSTAGRAM,
                        AccountType.DROPBOX,
                        AccountType.GOOGLEDRIVE,
                        AccountType.SKYDRIVE,
                        AccountType.PICASA,
                        AccountType.FLICKR)
                .configUrl("https://dl.dropboxusercontent.com/s/lat3x3asl2xexm3/config.json")
                .build();

        PhotoPicker.getInstance().init(config);
    }
}
