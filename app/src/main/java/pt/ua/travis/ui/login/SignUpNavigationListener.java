package pt.ua.travis.ui.login;

/**
 * Listener used by the signup fragments to interact with each other and
 * with the SignUp Activity. The hosting SignUp Activity should implement
 * this listener.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface SignUpNavigationListener {
    public void toFirst(String fromTag);

    public void toSecond(String fromTag);

    public void toThird(String fromTag);

    public void done();
}
