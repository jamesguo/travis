package pt.ua.travis.ui.addresspicker;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import com.google.common.collect.Lists;
import pt.ua.travis.utils.TravisUtils;

import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AutoCompleteAdapter extends ArrayAdapter<Address> implements Filterable {

        private LayoutInflater inflater;

        public AutoCompleteAdapter(final Context context) {
            super(context, -1);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final TextView tv;
            if (convertView != null) {
                tv = (TextView) convertView;
            } else {
                tv = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
            }

            tv.setText(TravisUtils.addressToString(getItem(position)));
            return tv;
        }

        @Override
        public Filter getFilter() {
            Filter myFilter = new Filter() {
                @Override
                protected FilterResults performFiltering(final CharSequence constraint) {
                    List<Address> addressList = Lists.newArrayList();
                    if (constraint != null) {

                        List<Address> retrieved = TravisUtils.addressesFromString(getContext(), constraint.toString());
                        if(retrieved != null){
                            addressList = retrieved;
                        }
                    }
                    final FilterResults filterResults = new FilterResults();
                    filterResults.values = addressList;
                    filterResults.count = addressList.size();

                    return filterResults;
                }

                @Override
                protected void publishResults(final CharSequence contraint, final FilterResults results) {
                    clear();
                    for (Address address : (List<Address>) results.values) {
                        add(address);
                    }
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(final Object resultValue) {
                    return resultValue == null ? "" : ((Address) resultValue).getAddressLine(0);
                }
            };
            return myFilter;
        }
    }