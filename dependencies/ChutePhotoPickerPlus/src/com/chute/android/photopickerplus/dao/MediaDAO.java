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
package com.chute.android.photopickerplus.dao;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * The definition of the Database Access Objects that handles the reading and
 * writing a class from the database.
 * 
 */
public class MediaDAO {

	private MediaDAO() {
	}

	/* CAMERA */
	/**
	 * Request a specific record in {@link MediaStore.Images.Media} database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to camera photos on the
	 *         device.
	 */
	public static Cursor getCameraPhotos(final Context context) {
		final String[] projection = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		final String query = MediaStore.Images.Media.DATA + " LIKE \"%DCIM%\"";
		return context.getContentResolver().query(images, projection, query,
				null, MediaStore.Images.Media.DATE_ADDED + " DESC");
	}

	/**
	 * Request a specific record in {@link MediaStore.Video.Thumbnails}
	 * database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to camera video
	 *         thumbnails on the device.
	 */
	public static Cursor getCameraVideosThumbnails(final Context context) {
		final String[] projection = new String[] {
				MediaStore.Video.Thumbnails._ID,
				MediaStore.Video.Thumbnails.DATA };
		final Uri videos = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
		final String query = MediaStore.Video.Thumbnails.DATA
				+ " LIKE \"%DCIM%\"";
		return context.getContentResolver().query(videos, projection, query,
				null, MediaStore.Video.Thumbnails.DEFAULT_SORT_ORDER);
	}

	/**
	 * Request a specific record in {@link MediaStore.Video.Media} database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to camera videos on the
	 *         device.
	 */
	public static Cursor getCameraVideos(final Context context) {
		final String[] projection = new String[] { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA };
		final Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		final String query = MediaStore.Video.Media.DATA + " LIKE \"%DCIM%\"";
		return context.getContentResolver().query(videos, projection, query,
				null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
	}

	/* ALL MEDIA */
	/**
	 * Request a specific record in {@link MediaStore.Images.Media} database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to all photos on the
	 *         device.
	 */
	public static Cursor getAllMediaPhotos(final Context context) {
		final String[] projection = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		return context.getContentResolver().query(images, projection, null,
				null, MediaStore.Images.Media.DATE_ADDED + " DESC");
	}

	/**
	 * Request a specific record in {@link MediaStore.Video.Thumbnails}
	 * database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to all video thumbnails
	 *         on the device.
	 */
	public static Cursor getAllMediaVideosThumbnails(final Context context) {
		final String[] projection = new String[] {
				MediaStore.Video.Thumbnails._ID,
				MediaStore.Video.Thumbnails.DATA };
		final Uri videos = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
		return context.getContentResolver().query(videos, projection, null,
				null, MediaStore.Video.Thumbnails.DEFAULT_SORT_ORDER);
	}

	/**
	 * Request a specific record in {@link MediaStore.Video.Media} database.
	 * 
	 * @param context
	 *            The application context.
	 * @return Cursor object enabling read-write access to all videos on the
	 *         device.
	 */
	public static Cursor getAllMediaVideos(final Context context) {
		final String[] projection = new String[] { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA };
		final Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		return context.getContentResolver().query(videos, projection, null,
				null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
	}

	/* LAST MEDIA */
	/**
	 * Returns the last photo URI from all photos on the device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastPhotoFromAllPhotos(final Context context) {
		Cursor allMediaPhotos = getAllMediaPhotos(context);
		Uri uri = getFirstImageItemUri(allMediaPhotos);
		safelyCloseCursor(allMediaPhotos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Returns the last photo URI from the camera photos on the device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastPhotoFromCameraPhotos(final Context context) {
		Cursor allMediaPhotos = getCameraPhotos(context);
		Uri uri = getFirstImageItemUri(allMediaPhotos);
		safelyCloseCursor(allMediaPhotos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Returns the last video thumbnail URI from all videos on the device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastVideoThumbnailFromAllVideos(final Context context) {
		Cursor allMediaVideos = getAllMediaVideosThumbnails(context);
		Uri uri = getFirstVideoThumbnailUri(allMediaVideos);
		safelyCloseCursor(allMediaVideos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Returns the last video thumbnail URI from the camera videos on the
	 * device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastVideoThumbnailFromCameraVideos(
			final Context context) {
		Cursor cameraVideos = getCameraVideosThumbnails(context);
		Uri uri = getFirstVideoThumbnailUri(cameraVideos);
		safelyCloseCursor(cameraVideos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Returns the last video URI from the camera videos on the device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastVideoFromCameraVideos(final Context context) {
		Cursor cameraVideos = getCameraVideos(context);
		Uri uri = getFirstVideoItemUri(cameraVideos);
		safelyCloseCursor(cameraVideos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Returns the last video URI from all videos on the device.
	 * 
	 * @param context
	 *            The application context.
	 * @return The URI for the requested query.
	 */
	public static Uri getLastVideoFromAllVideos(final Context context) {
		Cursor allVideos = getAllMediaVideos(context);
		Uri uri = getFirstVideoItemUri(allVideos);
		safelyCloseCursor(allVideos);
		if (uri == null) {
			return Uri.parse("");
		}
		return uri;
	}

	/**
	 * Request a specific record in {@link MediaStore.Video.Thumbnails}
	 * database.
	 * 
	 * @param context
	 *            The application context.
	 * @param dataCursor
	 *            Cursor object enabling read-write access to videos on the
	 *            device.
	 * @param position
	 *            Cursor position.
	 * @return Path of the video thumbnail.
	 */
	public static String getVideoThumbnailFromCursor(final Context context,
			final Cursor dataCursor, int position) {
		String thumbPath = null;
		String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
				MediaStore.Video.Thumbnails.VIDEO_ID };
		if (dataCursor.moveToPosition(position)) {
			int id = dataCursor.getInt(dataCursor
					.getColumnIndex(MediaStore.Video.Media._ID));
			Cursor thumbCursor = context.getContentResolver()
					.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
							thumbColumns,
							MediaStore.Video.Thumbnails.VIDEO_ID + "=" + id,
							null, null);
			if (thumbCursor.moveToFirst()) {
				thumbPath = thumbCursor.getString(thumbCursor
						.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
			}
			safelyCloseCursor(thumbCursor);
		}
		return thumbPath;
	}

	/**
	 * Returns the URI of the first item from all photos on the device.
	 * 
	 * @param cursor
	 *            Cursor object enabling read-write access to all photos on the
	 *            device.
	 * @return The URI for the requested query.
	 */
	private static Uri getFirstImageItemUri(Cursor cursor) {
		if (cursor != null && cursor.moveToFirst()) {
			return Uri.fromFile(new File(cursor.getString(cursor
					.getColumnIndex(MediaStore.Images.Media.DATA))));
		}
		return null;
	}

	/**
	 * Returns the URI of the first item from all videos on the device.
	 * 
	 * @param cursor
	 *            Cursor object enabling read-write access to all videos on the
	 *            device.
	 * @return The URI for the requested query.
	 */
	private static Uri getFirstVideoItemUri(Cursor cursor) {
		if (cursor != null && cursor.moveToFirst()) {
			return Uri.fromFile(new File(cursor.getString(cursor
					.getColumnIndex(MediaStore.Video.Media.DATA))));
		}
		return null;
	}

	/**
	 * Returns the URI of the first item from all video thumbnails on the
	 * device.
	 * 
	 * @param cursor
	 *            Cursor object enabling read-write access to all video
	 *            thumbnails on the device.
	 * @return The URI for the requested query.
	 */
	private static Uri getFirstVideoThumbnailUri(Cursor cursor) {
		if (cursor != null && cursor.moveToLast()) {
			return Uri.fromFile(new File(cursor.getString(cursor
					.getColumnIndex(MediaStore.Video.Thumbnails.DATA))));
		}
		return null;
	}

	/**
	 * Created a thumbnail of the specified image ID.
	 * 
	 * @param context
	 *            The application context
	 * @param cursor
	 *            Cursor object enabling read-write access to all videos on the
	 *            device used for retrieving the original image ID associated
	 *            with the video thumbnail.
	 * @return A Bitmap instance. It could be null if the original image
	 *         associated with origId doesn't exist or memory is not enough.
	 */
	public static Bitmap getVideoThumbnail(Context context, Cursor cursor) {
		int id = cursor.getInt(cursor
				.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
		return MediaStore.Video.Thumbnails.getThumbnail(
				context.getContentResolver(), id,
				MediaStore.Video.Thumbnails.MICRO_KIND,
				(BitmapFactory.Options) null);
	}

	/**
	 * Closes the Cursor, releasing all of its resources and making it
	 * completely invalid.
	 * 
	 * @param c
	 *            Cursor object enabling random read-write access to the result
	 *            set returned by a database query.
	 */
	public static void safelyCloseCursor(final Cursor c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.d("", e.toString());
		}
	}

}
