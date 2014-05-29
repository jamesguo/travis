package pt.ua.travis.backend;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface ParseWrapper {

    /**
     * Returns the object type name for this {@link com.parse.ParseUser}.
     */
    String thisObjectName();

    /**
     * Returns the wrapped {@link com.parse.ParseUser} ID.
     */
    String id();
}
