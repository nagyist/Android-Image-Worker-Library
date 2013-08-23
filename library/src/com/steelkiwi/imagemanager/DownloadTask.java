package com.steelkiwi.imagemanager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.steelkiwi.imagemanager.processing.ImageProcessingDecorator;
import com.steelkiwi.imagemanager.processing.ProportionalScaleProcessor;
import com.steelkiwi.imagemanager.processing.SquareCropProcessor;

public final class DownloadTask {
	
	/**
	 * Represents this task as string to create unique key to use it in caches
	 */
	private final static String TASK_ID_PATTERN = "%s.%s.%s.%s.%s.%s";

	private final String CACHE_IDENTIFIER;

	private String path;
	private boolean isNetworkDownload;
	
	private WeakReference<ImageView> view;
	private ImageLoaderCallback callback;
	
	private Bitmap.Config bitmapConfig;
	private boolean mCache;
	private boolean dCache;
	private Animation animation;
	private ImageProcessingDecorator imageProcessor;
	private boolean processCropToSquare;
	private boolean processScaleProportionally;
	private int processScaleMaxWidth;
	private int processScaleMaxHeight;
	private int placeholder;
	private int errorIcon;
	private String dCachePath;

	private Bitmap result;

	private DownloadTask(Builder builder) {
		this.path = builder.path;
		this.isNetworkDownload = builder.isNetworkDownload;
		this.view = builder.view;
		this.callback = builder.callback;
		this.bitmapConfig = builder.bitmapConfig;
		this.mCache = builder.mCache;
		this.dCache = builder.dCache;
		this.animation = builder.animation;
		this.imageProcessor = builder.imageProcessor;
		this.processCropToSquare = builder.processCropToSquare;
		this.processScaleProportionally = builder.processScaleProportionally;
		this.processScaleMaxWidth = builder.processScaleMaxWidth;
		this.processScaleMaxHeight = builder.processScaleMaxHeight;
		this.placeholder = builder.placeholder;
		this.errorIcon = builder.errorIcon;
		CACHE_IDENTIFIER = createCacheIdentifier();
	}

	public String getUrl() {
		return path;
	}

	public WeakReference<ImageView> getView() {
		return view;
	}

	public ImageLoaderCallback getCallback() {
		return callback;
	}

	public Bitmap.Config getBitmapConfig() {
		return bitmapConfig;
	}

	public Bitmap getResult() {
		return result;
	}
	
	public boolean ismCache() {
		return mCache;
	}

	public boolean isdCache() {
		return dCache;
	}

	public void setResult(Bitmap result) {
		this.result = result;
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public int getProcessScaleMaxWidth() {
		return processScaleMaxWidth;
	}

	public void setProcessScaleMaxWidth(int processScaleMaxWidth) {
		this.processScaleMaxWidth = processScaleMaxWidth;
	}

	public int getProcessScaleMaxHeight() {
		return processScaleMaxHeight;
	}

	public void setProcessScaleMaxHeight(int processScaleMaxHeight) {
		this.processScaleMaxHeight = processScaleMaxHeight;
	}

	public ImageProcessingDecorator getImageProcessor() {
		return imageProcessor;
	}
	
	public boolean isProcessScaleProportionally() {
		return processScaleProportionally;
	}
	
	public boolean isNetworkDownload() {
		return isNetworkDownload;
	}
	
	public void setCacheRoot(File cacheDir){
		this.dCachePath = cacheDir.getAbsolutePath() + File.separator + CACHE_IDENTIFIER;
	}
	
	public String getdCachePath() {
		return dCachePath;
	}
	
	public int getPlaceholder() {
		return placeholder;
	}

	public int getErrorIcon() {
		return errorIcon;
	}

	void changeParamsForDiscCacheDownload(){
		isNetworkDownload = false;
		path = getdCachePath();
		dCache = false;
	}

	private String createCacheIdentifier(){
		String result = "";
		try {
			String tag = String.format(TASK_ID_PATTERN, path, bitmapConfig, processCropToSquare, processScaleProportionally, processScaleMaxWidth, processScaleMaxHeight);
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(tag.getBytes());
			result = convertToHex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
	
	String getCacheId(){
		return CACHE_IDENTIFIER;
	}

	BitmapFactory.Options createPreDecodeOptions(){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = bitmapConfig;
		return options;
	}

	// =====================================================================
	// inner classes
	// =====================================================================

	public final static class Builder {
		/**
		 * Path to load image - url of image or filename in cache.
		 */
		private String path;
		/**
		 * This flag represents a source cause image can be loaded from Internet or local file system. 
		 */
		private boolean isNetworkDownload;
		
		private WeakReference<ImageView> view;
		private ImageLoaderCallback callback;
		private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
		private boolean mCache;
		private boolean dCache;
		private Animation animation;
		private ImageProcessingDecorator imageProcessor;
		private boolean processCropToSquare;
		private boolean processScaleProportionally;
		private int processScaleMaxWidth;
		private int processScaleMaxHeight;
		private int placeholder = ImageManager.NO_RESOURCE;
		private int errorIcon = ImageManager.NO_RESOURCE;

		/**
		 * Set target of image task - url of image to download.
		 * @param path Url of image
		 * @return builder instance
		 */
		public Builder url(String path){
			this.path = path;
			isNetworkDownload = true;
			return this;
		}
		
		/**
		 * Set target of task - filename of image that was already cached on disk. 
		 * @param filename
		 * @return builder instance
		 */
		public Builder file(String filename){
			this.path = filename;
			isNetworkDownload = false;
			return this;
		}
		
		/**
		 * Set ImageView container for target image.
		 * @param view
		 * @return builder instance.
		 */
		public Builder loadTo(ImageView view){
			this.view = new WeakReference<ImageView>(view);
			return this;
		}
		
		/**
		 * Set ImageLoaderCallback if you want to work with download result by yourself.
		 * @param callback
		 * @return builder instance.
		 */
		public Builder downloadCallback(ImageLoaderCallback callback){
			this.callback = callback;
			return this;
		}

		/**
		 * Set preferred Bitmap.Config to decode image. @see {@link Bitmap.Config} for details.}
		 * @param bitmapConfig
		 * @return builder instance.
		 */
		public Builder config(Bitmap.Config bitmapConfig) {
			if(bitmapConfig != null){
				this.bitmapConfig = bitmapConfig;
			}
			return this;
		}
		
		/**
		 * Set is this image need to be cached in memory.
		 * @return builder instance.
		 */
		public Builder mCache(){
			this.mCache = true;
			return this;
		}
		
		/**
		 * Set is this image need to be cached to disk.
		 * @return builder instance.
		 */
		public Builder dCache(){
			this.dCache = true;
			return this;
		}
		
		/**
		 * Animate target ImageView when download is ready.
		 * @param animation
		 * @return builder instance.
		 */
		public Builder animation(Animation animation){
			this.animation = animation;
			return this;
		}
		
		/**
		 * Crop target image after downloading to square using lesser side.
		 * @return builder instance.
		 */
		public Builder cropToSquare(){
			ImageProcessingDecorator processor = new SquareCropProcessor();
			addImageProcessor(processor);
			processCropToSquare = true;
			return this;
		}
		
		/**
		 * Scales result bitmap to be at most maxWidth and maxHeight. This method uses {@link Bitmap.createScaledBitmap}<br/>
		 * so it works more accurate than using of inSampleSize.
		 * @param maxWidth maximum desired width
		 * @param maxHeight maximum desired height
		 * @return builder instance.
		 */
		public Builder scaleToProportionaly(int maxWidth, int maxHeight){
			if(maxWidth <= 0 || maxHeight <=0){
				throw new IllegalArgumentException("Incorrect params for scaling width = " + maxWidth + ", height = " + maxHeight);
			} else {
				ImageProcessingDecorator processor = new ProportionalScaleProcessor(maxWidth, maxHeight);
				addImageProcessor(processor);
				processScaleProportionally = true;
				processScaleMaxWidth = maxWidth;
				processScaleMaxHeight = maxHeight;
				return this;
			}
		}
		
		public Builder placeholder(int placeholder){
			this.placeholder = placeholder;
			return this;
		}
		
		public Builder errorIcon(int errorIcon){
			this.errorIcon = errorIcon;
			return this;
		}
		
		private void addImageProcessor(ImageProcessingDecorator imageProcessor){
			if(this.imageProcessor != null){
				imageProcessor.decorate(this.imageProcessor);
			}
			this.imageProcessor = imageProcessor;
		}

		public DownloadTask build() {
			if(path == null || path.equals("")){
				throw new IllegalArgumentException("No path to get image specified!");
			}
			if(view == null && callback == null){
				throw new IllegalArgumentException("No ImageView to show image and no ImageLoaderCallback to return it. Use Builder.loadTo(ImageView view) or Builder.downloadCallback(ImageLoaderCallback callback) to set at least one of it.");
			}
			return new DownloadTask(this);
		}
	}
}
