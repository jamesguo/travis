package pt.ua.travis.ui.taxichooser;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.androidmapsextensions.Marker;
import pt.ua.travis.R;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.ui.mainscreen.MainClientActivity;
import pt.ua.travis.utils.CommonKeys;

import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiChooserListFragment extends TaxiChooserFragment {

    private ListView taxiSelector;

    @Override
    public void onStart() {
        super.onStart();
        List<Taxi> taxis = MainClientActivity.getCurrentTaxiListState();

        taxiSelector = (ListView) getActivity().findViewById(R.id.list);
        final TaxiItemListAdapter taxiAdapter = new TaxiItemListAdapter(getActivity(), convertTaxisToItems(taxis));
        this.itemAdapter = taxiAdapter;

        // selector (vertical list) configurations
        taxiSelector.setAdapter(taxiAdapter);
        taxiSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select(position);
            }
        });

        // selects the item that was selected in the previous fragment
        int indexToSelect = getArguments().getInt(CommonKeys.SELECTED_INDEX);
        select(indexToSelect);
    }


    @Override
    protected void finishSelect(int position, Marker marker) {
        taxiSelector.smoothScrollToPosition(position);
        super.finishSelect(position, marker);
    }
}