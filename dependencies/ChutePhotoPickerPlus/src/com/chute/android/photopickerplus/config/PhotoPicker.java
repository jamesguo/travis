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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;
import com.chute.android.photopickerplus.models.enums.DisplayType;
import com.chute.android.photopickerplus.models.enums.LocalServiceType;
import com.chute.android.photopickerplus.util.PhotoPickerPreferenceUtil;
import com.chute.sdk.v2.model.enums.AccountType;
import com.dg.libs.rest.callbacks.HttpCallback;
import com.dg.libs.rest.domain.ResponseStatus;

/**
 * The {@link PhotoPicker} class is a singleton object, maintaining a
 * thread-safe instance of itself. It is responsible for service configuration
 * and display. It abstracts all the calls in small, cohesive and modular
 * methods.
 * 
 * <b>NOTE:</b> {@link #init(PhotoPickerConfiguration)} method must be called
 * before any other method.
 * 
 */
public class PhotoPicker {

	private static final String LOG_INIT_CONFIG = "Initialize PhotoPicker with configuration";
	private static final String LOG_DESTROY = "Destroy PhotoPicker";

	private static final String WARNING_RE_INIT_CONFIG = "Try to initialize PhotoPicker which had already been initialized before. "
			+ "To re-init PhotoPicker with new configuration call PhotoPicker.destroy() at first.";
	private static final String ERROR_NOT_INIT = "PhotoPicker must be initialized with configuration before using";
	private static final String ERROR_INIT_CONFIG_WITH_NULL = "PhotoPicker configuration can not be initialized with null";
	private static final String ERROR_HTTP = "Error when trying to get services from server: ";
	private static final String WARNING_UNSUPPORTED_REMOTE_SERVICES = "Invalid service type. Supported valid services: Facebook, Google, Googledrive, Instagram, Flickr, Picasa, Dropbox, Skydrive, YouTube";
	private static final String WARNING_UNSUPPORTED_LOCAL_SERVICES = "Invalid service type. Supported valid services: Camera Media, All Media, Last Photo Taken, Take Photo, Record Video, Last Video Captured";
	private static final String ERROR_INIT_CONFIG_WITH_FALSE_IMAGES_VIDEOS = "PhotoPicker should support images, videos or both.";

	/**
	 * List of {@link AccountType} objects representing the remote services to
	 * be configured.
	 */
	private List<AccountType> remoteServices;
	/**
	 * List of {@link LocalServiceType} objects representing the local services
	 * to be configured.
	 */
	private List<LocalServiceType> localServices;

	private PhotoPickerConfiguration configuration;

	/**
	 * The static instance of the {@link PhotoPicker}, as per the typical
	 * Singleton design pattern.
	 * 
	 * Note the usage of the volatile keyword for thread-safe programming.
	 */
	private volatile static PhotoPicker instance;

	/**
	 * Static initialization method. This method will always return the
	 * singleton instance of the {@link PhotoPicker} object and it will
	 * initialize it the first time it is requested. It uses the double-checked
	 * locking mechanism and initialization on demand.
	 * 
	 * @return the system-wide singleton instance of {@link PhotoPicker}.
	 */
	public static PhotoPicker getInstance() {
		if (instance == null) {
			synchronized (PhotoPicker.class) {
				if (instance == null) {
					instance = new PhotoPicker();
				}
			}
		}
		return instance;
	}

	/**
	 * No-args default constructor, preventing the manual initialization of this
	 * object as per the singleton pattern.
	 */
	protected PhotoPicker() {
	}

	/**
	 * Initializes PhotoPicker instance with configuration.<br />
	 * To force initialization of new configuration you should
	 * {@linkplain #destroy() destroy PhotoPicker} first.
	 * 
	 * @param configuration
	 *            {@linkplain PhotoPickerConfiguration PhotoPicker
	 *            configuration}
	 * @throws IllegalArgumentException
	 *             if <b>configuration</b> parameter is null.
	 */
	public synchronized void init(PhotoPickerConfiguration configuration) {
		if (configuration == null) {
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
		}
		if (this.configuration == null) {
			Log.d("PhotoPicker", LOG_INIT_CONFIG);
			this.configuration = configuration;
		} else {
			Log.w("PhotoPicker", WARNING_RE_INIT_CONFIG);
		}
		if (configuration.supportImages == false
				&& configuration.supportVideos == false) {
			throw new RuntimeException(
					ERROR_INIT_CONFIG_WITH_FALSE_IMAGES_VIDEOS);
		}
		fetchConfigFromServer();
	}

	/**
	 * Gets configuration from server if <code>configUrl</code> is initialized.
	 */
	public void fetchConfigFromServer() {
		checkConfiguration();
		if (configuration.configUrl != null) {
			fetchConfigFromServer(configuration.configUrl);
		}
	}

	/**
	 * Initializes local services.
	 * 
	 * If the list of {@link LocalServiceType} services stored in
	 * {@link PhotoPickerPreferenceUtil} is empty, PhotoPicker is initialized
	 * with local services listed in {@link PhotoPickerConfiguration}.
	 * 
	 * @return List of {@link LocalServiceType} services.
	 */
	public List<LocalServiceType> getLocalServices() {
		checkConfiguration();
		ArrayList<LocalServiceType> localServiceListInPrefs = PhotoPickerPreferenceUtil
				.get().getLocalServiceList();
		if (localServiceListInPrefs.isEmpty()) {
			if (configuration.localMediaList != null) {
				return checkIfLocalServiceIsSupported(configuration.localMediaList);
			} else {
				return new ArrayList<LocalServiceType>();
			}
		} else {
			return checkIfLocalServiceIsSupported(localServiceListInPrefs);
		}
	}

	/**
	 * Initializes remote services.
	 * 
	 * If the list of {@link AccountType} services stored in
	 * {@link PhotoPickerPreferenceUtil} is empty, PhotoPicker is initialized
	 * with remote services listed in {@link PhotoPickerConfiguration}.
	 * 
	 * @return List of {@link AccountType} services.
	 */
	public List<AccountType> getRemoteServices() {
		checkConfiguration();
		ArrayList<AccountType> accountServiceListInPrefs = PhotoPickerPreferenceUtil
				.get().getAccountServiceList();
		if (accountServiceListInPrefs.isEmpty()) {
			if (configuration.accountList != null) {
				return checkIfRemoteServiceIsSupported(configuration.accountList);
			} else {
				return new ArrayList<AccountType>();
			}
		} else {
			return checkIfRemoteServiceIsSupported(accountServiceListInPrefs);
		}
	}

	/**
	 * Retrieves a map containing {@link AccountType} as key and
	 * {@link DisplayType} as value.
	 * 
	 * @return Map of {@link AccountType}s associated with their
	 *         {@link DisplayType} values.
	 */
	public Map<AccountType, DisplayType> getAccountDisplayType() {
		checkConfiguration();
		Map<AccountType, DisplayType> map = configuration.accountDisplayTypeMap;
		if (map != null) {
			return map;
		} else {
			return new HashMap<AccountType, DisplayType>();
		}
	}

	/**
	 * Gets the default display type of media items: {@link DisplayType#LIST} or
	 * {@link DisplayType#GRID} If not set, the default display type is always
	 * {@link DisplayType#GRID}.
	 * 
	 * @return {@link DisplayType}
	 */
	public DisplayType getDefaultAccountDisplayType() {
		return configuration.displayType;
	}

	/**
	 * Gets list of {@link AccountType} and {@link LocalServiceType} services
	 * from server.
	 * 
	 * @param url
	 *            An absolute URL giving the base location of the config file
	 *            containing the services.
	 */
	public void fetchConfigFromServer(String url) {
		new ServiceRequest(configuration.context, url,
				new ConfigServicesCallback()).executeAsync();
	}

	/**
	 * Callback that returns {@link ServiceResponseModel} if services are
	 * successfully retrieved from the server or {@link ResponseStatus}
	 * containing information concerning the error if the callback failed.
	 * 
	 * {@link ServiceResponseModel} contains both {@link AccountType} and
	 * {@link LocalServiceType} list of services which are saved in
	 * {@link PhotoPickerPreferenceUtil}.
	 * 
	 */
	private final class ConfigServicesCallback implements
			HttpCallback<ServiceResponseModel> {

		@Override
		public void onHttpError(ResponseStatus status) {
			Log.d("PhotoPicker", ERROR_HTTP + status.getStatusMessage() + " "
					+ status.getStatusCode());
		}

		@Override
		public void onSuccess(ServiceResponseModel data) {
			remoteServices = new ArrayList<AccountType>();
			localServices = new ArrayList<LocalServiceType>();
			if (data.getServices() != null) {
				for (String service : data.getServices()) {
					if (isInEnum(service, AccountType.class)) {
						AccountType accountType = AccountType.valueOf(service
								.toUpperCase());
						remoteServices.add(accountType);
					} else {
						Log.w("PhotoPicker", WARNING_UNSUPPORTED_REMOTE_SERVICES);
					}
				}
				PhotoPickerPreferenceUtil
						.get()
						.setAccountServiceList(
								(ArrayList<AccountType>) checkIfRemoteServiceIsSupported(remoteServices));
			}
			if (data.getLocalFeatures() != null) {
				for (String localFeature : data.getLocalFeatures()) {
					if (isInEnum(localFeature, LocalServiceType.class)) {
						LocalServiceType localMediaType = LocalServiceType
								.valueOf(localFeature.toUpperCase());
						localServices.add(localMediaType);
					} else {
						Log.w("PhotoPicker", WARNING_UNSUPPORTED_LOCAL_SERVICES);
					}
				}
				PhotoPickerPreferenceUtil
						.get()
						.setLocalServiceList(
								(ArrayList<LocalServiceType>) checkIfLocalServiceIsSupported(localServices));
			}
		}
	}

	/**
	 * PhotoPicker component enables selection of multiple photos or a single
	 * photo.
	 * 
	 * @return <b>true</b> if multi, <b>false</b> if single picker.
	 */
	public boolean isMultiPicker() {
		return configuration.isMultiPicker;

	}

	/**
	 * If true, PhotoPicker displays images for selection. Note that
	 * {@linkplain #supportImages()}, {@linkplain #supportVideos()} or both
	 * should return true in order to start the PhotoPicker component.
	 * 
	 * @return <b>true</b> if supports images, <b>false</b> otherwise.
	 */
	public boolean supportImages() {
		return configuration.supportImages;

	}

	/**
	 * If true, PhotoPicker displays videos for selection. Note that
	 * {@linkplain #supportImages()}, {@linkplain #supportVideos()} or both
	 * should return true in order to start the PhotoPicker component.
	 * 
	 * @return <b>true</b> if multi, <b>false</b> if single picker.
	 */
	public boolean supportVideos() {
		return configuration.supportVideos;

	}

	/**
	 * Checks if PhotoPicker's configuration was initialized.
	 * 
	 * @throws IllegalStateException
	 *             if configuration is not initialized.
	 */
	private void checkConfiguration() {
		if (configuration == null) {
			throw new IllegalStateException(ERROR_NOT_INIT);
		}
	}

	/**
	 * If the specified list of {@link AccountType} services contains service
	 * which is not supported by the Chute API, the service is immediately
	 * removed.
	 * 
	 * Remote supported services include: Facebook, Flickr, Picasa, Instagram,
	 * Dropbox, Google, GoogleDrive, SkyDrive and YouTube.
	 * 
	 * @param remoteServices
	 *            List of {@link AccountType} services.
	 * @return Filtered {@link AccountType} list.
	 */
	private List<AccountType> checkIfRemoteServiceIsSupported(
			List<AccountType> remoteServices) {
		List<AccountType> accountList = new ArrayList<AccountType>(
				remoteServices);
		Iterator<AccountType> iterator = accountList.iterator();
		while (iterator.hasNext()) {
			AccountType accountType = iterator.next();
			if (!accountType.equals(AccountType.FACEBOOK)
					&& !accountType.equals(AccountType.FLICKR)
					&& !accountType.equals(AccountType.PICASA)
					&& !accountType.equals(AccountType.INSTAGRAM)
					&& !accountType.equals(AccountType.DROPBOX)
					&& !accountType.equals(AccountType.GOOGLE)
					&& !accountType.equals(AccountType.GOOGLEDRIVE)
					&& !accountType.equals(AccountType.SKYDRIVE)
					&& !accountType.equals(AccountType.YOUTUBE)) {
				Log.w("PhotoPicker", WARNING_UNSUPPORTED_REMOTE_SERVICES);
				iterator.remove();
			}
		}
		if (accountList.isEmpty()) {
			return new ArrayList<AccountType>();
		} else {
			return accountList;
		}

	}

	/**
	 * If the specified list of {@link LocalServiceType} services contains
	 * service which is not supported by the Chute API, the service is
	 * immediately removed.
	 * 
	 * Local supported services include: All Media, Camera Media, Last Taken
	 * Photo, Take Photo, Last Video Captured and Record Video.
	 * 
	 * @param localServices
	 *            List of {@link LocalServiceType} services.
	 * @return Filtered {@link LocalServiceType} list.
	 */
	private List<LocalServiceType> checkIfLocalServiceIsSupported(
			List<LocalServiceType> localServices) {
		List<LocalServiceType> localServiceList = new ArrayList<LocalServiceType>(
				localServices);
		Iterator<LocalServiceType> iterator = localServiceList.iterator();
		while (iterator.hasNext()) {
			LocalServiceType localMediaType = iterator.next();
			if (!localMediaType.equals(LocalServiceType.ALL_MEDIA)
					&& !localMediaType.equals(LocalServiceType.CAMERA_MEDIA)
					&& !localMediaType
							.equals(LocalServiceType.LAST_PHOTO_TAKEN)
					&& !localMediaType.equals(LocalServiceType.TAKE_PHOTO)
					&& !localMediaType
							.equals(LocalServiceType.LAST_VIDEO_CAPTURED)
					&& !localMediaType.equals(LocalServiceType.RECORD_VIDEO)) {
				Log.w("PhotoPicker", WARNING_UNSUPPORTED_LOCAL_SERVICES);
				iterator.remove();
			}
		}
		if (localServiceList.isEmpty()) {
			return new ArrayList<LocalServiceType>();
		} else {
			return localServiceList;
		}

	}

	/**
	 * Checks if the specified string is a part of the specified enumeration.
	 * 
	 * @param value
	 *            String value.
	 * @param enumClass
	 *            Enumeration class.
	 * @return <b>true</b> if the string belongs to the given enum class,
	 *         <b>false</b> otherwise.
	 */
	public <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.name().equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * You can {@linkplain #init(PhotoPickerConfiguration) init} PhotoPicker
	 * with new configuration after calling this method.
	 */
	public void destroy() {
		if (configuration != null)
			Log.d("PhotoPicker", LOG_DESTROY);
		configuration = null;
	}

}