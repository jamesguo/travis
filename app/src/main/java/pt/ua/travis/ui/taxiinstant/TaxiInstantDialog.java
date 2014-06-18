package pt.ua.travis.ui.taxiinstant;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.taxichooser.TaxiItem;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiInstantDialog extends SimpleDialogFragment {

    public interface Listener {

        void onConfirmedTaxi(Taxi t);

        void onShowNextTaxi();
    }

    private MainClientActivity parentActivity;
    private Client client;
    private Taxi taxi;
    private Listener listener;

    public static void show(MainClientActivity activity, Client client, Taxi taxi, Listener listener) {
        TaxiInstantDialog dialog = new TaxiInstantDialog();
        dialog.parentActivity = activity;
        dialog.client = client;
        dialog.taxi = taxi;
        dialog.listener = listener;
        dialog.show(activity.getSupportFragmentManager(), "TaxiInstantDialog");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        builder.setTitle(R.string.instant_dialog_title);
        builder.setMessage(R.string.instant_dialog_msg);

        View v = LayoutInflater.from(parentActivity).inflate(R.layout.item_taxi_compact, null);
        TaxiItem.paintViewWithTaxi(parentActivity, v, client, taxi);
        builder.setView(v);

        builder.setPositiveButton(R.string.instant_dialog_positive, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onShowNextTaxi();
                }
                dismiss();
            }
        });

        builder.setNegativeButton(R.string.instant_dialog_negative, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmedTaxi(taxi);
                }
                dismiss();
            }
        });
        return builder;
    }
}
