package com.jingz.app.pano;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import android.util.Log;

import com.google.android.apps.lightcycle.panorama.LightCycleNative;
import com.jingz.app.pano.util.Callback;
import com.jingz.app.pano.util.Size;

public class IncrementalAligner extends Thread {
	public static final String TAG = IncrementalAligner.class.getSimpleName();

	private Callback<Void> doneCallback = null;
	boolean extractFeatureAndThummbnail = true;
	private final ArrayBlockingQueue<ImageData> imageToProcess = new ArrayBlockingQueue<ImageData>(
			20);
	private Size photoSize;
	private boolean processingImages = false;
	private final boolean useRealtimeAlignment;

	public IncrementalAligner(boolean useRealtimeAlignment) {
		this.useRealtimeAlignment = useRealtimeAlignment;
	}

	public void addImage(String filename, float[] rotation, int textureId) {
		try {
			imageToProcess.put(new ImageData(filename, rotation, textureId));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Unexpected interruption");
		}
	}

	@Override
	public void interrupt() {
		imageToProcess.add(new ImageData("Poison Pill", new float[9], 0));
	}

	public boolean isExtractFeatureAndThummbnail() {
		return extractFeatureAndThummbnail;
	}

	public boolean isProcessingImages() {
		return processingImages;
	}

	public boolean isRealtimeAlignment() {
		return useRealtimeAlignment;
	}

	@Override
	public void run() {
		boolean stopped = false;
		while (!stopped) {
			if (!isInterrupted()) {
				ArrayList<ImageData> images = new ArrayList<ImageData>();
				// :try_start_0
				try {
					ImageData imageData = imageToProcess.take();
					processingImages = true;
					images.add(imageData);

					// :goto_0
					while (!imageToProcess.isEmpty()) {
						images.add(imageToProcess.take());
					}

				} catch (InterruptedException e) {

				}

				// :cond_1
				//stopped = false;
				Iterator<ImageData> iterator = images.iterator();
				while (iterator.hasNext()) {
					ImageData imageData = iterator.next();
					if ("Poison Pill".equals(imageData.filename)) {
						stopped = true;
						break;
					}

					// :cond_6
					StringBuilder sb = new StringBuilder();
					sb.append("Processing file ").append(imageData.filename);
					Log.d(TAG, sb.toString());

					String filename = imageData.filename;
					int textureId = imageData.thumbnailTextureId;
					int width = photoSize.width;
					int height = photoSize.height;
					float[] rotation = imageData.rotation;
					LightCycleNative.AddImage(filename, textureId, width,
							height, rotation, extractFeatureAndThummbnail,
							useRealtimeAlignment);
				}

				// :cond_2
				if (useRealtimeAlignment && extractFeatureAndThummbnail) {
					LightCycleNative.ComputeAlignment();
				}

				// :cond_3
				processingImages = false;
			} else {
				break;
			}
		}
		
		// :cond_4
		Log.d(TAG,
				"Incremental aligner shutting down. Firing callback ...");
		if (doneCallback != null) {
			doneCallback.onCallback(null);
		}

		Log.d(TAG, "Incremental aligner thread shut down. Bye.");
		return;
	}

	public void shutdown(Callback<Void> callback) {}
	
	public void start(Size size) {
		photoSize = size;
		super.start();
		Log.d(TAG, "Aligner start");
	}
	
	private static class ImageData {
		public final String filename;
		public final float[] rotation;
		public final int thumbnailTextureId;

		ImageData(String filename, float[] rotation, int thumbnailTextureId) {
			this.filename = filename;
			this.rotation = rotation;
			this.thumbnailTextureId = thumbnailTextureId;
		}

	}
}
