package pt.ua.travis.backend.entities;

import android.util.Log;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudBackend;
import pt.ua.travis.backend.core.CloudEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class SelectTaxis extends SelectUsers<Taxi> {

    SelectTaxis(CloudBackend cb, User thisLoggedInUser){
        super(cb, thisLoggedInUser, Taxi.class, Taxi.KIND_NAME);
    }

    public List<Taxi> favoritedBy(Client c){
        List<String> taxiIds = c.favoriteTaxisIdsList();
        ArrayList<Taxi> result = Lists.newArrayList();
        result.ensureCapacity(taxiIds.size());

        try {
            List<CloudEntity> entities = cb.getAll(Taxi.KIND_NAME, taxiIds);

            for(CloudEntity ce : entities){
                result.add(new Taxi(ce));
            }

        } catch (IOException ex) {
            Log.e("ERROR QUERYING TAXIS (CloudBackendManager favoriteTaxisList())", ex.toString());
        }

        return result;
    }
}
