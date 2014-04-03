package pt.ua.travis.gui.main;

import pt.ua.travis.core.User;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class DrawerUser implements DrawerView {

    public final User loggedInUser;

    public DrawerUser(User loggedInUser){
        this.loggedInUser = loggedInUser;
    }
}
