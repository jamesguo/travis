package pt.ua.travis.backend.entities;

import com.google.common.base.Strings;
import pt.ua.travis.backend.core.CloudEntity;

import java.io.Serializable;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class User extends CloudEntityWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EMAIL = "email";
    public static final String PASSWORD_DIGEST = "password";
    public static final String NAME = "name";
    public static final String IMAGE_URI = "imageuri";


    protected User(CloudEntity ce){
        super(ce);
    }


    /**
     * Puts the specified email in the wrapped {@link CloudEntity} parameter "email".
     */
    public User setEmail(String email) {
        if(Strings.isNullOrEmpty(email))
            return this;

        ce.put(EMAIL, email);
        return this;
    }


    /**
     * Puts the specified {@link java.security.MessageDigest} sequence generated from
     * the real password, using the SHA1 algorithm, in the wrapped {@link CloudEntity}
     * parameter "password".
     * The real password is never stored in the backend for security reasons.
     */
    public User setPasswordDigest(String passwordDigest) {
        if(Strings.isNullOrEmpty(passwordDigest))
            return this;

        ce.put(PASSWORD_DIGEST, passwordDigest);
        return this;
    }


    /**
     * Puts the specified name in the wrapped {@link CloudEntity} parameter "name".
     */
    public User setName(String name) {
        if(Strings.isNullOrEmpty(name))
            return this;

        ce.put(NAME, name);
        return this;
    }


    /**
     * Puts the specified name in the wrapped {@link CloudEntity} parameter "imageuri".
     */
    public User setImageUri(String imageUri) {
        if(Strings.isNullOrEmpty(imageUri))
            return this;

        ce.put(IMAGE_URI, imageUri);
        return this;
    }


    /**
     * Returns the wrapped {@link CloudEntity} email parameter.
     */
    public String email() {
        return (String) ce.get(EMAIL);
    }


    /**
     * Returns the {@link java.security.MessageDigest} sequence generated from
     * the real password, using the SHA1 algorithm.
     * The real password is never stored in the backend for security reasons.
     */
    public String passwordDigest() {
        return (String) ce.get(PASSWORD_DIGEST);
    }


    /**
     * Returns the wrapped {@link CloudEntity} name parameter.
     */
    public String name() {
        return (String) ce.get(NAME);
    }


    /**
     * Returns the wrapped {@link CloudEntity} image URI parameter.
     */
    public String imageUri() {
        return (String) ce.get(IMAGE_URI);
    }
}
