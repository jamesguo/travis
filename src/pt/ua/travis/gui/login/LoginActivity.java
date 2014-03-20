package pt.ua.travis.gui.login;

import android.app.Activity;
import android.os.Bundle;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.TravisDB;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class LoginActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);


        Client c = TravisDB.getClientAccount();

        Taxi t = TravisDB.getTaxiAccount();


    }
}