package pt.ua.travis.backend;

import android.os.StrictMode;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * A class that handles wrapping of a {@link ParseObject} class and that provides direct
 * control of the wrapper entity's persistence in the backend.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class ParseObjectWrapper implements ParseWrapper {

    /**
     * The {@link ParseObject} in the backend wrapped in this class.
     */
    protected transient ParseObject po;

    protected ParseObjectWrapper(ParseObject po){
        this.po = po;
    }

    public abstract String thisObjectName();


    /**
     * Returns the wrapped {@link ParseObject} ID.
     */
    public String id(){
        return po.getObjectId();
    }



//    protected void writeObject(java.io.ObjectOutputStream stream) throws IOException {
//        stream.writeObject(thisObjectName());
//        stream.writeObject(po.getObjectId());
//    }
//
//    protected void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
//        String objectName = stream.readObject().toString();
//        String id = stream.readObject().toString();
//
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
//        try {
//            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(objectName);
//            po = query.get(id);
//        } catch (ParseException ex) {
//            throw new RuntimeException(ex);
//        }
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
//    }




//    /**
//     * Saves the wrapped {@link ParseObject} in the backend, by updating it or,
//     * if it doesn't exist yet, by inserting it.
//     */
//    public ParseObjectWrapper save() {
//        CloudBackend cb = new CloudBackend();
//
//        try {
//            cb.update(po);
//        }catch (IOException ex){
//            throw new RuntimeException(ex);
//        }
//
//        return this;
//    }


//    /**
//     * Deletes the wrapped {@link ParseObject} from the backend, by updating it or,
//     * if it doesn't exist yet, by inserting it.
//     */
//    public ParseObjectWrapper delete() {
//        CloudBackend cb = new CloudBackend();
//
//        try {
//            cb.delete(po);
//        }catch (IOException ex){
//            throw new RuntimeException(ex);
//        }
//
//        return this;
//    }
}
