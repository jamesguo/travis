package pt.ua.travis.backend.entities;

import com.google.common.collect.Lists;
import com.parse.ParseObject;

import java.io.Serializable;
import java.util.List;

/**
 * Builder and wrapper class for the Client entity stored in the Cloud Datastore API.
 * A Client is a {@link User} that:
 *      - HAS-MANY favorite {@link Taxi}
 *      - HAS-MANY requested {@link Ride}
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class Client extends User {
//    private static final long serialVersionUID = 1L;

    public static final String OBJECT_NAME = "Client";

    // Parse data column keys (DO NOT CHANGE)
    public static final String FAVORITE_TAXIS_IDS = "favorite_taxis_ids";

    public Client() {
        super(new ParseObject(OBJECT_NAME));
    }

    Client(ParseObject ce){
        super(ce);
        if(!ce.getClassName().equals(OBJECT_NAME)){
            throw new IllegalArgumentException("The specified ParseObject denotes a " +
                    ce.getClassName()+". Should denote a "+ OBJECT_NAME +" entity instead.");
        }
    }

    @Override
    protected String thisObjectName() {
        return OBJECT_NAME;
    }

    /**
     * Sets the specified taxi as a favorite of this user by adding it to the wrapped
     * {@link ParseObject} parameter "ratings".
     */
    public Client addTaxiAsFavorite(Taxi taxi) {
        if (taxi == null)
            return this;

        List<String> taxiIds = favoriteTaxisList();
        taxiIds.add(taxi.id());
        po.put(FAVORITE_TAXIS_IDS, taxiIds);

        return this;
    }

    /**
     * Removes the specified taxi as a favorite of this user by removing it from the
     * wrapped {@link ParseObject} parameter "ratings".
     */
    public Client removeTaxiAsFavorite(Taxi taxi){
        if (taxi == null)
            return this;

        List<String> favorites = favoriteTaxisList();
        favorites.remove(taxi.id());
        po.put(FAVORITE_TAXIS_IDS, favorites);

        return this;
    }

    /**
     * Checks if the specified taxi is as a favorite of this user.
     */
    public boolean taxiIsAFavorite(Taxi taxi){
        if(taxi==null)
            return false;

        List<String> favorites = favoriteTaxisList();
        return favorites.contains(taxi.id());
    }


    /**
     * Returns the wrapped {@link ParseObject} number of taxis that this set as favorite.
     */
    public int numberOfFavorites(){
        List<String> taxiIds = favoriteTaxisList();
        return taxiIds.size();
    }


    List<String> favoriteTaxisList(){
        List<String> obtainedList = po.getList(FAVORITE_TAXIS_IDS);

        if(obtainedList==null){
            return Lists.newArrayList();
        } else {
            return obtainedList;
        }
    }


    @Override
    public String toString() {
        return "< "+Client.OBJECT_NAME +
                " | "+super.toString()+
                " | "+ id()+" >";
    }
}
