package pt.ua.travis.core;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisObject {
    private static Set<String> usedIDs = new HashSet<>();

    private final String id;

    protected TravisObject(){
        this.id = generateUniqueID();
        usedIDs.add(id);
    }

    private String generateUniqueID(){
        String possibleNewID = UUID.randomUUID().toString();

        return usedIDs.contains(possibleNewID) ?
                generateUniqueID() :
                possibleNewID;
    }

    public String getId() {
        return id;
    }

    public TravisObject populateUsedIDs(Set<String> moreIDs){
        usedIDs.addAll(moreIDs);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(!(o instanceof TravisObject))
            return false;

        TravisObject other = (TravisObject) o;
        return this.id.equals(other.id);
    }
}
