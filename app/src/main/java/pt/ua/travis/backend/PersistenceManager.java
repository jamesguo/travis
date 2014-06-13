package pt.ua.travis.backend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import com.actionbarsherlock.app.SherlockActivity;
import com.firebase.client.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.parse.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.login.LoginTask;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.ui.riderequest.RideRequestTask;
import pt.ua.travis.utils.Pair;
import pt.ua.travis.utils.Uno;
import pt.ua.travis.utils.TravisUtils;

import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class PersistenceManager {

    private static final String TAG = PersistenceManager.class.getSimpleName();

    public static final String TYPE_CLIENT = "client";
    public static final String TYPE_TAXI = "taxi";

    public static final int NO_USER_WITH_THAT_EMAIL = 11;
    public static final int WRONG_CREDENTIALS = 22;
    public static final int SUCCESSFUL_LOGIN = 33;

    private static final String RIDE_STATUS_WAITING_FOR_ARRIVAL = "waiting_for_arrival";
    private static final String RIDE_STATUS_TAXI_ARRIVED = "taxi_arrived";
    private static final String RIDE_STATUS_TRAVELLING = "travelling";
    private static final String RIDE_STATUS_COMPLETED = "completed";

    private static final String FB_TAXIS = "taxis";
    private static final String FB_RIDES = "rides";

    private static Firebase fb;
    private static List<ValueEventListener> valueListeners;
    private static List<ChildEventListener> childListeners;

    private static Map<String, ParseWrapper> cachedParseObjects;

    private PersistenceManager(){}

    /**
     * Initializes the backend for insertion or selection.
     */
    public static void init(Context context) {
        Parse.initialize(context, "pu8HYbkiyqVucSgUNcsYcgu6AyRLlZQhik2CIdQt", "BzI7yfkTisCZXvHecCZHs92qOIu4ozDO3NbgTKTf");

        fb = new Firebase("https://burning-fire-4047.firebaseio.com/");
        valueListeners = Lists.newArrayList();
        childListeners = Lists.newArrayList();
        cachedParseObjects = TravisUtils.newMap();

        // UNCOMMENT THIS ONLY DURING DEVELOPMENT
        populateDB();
    }

    @SuppressWarnings("unchecked")
    public static <T extends User> T getCurrentlyLoggedInUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            String objectName = currentUser.getString(User.TYPE);

            if(objectName.equals(Client.OBJECT_NAME)) {
                return (T) new Client(currentUser);

            } else if(objectName.equals(Taxi.OBJECT_NAME)) {
                return (T) new Taxi(currentUser);

            }
        }

        return null;
    }

    public static <T extends ParseWrapper> void addToCache(T object) {
        cachedParseObjects.put(object.id(), object);
    }

    public static <T extends ParseWrapper> T getFromCache(String id) {
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
     * Registers a new user and wraps it into a {@link ParseUserWrapper} that
     * represents a Client or a Taxi in the backend.
     */
    public static void registerNewUser(final Activity context,
                                       final String userType,
                                       final String email,
                                       final String password,
                                       final String firstName,
                                       final String lastName,
                                       final byte[] imageData) throws ParseException {
        User u;
        if (userType.equals(TYPE_CLIENT)) {
            u = Client.create();
        } else if (userType.equals(TYPE_TAXI)) {
            u = Taxi.create();
        } else {
            return;
        }

        String capitalFirstName = TravisUtils.capitalizeWord(firstName);
        String capitalLastName = TravisUtils.capitalizeWord(lastName);
        u.setName(capitalFirstName + " " + capitalLastName);
        u.po.setUsername(email);
        u.setEmail(email);
        u.setPassword(password);

        final User fetchedUser;
        if (u instanceof Client) {
            u.po.signUp();
            fetchedUser = new Client(u.po.fetch());

        } else if (u instanceof Taxi) {
            u.po.signUp();
            fetchedUser = new Taxi(u.po.fetch());

        } else {
            return;
        }

        continueRegister(context, fetchedUser, imageData);
    }

    /**
     * Saved data to a user that was registered from social media authentication
     * mechanisms and wraps it into a {@link ParseUserWrapper} that represents a
     * Client or a Taxi in the backend.
     */
    public static void registerNewUserFromSocialMedia(final Activity context,
                                                      final String userType,
                                                      final String email,
                                                      final String firstName,
                                                      final String lastName,
                                                      final byte[] imageData) throws ParseException {
        User u;
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (userType.equals(TYPE_CLIENT)) {
            u = new Client(currentUser);

        } else if (userType.equals(TYPE_TAXI)) {
            u = new Taxi(currentUser);

        } else {
            return;
        }

        String capitalFirstName = TravisUtils.capitalizeWord(firstName);
        String capitalLastName = TravisUtils.capitalizeWord(lastName);
        u.setName(capitalFirstName + " " + capitalLastName);
        u.setEmail(email);

        continueRegister(context, u, imageData);
    }

    private static void continueRegister(final Activity context,
                                         final User registeredUser,
                                         final byte[] imageData) throws ParseException {


        Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        Bitmap resizeBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        resizeBitmap.compress(Bitmap.CompressFormat.PNG, 0, outStream);


        final ParseFile file = new ParseFile(registeredUser.id(), outStream.toByteArray());
        file.save();
        registeredUser.setImageUri(file.getUrl());

        registeredUser.po.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                LoginTask.goToMainActivity(context, registeredUser);
            }
        });
    }


    /**
     * Saves the wrapped {@link ParseObject} that represents a Client in the backend
     * by updating it.
     */
    public static void save(final Client toAdd, final Callback<Client> saveHandler){
        toAdd.po.saveInBackground(new com.parse.SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    if (saveHandler != null) {
                        toAdd.po.fetchInBackground(new GetCallback<ParseUser>() {
                            @Override
                            public void done(ParseUser object, ParseException e) {
                                saveHandler.onResult(new Client(object));
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "There was a problem saving the client " + toAdd, ex);
                }
            }
        });
    }


    /**
     * Saves the wrapped {@link ParseObject} that represents a Taxi in the backend
     * by updating it.
     */
    public static void save(final Taxi toAdd, final Callback<Taxi> saveHandler){
        toAdd.po.saveInBackground(new com.parse.SaveCallback() {
            @Override
            public void done(ParseException ex) {
                if (ex == null) {
                    toAdd.po.fetchInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser object, ParseException e) {

                            Map<String, Object> toSet = new HashMap<String, Object>();
                            toSet.put(object.getObjectId(), new Random().nextInt(Integer.MAX_VALUE));

                            fb.child(FB_TAXIS).updateChildren(toSet);

                            if (saveHandler != null) {
                                saveHandler.onResult(new Taxi(object));
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "There was a problem saving the taxi " + toAdd, ex);
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

                            if (saveHandler != null) {
                                Ride r = PersistenceManager.query().rides().fetchNow(object);
                                saveHandler.onResult(r);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "There was a problem saving the ride " + toAdd, ex);
                }
            }
        });
    }

    public static <T extends ParseObjectWrapper> void delete(final T toDelete){
        if(toDelete instanceof Ride){
            // cancels the ride
            Ride r = ((Ride) toDelete);
            fb.child(FB_RIDES).child(r.client().id()).child(r.id()).removeValue();
        }

        toDelete.po.deleteInBackground();
    }

    public static void setTaxiArrived(final Ride requestedRide){
        Map<String, Object> toSet = new HashMap<String, Object>();
        Map<String, Object> ride = new HashMap<String, Object>();
        ride.put(requestedRide.id(), RIDE_STATUS_TAXI_ARRIVED);
        toSet.put(requestedRide.client().id(), ride);

        fb.child(FB_RIDES).updateChildren(toSet);
    }

    public static void setRequestWaiting(final Ride requestedRide){
        String taxiId = requestedRide.po.getString(Ride.TAXI_ID);

        Map<String, Object> toSet = new HashMap<String, Object>();
        Map<String, Object> rideSet = new HashMap<String, Object>();
        rideSet.put(requestedRide.id(), RideRequestTask.RESPONSE_WAITING);
        toSet.put(taxiId, rideSet);

        fb.child(FB_RIDES).updateChildren(toSet);
    }

    public static void setRequestAccepted(final Ride requestedRide){
        Map<String, Object> toSet = new HashMap<String, Object>();
        Map<String, Object> ride = new HashMap<String, Object>();
        ride.put(requestedRide.id(), RideRequestTask.RESPONSE_ACCEPTED);
        toSet.put(requestedRide.taxi().id(), ride);

        fb.child(FB_RIDES).updateChildren(toSet);
    }

    public static void setRequestDeclined(final Ride requestedRide){
        Map<String, Object> toSet = new HashMap<String, Object>();
        Map<String, Object> ride = new HashMap<String, Object>();
        ride.put(requestedRide.id(), RideRequestTask.RESPONSE_DECLINED);
        toSet.put(requestedRide.taxi().id(), ride);

        fb.child(FB_RIDES).updateChildren(toSet);
    }

    public static void waitForRideResponse(final Ride requestedRide, final WatchEvent<String> responseEvent){
        final Firebase requestedRideRef = fb.child(FB_RIDES).child(requestedRide.taxi().id()).child(requestedRide.id());
        final Uno<Boolean> receivedResponse = TravisUtils.newUno(false);

        final ValueEventListener responseWatcher = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    String response = dataSnapshot.getValue().toString();

                    if (!response.equals(RideRequestTask.RESPONSE_WAITING)) {
                        receivedResponse.value = true;
                        requestedRideRef.removeEventListener(this);

                        if (response.equals(RideRequestTask.RESPONSE_ACCEPTED)) {
                            requestedRideRef.removeValue();
                            Firebase clientRideRef = fb.child(FB_RIDES).child(requestedRide.client().id()).child(requestedRide.id());
                            clientRideRef.setValue(RIDE_STATUS_WAITING_FOR_ARRIVAL);

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

    public static void startWatchingNewRidesForClient(final Client clientWithRidesToWatch, final WatchEvent<Ride> watchEvent){
        ChildEventListener rideWatcher = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                final String id = dataSnapshot.getName();
                final String status = dataSnapshot.getValue().toString();

                if(status.equals(RIDE_STATUS_TAXI_ARRIVED)) {
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
        fb.child(FB_RIDES).child(clientWithRidesToWatch.id()).addChildEventListener(rideWatcher);
    }

    public static void startWatchingNewRidesForTaxi(final Taxi taxiWithRidesToWatch, final WatchEvent<Ride> watchEvent){
        ChildEventListener rideWatcher = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                final String id = dataSnapshot.getName();
                final String status = dataSnapshot.getValue().toString();

                if(status.equals(RideRequestTask.RESPONSE_WAITING)) {
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
        fb.child(FB_RIDES).child(taxiWithRidesToWatch.id()).addChildEventListener(rideWatcher);
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
            ParseUser po = taxis.get(id).po;

            po.fetchInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
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
        return new Query();
    }



    /**
     * Saves the specified user as a static variable to be easily accessible
     * during the application's execution.
     * WARNING: This method must only be executed in an Async task!!
     */
    public static Pair<Integer, User> attemptLogin(String email, String pass) {
        try {
            User resultUser = testEmail(email);

            if(resultUser == null){
                // Email doesn't exist, can create a new account withUser credentials
                return TravisUtils.newPair(NO_USER_WITH_THAT_EMAIL, null);

            } else {

                resultUser = testPass(email, pass);
            }

            if(resultUser == null) {
                return TravisUtils.newPair(WRONG_CREDENTIALS, null);
            } else {
                return TravisUtils.newPair(SUCCESSFUL_LOGIN, resultUser);
            }

        } catch (ParseException ex){
            Log.e("PersistenceManager Login", ex.toString());
        }

        return null;
    }

    private static User testEmail(String email) throws ParseException {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.setLimit(1);
        query.whereEqualTo(User.EMAIL, email);
        List<ParseUser> queryResults = query.find();

        if(!queryResults.isEmpty()){
            ParseUser u = queryResults.get(0);
            String type = u.getString(User.TYPE);
            if(type.equals(Client.OBJECT_NAME))
                return new Client(u);
            else if(type.equals(Taxi.OBJECT_NAME))
                return new Taxi(u);

        }

        // Email doesn't exist, can create a new account with this email
        return null;
    }

    private static User testPass(String email, String pass) throws ParseException {
        try {
            ParseUser user = ParseUser.logIn(email, pass);

            if (user != null) {
                String type = user.getString(User.TYPE);
                if(type.equals(Client.OBJECT_NAME))
                    return new Client(user);
                else if(type.equals(Taxi.OBJECT_NAME)) {
                    Taxi t = new Taxi(user);
                    t.setAsAvailable();
                    t.setAsOnline();
                    save(t, null);
                    return t;
                }
            }
        }catch (ParseException ex){
            Log.e("PersistenceManager", "Login attempt was unsuccessful.", ex);
        }

        return null;
    }



    /**
     * Stops caching the currently logged in user on disk.
     */
    public static void logout(){
        User u = getCurrentlyLoggedInUser();
        if(u instanceof Taxi){
            ((Taxi)u).setAsOffline();
            save(((Taxi)u), null);
        }

        ParseUser.logOut();
    }












    // TEST METHOD, DELETE WHEN DB IS ALREADY POPULATED
    private static void populateDB() {

        // 1º Phase

//        Taxi t1 = Taxi.create();
//        t1.setEmail("aaaaa@gmail.com");
//        t1.setPassword("aaa");
//        t1.setName("André Figueiredo");
//        t1.setImageUri("http://placesheen.com/phpthumb/phpthumb.php?src=../uploads/sheen/33.jpeg&w=140&h=180&zc=1");
//        t1.setCurrentLocation(40.646808, -8.662223);
//        t1.addRating(2.5f);
//        t1.addRating(3f);
//        t1.addRating(4f);
//        t1.addRating(2f);
//        t1.addRating(3f);
//        t1.setAsAvailable();
//        t1.setAsOnline();
//        t1.po.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                Log.e("", "", e);
//            }
//        });
//
//
//        Taxi t2 = Taxi.create();
//        t2.setEmail("bbbbb@gmail.com");
//        t2.setPassword("bbb");
//        t2.setName("Ernesto Abreu");
//        t2.setImageUri("http://www.fillmurray.com/140/180");
//        t2.setCurrentLocation(40.635606, -8.659305);
//        t2.addRating(1f);
//        t2.addRating(0.5f);
//        t2.addRating(1f);
//        t2.addRating(2f);
//        t2.addRating(1f);
//        t2.setAsAvailable();
//        t2.setAsOffline();
//        t2.po.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                Log.e("", "", e);
//            }
//        });
//
//
//        Taxi t3 = Taxi.create();
//        t3.setEmail("ccccc@gmail.com");
//        t3.setPassword("ccc");
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
//        t3.setAsOnline();
//        t3.po.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                Log.e("", "", e);
//            }
//        });
//
//
//        Taxi t4 = Taxi.create();
//        t4.setEmail("ddddd@gmail.com");
//        t4.setPassword("ddd");
//        t4.setName("Duarte Manolo");
//        t4.setImageUri("http://www.fillmurray.com/280/360");
//        t4.setCurrentLocation(40.645631, -8.640480);
//        t4.addRating(4f);
//        t4.addRating(4f);
//        t4.addRating(2f);
//        t4.addRating(3f);
//        t4.addRating(4f);
//        t4.addRating(3.5f);
//        t4.setAsUnavailable();
//        t4.setAsOffline();
//        t4.po.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                Log.e("", "", e);
//            }
//        });
//
//
//        Taxi t5 = Taxi.create();
//        t5.setEmail("eeeee@gmail.com");
//        t5.setPassword("eee");
//        t5.setName("Óscar Cardoso");
//        t5.setImageUri("http://www.placecage.com/140/180");
//        t5.setCurrentLocation(40.635411, -8.619823);
//        t5.addRating(3f);
//        t5.setAsUnavailable();
//        t5.setAsOffline();
//        t5.po.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                Log.e("", "", e);
//            }
//        });


        // 2º Phase
//        PersistenceManager.query().taxis().withId("OReZSBly2X").later(new Callback<List<Taxi>>() {
//            @Override
//            public void onResult(List<Taxi> result) {
//                final Taxi t1 = result.get(0);
//                PersistenceManager.query().taxis().withId("aEWaR9OwUV").later(new Callback<List<Taxi>>() {
//                    @Override
//                    public void onResult(List<Taxi> result) {
//                        final Taxi t2 = result.get(0);
//
//                        Client c1 = Client.create();
//                        c1.setEmail("cr7@gmail.com");
//                        c1.setPassword("123");
//                        c1.setName("João Martins");
//                        c1.setImageUri("http://www.placecage.com/280/360");
//                        c1.addTaxiAsFavorite(t2);
//                        c1.addTaxiAsFavorite(t1);
//                        c1.po.signUpInBackground(new SignUpCallback() {
//                            @Override
//                            public void done(ParseException e) {
//
//                            }
//                        });
//
//                        Client c2 = Client.create();
//                        c2.setEmail("ico@ua.pt");
//                        c2.setPassword("456");
//                        c2.setName("Bárbara Esteves");
//                        c2.setImageUri("http://placeimg.com/210/270/people");
//                        c2.addTaxiAsFavorite(t2);
//                        c2.po.signUpInBackground(new SignUpCallback() {
//                            @Override
//                            public void done(ParseException e) {
//
//                            }
//                        });
//                    }
//                });
//            }
//        });



//        // 3º Phase
//        PersistenceManager.query().clients().withId("vRvTwhKTf4").later(new Callback<List<Client>>() {
//            @Override
//            public void onResult(List<Client> result) {
//                final Client c1 = result.get(0);
//
//                PersistenceManager.query().taxis().withId("OReZSBly2X").later(new Callback<List<Taxi>>() {
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
