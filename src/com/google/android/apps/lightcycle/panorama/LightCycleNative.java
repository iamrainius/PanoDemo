package com.google.android.apps.lightcycle.panorama;

import java.util.HashMap;
import java.util.Map;

import com.jingz.app.pano.ui.LightCycleView;

public class LightCycleNative {

	private static NativeUpdatePhotoRenderingCallback nativeTransformsCallback;
	private static NativeProgressCallback progressCallback;
	private static Map<Integer, LightCycleView.ProgressCallback> progressCallbacks;

	static {
		System.loadLibrary("lightcycle");
		progressCallbacks = new HashMap<Integer, LightCycleView.ProgressCallback>();
		progressCallback = new NativeProgressCallback();
		nativeTransformsCallback = new NativeUpdatePhotoRenderingCallback();
	}

	public static native void AddImage(String paramString, int paramInt1,
			int paramInt2, int paramInt3, float[] paramArrayOfFloat,
			boolean paramBoolean1, boolean paramBoolean2);

	public static native void ComputeAlignment();

	private static class NativeUpdatePhotoRenderingCallback {

	}

	private static class NativeProgressCallback {

	}
}
