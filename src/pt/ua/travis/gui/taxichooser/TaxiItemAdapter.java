package pt.ua.travis.gui.taxichooser;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public interface TaxiItemAdapter {

    int getCount();

    TaxiItem getItem(int position);

    int getItemPosition(Object object);

    void setSelectedIndex(int position);

    int getSelectedIndex();

    void notifyDataSetChanged();
}
