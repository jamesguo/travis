package pt.ua.travis.ui.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.chute.android.photopickerplus.models.enums.MediaType;
import pt.ua.travis.R;
import com.chute.sdk.v2.model.AssetModel;

import darko.imagedownloader.ImageLoader;

public class GridAdapter extends BaseAdapter {

    private static LayoutInflater inflater;
    public ImageLoader loader;
    private ArrayList<AssetModel> collection;

    public GridAdapter(final Activity context,
                       final ArrayList<AssetModel> collection) {
        if (collection == null) {
            this.collection = new ArrayList<AssetModel>();
        } else {
            this.collection = collection;
        }
        loader = ImageLoader.getLoader(context);
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return collection.size();
    }

    @Override
    public AssetModel getItem(int position) {
        return collection.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.item_grid, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) vi.findViewById(R.id.gcImageViewThumb);
            holder.videoIcon = (ImageView) vi.findViewById(R.id.gcImageViewVideo);
            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }
        loader.displayImage(getItem(position).getThumbnail(), holder.imageView, null);
        AssetModel asset = getItem(position);
        if (asset.getType().equalsIgnoreCase(
                MediaType.VIDEO.name().toLowerCase())) {
            holder.videoIcon.setVisibility(View.VISIBLE);
        } else {
            holder.videoIcon.setVisibility(View.GONE);
        }
        return vi;
    }

    public void changeData(ArrayList<AssetModel> collection) {
        this.collection = collection;
        notifyDataSetChanged();
    }

    public static class ViewHolder {
        public ImageView imageView;
        public ImageView videoIcon;
    }

}