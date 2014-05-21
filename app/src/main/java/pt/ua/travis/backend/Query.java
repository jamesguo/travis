package pt.ua.travis.backend;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.*;
import pt.ua.travis.utils.Utils;

import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Query {

    private static class Filter {

        private enum Type {
            EQ, // Equal to
            GT, // Greater than or Equal to
            LT  // Less than
        }

        private final String key;
        private final Object value;
        private final Type type;

        private Filter(final String key, final Object value, final Type type) {
            this.key = key;
            this.value = value;
            this.type = type;
        }

        private static Filter eq(String key, Object value) {
            return new Filter(key, value, Type.EQ);
        }

        private static Filter gt(String key, Object value) {
            return new Filter(key, value, Type.GT);
        }

        private static Filter lt(String key, Object value) {
            return new Filter(key, value, Type.LT);
        }
    }

    //    private Firebase fb;
    private User loggedInUser;
    private List<Filter> filters;
    private int limit;
    private String idFilter;
    private ParseGeoPoint locationFilterSW;
    private ParseGeoPoint locationFilterNE;
    private String sortKey;

    Query(final User loggedInUser){
//        this.fb = fb;
        this.loggedInUser = loggedInUser;
        this.filters = Lists.newArrayList();
        this.limit = -1;
        this.idFilter = null;
    }

    public QueryClients clients() {
        return new QueryClients();
    }

    public QueryTaxis taxis() {
        return new QueryTaxis();
    }

    public QueryRides rides() {
        return new QueryRides();
    }


    private void performSafeQuery(final String objectName, final FindCallback<ParseObject> queryHandler) {
        final ParseQuery<ParseObject> q = new ParseQuery<ParseObject>(objectName);

        if (limit > 0) {
            q.setLimit(limit);
        }

        if (locationFilterSW!=null && locationFilterNE!=null) {
            q.whereWithinGeoBox(Taxi.CURRENT_LOCATION, locationFilterSW, locationFilterNE);
        }

        if(sortKey!=null) {
            q.orderByAscending(sortKey);
        }

        if(idFilter==null) {
            for (Filter f : filters) {
                if (f.type.equals(Filter.Type.EQ)) {
                    q.whereEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.GT)){
                    q.whereGreaterThanOrEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.LT)){
                    q.whereLessThan(f.key, f.value);
                }
            }

            q.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException ex) {
                    if (ex != null) {
                        Log.e("PersistenceManager SafeQuery Find", "Error querying " + objectName + "s. ", ex);
                    } else {
                        queryHandler.done(parseObjects, null);
                    }
                }
            });

        } else {
            q.getInBackground(idFilter, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException ex) {
                    if(ex!=null){
                        Log.e("PersistenceManager SafeQuery Get", "Error querying " + objectName + "s. ", ex);
                    } else {
                        List<ParseObject> list = Lists.newArrayList();
                        list.add(object);
                        queryHandler.done(list, null);
                    }
                }
            });

        }
    }


    private List<ParseObject> performUnsafeQuery(final String objectName){
        final ParseQuery<ParseObject> q = new ParseQuery<ParseObject>(objectName);

        if (limit > 0) {
            q.setLimit(limit);
        }

        try {
            if(idFilter==null) {
                for (Filter f : filters) {
                    if (f.type.equals(Filter.Type.EQ)) {
                        q.whereEqualTo(f.key, f.value);
                    } else if (f.type.equals(Filter.Type.GT)){
                        q.whereGreaterThanOrEqualTo(f.key, f.value);
                    } else if (f.type.equals(Filter.Type.LT)){
                        q.whereLessThan(f.key, f.value);
                    }
                }

                return q.find();


            } else {
                List<ParseObject> list = Lists.newArrayList();
                list.add(q.get(idFilter));
                return list;
            }
        } catch (ParseException ex) {
            Log.e("PersistenceManager UnsafeQuery", "Error querying " + objectName + "s. ", ex);
        }

        return Lists.newArrayList();
    }



    public abstract class QueryGeneric<T extends ParseObjectWrapper> {

        public abstract void later(final Callback<List<T>> queryHandler);

        public abstract List<T> now();

        protected final void convertLater(final List<ParseObject> results, final Callback<List<T>> convertCallback){
            final List<T> queried = Lists.newArrayList();

            for (ParseObject po : results) {
                fetchLater(po, new Callback<T>() {
                    @Override
                    public void onResult(T result) {
                        queried.add(result);
                    }
                });
            }

            while (queried.size() != results.size()){

            }

            convertCallback.onResult(queried);
        }

        protected final List<T> convertNow(final List<ParseObject> results){
            final List<T> queried = Lists.newArrayList();

            for (ParseObject po : results) {
                queried.add(fetchNow(po));
            }

            return queried;
        }

        protected abstract void fetchLater(ParseObject po, Callback<T> instantiateCallback);

        protected abstract T fetchNow(ParseObject po);
    }




    public final class QueryClients extends QueryGeneric<Client> {

        private QueryClients() {}

        public Client loggedInThisDevice() {
            if (loggedInUser instanceof Client)
                return (Client) loggedInUser;
            else
                return null;
        }

        @Override
        public void later(final Callback<List<Client>> queryHandler) {

            performSafeQuery(Client.OBJECT_NAME, new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> results, ParseException e) {
                    convertLater(results, new Callback<List<Client>>() {
                        @Override
                        public void onResult(List<Client> queriedClients) {
                            queryHandler.onResult(queriedClients);
                        }
                    });
                }
            });
        }

        @Override
        public List<Client> now() {
            List<ParseObject> results = performUnsafeQuery(Client.OBJECT_NAME);
            return convertNow(results);
        }

        @Override
        protected void fetchLater(ParseObject po, Callback<Client> instantiateCallback) {
            instantiateCallback.onResult(new Client(po));
        }

        @Override
        protected Client fetchNow(ParseObject po) {
            return new Client(po);
        }

        public QueryClients withId(String id) {
            idFilter = id;
            return this;
        }

        public QueryClients withEmail(String email) {
            filters.add(Filter.eq(Client.EMAIL, email));
            return this;
        }

        public QueryClients withPasswordDigest(String passwordDigest) {
            filters.add(Filter.eq(Client.PASSWORD_DIGEST, passwordDigest));
            return this;
        }

        public QueryClients withName(String name) {
            filters.add(Filter.eq(Client.NAME, name));
            return this;
        }

        public QueryClients withImageUri(String imageUri) {
            filters.add(Filter.eq(Client.IMAGE_URI, imageUri));
            return this;
        }

        public QueryClients limitNumberOfResultsTo(int newLimit) {
            limit = newLimit;
            return this;
        }
    }





    public final class QueryTaxis extends QueryGeneric<Taxi> {

        private QueryTaxis() {}

        public Taxi loggedInThisDevice(){
            if(loggedInUser instanceof Taxi)
                return (Taxi) loggedInUser;
            else
                return null;
        }

        public QueryTaxis near(LatLng latLng){
            locationFilterSW = new ParseGeoPoint(latLng.latitude - 10, latLng.longitude - 10);
            locationFilterNE = new ParseGeoPoint(latLng.latitude + 10, latLng.longitude + 10);
            return this;
        }

        public void favoritedBy(final Client c, final Callback<List<Taxi>> queryHandler) {
//
            final List<String> favoriteTaxiIds = c.favoriteTaxisList();
            final List<Taxi> favoriteTaxis = Lists.newArrayList();

            for (final String id : favoriteTaxiIds) {
                PersistenceManager.query().taxis().withId(id).later(new Callback<List<Taxi>>() {
                    @Override
                    public void onResult(List<Taxi> result) {
                        favoriteTaxis.add(result.get(0));
                    }
                });
            }

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    while (favoriteTaxis.size() != favoriteTaxiIds.size()){
                        // LOCK AND WAIT
                    }

                    queryHandler.onResult(favoriteTaxis);
                    return null;
                }
            }.execute();
        }



        @Override
        public void later(final Callback<List<Taxi>> queryHandler) {

            performSafeQuery(Taxi.OBJECT_NAME, new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> results, ParseException e) {
                    convertLater(results, new Callback<List<Taxi>>() {
                        @Override
                        public void onResult(List<Taxi> queriedTaxis) {
                            queryHandler.onResult(queriedTaxis);
                        }
                    });
                }
            });
        }


        @Override
        public List<Taxi> now() {
            List<ParseObject> results = performUnsafeQuery(Taxi.OBJECT_NAME);
            return convertNow(results);
        }

        @Override
        protected void fetchLater(ParseObject po, Callback<Taxi> instantiateCallback) {
            instantiateCallback.onResult(new Taxi(po));
        }

        @Override
        protected Taxi fetchNow(ParseObject po) {
            return new Taxi(po);
        }

        public QueryTaxis sortedByRating() {
            sortKey = Taxi.RATINGS_LIST;
            return this;
        }

        public QueryTaxis withId(String id) {
            idFilter = id;
            return this;
        }

        public QueryTaxis withEmail(String email){
            filters.add(Filter.eq(Taxi.EMAIL, email));
            return this;
        }

        public QueryTaxis withPasswordDigest(String passwordDigest){
            filters.add(Filter.eq(Taxi.PASSWORD_DIGEST, passwordDigest));
            return this;
        }

        public QueryTaxis withName(String name){
            filters.add(Filter.eq(Taxi.NAME, name));
            return this;
        }

        public QueryTaxis withImageUri(String imageUri){
            filters.add(Filter.eq(Taxi.IMAGE_URI, imageUri));
            return this;
        }

        public QueryTaxis limitNumberOfResultsTo(int newLimit) {
            limit = newLimit;
            return this;
        }
    }





    public final class QueryRides extends QueryGeneric<Ride> {

        private boolean sortedByTime;

        private QueryRides() {
            this.sortedByTime = false;
        }


        @Override
        public void later(final Callback<List<Ride>> queryHandler) {

            performSafeQuery(Ride.OBJECT_NAME, new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    convertLater(parseObjects, new Callback<List<Ride>>() {
                        @Override
                        public void onResult(List<Ride> queriedRides) {
                            if (sortedByTime) {
                                Collections.sort(queriedRides, new Comparator<Ride>() {
                                    @Override
                                    public int compare(Ride r1, Ride r2) {
//                                        Integer hour1 = r1.scheduledTime.hourOfDay().performQueryForSpecific();
//                                        Integer hour2 = r2.scheduledTime.hourOfDay().performQueryForSpecific();
//                                        int hourCompare = hour1.compareTo(hour2);
//                                        Integer minute1 = r1.scheduledTime.minuteOfHour().performQueryForSpecific();
//                                        Integer minute2 = r2.scheduledTime.minuteOfHour().performQueryForSpecific();
//                                        int minuteCompare = minute1.compareTo(minute2);
                                        Calendar scheduledTime1 = r1.getScheduledTime();
                                        Calendar scheduledTime2 = r2.getScheduledTime();
                                        return scheduledTime1.compareTo(scheduledTime2);
                                    }
                                });
                            }

                            queryHandler.onResult(queriedRides);
                        }
                    });
                }
            });
        }


        @Override
        public List<Ride> now() {
            List<ParseObject> results = performUnsafeQuery(Taxi.OBJECT_NAME);
            List<Ride> queriedRides = convertNow(results);

            if (sortedByTime) {
                Collections.sort(queriedRides, new Comparator<Ride>() {
                    @Override
                    public int compare(Ride r1, Ride r2) {
//                            Integer hour1 = r1.scheduledTime.hourOfDay().performQueryForSpecific();
//                            Integer hour2 = r2.scheduledTime.hourOfDay().performQueryForSpecific();
//                            int hourCompare = hour1.compareTo(hour2);
//                            Integer minute1 = r1.scheduledTime.minuteOfHour().performQueryForSpecific();
//                            Integer minute2 = r2.scheduledTime.minuteOfHour().performQueryForSpecific();
//                            int minuteCompare = minute1.compareTo(minute2);
                        Calendar scheduledTime1 = r1.getScheduledTime();
                        Calendar scheduledTime2 = r2.getScheduledTime();
                        return scheduledTime1.compareTo(scheduledTime2);
                    }
                });
            }

            return queriedRides;
        }

        @Override
        protected void fetchLater(final ParseObject po, final Callback<Ride> instantiateCallback) {
            Ride ride = Utils.lockThreadAndExecute(new Utils.Code<Ride>() {
                @Override
                public Ride execute() {
                    return fetchNow(po);
                }
            });
            instantiateCallback.onResult(ride);
        }

        @Override
        protected Ride fetchNow(ParseObject po) {
            final String cId = po.getString(Ride.CLIENT_ID);
            final String tId = po.getString(Ride.TAXI_ID);
            final Client c = PersistenceManager.query().clients().withId(cId).now().get(0);
            final Taxi t = PersistenceManager.query().taxis().withId(tId).now().get(0);

            return new Ride(po, c, t);
        }

        public QueryRides withId(String id) {
            idFilter = id;
            return this;
        }

        public QueryRides sortedByTime() {
            this.sortedByTime = true;
            return this;
        }

        public QueryRides performed() {
            filters.add(Filter.lt(Ride.SCHEDULED_TIME, Utils.newTime().toNow()));
            return this;
        }

        public QueryRides scheduled() {
            filters.add(Filter.gt(Ride.SCHEDULED_TIME, Utils.newTime().toNow()));
            return this;
        }

        public QueryRides withUser(User u) {
            if (u instanceof Taxi) {
                filters.add(Filter.eq(Ride.TAXI_ID, u.id()));
            } else if (u instanceof Client) {
                filters.add(Filter.eq(Ride.CLIENT_ID, u.id()));
            }
            return this;
        }

        public QueryRides limitNumberOfResultsTo(int newLimit) {
            limit = newLimit;
            return this;
        }
    }
}
