package pt.ua.travis.ui.travel;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.squareup.picasso.Picasso;
import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.Callback;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.PersistenceManager;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.ui.main.MainActivity;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RateAndFavDialog extends SimpleDialogFragment {

    private MainActivity parentActivity;
    private Client currentClient;
    private Taxi taxiToRate;

    private RatingBar ratingBar;
    private ToggleButton favButton;

    public static RateAndFavDialog newInstance(MainActivity parentActivity,
                                               Client currentClient,
                                               Taxi taxiToRate) {
        RateAndFavDialog f = new RateAndFavDialog();
        f.parentActivity = parentActivity;
        f.currentClient = currentClient;
        f.taxiToRate = taxiToRate;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

    }
    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        builder.setView(generateView(inflater));
        builder.setPositiveButton(R.string.submit, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdateFavorite();
                PersistenceManager.query().taxis().withId(taxiToRate.id()).later(new Callback<List<Taxi>>() {
                    @Override
                    public void onResult(List<Taxi> result) {
                        Taxi t = result.get(0);
                        t.addRating(ratingBar.getRating());
                        PersistenceManager.save(t, new Callback<Taxi>() {
                            @Override
                            public void onResult(Taxi result) {
                                parentActivity.updateRideList();
                            }
                        });
                        dismiss();
                    }
                });
            }
        });
        builder.setNegativeButton(R.string.dont_rate, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdateFavorite();
                parentActivity.updateRideList();
                dismiss();
            }
        });
        return builder;
    }


    public View generateView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.dialog_ratefav, null);

        CircularImageView photo = (CircularImageView) v.findViewById(R.id.ratefav_taxi_photo);
        TextView name = (TextView) v.findViewById(R.id.ratefav_taxi_name);
        ratingBar = (RatingBar) v.findViewById(R.id.ratefav_ratingbar);
        favButton = (ToggleButton) v.findViewById(R.id.ratefav_favorite_toggle);

        Picasso.with(parentActivity).load(taxiToRate.imageUri()).into(photo);
        name.setText(taxiToRate.name());

        favButton.setChecked(currentClient.taxiIsAFavorite(taxiToRate));

        return v;
    }

    private void attemptUpdateFavorite() {
        if(favButton.isChecked()) {
            currentClient.addTaxiAsFavorite(taxiToRate);
        } else {
            currentClient.removeTaxiAsFavorite(taxiToRate);
        }
        PersistenceManager.save(currentClient, null);
    }
}

