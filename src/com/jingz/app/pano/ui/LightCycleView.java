package com.jingz.app.pano.ui;


import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class LightCycleView extends GLSurfaceView {

	

	private Context mContext;

	public LightCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LightCycleView(Activity activity,
			CameraPreview cameraPreview, SensorReader sensorReader,
			LocalSessionStorage localSessionStorage,
			IncrementalAligner aligner,
			LightCycleRenderer renderer,
			SurfaceTexture surfaceTexture) {
		super(activity);
		mContext = activity;
	}

	public interface ProgressCallback {
		void progress(int progress);
	}
}
