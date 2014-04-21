package pt.ua.travis.backend;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.firebase.client.*;
import com.google.common.collect.Lists;
import com.parse.*;
import pt.ua.travis.ui.taxiridesetup.RideRequestTask;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.Uno;
import pt.ua.travis.utils.Utils;

import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class PersistenceManager {

    public static final int NO_USER_WITH_THAT_EMAIL = 11;
    public static final int WRONG_CREDENTIALS = 22;
    public static final int SUCCESSFUL_LOGIN = 33;

    private static final String RIDE_STATUS_WAITING_FOR_ARRIVAL = "waiting_for_arrival";
    private static final String RIDE_STATUS_TAXI_ARRIVED = "taxi_arrived";
    private static final String RIDE_STATUS_COMPLETED = "completed";

    private static final String FB_TAXIS = "taxis";
    private static final String FB_RIDES = "rides";

    private static Firebase fb;
    private static List<ValueEventListener> valueListeners;
    private static List<ChildEventListener> childListeners;

    private static Map<String, ParseObjectWrapper> cachedParseObjects;

    private static User thisLoggedInUser;

    private PersistenceManager(){}

    /**
     * Initializes the backend for insertion or selection.
     */
    public static void init(Context context) {
        Parse.initialize(context, "pu8HYbkiyqVucSgUNcsYcgu6AyRLlZQhik2CIdQt", "BzI7yfkTisCZXvHecCZHs92qOIu4ozDO3NbgTKTf");
        fb = new Firebase("https://burning-fire-4047.firebaseio.com/");
        valueListeners = Lists.newArrayList();
        childListeners = Lists.newArrayList();
        cachedParseObjects = Utils.newMap();

//        // UNCOMMENT THIS ONLY DURING DEVELOPMENT
//        populateDB();
    }

    public static <T extends ParseObjectWrapper> void addToCache(T object) {
        cachedParseObjects.put(object.id(), object);
    }

    public static <T extends ParseObjectWrapper> T getFromCache(String id) {
        return (T)cachedParseObjects.get(id);
    }


    /**
     * Cancels every subscription to Taxi entities, cancelling the reception
     * of REST responses to update {@link ParseObject ParseObjects} that
     * correspond to changed Taxis.
     */
    public static void stopWatchingTaxis() {
        Firebase taxisRef = fb.child(FB_TAXIS);
        for (ValueEventListener l : valueListeners) {
            taxisRef.removeEventListener(l);
        }
        valueListeners.clear();
    }


    /**
     * Cancels every subscription to Ride entities, cancelling the reception
     * of REST responses to update {@link ParseObject ParseObjects} that
     * correspond to added or changed Rides.
     */
    public static void stopWatchingRides(){
        Firebase ridesRef = fb.child(FB_RIDES);
        for(ChildEventListener l : childListeners) {
            ridesRef.removeEventListener(l);
        }
        childListeners.clear();
    }


    /**
     * Saves the wrapped {@link ParseObject} that represents a Client in the backend,
     * by updating it or, if it doesn't exist yet, by inserting it.
     */
    public static void save(final Client toAdd, final Callback<Client> saveHandler){
        toAdd.po.saveInBackground(new com.parse.SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    if (saveHandler != null) {
                        toAdd.po.fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                saveHandler.onResult(new Client(object));
                            }
                        });
                    }
                } else {
                    Log.e("PersistenceManager", "There was a problem inserting the client " + toAdd, ex);
                }
            }
        });
    }


    /**
     * Saves the wrapped {@link ParseObject} that represents a Taxi in the backend,
     * by updating it or, if it doesn't exist yet, by inserting it.
     */
    public static void save(final Taxi toAdd, final Callback<Taxi> saveHandler){
        toAdd.po.saveInBackground(new com.parse.SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    toAdd.po.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {

                            Map<String, Object> toSet = new HashMap<String, Object>();
                            toSet.put(object.getObjectId(), new Random().nextInt(Integer.MAX_VALUE));

                            fb.child(FB_TAXIS).updateChildren(toSet);

                            if (saveHandler != null) {
                                saveHandler.onResult(new Taxi(object));
                            }
                        }
                    });
                } else {
                    Log.e("PersistenceManager", "There was a problem inserting the client " + toAdd, ex);
                }
            }
        });
    }


    /**
     * Saves the wrapped {@link ParseObject} that represents a Ride in the backend,
     * by updating it or, if it doesn't exist yet, by inserting it.
     */
    public static void save(final Ride toAdd, final Callback<Ride> saveHandler){
        toAdd.po.saveInBackground(new com.parse.SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    toAdd.po.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {

                            PersistenceManager.query().rides().fetchLater(object, new Callback<Ride>() {
                                @Override
                                public void onResult(Ride result) {

                                    Map<String, Object> toSet = new HashMap<String, Object>();
                                    Map<String, Object> ride = new HashMap<String, Object>();
                                    ride.put(result.id(), RideRequestTask.RESPONSE_WAITING);
                                    toSet.put(result.taxi().id(), ride);

                                    fb.child(FB_RIDES).updateChildren(toSet);

                                    if (saveHandler != null) {
                                        saveHandler.onResult(result);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Log.e("PersistenceManager", "There was a problem inserting the client " + toAdd, ex);
                }
            }
        });
    }

    public static <T extends ParseObjectWrapper> void delete(final T toDelete){
        toDelete.po.deleteEventually();
    }


    public static void waitForRideResponse(final Ride requestedRide, final WatchEvent<String> responseEvent){
        final Firebase requestedRideRef = fb.child(FB_RIDES).child(requestedRide.taxi().id()).child(requestedRide.id());
        final Uno<Boolean> receivedResponse = Utils.newUno(false);

        final ValueEventListener responseWatcher = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    String response = dataSnapshot.getValue().toString();

                    if (!response.equals(RideRequestTask.RESPONSE_WAITING)) {
                        receivedResponse.value = true;
                        requestedRideRef.removeEventListener(this);

                        if (response.equals(RideRequestTask.RESPONSE_ACCEPTED)) {
                            requestedRideRef.setValue(RIDE_STATUS_WAITING_FOR_ARRIVAL);

                        }
                        responseEvent.onEvent(response);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        };
        requestedRideRef.addValueEventListener(responseWatcher);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!receivedResponse.value){
                    requestedRideRef.removeEventListener(responseWatcher);
                    responseEvent.onEvent(RideRequestTask.RESPONSE_TIMEOUT);
                }
            }
        }, 18000);
    }



    public static void startWatchingNewRidesForTaxi(final Taxi taxiWithRidesToWatch, final WatchEvent<Ride> watchEvent){
        Firebase ridesRef = fb.child(FB_RIDES);

        ChildEventListener rideWatcher = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                final String id = dataSnapshot.getName();
                final String status = dataSnapshot.getValue().toString();

                if(status.equals(RideRequestTask.RESPONSE_WAITING)) {
                    Log.e("RideWatcher", "Ride with id " + id + " was added!");

                    PersistenceManager.query().rides().withId(id).later(new Callback<List<Ride>>() {
                        @Override
                        public void onResult(List<Ride> result) {
                            watchEvent.onEvent(result.get(0));
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        };
        childListeners.add(rideWatcher);
        ridesRef.child(taxiWithRidesToWatch.id()).addChildEventListener(rideWatcher);
    }

    public static void watchTaxis(final List<Taxi> toWatch, final WatchEvent<List<Taxi>> action){
        Firebase taxisRef = fb.child(FB_TAXIS);

        for(Taxi t : toWatch) {
            TaxiWatcher taxiWatcher = new TaxiWatcher(toWatch, action);
            valueListeners.add(taxiWatcher);
            taxisRef.child(t.id()).addValueEventListener(taxiWatcher);
        }
    }

    public static class TaxiWatcher implements ValueEventListener {

        private Map<String, Taxi> taxis;
        private WatchEvent<List<Taxi>> action;

        private TaxiWatcher(List<Taxi> toWatch, WatchEvent<List<Taxi>> action) {
            taxis = new LinkedHashMap<String, Taxi>();
            for (Taxi t : toWatch) {
                String id = t.id();
                taxis.put(id, t);
            }
            this.action = action;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            final String id = dataSnapshot.getName();
            Log.e("TaxiWatcher", "Taxi with id "+id+" was changed!");
            ParseObject po = taxis.get(id).po;

            po.fetchInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    taxis.put(id, new Taxi(object));
                    action.onEvent(Lists.newArrayList(taxis.values()));
                }
            });
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {}
    }


    /**
     * Returns a Fluent API to build search parameters and query the backend.
     */
    public static Query query() {
        return new Query(thisLoggedInUser);
    }



    /**
     * Saves the specified user as a static variable to be easily accessible
     * during the application's execution.
     * WARNING: This method must only be executed in an Async task!!
     */
    public static Pair<Integer, User> attemptLogin(String email, String passDigest) {
        try {
            Pair<User, String> pair = testEmail(email);
            User resultUser = null;

            if(pair == null){
                // Email doesn't exist, can create a new account withUser credentials
                thisLoggedInUser = null;
                return Utils.newPair(NO_USER_WITH_THAT_EMAIL, null);

            } else if(pair.second.equals(Client.OBJECT_NAME)){
                // Email corresponds to a Client, so test if querying clients
                // withUser that email and the specified passDigest returns a Client entity.
                resultUser = testPass(Client.OBJECT_NAME, email, passDigest);

            } else if(pair.second.equals(Taxi.OBJECT_NAME)){
                // Email corresponds to a Taxi, so test if querying taxis
                // withUser that email and the specified passDigest returns a Taxi entity.
                resultUser = testPass(Taxi.OBJECT_NAME, email, passDigest);
            }

            if(resultUser == null) {
                thisLoggedInUser = null;
                return Utils.newPair(WRONG_CREDENTIALS, null);
            } else {
                thisLoggedInUser = resultUser;
                return Utils.newPair(SUCCESSFUL_LOGIN, resultUser);
            }

        } catch (ParseException ex){
            Log.e("PersistenceManager Login", ex.toString());
        }

        return null;
    }

    private static Pair<User, String> testEmail(String email) throws ParseException {
        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>(Client.OBJECT_NAME);
        query1.setLimit(1);
        query1.whereEqualTo(Client.EMAIL, email);
        List<ParseObject> o1 = query1.find();
        if(!o1.isEmpty()){
            User u = new Client(o1.get(0));
            return Utils.newPair(u, Client.OBJECT_NAME);
        } else {
            // Email doesn't exist, could be a taxi instead
            ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>(Taxi.OBJECT_NAME);
            query2.setLimit(1);
            query2.whereEqualTo(Taxi.EMAIL, email);
            List<ParseObject> o2 = query2.find();
            if(!o2.isEmpty()){
                User u = new Taxi(o2.get(0));
                return Utils.newPair(u, Taxi.OBJECT_NAME);
            } else {
                // Email doesn't exist, can create a new account withUser credentials
                return null;
            }
        }
    }

    private static User testPass(String objectName, String email, String passDigest) throws ParseException {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(objectName);
        query.setLimit(1);
        query.whereEqualTo(User.EMAIL, email);
        query.whereEqualTo(User.PASSWORD_DIGEST, passDigest);
        List<ParseObject> o = query.find();

        if(!o.isEmpty()){
            if(objectName.equals(Client.OBJECT_NAME))
                return new Client(o.get(0));
            else if(objectName.equals(Taxi.OBJECT_NAME))
                return new Taxi(o.get(0));
        }

        return null;
    }



    /**
     * Stops saving the static variable that holds the logged in user.
     */
    public static void logout(){
        thisLoggedInUser = null;
    }












    // TEST METHOD, DELETE WHEN DB IS ALREADY POPULATED
    private static void populateDB() {

//        // 1º Phase
//        Taxi t1 = new Taxi();
//        t1.setEmail("a@a.a");
//        t1.setPasswordDigest(Utils.generateSHA1DigestFromString("111"));
//        t1.setName("André Figueiredo");
//        t1.setImageUri("http://placesheen.com/phpthumb/phpthumb.php?src=../uploads/sheen/33.jpeg&w=140&h=180&zc=1");
//        t1.setCurrentLocation(40.646808, -8.662223);
//        t1.addRating(2.5f);
//        t1.addRating(3f);
//        t1.addRating(4f);
//        t1.addRating(2f);
//        t1.addRating(3f);
//        t1.setAsAvailable();
//        save(t1, null);
//
//
//        Taxi t2 = new Taxi();
//        t2.setEmail("b@b.b");
//        t2.setPasswordDigest(Utils.generateSHA1DigestFromString("222"));
//        t2.setName("Ernesto Abreu");
//        t2.setImageUri("http://www.fillmurray.com/140/180");
//        t2.setCurrentLocation(40.635606, -8.659305);
//        t2.addRating(1f);
//        t2.addRating(0.5f);
//        t2.addRating(1f);
//        t2.addRating(2f);
//        t2.addRating(1f);
//        t2.setAsAvailable();
//        save(t2, null);
//
//
//        Taxi t3 = new Taxi();
//        t3.setEmail("c@c.c");
//        t3.setPasswordDigest(Utils.generateSHA1DigestFromString("333"));
//        t3.setName("Carlos Oliveira");
//        t3.setImageUri("http://www.fillmurray.com/g/280/360");
//        t3.setCurrentLocation(40.645831, -8.640680);
//        t3.addRating(3f);
//        t3.addRating(4.5f);
//        t3.addRating(5f);
//        t3.addRating(1.5f);
//        t3.addRating(4f);
//        t3.addRating(3.5f);
//        t3.addRating(4f);
//        t3.addRating(2.5f);
//        t3.addRating(4f);
//        t3.addRating(2f);
//        t3.setAsAvailable();
//        save(t3, null);
//
//
//        Taxi t4 = new Taxi();
//        t4.setEmail("d@d.d");
//        t4.setPasswordDigest(Utils.generateSHA1DigestFromString("444"));
//        t4.setName("Duarte Manolo");
//        t4.setImageUri("http://www.fillmurray.com/280/360");
//        t4.setCurrentLocation(40.645631, -8.640480);
//        t4.addRating(4f);
//        t4.addRating(4f);
//        t4.addRating(2f);
//        t4.addRating(3f);
//        t4.addRating(4f);
//        t4.addRating(3.5f);
//        t4.setAsAvailable();
//        save(t4, null);
//
//
//        Taxi t5 = new Taxi();
//        t5.setEmail("e@e.e");
//        t5.setPasswordDigest(Utils.generateSHA1DigestFromString("222"));
//        t5.setName("Óscar Cardoso");
//        t5.setImageUri("http://www.placecage.com/140/180");
//        t5.setCurrentLocation(40.635411, -8.619823);
//        t5.addRating(3f);
//        t5.setAsUnavailable();
//        save(t5, null);


        // 2º Phase
//            new ParseQuery<ParseObject>(Taxi.OBJECT_NAME).getInBackground("9olwiJXIp2", new GetCallback<ParseObject>() {
//                @Override
//                public void done(ParseObject object, ParseException e) {
//                    final Taxi t1 = new Taxi(object);
//                    new ParseQuery<ParseObject>(Taxi.OBJECT_NAME).getInBackground("XEhWbTDfAf", new GetCallback<ParseObject>() {
//                        @Override
//                        public void done(ParseObject object, ParseException e) {
//                            final Taxi t3 = new Taxi(object);
//                            new ParseQuery<ParseObject>(Taxi.OBJECT_NAME).getInBackground("5khKZCBuhL", new GetCallback<ParseObject>() {
//                                @Override
//                                public void done(ParseObject object, ParseException e) {
//                                    final Taxi t5 = new Taxi(object);
//
//                                    Log.e("jjjjjjjjjjjjjjjjjjjjjjjj", t1.toString());
//                                    Log.e("jjjjjjjjjjjjjjjjjjjjjjjj", t3.toString());
//                                    Log.e("jjjjjjjjjjjjjjjjjjjjjjjj", t5.toString());
//
//                                    Client c1 = new Client();
//                                    c1.setEmail("a@b.c");
//                                    c1.setPasswordDigest(Utils.generateSHA1DigestFromString("123"));
//                                    c1.setName("João Martins");
//                                    c1.setImageUri("http://www.placecage.com/280/360");
//                                    c1.addTaxiAsFavorite(t3);
//                                    c1.addTaxiAsFavorite(t1);
//                                    save(c1, null);
//
//
//                                    Client c2 = new Client();
//                                    c2.setEmail("d@e.f");
//                                    c2.setPasswordDigest(Utils.generateSHA1DigestFromString("456"));
//                                    c2.setName("Bárbara Esteves");
//                                    c2.setImageUri("http://placeimg.com/210/270/people");
//                                    c2.addTaxiAsFavorite(t5);
//                                    c2.addTaxiAsFavorite(t3);
//                                    save(c2, null);
//
//                                }
//                            });
//                        }
//                    });
//                }
//            });



//        // 3º Phase
//        PersistenceManager.query().clients().withId("JYnjsQ0QI9").later(new Callback<List<Client>>() {
//            @Override
//            public void onResult(List<Client> result) {
//                final Client c1 = result.get(0);
//
//                PersistenceManager.query().taxis().withId("5khKZCBuhL").later(new Callback<List<Taxi>>() {
//                    @Override
//                    public void onResult(List<Taxi> result) {
//                        final Taxi t5 = result.get(0);
//
//                        Ride r1 = new Ride();
//                        r1.setClient(c1);
//                        r1.setTaxi(t5);
//                        r1.setScheduledTime(17, 28);
//                        r1.setOriginLocation(40.61790936, -8.642008904);
//                        r1.setDestinationLocation(40.61795936, -8.642098904);
//                        save(r1, null);
//                    }
//                });
//            }
//        });


//        // 4º Phase
//        Map<String, Object> toSet = new HashMap<String, Object>();
//        toSet.put("9olwiJXIp2", 0);
//        toSet.put("hh4LLQYlDm", 0);
//        toSet.put("XEhWbTDfAf", 0);
//        toSet.put("wn6j97bBVP", 0);
//        toSet.put("5khKZCBuhL", 0);
//
//        fb.child("taxis").setValue(toSet);

//        // 5º Phase
//        Map<String, Object> toSet = new HashMap<String, Object>();
//
//        Map<String, Object> rides;
//
//        rides = new HashMap<String, Object>();
//        rides.put("aBQZsDUy2l", "accepted");
//        toSet.put("wn6j97bBVP", rides);
//
//        rides = new HashMap<String, Object>();
//        rides.put("LSFQRwa578", "accepted");
//        toSet.put("hh4LLQYlDm", rides);
//
//        fb.child("rides").setValue(toSet);

    }
}
