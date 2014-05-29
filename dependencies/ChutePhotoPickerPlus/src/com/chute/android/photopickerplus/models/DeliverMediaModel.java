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
package com.chute.android.photopickerplus.models;

import com.chute.android.photopickerplus.models.enums.MediaType;

/**
 * {@link DeliverMediaModel} wraps up asset attributes needed when delivering a
 * result back to the activity that started the PhotoPicker+.
 * 
 */
public class DeliverMediaModel {

	/**
	 * URL of the image
	 */
	private String imageUrl;
	/**
	 * URL of the video
	 */
	private String videoUrl;
	/**
	 * Thumbnail URL
	 */
	private String thumbnail;
	/**
	 * Media type: image or video
	 */
	private MediaType mediaType;

	/**
	 * Getters and setters
	 * 
	 */
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeliverMediaModel [imageUrl=");
		builder.append(imageUrl);
		builder.append(", videoUrl=");
		builder.append(videoUrl);
		builder.append(", thumbnail=");
		builder.append(thumbnail);
		builder.append(", mediaType=");
		builder.append(mediaType);
		builder.append("]");
		return builder.toString();
	}

}
