package com.jingz.app.pano;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.jingz.app.pano.util.Callback;
import com.jingz.app.pano.util.Size;


public class IncrementalAligner extends Thread {
	public static final String TAG = IncrementalAligner.class.getSimpleName();
	
	private Callback<Void> doneCallback = null;
	boolean extractFeatureAndThummbnail = true;
	private final ArrayBlockingQueue<ImageData> imageToProcess = 
			new ArrayBlockingQueue<ImageData>(20);
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
		ArrayList<ImageData> images;
		if (!isInterrupted()) {
			images = new ArrayList<ImageData>();
		}
		
		ImageData image1;
		
		try {
			ImageData imageData = imageToProcess.take();
			processingImages = true;
			images.add(imageData);
			
			if (imageToProcess.isEmpty()) {
				
			}
			
			
		} catch (InterruptedException e) {
		}
		
		
		super.run();
	}



	private static class ImageData {
		public final String filenameString;
		public final float[] rotation;
		public final int thumbnailTextureId;
		
		ImageData(String filenameString, float[] rotation,
				int thumbnailTextureId) {
			this.filenameString = filenameString;
			this.rotation = rotation;
			this.thumbnailTextureId = thumbnailTextureId;
		}
		
		
	}
}
