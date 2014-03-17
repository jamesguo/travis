package pt.ua.travis.core;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Taxi {

    private final String name;
    private final String imageUrl;
    private double rating;
    private double lat;
    private double log;

    public Taxi(final String name, final String imageUrl){
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public double getLat() {
        return lat;
    }

    public double getLog() {
        return log;
    }
}
