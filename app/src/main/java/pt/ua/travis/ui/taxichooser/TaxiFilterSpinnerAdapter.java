package pt.ua.travis.ui.taxichooser;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import pt.ua.travis.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiFilterSpinnerAdapter extends ArrayAdapter<String> {

    private Activity activity;
    private static final int[] textIDs = { R.string.closest_to_you, R.string.highest_rated, R.string.your_favorites, R.string.search  };
    private static final int[] iconIDs = { R.drawable.ic_closest, R.drawable.ic_star, R.drawable.ic_fav, R.drawable.ic_search };

    public static List<String> getTexts(Activity parentActivity) {
        List<String> result = new ArrayList<String>(textIDs.length);
        for(int id : textIDs) {
            String s = parentActivity.getResources().getString(id);
            result.add(s);
        }
        return result;
    }

    public TaxiFilterSpinnerAdapter(Activity parentActivity) {
        super(parentActivity, R.layout.item_sort_spinner, getTexts(parentActivity));
        this.activity = parentActivity;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        LayoutInflater inflater = activity.getLayoutInflater();
        View item = inflater.inflate(R.layout.item_sort_spinner, parent, false);

        TextView label = (TextView) item.findViewById(R.id.spinner_text);
        label.setText(textIDs[position]);

        ImageView icon = (ImageView) item.findViewById(R.id.spinner_icon);
        icon.setImageResource(iconIDs[position]);

        return item;
    }
}
