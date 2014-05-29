package pt.ua.travis.backend;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class ParseUserWrapper implements ParseWrapper {

    /**
     * The {@link com.parse.ParseUser} in the backend wrapped in this class.
     */
    protected ParseUser po;

    protected ParseUserWrapper(ParseUser po){
        this.po = po;
    }

    public abstract String thisObjectName();


    /**
     * Returns the wrapped {@link ParseUser} ID.
     */
    public String id(){
        return po.getObjectId();
    }

}
