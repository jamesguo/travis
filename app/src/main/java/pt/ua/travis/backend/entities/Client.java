package pt.ua.travis.backend.entities;

import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudEntity;

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
public final class Client extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String KIND_NAME = "Client";

    public static final String FAVORITE_TAXIS_IDS = "favoritetaxisids";

    public Client() {
        super(new CloudEntity(KIND_NAME));
    }

    Client(CloudEntity ce){
        super(ce);
        if(!ce.getKindName().equals(KIND_NAME)){
            throw new IllegalArgumentException("The specified CloudEntity denotes a " +
                    ce.getKindName()+". Should denote a "+KIND_NAME+" entity instead.");
        }
    }

    /**
     * Sets the specified taxi as a favorite of this user by adding it to the wrapped
     * {@link CloudEntity} parameter "ratings".
     */
    public Client addTaxiAsFavorite(Taxi taxiToFavorite){
        if(taxiToFavorite==null)
            return this;

        List<String> taxiIds = favoriteTaxisIdsList();
        taxiIds.add(taxiToFavorite.getId());

        return this;
    }

    /**
     * Removes the specified taxi as a favorite of this user by removing it from the
     * wrapped {@link CloudEntity} parameter "ratings".
     */
    public Client removeTaxiAsFavorite(Taxi taxiToUnfavorite){
        if(taxiToUnfavorite==null)
            return this;

        List<String> taxiIds = favoriteTaxisIdsList();
        taxiIds.remove(taxiToUnfavorite.getId());

        return this;
    }

    /**
     * Checks if the specified taxi is as a favorite of this user.
     */
    public boolean taxiIsAFavorite(Taxi taxi){
        if(taxi==null)
            return false;

        List<String> taxiIds = favoriteTaxisIdsList();
        return taxiIds.contains(taxi.getId());
    }

    /**
     * Returns the wrapped {@link CloudEntity} list of this user's favorite taxis ids.
     */
    List<String> favoriteTaxisIdsList(){
        Object obtainedObject = ce.get(FAVORITE_TAXIS_IDS);

        if(obtainedObject==null){
            return Lists.newArrayList();
        } else {
            return  (List<String>) obtainedObject;
        }
    }


    /**
     * Returns the wrapped {@link CloudEntity} number of taxis that this set as favorite.
     */
    public int numberOfFavorites(){
        List<String> taxiIds = favoriteTaxisIdsList();
        return taxiIds.size();
    }
}
