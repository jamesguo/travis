package pt.ua.travis.backend.entities;

import android.util.Log;
import com.google.common.collect.Lists;
import pt.ua.travis.backend.core.CloudBackend;
import pt.ua.travis.backend.core.CloudEntity;
import pt.ua.travis.backend.core.CloudQuery;
import pt.ua.travis.backend.core.Filter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public final class SelectRides {

    private CloudBackend cb;
    private List<Filter> filters;
    private boolean sorted;

    SelectRides(CloudBackend cb) {
        this.cb = cb;
        this.filters = Lists.newArrayList();
        this.sorted = false;
    }

    public List<Ride> execute() {
        List<Ride> selectedRides = Lists.newArrayList();

        try {
            CloudQuery cq = new CloudQuery(Ride.KIND_NAME);
            if(!filters.isEmpty()){
                cq.setFilter(Filter.and(filters.toArray(new Filter[filters.size()])));
            }
            List<CloudEntity> entities = cb.list(cq);

            for (CloudEntity ce : entities) {
                selectedRides.add(new Ride(ce));
            }

            if(sorted){
                sort(selectedRides);
            }

        } catch (IOException ex) {
            Log.e("ERROR QUERYING TAXIS (CloudBackendManager selectRidesWithUser())", ex.toString());
        }

        return selectedRides;
    }

    public SelectRides sortedByTime(){
        this.sorted = true;
        return this;
    }

    public SelectRides completed() {
        filters.add(Filter.eq(Ride.COMPLETED_FLAG, Boolean.TRUE));
        return this;
    }

    public SelectRides uncompleted() {
        filters.add(Filter.eq(Ride.COMPLETED_FLAG, Boolean.FALSE));
        return this;
    }

    public SelectRides with(User u) {
        if (u instanceof Taxi) {
            filters.add(Filter.eq(Ride.TAXI, u.getId()));
        } else if (u instanceof Client) {
            filters.add(Filter.eq(Ride.CLIENT, u.getId()));
        }
        return this;
    }

    private void sort(List<Ride> selectedRides){

        Collections.sort(selectedRides, new Comparator<Ride>() {
            @Override
            public int compare(Ride r1, Ride r2) {
//                Integer hour1 = r1.scheduledTime.hourOfDay().get();
//                Integer hour2 = r2.scheduledTime.hourOfDay().get();
//                int hourCompare = hour1.compareTo(hour2);
//                Integer minute1 = r1.scheduledTime.minuteOfHour().get();
//                Integer minute2 = r2.scheduledTime.minuteOfHour().get();
//                int minuteCompare = minute1.compareTo(minute2);
                Calendar scheduledTime1 = r1.getScheduledTime();
                Calendar scheduledTime2 = r2.getScheduledTime();
                return scheduledTime1.compareTo(scheduledTime2);
            }
        });
    }
}
