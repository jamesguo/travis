package pt.ua.travis.gui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.ami.fundapter.BindDictionary;
import com.ami.fundapter.FunDapter;
import pt.ua.travis.R;
import pt.ua.travis.core.Taxi;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class TaxiList extends SherlockListFragment {

//    private List<Taxi> list;
//    private ListView listView;
//    private FunDapter<Taxi> listAdapter;
//
////    private OnTaskSelectedListener taskSelectedListener = null;
//
//    @Override
//    public void onActivityCreated(Bundle savedState) {
//        super.onActivityCreated(savedState);
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
////        try {
////            taskSelectedListener = (OnTaskSelectedListener) activity;
////
////        } catch (ClassCastException e) {
////            throw new ClassCastException(
////                    "TaxiList must implement OnTaskSelectedListener");
////        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        View v = inflater.inflate(R.layout.taxi_list_fragment, null);
//
//
//        // if a task is selected, proceed to show the "editing" activity with that task
//        listView = (ListView) v.findViewById(R.id.taxi_list);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> parent,
//                                    View view,
//                                    int position,
//                                    long id) {
//
////                taskSelectedListener.onTaskSelection(position);
//            }
//        });
//
//
//        // instantiates the array list that will manage the added tasks
//        list = new ArrayList<>();
//
//
//        // prepares an array adapter with the list of items
//        filter(list);
//
//        return v;
//    }
//
//    List<Taxi> getList() {
//        return list;
//    }
//
//    void removeTaxi(Taxi taskToRemove) {
//        list.remove(taskToRemove);
//        filter(list);
//    }
//
//
//    void filter(List<Taxi> newList){
//        BindDictionary<Taxi> dict = new BindDictionary<>();
//
//        listAdapter = new FunDapter<>(getActivity(),
//                newList, android.R.layout.simple_list_item_1, dict);
//
//        listView.setAdapter(listAdapter);
//        listAdapter.notifyDataSetChanged();
//    }
//
//    void unfilter(){
//        filter(list);
//    }
//
//    public void update(){
//        listAdapter.notifyDataSetChanged();
//    }
}