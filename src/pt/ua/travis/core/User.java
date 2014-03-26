package pt.ua.travis.core;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class User extends TravisObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String userName;
    public final String passwordDigest;
    public final String realName;
    public final String imageUrl;


    protected User(final String userName,
                   final String passwordDigest,
                   final String realName,
                   final String imageUrl){
        super();
        this.userName = userName;
        this.passwordDigest = passwordDigest;
        this.realName = realName;
        this.imageUrl = imageUrl;
    }
}
