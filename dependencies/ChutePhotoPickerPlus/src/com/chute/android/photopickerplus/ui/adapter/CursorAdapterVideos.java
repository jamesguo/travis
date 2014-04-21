/**
 * The MIT License (MIT)

Copyright (c) 2013 Chute

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.chute.android.photopickerplus.ui.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.config.PhotoPicker;
import com.chute.android.photopickerplus.dao.MediaDAO;
import com.chute.android.photopickerplus.models.DeliverMediaModel;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.android.photopickerplus.ui.activity.AssetActivity;
import com.chute.android.photopickerplus.ui.activity.ServicesActivity;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.ui.listener.ListenerVideoSelection;
import com.chute.android.photopickerplus.util.AssetUtil;

public class CursorAdapterVideos extends BaseCursorAdapter implements
		ListenerVideoSelection {

	private ListenerFilesCursor listener;
	private Context context;
	private int position;

	public CursorAdapterVideos(Context context, Cursor c,
			ListenerFilesCursor listener) {
		super(context, c);
		this.listener = listener;
		this.context = context;
		if (context.getResources().getBoolean(R.bool.has_two_panes)) {
			((ServicesActivity) context).setVideosSelectListener(this);
		} else {
			((AssetActivity) context).setVideosSelectListener(this);
		}
	}

	@Override
	public List<Integer> getCursorVideosSelection() {
		final List<Integer> selectedItemPositions = new ArrayList<Integer>();
		final Iterator<Integer> iterator = tick.keySet().iterator();
		while (iterator.hasNext()) {
			selectedItemPositions.add(iterator.next());
		}
		return selectedItemPositions;

	}

	@Override
	public int getDataIndex(Cursor cursor) {
		if (cursor == null) {
			return 0;
		} else {
			return cursor.getColumnIndex(MediaStore.Video.Media.DATA);
		}
	}

	@Override
	public void setViewClickListener(View view, String path, int position) {
		view.setOnClickListener(new VideoClickListener(path, position));

	}

	@Override
	public void setPlayButtonVisibility(ImageView imageView) {
		imageView.setVisibility(View.VISIBLE);

	}

	@Override
	public void loadImageView(ImageView imageView, Cursor cursor) {
		imageView.setImageBitmap(MediaDAO.getVideoThumbnail(context, cursor));

	}

	private final class VideoClickListener implements OnClickListener {

		private String path;
		private int itemPosition;

		private VideoClickListener(String path, int itemPosition) {
			this.path = path;
			this.itemPosition = itemPosition;
		}

		@Override
		public void onClick(View v) {
			position = itemPosition;
			if (PhotoPicker.getInstance().isMultiPicker()) {
				toggleTick(position);
			} else {
				String thumb = MediaDAO.getVideoThumbnailFromCursor(context,
						getCursor(), position);
				listener.onCursorAssetsSelect(AssetUtil
						.getMediaModel(createMediaResultModel(thumb, path)));
			}

		}

	}

	public List<DeliverMediaModel> getSelectedFilePaths() {
		final List<DeliverMediaModel> deliverList = new ArrayList<DeliverMediaModel>();
		Iterator<Entry<Integer, String>> iterator = tick.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, String> pairs = iterator.next();
			String path = pairs.getValue();
			int position = pairs.getKey();

			String thumbnail = MediaDAO.getVideoThumbnailFromCursor(context,
					getCursor(), position);
			deliverList.add(createMediaResultModel(thumbnail, path));
		}
		return deliverList;
	}

	public void toggleTick(int positionSelected) {
		if (tick.containsKey(positionSelected)) {
			tick.remove(positionSelected);
		} else {
			tick.put(positionSelected, getItem(positionSelected));
		}
		notifyDataSetChanged();
	}

	private DeliverMediaModel createMediaResultModel(String thumb,
			String videoUrl) {
		DeliverMediaModel model = new DeliverMediaModel();
		model.setVideoUrl(Uri.fromFile(new File(videoUrl)).toString());
		model.setMediaType(MediaType.VIDEO);
		model.setImageUrl(thumb);
		model.setThumbnail(thumb);
		return model;
	}

}
