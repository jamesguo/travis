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
package com.chute.android.photopickerplus.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.chute.android.photopickerplus.models.enums.DisplayType;
import com.chute.android.photopickerplus.models.enums.LocalServiceType;
import com.chute.sdk.v2.model.enums.AccountType;

/**
 * Use this builder to construct a PhotoPicker instance when you need to set
 * configuration options other than the default. PhotoPickerConfiguration is
 * best used by creating it, and then invoking its various configuration
 * methods, and finally calling build. The following is an example shows how to
 * use the PhotoPickerConfiguration to construct a PhotoPicker instance:
 * 
 * <code>
 *  PhotoPickerConfiguration config = new PhotoPickerConfiguration.Builder(
 *  getApplicationContext())
 *  .isMultiPicker(true)
 *  .accountList(AccountType.FLICKR, AccountType.FACEBOOK)
 *  .localImageList(LocalServiceType.ALL_MEDIA, LocalServiceType.CAMERA_MEDIA)
 *  .configUrl("http://s3.amazonaws.com/store.getchute.com/51eeae5e6e29310c9a000001")
 * .build();
 * PhotoPicker.getInstance().init(config);
 * </code>
 * 
 * NOTE: the order of invocation of configuration methods does not matter.
 * 
 */
public final class PhotoPickerConfiguration {

	final Context context;
	final List<AccountType> accountList;
	final List<LocalServiceType> localMediaList;
	final String configUrl;
	final boolean isMultiPicker;
	final boolean supportVideos;
	final boolean supportImages;
	final DisplayType displayType;
	final Map<AccountType, DisplayType> accountDisplayTypeMap;

	private PhotoPickerConfiguration(final Builder builder) {
		context = builder.context;
		isMultiPicker = builder.isMultiPicker;
		accountList = builder.accountList;
		localMediaList = builder.localMediaList;
		configUrl = builder.configUrl;
		supportImages = builder.supportImages;
		supportVideos = builder.supportVideos;
		displayType = builder.displayType;
		accountDisplayTypeMap = builder.accountDisplayTypeMap;
	}

	/**
	 * Creates default configuration for {@link PhotoPicker} <br />
	 * <b>Default values:</b>
	 * <ul>
	 * <li>isMultiPicker = false</li>
	 * <li>accountList = {@link AccountType#FACEBOOK},
	 * {@link AccountType#INSTAGRAM}</li>
	 * <li>localMediaList = {@link LocalServiceType#ALL_MEDIA},
	 * {@link LocalServiceType#CAMERA_MEDIA}</li>
	 * <li>supportImages = true</li>
	 * <li>supportVideos = true</li>
	 * <li>displayType = {@link DisplayType#GRID}</li>
	 * </ul>
	 * */
	public static PhotoPickerConfiguration createDefault(Context context) {
		return new Builder(context).build();
	}

	/**
	 * Builder for {@link PhotoPickerConfiguration}
	 * 
	 */
	public static class Builder {

		private Context context;
		private boolean isMultiPicker = false;
		private List<AccountType> accountList = null;
		private List<LocalServiceType> localMediaList = null;
		private String configUrl = null;
		private boolean supportImages = true;
		private boolean supportVideos = true;
		private DisplayType displayType = DisplayType.GRID;
		private Map<AccountType, DisplayType> accountDisplayTypeMap = null;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		/** Builds configured {@link PhotoPickerConfiguration} object */
		public PhotoPickerConfiguration build() {
			initEmptyFieldsWithDefaultValues();
			return new PhotoPickerConfiguration(this);
		}

		/**
		 * Sets list of remote services.
		 * 
		 * @param accountList
		 *            List of {@link AccountType} services.
		 */
		public Builder accountList(AccountType... accountList) {
			this.accountList = Arrays.asList(accountList);
			return this;
		}

		/**
		 * Sets list of local services.
		 * 
		 * @param localMediaList
		 *            List of {@link LocalServiceType} services.
		 */
		public Builder localMediaList(LocalServiceType... localMediaList) {
			this.localMediaList = Arrays.asList(localMediaList);
			return this;
		}

		/**
		 * Sets the URL giving the base location of the config file containing
		 * the services.
		 * 
		 * @param configUrl
		 */
		public Builder configUrl(String configUrl) {
			this.configUrl = configUrl;
			return this;
		}

		/**
		 * Sets the default {@link DisplayType} for the media items.
		 * 
		 * @param displayType
		 *            It can be either list of gird. If not set, the default
		 *            value is {@link DisplayType#GRID}
		 * @return
		 */
		public Builder defaultAccountDisplayType(DisplayType displayType) {
			this.displayType = displayType;
			return this;
		}

		/**
		 * Sets the {@link DisplayType} for each account individually and stores
		 * the values in a map.
		 * 
		 * @param accountDisplayTypeMap
		 *            Map that stores {@link AccountType}s as keys and
		 *            {@link DisplayType}s as values.
		 * @return
		 */
		public Builder accountDisplayType(
				Map<AccountType, DisplayType> accountDisplayTypeMap) {
			this.accountDisplayTypeMap = accountDisplayTypeMap;
			return this;

		}

		/**
		 * Enables video support.
		 * 
		 * @param supportVideos
		 *            <b>true</b> if PhotoPicker should support videos and
		 *            <b>false</b> otherwise.
		 * @return
		 */
		public Builder supportVideos(boolean supportVideos) {
			this.supportVideos = supportVideos;
			return this;
		}

		/**
		 * Enables image support.
		 * 
		 * @param supportImages
		 *            <b>true</b> if PhotoPicker should support images and
		 *            <b>false</b> otherwise.
		 * @return
		 */
		public Builder supportImages(boolean supportImages) {
			this.supportImages = supportImages;
			return this;
		}

		/**
		 * Allows selection of one or multiple media items in the PhotoPicker.
		 * 
		 * @param isMultiPicker
		 *            <b>true</b> for enabling multi-picking and <b>false</b>
		 *            for enabling single-picking feature.
		 * @return
		 */
		public Builder isMultiPicker(boolean isMultiPicker) {
			this.isMultiPicker = isMultiPicker;
			return this;
		}

		private void initEmptyFieldsWithDefaultValues() {
			if (localMediaList == null) {
				localMediaList = DefaultConfigurationFactory
						.createLocalMediaList();
			}
			if (accountList == null) {
				accountList = DefaultConfigurationFactory
						.createAccountTypeList();
			}

		}
	}
}
