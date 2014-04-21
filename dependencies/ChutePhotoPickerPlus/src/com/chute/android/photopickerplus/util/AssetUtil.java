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
package com.chute.android.photopickerplus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import com.chute.android.photopickerplus.models.DeliverMediaModel;
import com.chute.sdk.v2.model.AccountAlbumModel;
import com.chute.sdk.v2.model.AccountBaseModel;
import com.chute.sdk.v2.model.AccountMediaModel;
import com.chute.sdk.v2.model.AssetModel;

/**
 * Helper class containing with static methods regarding {@link AssetModel}s.
 * 
 */
public class AssetUtil {

	/**
	 * Creates a list of {@link AssetModel}s from the given
	 * {@link DeliverMediaModel} list.
	 * 
	 * @param resultList
	 *            List of {@link DeliverMediaModel}s
	 * @return List of {@link AssetModel}s
	 */
	public static List<AssetModel> getPhotoCollection(
			List<DeliverMediaModel> resultList) {
		final List<AssetModel> collection = new ArrayList<AssetModel>();
		for (DeliverMediaModel result : resultList) {
			AssetModel asset = getMediaModel(result);
			collection.add(asset);
		}
		return collection;
	}

	/**
	 * Creates and {@link AssetModel} out of the given {@link DeliverMediaModel}
	 * .
	 * 
	 * @param model
	 *            {@link DeliverMediaModel}
	 * @return {@link AssetModel}
	 */
	public static AssetModel getMediaModel(DeliverMediaModel model) {
		final AssetModel asset = new AssetModel();
		asset.setThumbnail(Uri.fromFile(new File(model.getThumbnail()))
				.toString());
		asset.setUrl(model.getImageUrl());
		asset.setVideoUrl(model.getVideoUrl());
		asset.setType(model.getMediaType().name().toLowerCase());
		return asset;
	}

	/**
	 * Filters the media items that should be displayed according to the options
	 * retrieved from the configuration regarding if the application should
	 * support images, videos or both.
	 * 
	 * @param accountBaseModel
	 *            {@link AccountBaseModel}
	 * @param supportImages
	 *            boolean value indicating whether the application supports
	 *            images.
	 * @param supportVideos
	 *            boolean value indicating whether the application supports
	 *            videos.
	 * @return {@link AccountBaseModel} containing filtered media files.
	 */
	public static AccountBaseModel filterFiles(
			AccountBaseModel accountBaseModel, boolean supportImages,
			boolean supportVideos) {
		AccountBaseModel model = new AccountBaseModel();
		List<AccountAlbumModel> folders = accountBaseModel.getFolders();
		List<AccountMediaModel> files = new ArrayList<AccountMediaModel>();
		List<AccountMediaModel> videos = new ArrayList<AccountMediaModel>();
		List<AccountMediaModel> images = new ArrayList<AccountMediaModel>();
		if (accountBaseModel.getFiles() != null) {
			for (AccountMediaModel file : accountBaseModel.getFiles()) {
				if (file.getVideoUrl() != null && supportVideos == true) {
					videos.add(file);
				}
				if (file.getVideoUrl() == null && supportImages == true) {
					images.add(file);
				}
			}
		}
		files.addAll(images);
		files.addAll(videos);
		model.setFiles(files);
		model.setFolders(folders);
		return model;
	}

}
