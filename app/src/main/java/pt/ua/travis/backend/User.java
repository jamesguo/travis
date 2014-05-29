package pt.ua.travis.backend;

import android.util.Log;
import com.google.common.base.Strings;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class User extends ParseUserWrapper {

    // Parse data column keys (DO NOT CHANGE)
    public static final String EMAIL = "email";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String IMAGE_URI = "imageuri";


    protected User(ParseUser po){
        super(po);
    }


    /**
     * Puts the specified email in the wrapped {@link ParseObject} parameter "email".
     */
    public User setEmail(String email) {
        if(Strings.isNullOrEmpty(email))
            return this;

        String username = email.replaceAll("[\\s.]", "").replaceAll("@", "");
        po.setUsername(username);
        po.setEmail(email);
        return this;
    }


    /**
     * Puts the specified {@link java.security.MessageDigest} sequence generated from
     * the real password, using the SHA1 algorithm, in the wrapped {@link ParseObject}
     * parameter "password".
     * The real password is never stored in the backend for security reasons.
     */
    public User setPassword(String password) {
        if(Strings.isNullOrEmpty(password))
            return this;

        po.setPassword(password);
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
        return po.getEmail();
    }


//    /**
//     * Returns the {@link java.security.MessageDigest} sequence generated from
//     * the real password, using the SHA1 algorithm.
//     * The real password is never stored in the backend for security reasons.
//     */
//    public String password() {
//        return po.getString(PASSWORD_DIGEST);
//    }


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
