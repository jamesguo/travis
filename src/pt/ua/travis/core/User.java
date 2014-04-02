package pt.ua.travis.core;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class User extends TravisObject implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String userName;
    public final String passwordDigest;
    public final String realName;
    public final String imageUri;


    protected User(final String userName,
                   final String passwordDigest,
                   final String realName,
                   final String imageUri){
        super();
        this.userName = userName;
        this.passwordDigest = passwordDigest;
        this.realName = realName;
        this.imageUri = imageUri;
    }
}
