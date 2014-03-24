package pt.ua.travis.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Client extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public final List<Integer> favorites;
    private String pass;

    public Client(final String name, final String imageUrl){
        super(name, imageUrl);
        pass = "client";
        this.favorites = new ArrayList<>();
    }

    public String getPass()
    {
         return pass;
    }
}
