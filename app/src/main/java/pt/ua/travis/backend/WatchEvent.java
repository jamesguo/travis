package pt.ua.travis.backend;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface WatchEvent<T> {

    void onEvent(final T response);

}
