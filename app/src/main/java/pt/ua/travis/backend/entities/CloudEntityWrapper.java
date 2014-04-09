package pt.ua.travis.backend.entities;

import pt.ua.travis.backend.core.CloudBackend;
import pt.ua.travis.backend.core.CloudEntity;

import java.io.IOException;

/**
 * A class that handles wrapping of a {@link CloudEntity} class and that provides direct
 * control of the wrapper entity's persistence in the backend.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class CloudEntityWrapper {

    /**
     * The {@link CloudEntity} in the backend wrapped in this class.
     */
    protected final CloudEntity ce;


    protected CloudEntityWrapper(CloudEntity ce){
        this.ce = ce;
    }


    /**
     * Returns the wrapped {@link CloudEntity} ID.
     */
    public String getId(){
        return ce.getId();
    }


    /**
     * Saves the wrapped {@link CloudEntity} in the backend, by updating it or,
     * if it doesn't exist yet, by inserting it.
     */
    public CloudEntityWrapper save() {
        CloudBackend cb = new CloudBackend();

        try {
            cb.update(ce);
        }catch (IOException ex){
            throw new RuntimeException(ex);
        }

        return this;
    }


//    /**
//     * Deletes the wrapped {@link CloudEntity} from the backend, by updating it or,
//     * if it doesn't exist yet, by inserting it.
//     */
//    public CloudEntityWrapper delete() {
//        CloudBackend cb = new CloudBackend();
//
//        try {
//            cb.delete(ce);
//        }catch (IOException ex){
//            throw new RuntimeException(ex);
//        }
//
//        return this;
//    }
}
