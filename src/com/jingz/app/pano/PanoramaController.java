package com.jingz.app.pano;

import com.jingz.app.pano.ui.RotateImageView;
import com.jingz.app.pano.ui.ShutterButton;
import com.jingz.app.pano.ui.ShutterButton.OnShutterButtonListener;
import com.jingz.app.pano.util.Util;

import android.os.Build;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;

public class PanoramaController {
	private static PanoramaController sInstance;
	private static Object sLock = new Object();
	
	private PanoActivity mActivity;
	private View mRootView;
	private ViewGroup mContainer;
	private boolean mIsPaused;
	private ShutterButton mShutterButton;
	private RotateImageView mUndoButton;
	private int mNumberOfImages;
	
	private PanoramaController() {
		
	}
	
	public static PanoramaController getInstance() {
		synchronized (sLock) {
			if (sInstance == null) {
				sInstance = new PanoramaController();
			}
			
			return sInstance;
		}
	}
	
	public void init(PanoActivity activity, View frame,
			boolean reuseScreenNail) {
		mActivity = activity;
		mRootView = frame;
		//mPreferences = new ComboPreferences(mActivity);
		mActivity.getLayoutInflater().inflate(R.layout.photo_pano_module,
				(ViewGroup) mRootView);
		mContainer = (ViewGroup) mRootView.findViewById(R.id.camera_app_root);
		// mScreenNail =
		// ((CameraScreenNail)this.mActivity.createCameraScreenNail(true));
		int w = mRootView.getWidth();
		int h = mRootView.getHeight();
		if (Util.getDisplayRotation(mActivity) % 180 != 0) {
			int k = w;
			w = h;
			h = k;
		}
		//mScreenNail.setSize(w, h);
	}
	
	public void onResume() {
		mIsPaused = false;
		initButtons();
		//mScreenNail.acquireSurfaceTexture();
		//mActivity.notifyScreenNailChanged();
		String str = Build.MODEL + " (" + Build.MANUFACTURER + ")";
//		if (!DeviceManager.isDeviceSupported()) {
//			 displayErrorAndExit("Sorry, your device is not yet supported. Model : " + str);
//			return;
//		}
		
//		if (!LightCycleApp.getCameraUtility().hasBackFacingCamera())
//	    {
//	      displayErrorAndExit("Sorry, your device does not have a back facing camera");
//	      return;
//	    }
		
		Process.setThreadPriority(-19);
		mRootView.setKeepScreenOn(true);
		//mStorageManager.init(mActivity);
		//mStorageManager.setPanoramaDestination(Storage.DIRECTORY);
		//setDisplayRotation();
		startCapture();
	}
	
	private void startCapture() {
		
	}

	private void initButtons() {
		if (mUndoButton != null) {
			mContainer.removeView(mUndoButton);
			mActivity.getLayoutInflater().inflate(R.layout.photo_pano_undo, mContainer);
		}
		
		mUndoButton = (RotateImageView) mContainer.findViewById(R.id.undo_button);
		mUndoButton.enableFilter(false);
		//mUndoButton.setOnClickListener(mUndoListener);
		mShutterButton = mActivity.getShutterButton();
		mShutterButton.setImageResource(R.drawable.btn_shutter_recording);
		mShutterButton.setOnShutterButtonListener(new OnShutterButtonListener() {
			
			@Override
			public void onShutterButtonFocus(boolean pressed) {}
			
			@Override
			public void onShutterButtonClick() {
				onDoneButtonPressed();
			}

		});
	}

	private void onDoneButtonPressed() {
		
	}
	
	public void onPause() {
		mIsPaused = true;
		mShutterButton.setOnShutterButtonListener(null);
//		if (mLocalStorage != null) {
//			mStorageManager.addSessionData(this.mLocalStorage);
//		}
//		stopCapture();
//		mSensorReader.stop();
//		if ((mAligner != null) && (!mAligner.isInterrupted())) {
//			mAligner.interrupt();
//		}

		mRootView.setKeepScreenOn(false);
		//mScreenNail.releaseSurfaceTexture();
	}
}
