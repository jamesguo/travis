package pt.ua.travis.gui.main;

import android.content.Intent;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import pt.ua.travis.gui.login.LoginActivity;
import pt.ua.travis.gui.ridelist.RideDeletedListener;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class MainActivity extends SherlockFragmentActivity implements RideDeletedListener {


    public void logout(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}