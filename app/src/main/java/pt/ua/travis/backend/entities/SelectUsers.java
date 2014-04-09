package pt.ua.travis.backend.entities;

import android.util.Log;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudBackend;
import pt.ua.travis.backend.core.CloudEntity;
import pt.ua.travis.backend.core.CloudQuery;
import pt.ua.travis.backend.core.Filter;

import java.io.IOException;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SelectUsers<T extends User> {

    protected CloudBackend cb;
    private User thisLoggedInUser;
    private Class<T> tClass;
    private String kindName;
    private List<Filter> filters;

    SelectUsers(CloudBackend cb, User thisLoggedInUser, Class<T> tClass, String kindName) {
        this.cb = cb;
        this.thisLoggedInUser = thisLoggedInUser;
        this.tClass = tClass;
        this.kindName = kindName;
        this.filters = Lists.newArrayList();
    }

    public T loggedInThisDevice() {
        return (T) thisLoggedInUser;
    }

    public T from(Ride r){
        Object obtainedObject = r.ce.get(Ride.TAXI);

        if(obtainedObject==null)
            return null;

        String id = (String) obtainedObject;
        try {
            CloudEntity entity = cb.get(Taxi.KIND_NAME, id);
            return tClass.getConstructor(CloudEntity.class).newInstance(entity);

        } catch (IOException ex) {
            Log.e("ERROR QUERYING TAXIS (CloudBackendManager SelectUsers.from(Ride)", ex.toString());
        } catch (ReflectiveOperationException ex){
            Log.e("BAD CONSTRUCTOR (CloudBackendManager SelectUsers.from(Ride)", ex.toString());
        }

        return null;
    }

    public List<T> execute() {
        List<T> selectedUser = Lists.newArrayList();

        try {
            Filter finalFilter = Filter.and(filters.toArray(new Filter[filters.size()]));
            if (kindName == null) {
                // if no kind name was provided, then select from both user entities (Client and Taxi)

                CloudQuery cq = new CloudQuery(Client.KIND_NAME);
                if (!filters.isEmpty()) {
                    cq.setFilter(finalFilter);
                }
                List<CloudEntity> selectedClients = cb.list(cq);
                for(CloudEntity ce : selectedClients) {
                    T u = (T) new Client(ce);
                    selectedUser.add(u);
                }

                cq = new CloudQuery(Taxi.KIND_NAME);
                if (!filters.isEmpty()) {
                    cq.setFilter(finalFilter);
                }
                List<CloudEntity> selectedTaxis = cb.list(cq);
                for(CloudEntity ce : selectedTaxis) {
                    T u = (T) new Taxi(ce);
                    selectedUser.add(u);
                }

            } else {
                CloudQuery cq = new CloudQuery(kindName);
                if (!filters.isEmpty()) {
                    cq.setFilter(finalFilter);
                }
                List<CloudEntity> entities = cb.list(cq);

                for (CloudEntity ce : entities) {
                    if (tClass.isInstance(Client.class))
                        selectedUser.add(tClass.getConstructor(CloudEntity.class).newInstance(ce));
                }
            }

        } catch (IOException ex) {
            Log.e("ERROR QUERYING USERS (CloudBackendManager SelectUsers.execute())", ex.toString());
        } catch (ReflectiveOperationException ex){
            Log.e("BAD CONSTRUCTOR (CloudBackendManager SelectUsers.execute())", ex.toString());
        }

        return selectedUser;
    }

    public SelectUsers<T> withEmail(String email){
        filters.add(Filter.eq(User.EMAIL, email));
        return this;
    }

    public SelectUsers<T> withPasswordDigest(String passwordDigest){
        filters.add(Filter.eq(User.PASSWORD_DIGEST, passwordDigest));
        return this;
    }

    public SelectUsers<T> withName(String name){
        filters.add(Filter.eq(User.NAME, name));
        return this;
    }

    public SelectUsers<T> withImageUri(String imageUri){
        filters.add(Filter.eq(User.IMAGE_URI, imageUri));
        return this;
    }
}
