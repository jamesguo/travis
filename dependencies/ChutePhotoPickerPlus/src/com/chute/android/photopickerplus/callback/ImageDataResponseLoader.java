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
package com.chute.android.photopickerplus.callback;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import android.util.Log;
import com.chute.android.photopickerplus.R;
import com.chute.android.photopickerplus.models.MediaResponseModel;
import com.chute.android.photopickerplus.models.MediaDataModel;
import com.chute.android.photopickerplus.models.MediaModel;
import com.chute.android.photopickerplus.models.OptionsModel;
import com.chute.android.photopickerplus.models.enums.MediaType;
import com.chute.android.photopickerplus.ui.listener.ListenerFilesAccount;
import com.chute.android.photopickerplus.util.NotificationUtil;
import com.chute.sdk.v2.api.Chute;
import com.chute.sdk.v2.api.authentication.AuthConstants;
import com.chute.sdk.v2.api.authentication.AuthenticationFactory;
import com.chute.sdk.v2.api.authentication.TokenAuthenticationProvider;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.chute.sdk.v2.model.response.ResponseModel;
import com.dg.libs.rest.HttpRequest;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

/**
 * The {@link ImageDataResponseLoader} has the responsibility for uploading the
 * selected media URL(s) and delivering the retrieved data to the main activity
 * i.e. the activity that started the PhotoPicker.
 * 
 * This class consists exclusively of static methods and is used only with media
 * from social accounts.
 * 
 */
public class ImageDataResponseLoader {

	public static void postImageData(Context context,
			ArrayList<AccountMediaModel> selectedImages,
			ListenerFilesAccount accountListener, AccountType accountType) {

		String token = TokenAuthenticationProvider.getInstance().getToken();
		AuthConstants authConstants = AuthenticationFactory.getInstance()
				.getAuthConstants();
		String clientId = authConstants.clientId;
		String clientSecret = authConstants.clientSecret;
		Chute.init(context, new AuthConstants(clientId, clientSecret), token);

		ArrayList<MediaDataModel> mediaModelList = new ArrayList<MediaDataModel>();
		for (AccountMediaModel accountMediaModel : selectedImages) {
			MediaDataModel mediaModel = new MediaDataModel();
			if (accountMediaModel.getVideoUrl() != null) {
				mediaModel.setFileType(MediaType.VIDEO.name().toLowerCase());
			} else {
				mediaModel.setFileType(MediaType.IMAGE.name().toLowerCase());
			}
			mediaModel.setVideoUrl(accountMediaModel.getVideoUrl());
			mediaModel.setImageUrl(accountMediaModel.getImageUrl());
			mediaModel.setThumbnail(accountMediaModel.getThumbnail());
			mediaModelList.add(mediaModel);
		}

		OptionsModel options = new OptionsModel();
		options.setCliendId(clientId);
		MediaModel imageDataModel = new MediaModel();
		imageDataModel.setOptions(options);
		imageDataModel.setMedia(mediaModelList);

		getImageData(context, imageDataModel,
				new ImageDataCallback(context, accountListener, accountType))
				.executeAsync();

	}

	private static HttpRequest getImageData(final Context context,
			MediaModel imageData,
			final HttpCallback<ResponseModel<MediaResponseModel>> callback) {
		return new ImageDataRequest(context, imageData, callback);
	}

	private static final class ImageDataCallback implements
			HttpCallback<ResponseModel<MediaResponseModel>> {

		private Context context;
		private ListenerFilesAccount listener;
		private AccountType accountType;

		private ImageDataCallback(Context context,
				ListenerFilesAccount listener, AccountType accountType) {
			this.context = context;
			this.listener = listener;
			this.accountType = accountType;
		}

		@Override
		public void onHttpError(ResponseStatus responseStatus) {
			NotificationUtil.makeToast(context, R.string.general_error);
			Log.d("ImageDataResponseLoader", "Http Error: " + responseStatus.getStatusCode() + " "
                    + responseStatus.getStatusMessage());

		}

		@Override
		public void onSuccess(ResponseModel<MediaResponseModel> responseData) {
			if (responseData.getData() != null) {
				List<AssetModel> assetList = responseData.getData()
						.getAssetList();
				listener.onDeliverAccountFiles(
						(ArrayList<AssetModel>) assetList, accountType);
			}

		}
	}

}
