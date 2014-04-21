package pt.ua.travis.backend.entities;

import com.google.common.base.Strings;
import com.parse.ParseObject;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class User extends ParseObjectWrapper {
//    private static final long serialVersionUID = 1L;

    // Parse data column keys (DO NOT CHANGE)
    public static final String EMAIL = "email";
    public static final String PASSWORD_DIGEST = "password";
    public static final String NAME = "name";
    public static final String IMAGE_URI = "imageuri";


    protected User(ParseObject po){
        super(po);
    }


    /**
     * Puts the specified email in the wrapped {@link ParseObject} parameter "email".
     */
    public User setEmail(String email) {
        if(Strings.isNullOrEmpty(email))
            return this;

        po.put(EMAIL, email);
        return this;
    }


    /**
     * Puts the specified {@link java.security.MessageDigest} sequence generated from
     * the real password, using the SHA1 algorithm, in the wrapped {@link ParseObject}
     * parameter "password".
     * The real password is never stored in the backend for security reasons.
     */
    public User setPasswordDigest(String passwordDigest) {
        if(Strings.isNullOrEmpty(passwordDigest))
            return this;

        po.put(PASSWORD_DIGEST, passwordDigest);
        return this;
    }


    /**
     * Puts the specified name in the wrapped {@link ParseObject} parameter "name".
     */
    public User setName(String name) {
        if(Strings.isNullOrEmpty(name))
            return this;

        po.put(NAME, name);
        return this;
    }


    /**
     * Puts the specified name in the wrapped {@link ParseObject} parameter "imageuri".
     */
    public User setImageUri(String imageUri) {
        if(Strings.isNullOrEmpty(imageUri))
            return this;

        po.put(IMAGE_URI, imageUri);
        return this;
    }


    /**
     * Returns the wrapped {@link ParseObject} email parameter.
     */
    public String email() {
        return po.getString(EMAIL);
    }


    /**
     * Returns the {@link java.security.MessageDigest} sequence generated from
     * the real password, using the SHA1 algorithm.
     * The real password is never stored in the backend for security reasons.
     */
    public String passwordDigest() {
        return po.getString(PASSWORD_DIGEST);
    }


    /**
     * Returns the wrapped {@link ParseObject} name parameter.
     */
    public String name() {
        return po.getString(NAME);
    }


    /**
     * Returns the wrapped {@link ParseObject} image URI parameter.
     */
    public String imageUri() {
        return po.getString(IMAGE_URI);
    }


    @Override
    public String toString() {
        return name()+" | "+email();
    }
}
