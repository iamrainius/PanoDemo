package com.jingz.app.pano;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.android.gallery3d.app.OrientationManager;
import com.android.gallery3d.ui.ScreenNail;
//import com.android.debug.hv.ViewServer;
import com.android.gallery3d.ui.ShutterButton;

public class PanoActivity extends Activity {
	
	private FrameLayout mFrame;
	private PanoramaController mPanoController;
	private View mControlsBackground;
	private View mShutterSwitcher;
	private ShutterButton mShutter;
	private boolean mPaused;
	private View mCameraAppView;
	//private MyAppBridge mAppBridge;
	private ScreenNail mCameraScreenNail;
	private OrientationManager mOrientationManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pano_activity);
		
		mOrientationManager = new OrientationManager(this);
		//ViewServer.get(this).addWindow(this);
		
		mFrame = (FrameLayout) findViewById(R.id.main_content);
		init();
		mPanoController = PanoramaController.getInstance();
		mPanoController.init(this, mFrame, true);
		//mOrientationListener = new MyOrientationEventListener(this);
	}

	private void init() {
		mControlsBackground = findViewById(R.id.controls);
		mShutterSwitcher = findViewById(R.id.camera_shutter_switcher);
		mShutter = (ShutterButton) findViewById(R.id.shutter_button);
	}
	
	@Override
	protected void onResume() {
		mPaused = false;
		//mOrientationListener.enable();
		super.onResume();
		mOrientationManager.resume();
		//ViewServer.get(this).setFocusedWindow(this);
		
		mPanoController.onResume();
	}
	
	@Override
	protected void onPause() {
		mPaused = true;
		//mOrientationListener.disable();
		mPanoController.onPause();
		mOrientationManager.pause();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		// getStateManager().clearTasks();
	}
	
	@Override
    public void onDestroy() {
    	super.onDestroy();
    	//ViewServer.get(this).removeWindow(this);
    }

	public ShutterButton getShutterButton() {
		return mShutter;
	}
	
	// Call this after setContentView.
    public ScreenNail createCameraScreenNail(boolean getPictures) {
        mCameraAppView = findViewById(R.id.camera_app_root);
//        Bundle data = new Bundle();
//        String path;
//        if (getPictures) {
//            if (mSecureCamera) {
//                path = "/secure/all/" + sSecureAlbumId;
//            } else {
//                path = "/local/all/" + MediaSetUtils.CAMERA_BUCKET_ID;
//            }
//        } else {
//            path = "/local/all/0"; // Use 0 so gallery does not show anything.
//        }
//        data.putString(PhotoPage.KEY_MEDIA_SET_PATH, path);
//        data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, path);
//        data.putBoolean(PhotoPage.KEY_SHOW_WHEN_LOCKED, mSecureCamera);
//
//        // Send an AppBridge to gallery to enable the camera preview.
//        if (mAppBridge != null) {
//            mCameraScreenNail.recycle();
//        }
//        mAppBridge = new MyAppBridge();
//        data.putParcelable(PhotoPage.KEY_APP_BRIDGE, mAppBridge);
//        if (getStateManager().getStateCount() == 0) {
//            getStateManager().startState(PhotoPage.class, data);
//        } else {
//            getStateManager().switchState(getStateManager().getTopState(),
//                    PhotoPage.class, data);
//        }
//        mCameraScreenNail = mAppBridge.getCameraScreenNail();
        return mCameraScreenNail;
    }
    
    public void notifyScreenNailChanged() {
        //mAppBridge.notifyScreenNailChanged();
    }
    
    public void setSwipingEnabled(boolean enabled) {
        //mAppBridge.setSwipingEnabled(enabled);
    }
    
    public void hideSwitcher() {
//        mSwitcher.closePopup();
//        mSwitcher.setVisibility(View.INVISIBLE);
    }
    
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }
    
    public void showSwitcher() {
//        if (mCurrentModule.needsSwitcher()) {
//            mSwitcher.setVisibility(View.VISIBLE);
//        }
    }
}
