package pt.ua.travis.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TravisTaxi extends TravisUser implements Serializable {

    private final List<Float> ratings;
    private boolean isBusy;

    public TravisTaxi(final String name, final String imageUrl, final List<Float> ratings){
        super(name, imageUrl);
        this.ratings = new ArrayList<>(ratings);
    }

    public TravisTaxi addRating(float r){
        ratings.add(r);
        return this;
    }

    public float getRatingAverage() {
        float result = 0;
        for(Float rate : ratings){
            result = result + rate;
        }

        return result / ratings.size();
    }

    public TravisTaxi setBusy(boolean isBusy) {
        this.isBusy = isBusy;
        return this;
    }

    public boolean isBusy() {
        return isBusy;
    }
}
