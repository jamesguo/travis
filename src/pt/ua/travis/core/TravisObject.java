package pt.ua.travis.core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Set<Integer> usedIDs = new HashSet<Integer>();
    private static Random rnd = new Random();

    public final int id;

    protected TravisObject(){
        this.id = generateUniqueID();
        usedIDs.add(id);
    }

    private int generateUniqueID(){
        int possibleNewID = rnd.nextInt(999999) + 1;

        return usedIDs.contains(possibleNewID) ?
                generateUniqueID() :
                possibleNewID;
    }


    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof TravisObject))
            return false;

        TravisObject other = (TravisObject) o;
        return this.id == other.id;
    }

    public static void populateUsedIDs(Set<Integer> moreIDs){
        usedIDs.addAll(moreIDs);
    }
}
