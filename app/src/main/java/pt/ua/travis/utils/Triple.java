package pt.ua.travis.utils;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * Container to ease passing around a tuple of three objects. This object provides a sensible
 * implementation of equals(), returning true if equals() is true on each of the contained
 * objects.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class Triple<F, S, T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public F first;
    public S second;
    public T third;

    /**
     * Constructor for a Triple.
     *
     * @param first the first object in the triple
     * @param second the second object in the triple
     * @param third the third object in the triple
     */
    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    /**
     * Checks the two objects for equality by delegating to their respective
     * {@link Object#equals(Object)} methods.
     *
     * @param o the {@link Triple} to which this one is to be checked for equality
     * @return true if the underlying objects of the triple are both considered equal
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triple)) {
            return false;
        }
        Triple<?, ?, ?> t = (Triple<?, ?, ?>) o;
        return Objects.equal(t.first, first) && Objects.equal(t.second, second) && Objects.equal(t.third, third);
    }

    /**
     * Compute a hash code using the hash codes of the underlying objects
     *
     * @return a hashcode of the triple
     */
    @Override
    public int hashCode() {
        return  (first == null ? 0 : first.hashCode()) ^
                (second == null ? 0 : second.hashCode()) ^
                (third == null ? 0 : third.hashCode());
    }

}

