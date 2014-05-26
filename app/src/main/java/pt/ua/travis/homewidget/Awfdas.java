package pt.ua.travis.homewidget;

import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.User;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Awfdas {

    public void doooo() {
        User u = PersistenceManager.getUserLoggedInThisDevice();
        PersistenceManager.query().rides().withUser(u).later(new Callback<List<Ride>>() {
            @Override
            public void onResult(List<Ride> result) {

            }
        });
    }
}
