package pt.ua.travis.backend.entities;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudBackend;
import pt.ua.travis.backend.core.CloudEntity;
import pt.ua.travis.backend.core.CloudQuery;
import pt.ua.travis.backend.core.Filter;
import pt.ua.travis.utils.Utils;

import java.io.IOException;
import java.util.*;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class CloudBackendManager {

    private static CloudBackend cb = new CloudBackend();

    private static User thisLoggedInUser;

    private CloudBackendManager(){}

    public static User login(String email, String passwordDigest){
        List<User> matchingUsers = select().users().withEmail(email).execute();
        for(User u : matchingUsers){
            if(u.passwordDigest().equals(passwordDigest)){
                thisLoggedInUser = u;
                return u;
            }
        }
        return null;
    }

    public static void logout(){
        thisLoggedInUser = null;
    }

    public static Select select() {
        return new Select();
    }

    public static final class Select {

        private Select(){}

        public SelectRides rides(){
            return new SelectRides(cb);
        }

        public SelectUsers<Client> clients() {
            return new SelectUsers<Client>(cb, thisLoggedInUser, Client.class, Client.KIND_NAME);
        }

        public SelectTaxis taxis() {
            return new SelectTaxis(cb, thisLoggedInUser);
        }

        public SelectUsers<User> users() {
            return new SelectUsers<User>(cb, thisLoggedInUser, User.class, null);
        }

    }







//    static {
//        try{
//            Taxi t1 = new Taxi();
//            t1.setName("a@a.a");
//            t1.setPasswordDigest(Utils.generateSHA1DigestFromString("111"));
//            t1.setName("André Figueiredo");
//            t1.setImageUri("http://placesheen.com/phpthumb/phpthumb.php?src=../uploads/sheen/33.jpeg&w=140&h=180&zc=1");
//            t1.setCurrentPositionFromLatLng(new LatLng(40.646808, -8.662223));
//            t1.addRating(2.5f);
//            t1.addRating(3f);
//            t1.addRating(4f);
//            t1.addRating(2f);
//            t1.addRating(3f);
//            t1.setAsAvailable();
//            cb.insert(t1.ce);
//
//
//            Taxi t2 = new Taxi();
//            t2.setEmail("b@b.b");
//            t2.setPasswordDigest(Utils.generateSHA1DigestFromString("222"));
//            t2.setName("Ernesto Abreu");
//            t2.setImageUri("http://www.fillmurray.com/140/180");
//            t2.setCurrentPositionFromLatLng(new LatLng(40.635606, -8.659305));
//            t2.addRating(1f);
//            t2.addRating(0.5f);
//            t2.addRating(1f);
//            t2.addRating(2f);
//            t2.addRating(1f);
//            t2.setAsAvailable();
//            cb.insert(t2.ce);
//
//
//            Taxi t3 = new Taxi();
//            t3.setEmail("c@c.c");
//            t3.setPasswordDigest(Utils.generateSHA1DigestFromString("333"));
//            t3.setName("Carlos Oliveira");
//            t3.setImageUri("http://www.fillmurray.com/g/280/360");
//            t3.setCurrentPositionFromLatLng(new LatLng(40.645831, -8.640680));
//            t3.addRating(3f);
//            t3.addRating(4.5f);
//            t3.addRating(5f);
//            t3.addRating(1.5f);
//            t3.addRating(4f);
//            t3.addRating(3.5f);
//            t3.addRating(4f);
//            t3.addRating(2.5f);
//            t3.addRating(4f);
//            t3.addRating(2f);
//            t3.setAsAvailable();
//            cb.insert(t3.ce);
//
//
//            Taxi t4 = new Taxi();
//            t4.setEmail("d@d.d");
//            t4.setPasswordDigest(Utils.generateSHA1DigestFromString("444"));
//            t4.setName("Duarte Manolo");
//            t4.setImageUri("http://www.fillmurray.com/280/360");
//            t4.setCurrentPositionFromLatLng(new LatLng(40.645631, -8.640480));
//            t4.addRating(4f);
//            t4.addRating(4f);
//            t4.addRating(2f);
//            t4.addRating(3f);
//            t4.addRating(4f);
//            t4.addRating(3.5f);
//            t4.setAsAvailable();
//            cb.insert(t4.ce);
//
//
//            Taxi t5 = new Taxi();
//            t5.setEmail("e@e.e");
//            t5.setPasswordDigest(Utils.generateSHA1DigestFromString("222"));
//            t5.setName("Óscar Cardoso");
//            t5.setImageUri("http://www.placecage.com/140/180");
//            t5.setCurrentPositionFromLatLng(new LatLng(40.635411, -8.619823));
//            t5.addRating(3f);
//            t5.setAsUnavailable();
//            cb.insert(t5.ce);
//
//
//            Client c1 = new Client();
//            c1.setEmail("a@b.c");
//            c1.setPasswordDigest(Utils.generateSHA1DigestFromString("123"));
//            c1.setName("João Martins");
//            c1.setImageUri("http://www.placecage.com/280/360");
//            c1.addTaxiAsFavorite(t3);
//            c1.addTaxiAsFavorite(t1);
//            cb.insert(c1.ce);
//
//
//            Client c2 = new Client();
//            c2.setEmail("d@e.f");
//            c2.setPasswordDigest(Utils.generateSHA1DigestFromString("456"));
//            c2.setName("Bárbara Esteves");
//            c2.setImageUri("http://placeimg.com/210/270/people");
//            c2.addTaxiAsFavorite(t2);
//            c2.addTaxiAsFavorite(t3);
//            cb.insert(c2.ce);
//
//
//            Ride r1 = new Ride();
//            r1.setTaxi(t5);
//            r1.setClient(c1);
//            r1.setScheduledTime(17, 28);
//            r1.setOriginPosition(40.61790936, -8.642008904);
//            r1.setDestinationPosition(40.61795936, -8.642098904);
//            cb.insert(r1.ce);
//
//        }catch (IOException ex){
//            Log.e("ERROR LOADING DATABASE ENTITIES", ex.toString());
//        }
//    }
}
