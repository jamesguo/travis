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

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter that simply returns row views from a list.
 * 
 * If you supply a size, you must implement newView(), to create a required
 * view. The adapter will then cache these views.
 * 
 * If you supply a list of views in the constructor, that list will be used
 * directly. If any elements in the list are null, then newView() will be called
 * just for those slots.
 * 
 * Subclasses may also wish to override areAllItemsEnabled() (default: false)
 * and isEnabled() (default: false), if some of their rows should be selectable.
 * 
 * It is assumed each view is unique, and therefore will not get recycled.
 * 
 * Note that this adapter is not designed for long lists. It is more for screens
 * that should behave like a list. This is particularly useful if you combine
 * this with other adapters (e.g., SectionedAdapter) that might have an
 * arbitrary number of rows, so it all appears seamless.
 */
public class SackOfViewsAdapter extends BaseAdapter {
	private List<View> views = null;

	/**
	 * Constructor creating an empty list of views, but with a specified count.
	 * Subclasses must override newView().
	 */
	public SackOfViewsAdapter(int count) {
		super();

		views = new ArrayList<View>(count);

		for (int i = 0; i < count; i++) {
			views.add(null);
		}
	}

	/**
	 * Constructor wrapping a supplied list of views. Subclasses must override
	 * newView() if any of the elements in the list are null.
	 */
	public SackOfViewsAdapter(List<View> views) {
		super();

		this.views = views;
	}

	/**
	 * Get the data item associated with the specified position in the data set.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 */
	@Override
	public Object getItem(int position) {
		return (views.get(position));
	}

	/**
	 * How many items are in the data set represented by this Adapter.
	 */
	@Override
	public int getCount() {
		return (views.size());
	}

	/**
	 * Returns the number of types of Views that will be created by getView().
	 */
	@Override
	public int getViewTypeCount() {
		return (getCount());
	}

	/**
	 * Get the type of View that will be created by getView() for the specified
	 * item.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 */
	@Override
	public int getItemViewType(int position) {
		return (position);
	}

	/**
	 * Are all items in this ListAdapter enabled? If yes it means all items are
	 * selectable and clickable.
	 */
	@Override
	public boolean areAllItemsEnabled() {
		return (false);
	}

	/**
	 * Returns true if the item at the specified position is not a separator.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 */
	@Override
	public boolean isEnabled(int position) {
		return (false);
	}

	/**
	 * Get a View that displays the data at the specified position in the data
	 * set.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 * @param convertView
	 *            View to recycle, if not null
	 * @param parent
	 *            ViewGroup containing the returned View
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View result = views.get(position);

		if (result == null) {
			result = newView(position, parent);
			views.set(position, result);
		}

		return (result);
	}

	/**
	 * Get the row id associated with the specified position in the list.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 */
	@Override
	public long getItemId(int position) {
		return (position);
	}

	public boolean hasView(View v) {
		return (views.contains(v));
	}

	/**
	 * Create a new View to go into the list at the specified position.
	 * 
	 * @param position
	 *            Position of the item whose data we want
	 * @param parent
	 *            ViewGroup containing the returned View
	 */
	protected View newView(int position, ViewGroup parent) {
		throw new RuntimeException("You must override newView()!");
	}
}