package pt.ua.travis.backend;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.*;
import pt.ua.travis.utils.TravisUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Query {

    private static class Filter {

        private enum Type {
            EQ, // Equal to
            CT, // Contains
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

        private static Filter ct(String key, String value) { return new Filter(key, value, Type.CT); }

        private static Filter gt(String key, Object value) {
            return new Filter(key, value, Type.GT);
        }

        private static Filter lt(String key, Object value) {
            return new Filter(key, value, Type.LT);
        }
    }

    //    private Firebase fb;
    private List<Filter> filters;
    private int limit;
    private String idFilter;
    private ParseGeoPoint currentLocation;
    private boolean filterNear;
    private String sortKey;

    Query(){
//        this.fb = fb;
        this.filters = Lists.newArrayList();
        this.limit = -1;
        this.idFilter = null;
        filterNear = false;
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


    private void performSafeRideQuery(final FindCallback<ParseObject> queryHandler) {
        final ParseQuery<ParseObject> q = new ParseQuery<ParseObject>(Ride.OBJECT_NAME);

        if (limit > 0) {
            q.setLimit(limit);
        }

        if(sortKey!=null) {
            q.orderByAscending(sortKey);
        }

        for (Filter f : filters) {
            if (f.type.equals(Filter.Type.EQ)) {
                q.whereEqualTo(f.key, f.value);
            } else if(f.type.equals(Filter.Type.CT)) {
                q.whereContains(f.key, (String)f.value);
            } else if (f.type.equals(Filter.Type.GT)){
                q.whereGreaterThanOrEqualTo(f.key, f.value);
            } else if (f.type.equals(Filter.Type.LT)){
                q.whereLessThan(f.key, f.value);
            }
        }

        if(idFilter==null) {
            q.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException ex) {
                    if (ex != null) {
                        Log.e("PersistenceManager SafeQuery Find", "Error querying rides. ", ex);
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
                        Log.e("PersistenceManager SafeQuery Get", "Error querying rides. ", ex);
                    } else {
                        List<ParseObject> list = Lists.newArrayList();
                        list.add(object);
                        queryHandler.done(list, null);
                    }
                }
            });
        }
    }


    private List<ParseObject> performUnsafeRideQuery(){
        final ParseQuery<ParseObject> q = new ParseQuery<ParseObject>(Ride.OBJECT_NAME);

        if (limit > 0) {
            q.setLimit(limit);
        }

        if(sortKey!=null) {
            q.orderByAscending(sortKey);
        }

        try {
            for (Filter f : filters) {
                if (f.type.equals(Filter.Type.EQ)) {
                    q.whereEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.GT)){
                    q.whereGreaterThanOrEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.LT)){
                    q.whereLessThan(f.key, f.value);
                }
            }

            if(idFilter==null) {
                return q.find();

            } else {
                List<ParseObject> list = Lists.newArrayList();
                list.add(q.get(idFilter));
                return list;
            }
        } catch (ParseException ex) {
            Log.e("PersistenceManager UnsafeQuery", "Error querying rides. ", ex);
        }

        return Lists.newArrayList();
    }


    private void performSafeUserQuery(final String objectName, final FindCallback<ParseUser> queryHandler) {
        final ParseQuery<ParseUser> q = ParseUser.getQuery();
        q.whereEqualTo(User.TYPE, objectName);

        if (limit > 0) {
            q.setLimit(limit);
        }

        if (filterNear) {
            q.whereNear(Taxi.CURRENT_LOCATION, currentLocation);
        }

        if(sortKey!=null) {
            q.orderByDescending(sortKey);
        }

        for (Filter f : filters) {
            if (f.type.equals(Filter.Type.EQ)) {
                q.whereEqualTo(f.key, f.value);
            } else if(f.type.equals(Filter.Type.CT)) {
                q.whereContains(f.key, (String)f.value);
            } else if (f.type.equals(Filter.Type.GT)){
                q.whereGreaterThanOrEqualTo(f.key, f.value);
            } else if (f.type.equals(Filter.Type.LT)){
                q.whereLessThan(f.key, f.value);
            }
        }

        if(idFilter==null) {
            q.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException ex) {
                    if (ex != null) {
                        Log.e("PersistenceManager SafeQuery Find", "Error querying " + objectName + "s. ", ex);
                    } else {
                        queryHandler.done(parseUsers, null);
                    }
                }
            });

        } else {
            q.getInBackground(idFilter, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser user, ParseException ex) {
                    if(ex!=null){
                        Log.e("PersistenceManager SafeQuery Get", "Error querying " + objectName + "s. ", ex);
                    } else {
                        List<ParseUser> list = Lists.newArrayList();
                        list.add(user);
                        queryHandler.done(list, null);
                    }
                }
            });
        }
    }


    private List<ParseUser> performUnsafeUserQuery(final String objectName){
        final ParseQuery<ParseUser> q = ParseUser.getQuery();
        q.whereEqualTo(User.TYPE, objectName);

        if (limit > 0) {
            q.setLimit(limit);
        }

        if (filterNear) {
            q.whereNear(Taxi.CURRENT_LOCATION, currentLocation);
        }

        if(sortKey!=null) {
            q.orderByDescending(sortKey);
        }

        try {
            for (Filter f : filters) {
                if (f.type.equals(Filter.Type.EQ)) {
                    q.whereEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.GT)){
                    q.whereGreaterThanOrEqualTo(f.key, f.value);
                } else if (f.type.equals(Filter.Type.LT)){
                    q.whereLessThan(f.key, f.value);
                }
            }

            if(idFilter==null) {
                return q.find();

            } else {
                List<ParseUser> list = Lists.newArrayList();
                list.add(q.get(idFilter));
                return list;
            }
        } catch (ParseException ex) {
            Log.e("PersistenceManager UnsafeQuery", "Error querying " + objectName + "s. ", ex);
        }

        return Lists.newArrayList();
    }


    public abstract class QueryGeneric<T extends ParseWrapper, E extends ParseObject> {

        public abstract void later(final Callback<List<T>> queryHandler);

        public abstract List<T> now();

        protected final void convertLater(final List<E> results, final Callback<List<T>> convertCallback){
            final List<T> queried = Lists.newArrayList();

            for (E po : results) {
                fetchLater(po, new Callback<T>() {
                    @Override
                    public void onResult(T result) {
                        queried.add(result);
                    }
                });
            }

            while (queried.size() != results.size()){
                // lock until
            }

            convertCallback.onResult(queried);
        }

        protected final List<T> convertNow(final List<E> results){
            final List<T> queried = Lists.newArrayList();

            for (E po : results) {
                queried.add(fetchNow(po));
            }

            return queried;
        }

        protected abstract void fetchLater(E po, Callback<T> instantiateCallback);

        protected abstract T fetchNow(E po);
    }




    public final class QueryClients extends QueryGeneric<Client, ParseUser> {

        private QueryClients() {}

        @Override
        public void later(final Callback<List<Client>> queryHandler) {

            performSafeUserQuery(Client.OBJECT_NAME, new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    convertLater(parseUsers, new Callback<List<Client>>() {
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
            List<ParseUser> results = performUnsafeUserQuery(Client.OBJECT_NAME);
            return convertNow(results);
        }

        @Override
        protected void fetchLater(ParseUser po, Callback<Client> instantiateCallback) {
            instantiateCallback.onResult(new Client(po));
        }

        @Override
        protected Client fetchNow(ParseUser po) {
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

        public QueryClients withName(String name) {
            filters.add(Filter.ct(Client.NAME, name));
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





    public final class QueryTaxis extends QueryGeneric<Taxi, ParseUser> {

        private QueryTaxis() {}

        public QueryTaxis near(LatLng latLng) {
            filterNear = true;
            currentLocation = new ParseGeoPoint(latLng.latitude, latLng.longitude);
            return this;
        }

        public List<Taxi> favoritedBy(final Client c) {
            final List<String> favoriteTaxiIds = c.favoriteTaxisList();
            final List<Taxi> favoriteTaxis = Lists.newArrayList();

            for (final String id : favoriteTaxiIds) {
                favoriteTaxis.add(PersistenceManager.query().taxis().withId(id).now().get(0));
            }

            return favoriteTaxis;
        }



        @Override
        public void later(final Callback<List<Taxi>> queryHandler) {

            performSafeUserQuery(Taxi.OBJECT_NAME, new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> parseUsers, ParseException e) {
                    convertLater(parseUsers, new Callback<List<Taxi>>() {
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
            List<ParseUser> results = performUnsafeUserQuery(Taxi.OBJECT_NAME);
            return convertNow(results);
        }

        @Override
        protected void fetchLater(ParseUser po, Callback<Taxi> instantiateCallback) {
            instantiateCallback.onResult(new Taxi(po));
        }

        @Override
        protected Taxi fetchNow(ParseUser po) {
            return new Taxi(po);
        }

        public QueryTaxis sortedByRating() {
            sortKey = Taxi.RATING_AVERAGE;
            return this;
        }

        public QueryTaxis online() {
            filters.add(Filter.eq(Taxi.ONLINE_FLAG, Boolean.TRUE));
            return this;
        }

        public QueryTaxis available() {
            filters.add(Filter.eq(Taxi.AVAILABLE_FLAG, Boolean.TRUE));
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

        public QueryTaxis withName(String name){
            filters.add(Filter.ct(Taxi.NAME, name));
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





    public final class QueryRides extends QueryGeneric<Ride, ParseObject> {

        private boolean sortedByTime;

        private QueryRides() {
            this.sortedByTime = false;
        }


        @Override
        public void later(final Callback<List<Ride>> queryHandler) {

            performSafeRideQuery(new FindCallback<ParseObject>() {
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
            List<ParseObject> results = performUnsafeRideQuery();
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
//            new AsyncTask<Void, Void, Ride>() {
//
//                @Override
//                protected Ride doInBackground(Void... params) {
//                    return fetchNow(po);
//                }
//
//                @Override
//                protected void onPostExecute(Ride ride) {
//                    super.onPostExecute(ride);
//                    instantiateCallback.onResult(ride);
//                }
//
//            }.execute();
            instantiateCallback.onResult(fetchNow(po));
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
            filters.add(Filter.lt(Ride.SCHEDULED_TIME, TravisUtils.newTime().toNow()));
            return this;
        }

        public QueryRides completed() {
            filters.add(Filter.eq(Ride.COMPLETED_FLAG, Boolean.TRUE));
            return this;
        }

        public QueryRides uncompleted() {
            filters.add(Filter.eq(Ride.COMPLETED_FLAG, Boolean.FALSE));
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
