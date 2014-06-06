package pt.ua.travis.ui.main;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.ui.customviews.TravisMapFragment;
import pt.ua.travis.utils.TravisUtils;

import java.util.List;
import java.util.Map;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AboutDialog extends SimpleDialogFragment {

    private SherlockFragmentActivity parentActivity;


    public static AboutDialog newInstance(final SherlockFragmentActivity parentActivity) {

        AboutDialog instance = new AboutDialog();
        instance.parentActivity = parentActivity;
        return instance;
    }

    @Override
    public BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        builder.setView(generateView(inflater));
        builder.setPositiveButton(R.string.done, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogStyleLight_FullScreen);
    }


    public View generateView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.dialog_about, null);

        ImageView view1 = (ImageView) v.findViewById(R.id.deti_logo);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.ua.pt/deti/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        ImageView view2 = (ImageView) v.findViewById(R.id.deca_logo);
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.ua.pt/deca/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        return v;
    }
}
