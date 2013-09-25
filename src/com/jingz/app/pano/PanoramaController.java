package com.jingz.app.pano;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.camera.CameraScreenNail;
import com.android.camera.ComboPreferences;
import com.android.camera.RecordLocationPreference;
import com.android.camera.Util;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.ui.RotateImageView;
import com.android.gallery3d.ui.ShutterButton;
import com.android.gallery3d.ui.ShutterButton.OnShutterButtonListener;
import com.google.android.apps.lightcycle.LightCycleApp;
import com.google.android.apps.lightcycle.PanoramaModule;
import com.google.android.apps.lightcycle.camera.CameraApiProxy;
import com.google.android.apps.lightcycle.camera.CameraApiProxyAndroidImpl;
import com.google.android.apps.lightcycle.camera.CameraPreview;
import com.google.android.apps.lightcycle.camera.CameraUtility;
import com.google.android.apps.lightcycle.camera.NullSurfaceCameraPreview;
import com.google.android.apps.lightcycle.camera.TextureCameraPreview;
import com.google.android.apps.lightcycle.panorama.DeviceManager;
import com.google.android.apps.lightcycle.panorama.IncrementalAligner;
import com.google.android.apps.lightcycle.panorama.LightCycleNative;
import com.google.android.apps.lightcycle.panorama.LightCycleRenderer;
import com.google.android.apps.lightcycle.panorama.LightCycleView;
import com.google.android.apps.lightcycle.panorama.RenderedGui;
import com.google.android.apps.lightcycle.panorama.StitchingService;
import com.google.android.apps.lightcycle.panorama.StitchingServiceManager;
import com.google.android.apps.lightcycle.sensor.SensorReader;
import com.google.android.apps.lightcycle.storage.LocalSessionStorage;
import com.google.android.apps.lightcycle.storage.StorageManager;
import com.google.android.apps.lightcycle.storage.StorageManagerFactory;
import com.google.android.apps.lightcycle.util.Callback;
import com.google.android.apps.lightcycle.util.LG;
import com.google.android.apps.lightcycle.util.Size;
import com.google.android.apps.lightcycle.util.UiUtil;

//import android.hardware.Camera.Size;

public class PanoramaController {
	private static PanoramaController sInstance;
	private static Object sLock = new Object();

	private PanoActivity mActivity;
	private IncrementalAligner mAligner;
	private View mRootView;
	private LightCycleView mMainView;
	private ViewGroup mContainer;
	private boolean mIsPaused;
	private ShutterButton mShutterButton;
	private RotateImageView mUndoButton;
	private int mNumberOfImages;
	private CameraScreenNail mScreenNail;
	private SensorReader mSensorReader = new SensorReader();

	private LocalSessionStorage mLocalStorage;
	private StorageManager mStorageManager = StorageManagerFactory
			.getStorageManager();
	
	private Callback<Void> mCaptureListener = new Callback<Void>() {

		@Override
		public void onCallback(Void arg0) {

			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					adjustSwitcherAndSwipe();
				}

			});
		}
	};
	
	private Callback<Boolean> mUndoEnabledListener = new Callback<Boolean>() {

		@Override
		public void onCallback(Boolean enabled) {
			final boolean isEnabled = enabled;
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mUndoButton.setEnabled(isEnabled);
				}
			});
		}
	};
	
	private OnClickListener mUndoListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mNumberOfImages <= 0) {
				return;
			}
			
			mMainView.undoLastCapturedPhoto();
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					adjustSwitcherAndSwipe();
				}
			});
		}
	};
	
	private Callback<Boolean> mUndoVisibilityListener = new Callback<Boolean>() {

		@Override
		public void onCallback(Boolean visible) {
			final boolean v = visible;
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (v) {
						mUndoButton.setVisibility(View.VISIBLE);
					} else {
						mUndoButton.setVisibility(View.GONE);
					}
				}
			});
		}
	};

	LightCycleRenderer mRenderer;
	private boolean mStichingPaused;
	private SharedPreferences mPreferences;
	private boolean mFullScreen = true;
	private int mOrientation;
	private int mOrientationCompensation;
	private int mDisplayRotation;
	private Thread mPhotoSpherePreviewWriter;
	
	static {
		CameraApiProxy.setActiveProxy(new CameraApiProxyAndroidImpl());
		LightCycleApp.initLightCycleNative();
	}
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

	public void init(PanoActivity activity, View frame, boolean reuseScreenNail) {
		mActivity = activity;
		mRootView = frame;
		mPreferences = new ComboPreferences(mActivity);
		mActivity.getLayoutInflater().inflate(R.layout.photo_pano_module,
				(ViewGroup) mRootView);
		mContainer = (ViewGroup) mRootView.findViewById(R.id.camera_app_root);
		mScreenNail = ((CameraScreenNail) mActivity
				.createCameraScreenNail(true));
		int w = mRootView.getWidth();
		int h = mRootView.getHeight();
		if (Util.getDisplayRotation(mActivity) % 180 != 0) {
			int k = w;
			w = h;
			h = k;
		}
		mScreenNail.setSize(w, h);
	}

	public void onResume() {
		mIsPaused = false;
		initButtons();
		mScreenNail.acquireSurfaceTexture();
		mActivity.notifyScreenNailChanged();
		String str = Build.MODEL + " (" + Build.MANUFACTURER + ")";
		if (!DeviceManager.isDeviceSupported()) {
			displayErrorAndExit("Sorry, your device is not yet supported. Model : "
					+ str);
			return;
		}

		if (!LightCycleApp.getCameraUtility().hasBackFacingCamera()) {
			displayErrorAndExit("Sorry, your device does not have a back facing camera");
			return;
		}

		Process.setThreadPriority(-19);
		mRootView.setKeepScreenOn(true);
		// mStorageManager.init(mActivity);
		// mStorageManager.setPanoramaDestination(Storage.DIRECTORY);
		// setDisplayRotation();
		startCapture();
	}

	private void initButtons() {
		if (mUndoButton != null) {
			mContainer.removeView(mUndoButton);
			mActivity.getLayoutInflater().inflate(R.layout.photo_pano_undo,
					mContainer);
		}

		mUndoButton = (RotateImageView) mContainer
				.findViewById(R.id.undo_button);
		mUndoButton.enableFilter(false);
		mUndoButton.setOnClickListener(mUndoListener);
		mShutterButton = mActivity.getShutterButton();
		mShutterButton.setImageResource(R.drawable.btn_shutter_recording);
		mShutterButton
				.setOnShutterButtonListener(new OnShutterButtonListener() {

					@Override
					public void onShutterButtonFocus(boolean pressed) {
					}

					@Override
					public void onShutterButtonClick() {
						onDoneButtonPressed();
					}

				});
	}

	private void onDoneButtonPressed() {
		pauseCapture();
		mNumberOfImages = 0;
		adjustSwitcherAndSwipe();
		mScreenNail.animateCapture(mDisplayRotation);
		mPhotoSpherePreviewWriter = new Thread() {

			@Override
			public void run() {
				Bitmap bitmap = ((BitmapDrawable) mActivity.getResources()
						.getDrawable(R.drawable.ic_view_photosphere))
						.getBitmap();
				
				FileOutputStream out = null;
				
				try {
					
					out = new FileOutputStream(mLocalStorage.mosaicFilePath);
					bitmap.compress(CompressFormat.JPEG, 100, out);
					
					ContentValues contentValues = StitchingService
							.createImageContentValues(mLocalStorage.mosaicFilePath);
					contentValues.put("mimeType", "application/stitching-preview");
					Uri uri = mActivity.getContentResolver().insert(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							contentValues);
					mLocalStorage.imageUri = uri;
					
					StitchingServiceManager.getStitchingServiceManager(
							mActivity).onStitchingQueued(mLocalStorage);
				} catch (IOException e) {
					if (out == null) {
						Log.e("LightCycle", "Could not write image: "
								+ mLocalStorage.mosaicFilePath);
					}
				} finally {
					try {
						out.close();
					} catch (IOException e) {
						Log.e("LightCycle", "Could not close write image: "
								+ mLocalStorage.mosaicFilePath);
					}
				}
				
			}
			
		};
		
		mPhotoSpherePreviewWriter.start();
	}

	private void pauseCapture() {
		mMainView.stopCamera();
		mSensorReader.stop();
	}

	public void onPause() {
		mIsPaused = true;
		mShutterButton.setOnShutterButtonListener(null);
		if (mLocalStorage != null) {
			mStorageManager.addSessionData(this.mLocalStorage);
		}
		stopCapture();
		mSensorReader.stop();
		if ((mAligner != null) && (!mAligner.isInterrupted())) {
			mAligner.interrupt();
		}

		mRootView.setKeepScreenOn(false);
		mScreenNail.releaseSurfaceTexture();
	}

	private void displayErrorAndExit(String error) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setMessage(error).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mActivity.finish();
					}
				});
		builder.create().show();
	}

	private void startCapture() {
		mNumberOfImages = 0;
		CameraUtility cameraUtility = LightCycleApp.getCameraUtility();
		CameraPreview cameraPreview = null;
		if (Build.VERSION.SDK_INT < 11) {
			cameraPreview = new NullSurfaceCameraPreview(cameraUtility);
		} else {
			cameraPreview = new TextureCameraPreview(cameraUtility);
		}

		while (true) {
			boolean useRealtimeAlignment = PreferenceManager
					.getDefaultSharedPreferences(mActivity.getBaseContext())
					.getBoolean("useRealtimeAlignment", false);
			mAligner = new IncrementalAligner(useRealtimeAlignment);
			RenderedGui renderedGui = new RenderedGui();
			renderedGui.setShowOwnDoneButton(false);
			renderedGui.setShowOwnUndoButton(false);
			renderedGui.setUndoButtonStatusListener(mUndoEnabledListener);
			renderedGui
					.setUndoButtonVisibilityListener(mUndoVisibilityListener);

			try {
				mRenderer = new LightCycleRenderer(mActivity, renderedGui,
						useRealtimeAlignment);
			} catch (Exception e) {
				Log.e("LightCycle", "Error creating PanoRenderer.", e);
			}

			mSensorReader.start(mActivity);
			mLocalStorage = mStorageManager.getLocalSessionStorage();
			LG.d("storage : " + this.mLocalStorage.metadataFilePath + " "
					+ this.mLocalStorage.mosaicFilePath + " "
					+ this.mLocalStorage.orientationFilePath + " "
					+ this.mLocalStorage.sessionDir + " "
					+ this.mLocalStorage.sessionId + " "
					+ this.mLocalStorage.thumbnailFilePath);

			mMainView = new LightCycleView(mActivity, cameraPreview,
					mSensorReader, mLocalStorage, mAligner, mRenderer,
					mScreenNail.getSurfaceTexture());

			mMainView.setOnPhotoTakenCallback(new Callback<Void>() {

				@Override
				public void onCallback(Void arg0) {
					if (mStichingPaused) {
						return;
					}

					mStichingPaused = true;
					LocalBroadcastManager
							.getInstance(mActivity)
							.sendBroadcast(
									new Intent(
											"com.google.android.apps.lightcycle.panorama.PAUSE"));

				}
			});

			mMainView.setOnPhotoTakenCallback(mCaptureListener);

			if (Build.VERSION.SDK_INT < 11) {
				// ?
			}

			PreviewCallback previewCallback = mMainView.getPreviewCallback();
			Size size = cameraPreview.initCamera(previewCallback, 320, 240,
					true);
			mMainView.setFrameDimensions(size.width, size.height);
			mMainView.startCamera();
			applyPreferences();
			Camera.Size photoSize = cameraPreview.getPhotoSize();
			mAligner.start(new Size(photoSize.width, photoSize.height));

			int w = mScreenNail.getWidth();
			int h = mScreenNail.getHeight();
			((ViewGroup) mRootView).addView(mMainView, 0,
					new ViewGroup.LayoutParams(w, h));

			UiUtil.switchSystemUiToLightsOut(mActivity.getWindow());
			mUndoButton.setVisibility(View.GONE);

			adjustSwitcherAndSwipe();
		}

	}

	private void stopCapture() {
		mStichingPaused = false;
		LocalBroadcastManager
				.getInstance(mActivity)
				.sendBroadcast(
						new Intent(
								"com.google.android.apps.lightcycle.panorama.RESUME"));
		if (mMainView != null) {
			mMainView.onPause();
			((ViewGroup) mRootView).removeView(mMainView);
			mMainView.stopCamera();
		}
		
		mMainView = null;
		mNumberOfImages = 0;
		adjustSwitcherAndSwipe();
	}
	
	private void adjustSwitcherAndSwipe() {
		boolean swipingEnabled = true;
		boolean hasImage;
		int visibility;
		if (!mFullScreen) {
			return;
		}
		
		if (mNumberOfImages != 0) {
			hasImage = true;
		} else {
			hasImage = false;
		}
		
		if (hasImage) {
			swipingEnabled = false;
		}
		
		mActivity.setSwipingEnabled(swipingEnabled);
		
		if (hasImage) {
			//
			mActivity.hideSwitcher();
			mShutterButton.setVisibility(View.VISIBLE);
			mActivity.getOrientationManager().lockOrientation();
			return;
		}
		
		mActivity.showSwitcher();
		mShutterButton.setVisibility(View.GONE);
		mActivity.getOrientationManager().unlockOrientation();
	}

	private void applyPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mActivity.getBaseContext());
		boolean useFastShutter = sharedPreferences.getBoolean("useFastShutter",
				true);

		if (useFastShutter) {
			mMainView.getCameraPreview().setFastShutter(useFastShutter);
		}

		mSensorReader.enableEkf(sharedPreferences.getBoolean("useGyro", true));

		boolean displayLiveImage = sharedPreferences.getBoolean(
				"displayLiveImage", false);
		mMainView.setLiveImageDisplay(displayLiveImage);

		LightCycleNative.AllowFastMotion(sharedPreferences.getBoolean(
				"allowFastMotion", false));

		boolean enable = RecordLocationPreference.get(mPreferences,
				mActivity.getContentResolver());
		mMainView.setLocationProviderEnabled(enable);
	}
	
	public void onOrientationChanged(int orientation) {
		mOrientation = Util.roundOrientation(orientation, mOrientation);
		int orient = mOrientation + Util.getDisplayRotation(mActivity);
		if (mOrientationCompensation == orient) {
			return;
		}
		
		mOrientationCompensation = orient;
		if (mUndoButton != null) {
			mUndoButton.setOrientation(mOrientationCompensation, true);
		}
		setDisplayRotation();
	}

	private void setDisplayRotation() {
		mDisplayRotation = Util.getDisplayRotation(mActivity);
//		mActivity.getGLRoot().
		
	}

	public void onPreviewTextureCopied() {
	}

	public void onCaptureTextureCopied() {
		if (mMainView == null) {
			return;
		}
		
		mMainView.clearRendering();
		mAligner.shutdown(new Callback<Void>() {

			@Override
			public void onCallback(Void arg0) {
				if (mAligner.isRealtimeAlignmentEnabled()
						|| mAligner.isExtractFeaturesAndThumbnailEnabled()) {
					// :cond_0
					try {
						mPhotoSpherePreviewWriter.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// :goto_0
					LightCycleNative.PreviewStitch(mLocalStorage.mosaicFilePath);
					if (mLocalStorage.imageUri != null) {
						// :cond_2
//						GalleryApp app = (GalleryApp) mActivity.getApplication();
//						app.getStitchingProgressManager().;
						
						ContentValues contentValues = StitchingService.createImageContentValues(mLocalStorage.mosaicFilePath);
						contentValues.remove("mime_type");
						contentValues.remove("datetaken");
						mActivity.getContentResolver().update(mLocalStorage.imageUri, contentValues, null, null);
						
					} else {
						Log.w ("LightCycle", "Prepared preview doesn\'t exist");
					}
				}
				
				// :cond_1
				// :goto_1
				mActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						
					}
				});
			}
		});
	}
}
