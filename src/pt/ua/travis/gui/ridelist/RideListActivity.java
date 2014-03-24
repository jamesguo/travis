package pt.ua.travis.gui.ridelist;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockListActivity;
import pt.ua.travis.db.PersistenceManager;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RideListActivity extends SherlockListActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RideItemListAdapter adapter = new RideItemListAdapter(this, PersistenceManager.getRides());
        setListAdapter(adapter);
    }
}