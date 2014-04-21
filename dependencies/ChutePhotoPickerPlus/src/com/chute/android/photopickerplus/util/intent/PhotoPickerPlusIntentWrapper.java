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
package com.chute.android.photopickerplus.util.intent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.chute.android.photopickerplus.ui.activity.ServicesActivity;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link PhotoPickerPlusIntentWrapper} is a wrapper class that wraps the
 * following parameters needed for the intent:
 * <ul>
 * <li>Account ID
 * <li>Album ID
 * <li>List of {@link AssetModel}s
 * </ul>
 * 
 */
public class PhotoPickerPlusIntentWrapper extends IntentWrapper {

	public static final int ACTIVITY_FOR_RESULT_KEY = 115;
	private static final String KEY_ACCOUNT_ID = "accountId";
	private static final String KEY_ALBUM_ID = "albumId";
	private static final String KEY_ACCOUNT_TYPE = "account_type";
	public static final String KEY_PHOTO_COLLECTION = "photoCollection";

	public PhotoPickerPlusIntentWrapper(Context context) {
		super(context, ServicesActivity.class);
	}

	public PhotoPickerPlusIntentWrapper(Intent intent) {
		super(intent);
	}

	public String getAccountId() {
		return getIntent().getExtras().getString(KEY_ACCOUNT_ID);
	}

	public void setAccountId(String accountId) {
		getIntent().putExtra(KEY_ACCOUNT_ID, accountId);
	}

	public String getAlbumId() {
		return getIntent().getExtras().getString(KEY_ALBUM_ID);
	}

	public void setAlbumId(String albumId) {
		getIntent().putExtra(KEY_ALBUM_ID, albumId);
	}

	public void setAccountType(AccountType accountType) {
		getIntent().putExtra(KEY_ACCOUNT_TYPE, accountType.name());
	}

	public AccountType getAccountType() {
		String accountName = getIntent().getExtras()
				.getString(KEY_ACCOUNT_TYPE);
		AccountType type = null;
		for (AccountType accountType : AccountType.values()) {
			if (accountName != null
					&& accountName.equalsIgnoreCase(accountType.name())) {
				type = accountType;
			}
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<AssetModel> getMediaCollection() {
		return (ArrayList<AssetModel>) getIntent().getSerializableExtra(KEY_PHOTO_COLLECTION);
	}

	public void setMediaCollection(List<AssetModel> mediaCollection) {
		getIntent().putExtra(KEY_PHOTO_COLLECTION,
				(ArrayList<AssetModel>) mediaCollection);
	}

	public void startActivityForResult(Activity context) {
		context.startActivityForResult(getIntent(), ACTIVITY_FOR_RESULT_KEY);
	}
}
