package pt.ua.travis.core;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
class TravisUser extends TravisObject {

    private final String name;
    private final String imageUrl;
    private LatLng actualPosition;

    protected TravisUser(final String name, final String imageUrl){
        super();
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setPosition(LatLng actualPosition) {
        this.actualPosition = actualPosition;
    }

    public LatLng getPosition() {
        return actualPosition;
    }

    public String getPositionString() {

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(new BigDecimal(actualPosition.latitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append("; ");
        sb.append(new BigDecimal(actualPosition.longitude).setScale(2, BigDecimal.ROUND_HALF_UP));
        sb.append(")");
        return sb.toString();
    }

    public TravisUser setLatLng(final LatLng newActualPosition) {
        this.actualPosition = newActualPosition;
        return this;
    }
}
