package pt.ua.travis.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Taxi extends User implements Serializable {

    private final List<Float> ratings;
    private boolean isAvailable;

    public Taxi(final String name, final String imageUrl){
        super(name, imageUrl);
        this.ratings = new ArrayList<>();
    }

    public Taxi(final String name, final String imageUrl, final List<Float> ratings){
        super(name, imageUrl);
        this.ratings = new ArrayList<>(ratings);
    }

    public Taxi addRating(double r){
        ratings.add((float)r);
        return this;
    }

    public float getRatingAverage() {
        float result = 0;
        for(Float rate : ratings){
            result = result + rate;
        }

        return result / ratings.size();
    }

    public Taxi setAvailable(boolean available) {
        this.isAvailable = available;
        return this;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
