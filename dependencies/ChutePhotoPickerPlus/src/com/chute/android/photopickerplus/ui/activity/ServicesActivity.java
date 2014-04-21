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
package com.chute.android.photopickerplus.ui.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.callback.CustomAuthenticationProvider;
import com.chute.android.photopickerplus.dao.MediaDAO;
import com.chute.android.photopickerplus.models.DeliverMediaModel;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.android.photopickerplus.models.enums.PhotoFilterType;
import com.chute.android.photopickerplus.ui.fragment.FragmentEmpty;
import com.chute.android.photopickerplus.ui.fragment.FragmentRoot;
import com.chute.android.photopickerplus.ui.fragment.FragmentServices.ServiceClickedListener;
import com.chute.android.photopickerplus.ui.fragment.FragmentSingle;
import com.chute.android.photopickerplus.ui.listener.ListenerAccountAssetsSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesCursor;
import com.chute.android.photopickerplus.ui.listener.ListenerImageSelection;
import com.chute.android.photopickerplus.ui.listener.ListenerVideoSelection;
import com.chute.android.photopickerplus.util.AppUtil;
import com.chute.android.photopickerplus.util.AssetUtil;
import com.chute.android.photopickerplus.util.Constants;
import com.chute.android.photopickerplus.util.NotificationUtil;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.android.photopickerplus.util.intent.IntentUtil;
import com.chute.android.photopickerplus.util.intent.PhotosIntentWrapper;
import com.chute.sdk.v2.api.accounts.CurrentUserAccountsRequest;
import com.chute.sdk.v2.api.accounts.GCAccounts;
import com.chute.sdk.v2.api.authentication.AuthenticationActivity;
import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
import com.chute.sdk.v2.api.authentication.AuthenticationOptions;
import com.chute.sdk.v2.api.authentication.TokenAuthenticationProvider;
import com.chute.sdk.v2.model.AccountModel;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ListResponseModel;
import com.chute.sdk.v2.utils.PreferenceUtil;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

/**
 * Activity for displaying the services.
 * <p/>
 * This activity is used to display both local and remote services in a
 * GridView.
 */
public class ServicesActivity extends FragmentActivity implements
		ListenerFilesAccount, ListenerFilesCursor, ServiceClickedListener {

	private static FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private AccountType accountType;
	private boolean dualPanes;
	private List<Integer> accountItemPositions;
	private List<Integer> imageItemPositions;
	private List<Integer> videoItemPositions;
	private String folderId;
	private AccountModel account;
	private ListenerAccountAssetsSelection listenerAssetsSelection;
	private ListenerImageSelection listenerImagesSelection;
	private ListenerVideoSelection listenerVideosSelection;
	private FragmentSingle fragmentSingle;
	private FragmentRoot fragmentRoot;
	private TextView signOut;
	private int photoFilterType;

	public void setAssetsSelectListener(
			ListenerAccountAssetsSelection adapterListener) {
		this.listenerAssetsSelection = adapterListener;
	}

	public void setImagesSelectListener(ListenerImageSelection adapterListener) {
		this.listenerImagesSelection = adapterListener;
	}

	public void setVideosSelectListener(ListenerVideoSelection adapterListener) {
		this.listenerVideosSelection = adapterListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentManager = getSupportFragmentManager();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_layout);

		dualPanes = getResources().getBoolean(R.bool.has_two_panes);

		signOut = (TextView) findViewById(R.id.gcTextViewSignOut);
		signOut.setOnClickListener(new SignOutListener());

		retrieveValuesFromBundle(savedInstanceState);

		if (dualPanes
				&& savedInstanceState == null
				&& getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
			replaceContentWithEmptyFragment();
		}

	}

	@Override
	public void recordVideo() {
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		Uri uri = AppUtil.getTempVideoFile();
		if (uri != null) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, Constants.CAMERA_VIDEO_REQUEST);

	}

	@Override
	public void lastVideo() {
		Uri lastVideoThumbnailFromCameraVideos = MediaDAO
				.getLastVideoThumbnailFromCameraVideos(getApplicationContext());
		Uri lastVideoItemFromCameraVideos = MediaDAO
				.getLastVideoFromCameraVideos(getApplicationContext());
		if (lastVideoThumbnailFromCameraVideos.toString().equals("")) {
			NotificationUtil.makeToast(getApplicationContext(), getResources()
					.getString(R.string.no_camera_photos));
		} else {
			final AssetModel model = new AssetModel();
			model.setThumbnail(lastVideoThumbnailFromCameraVideos.toString());
			model.setUrl(lastVideoThumbnailFromCameraVideos.toString());
			model.setVideoUrl(lastVideoItemFromCameraVideos.toString());
			model.setType(MediaType.VIDEO.name().toLowerCase());
			IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
					model, null);
		}
	}

	@Override
	public void takePhoto() {
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			NotificationUtil.makeToast(getApplicationContext(),
					R.string.toast_feature_camera);
			return;
		}
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (AppUtil.hasImageCaptureBug() == false) {
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(AppUtil
					.getTempImageFile(ServicesActivity.this)));
		} else {
			intent.putExtra(
					android.provider.MediaStore.EXTRA_OUTPUT,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		}
		startActivityForResult(intent, Constants.CAMERA_PIC_REQUEST);
	}

	@Override
	public void lastPhoto() {
		Uri uri = MediaDAO
				.getLastPhotoFromCameraPhotos(getApplicationContext());
		if (uri.toString().equals("")) {
			NotificationUtil.makeToast(getApplicationContext(), getResources()
					.getString(R.string.no_camera_photos));
		} else {
			final AssetModel model = new AssetModel();
			model.setThumbnail(uri.toString());
			model.setUrl(uri.toString());
			model.setType(MediaType.IMAGE.name().toLowerCase());
			IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
					model, null);
		}

	}

	@Override
	public void photoStream() {
		photoFilterType = PhotoFilterType.ALL_MEDIA.ordinal();
		accountItemPositions = null;
		imageItemPositions = null;
		videoItemPositions = null;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.ALL_MEDIA);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(null, PhotoFilterType.ALL_MEDIA);
		}

	}

	@Override
	public void cameraRoll() {
		photoFilterType = PhotoFilterType.CAMERA_ROLL.ordinal();
		accountItemPositions = null;
		imageItemPositions = null;
		videoItemPositions = null;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.CAMERA_ROLL);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(null, PhotoFilterType.CAMERA_ROLL);
		}

	}

	public void accountClicked(AccountModel account, AccountType accountType) {
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		photoFilterType = PhotoFilterType.SOCIAL_MEDIA.ordinal();
		accountItemPositions = null;
		imageItemPositions = null;
		videoItemPositions = null;
		this.account = account;
		if (!dualPanes) {
			final PhotosIntentWrapper wrapper = new PhotosIntentWrapper(
					ServicesActivity.this);
			wrapper.setFilterType(PhotoFilterType.SOCIAL_MEDIA);
			wrapper.setAccount(account);
			wrapper.startActivityForResult(ServicesActivity.this,
					PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY);
		} else {
			replaceContentWithRootFragment(account,
					PhotoFilterType.SOCIAL_MEDIA);
		}

	}

	public void replaceContentWithSingleFragment(AccountModel account,
			String folderId, List<Integer> selectedItemPositions) {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentSingle
				.newInstance(account, folderId, selectedItemPositions),
				Constants.TAG_FRAGMENT_FILES);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();

	}

	public void replaceContentWithRootFragment(AccountModel account,
			PhotoFilterType filterType) {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments, FragmentRoot.newInstance(
				account, filterType, accountItemPositions, imageItemPositions,
				videoItemPositions), Constants.TAG_FRAGMENT_FOLDER);
		fragmentTransaction.commit();
	}

	public void replaceContentWithEmptyFragment() {
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.gcFragments,
				FragmentEmpty.newInstance(), Constants.TAG_FRAGMENT_EMPTY);
		fragmentTransaction.commit();
	}

	@Override
	public void accountLogin(AccountType type) {
		accountType = type;
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		if (PreferenceUtil.get().hasAccount(type.getLoginMethod())) {
			AccountModel account = PreferenceUtil.get().getAccount(
					type.getLoginMethod());
			accountClicked(account, accountType);
		} else {
			AuthenticationFactory.getInstance().startAuthenticationActivity(
					ServicesActivity.this,
					accountType,
					new AuthenticationOptions.Builder()
							.setClearCookiesForAccount(false)
							.setShouldRetainSession(false).build());
		}

	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK
				&& resultCode != AuthenticationActivity.RESULT_DIFFERENT_CHUTE_USER_AUTHENTICATED) {
			return;
		}
		if (requestCode == AuthenticationFactory.AUTHENTICATION_REQUEST_CODE) {
			if (data != null) {
				String newSessionToken = data
						.getExtras()
						.getString(
								AuthenticationActivity.INTENT_DIFFERENT_CHUTE_USER_TOKEN);
				String previousSessionToken = TokenAuthenticationProvider
						.getInstance().getToken();
				if (!newSessionToken.equals(previousSessionToken)) {
					CurrentUserAccountsRequest request = new CurrentUserAccountsRequest(
							getApplicationContext(), new AccountsCallback());
					request.getClient().setAuthentication(
							new CustomAuthenticationProvider(newSessionToken));
					request.executeAsync();
				}
			} else {
				GCAccounts.allUserAccounts(getApplicationContext(),
						new AccountsCallback()).executeAsync();
			}
			return;
		}

		if (requestCode == PhotosIntentWrapper.ACTIVITY_FOR_RESULT_STREAM_KEY) {
			finish();
			return;
		}
		if (requestCode == Constants.CAMERA_PIC_REQUEST) {
			String path = "";
			File tempFile = AppUtil.getTempImageFile(getApplicationContext());
			if (AppUtil.hasImageCaptureBug() == false && tempFile.length() > 0) {
				try {
					android.provider.MediaStore.Images.Media.insertImage(
							getContentResolver(), tempFile.getAbsolutePath(),
							null, null);
					tempFile.delete();
					path = MediaDAO.getLastPhotoFromCameraPhotos(
							getApplicationContext()).toString();
				} catch (FileNotFoundException e) {
					Log.d("", e.toString());
				}
			} else {
				Log.e("ServicesActivity", "Bug " + data.getData().getPath());
				path = Uri.fromFile(
						new File(AppUtil.getPath(getApplicationContext(),
								data.getData()))).toString();
			}
			final AssetModel model = new AssetModel();
			model.setThumbnail(path);
			model.setUrl(path);
			model.setType(MediaType.IMAGE.name().toLowerCase());
			ArrayList<AssetModel> mediaCollection = new ArrayList<AssetModel>();
			mediaCollection.add(model);
			setResult(Activity.RESULT_OK, new Intent().putExtra(
					PhotosIntentWrapper.KEY_PHOTO_COLLECTION, mediaCollection));
			finish();
		}
		if (requestCode == Constants.CAMERA_VIDEO_REQUEST) {
			Uri uriVideo = data.getData();
			File file = new File(uriVideo.getPath());

			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
					file.getAbsolutePath(),
					MediaStore.Images.Thumbnails.MINI_KIND);

			final AssetModel model = new AssetModel();
			model.setThumbnail(AppUtil.getImagePath(getApplicationContext(),
					thumbnail));
			model.setVideoUrl(uriVideo.toString());
			model.setUrl(AppUtil.getImagePath(getApplicationContext(),
					thumbnail));
			model.setType(MediaType.VIDEO.name().toLowerCase());
			ArrayList<AssetModel> mediaCollection = new ArrayList<AssetModel>();
			mediaCollection.add(model);
			setResult(Activity.RESULT_OK, new Intent().putExtra(
					PhotosIntentWrapper.KEY_PHOTO_COLLECTION, mediaCollection));
			finish();
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setResult(Activity.RESULT_OK,
				new Intent().putExtras(intent.getExtras()));
		ServicesActivity.this.finish();
	}

	@Override
	public void onDeliverAccountFiles(ArrayList<AssetModel> assetList, AccountType accountType) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetList, accountType);

	}

	@Override
	public void onDeliverCursorAssets(List<DeliverMediaModel> deliverList) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				AssetUtil.getPhotoCollection(deliverList), null);

	}

	@Override
	public void onAccountFilesSelect(AssetModel assetModel, AccountType accountType) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetModel, accountType);
	}

	@Override
	public void onCursorAssetsSelect(AssetModel assetModel) {
		IntentUtil.deliverDataToInitialActivity(ServicesActivity.this,
				assetModel, null);
	}

	@Override
	public void onAccountFolderSelect(AccountModel account, String folderId) {
		accountItemPositions = null;
		imageItemPositions = null;
		videoItemPositions = null;
		photoFilterType = PhotoFilterType.SOCIAL_MEDIA.ordinal();
		this.folderId = folderId;
		this.account = account;
		replaceContentWithSingleFragment(account, folderId,
				accountItemPositions);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(Constants.KEY_FOLDER_ID, folderId);
		outState.putParcelable(Constants.KEY_ACCOUNT, account);
		outState.putInt(Constants.KEY_PHOTO_FILTER_TYPE, photoFilterType);
		List<Integer> accountPositions = new ArrayList<Integer>();
		List<Integer> imagePaths = new ArrayList<Integer>();
		List<Integer> videoPaths = new ArrayList<Integer>();
		if (listenerAssetsSelection != null
				&& listenerAssetsSelection.getSocialPhotosSelection() != null) {
			accountPositions.addAll(listenerAssetsSelection
					.getSocialPhotosSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS,
					(ArrayList<Integer>) accountPositions);
		}
		if (listenerImagesSelection != null
				&& listenerImagesSelection.getCursorImagesSelection() != null) {
			imagePaths.addAll(listenerImagesSelection
					.getCursorImagesSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS,
					(ArrayList<Integer>) imagePaths);
		}
		if (listenerVideosSelection != null
				&& listenerVideosSelection.getCursorVideosSelection() != null) {
			videoPaths.addAll(listenerVideosSelection
					.getCursorVideosSelection());
			outState.putIntegerArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS,
					(ArrayList<Integer>) videoPaths);
		}

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		fragmentSingle = (FragmentSingle) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		if (fragmentSingle != null
				&& photoFilterType == PhotoFilterType.SOCIAL_MEDIA.ordinal()) {
			fragmentSingle.updateFragment(account, folderId,
					accountItemPositions);
		}
		if (fragmentRoot != null) {
			fragmentRoot.updateFragment(account,
					PhotoFilterType.values()[photoFilterType],
					accountItemPositions, imageItemPositions,
					videoItemPositions);
		}
	}

	private void retrieveValuesFromBundle(Bundle savedInstanceState) {
		accountItemPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_ACCOUNTS_ITEMS)
				: null;

		imageItemPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_IMAGES_ITEMS)
				: null;

		videoItemPositions = savedInstanceState != null ? savedInstanceState
				.getIntegerArrayList(Constants.KEY_SELECTED_VIDEOS_ITEMS)
				: null;

		folderId = savedInstanceState != null ? savedInstanceState
				.getString(Constants.KEY_FOLDER_ID) : null;

		account = (AccountModel) (savedInstanceState != null ? savedInstanceState
				.getParcelable(Constants.KEY_ACCOUNT) : null);

		photoFilterType = savedInstanceState != null ? savedInstanceState
				.getInt(Constants.KEY_PHOTO_FILTER_TYPE) : 0;

	}

	@Override
	public void onDestroy() {
		Fragment fragmentFolder = fragmentManager
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		Fragment fragmentFiles = fragmentManager
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		if (fragmentFolder != null && fragmentFolder.isResumed()) {
			fragmentManager.beginTransaction().remove(fragmentFolder).commit();
		}
		if (fragmentFiles != null && fragmentFiles.isResumed()) {
			fragmentManager.beginTransaction().remove(fragmentFiles).commit();
		}
		super.onDestroy();
	}

	@Override
	public void onSessionExpired(AccountType accountType) {
		PhotoPickerPreferenceUtil.get().setAccountType(accountType);
		AuthenticationFactory.getInstance().startAuthenticationActivity(
				ServicesActivity.this,
				accountType,
				new AuthenticationOptions.Builder()
						.setShouldRetainSession(true).build());
	}

	@Override
	public void onBackPressed() {
		fragmentRoot = (FragmentRoot) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FOLDER);
		fragmentSingle = (FragmentSingle) getSupportFragmentManager()
				.findFragmentByTag(Constants.TAG_FRAGMENT_FILES);
		if (fragmentRoot != null && fragmentRoot.isVisible()) {
			this.finish();
		} else {
			super.onBackPressed();
		}

	}

	private final class AccountsCallback implements
			HttpCallback<ListResponseModel<AccountModel>> {

		@Override
		public void onSuccess(ListResponseModel<AccountModel> responseData) {
			if (accountType == null) {
				accountType = PhotoPickerPreferenceUtil.get().getAccountType();
			}
			if (responseData.getData().size() == 0) {
				NotificationUtil.makeToast(getApplicationContext(),
						R.string.no_albums_found);
				return;
			}
			for (AccountModel accountModel : responseData.getData()) {
				if (accountModel.getType().equals(accountType.getLoginMethod())) {
					PreferenceUtil.get().saveAccount(accountModel);
					accountClicked(accountModel, accountType);
				}
			}

		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			Log.d("ServicesActivity", "Http Error: " + responseStatus.getStatusCode() + " "
					+ responseStatus.getStatusMessage());
		}

	}

	private final class SignOutListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (dualPanes) {
				replaceContentWithEmptyFragment();
			}
			NotificationUtil.makeToast(getApplicationContext(),
					R.string.toast_signed_out);
			TokenAuthenticationProvider.getInstance().clearAuth();
			PhotoPickerPreferenceUtil.get().clearAll();

		}

	}

}