package com.jingz.app.pano;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.camera.CameraScreenNail;
import com.android.camera.StaticBitmapScreenNail;
import com.android.camera.Util;
import com.android.gallery3d.app.AppBridge;
import com.android.gallery3d.app.OrientationManager;
import com.android.gallery3d.common.ApiHelper;
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
	private MyOrientationEventListener mOrientationListener;
	// The degrees of the device rotated clockwise from its natural orientation.
    private int mLastRawOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	private int mOrientation;
	public int mOrientationCompensation;
	private boolean mSecureCamera = false;
	private MyAppBridge mAppBridge;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onSuperCreate();
		setContentView(R.layout.pano_activity);
		mFrame = (FrameLayout) findViewById(R.id.main_content);
		init();
		mPanoController = PanoramaController.getInstance();
		mPanoController.init(this, mFrame, false);
		mOrientationListener = new MyOrientationEventListener(this);
	}
	
	private void onSuperCreate() {
		mOrientationManager = new OrientationManager(this);
		getWindow().setBackgroundDrawable(null);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void init() {
		mControlsBackground = findViewById(R.id.controls);
		mShutterSwitcher = findViewById(R.id.camera_shutter_switcher);
		mShutter = (ShutterButton) findViewById(R.id.shutter_button);
	}
	
	@Override
	protected void onResume() {
		mPaused = false;
		mOrientationListener.enable();
		super.onResume();
		mOrientationManager.resume();
		mPanoController.onResume();
	}
	
	@Override
	protected void onPause() {
		mPaused = true;
		mOrientationListener.disable();
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
        Bundle bundle = new Bundle();
        String path = null;
        
        if (getPictures) {
        	if (mSecureCamera) {
        		//str0 = "/secure/all" + sSecureAlbumId;
        	} else {
        		// :cond_1
        		path = "/local/all"
						+ getBucketId(Environment.getExternalStorageDirectory()
								.toString() + "/DCIM/Camera");
        	}
        } else {
        	// :cond_2
        	path = "/local/all/0";
        }
        
        // :goto_0
        bundle.putString("media-set-path", path);
        bundle.putString("media-item-path", path);
        bundle.putBoolean("show-when-locked", mSecureCamera);
        
        if (mAppBridge != null) {
        	mCameraScreenNail.recycle();
        }
        // :cond_0
        mAppBridge = new MyAppBridge();
        bundle.putParcelable("app-bridge", mAppBridge);
//        int stateCount = getStateManager().getStateCount();
//        if (stateCount == 0) {
//        	getStateManager().startState(PhotoPage.class, bundle);
//        } else {
//        	// :cond_3
//        	StateManager stateManager = getStateManager();
//        	ActivityState topState = stateManager.getTopState();
//        	stateManager.switchState(topState, PhotoPage.class, bundle);
//        }
        
        // :goto_1
        mCameraScreenNail = mAppBridge.getCameraScreenNail();
        return mCameraScreenNail;
    }
    
//    private StateManager getStateManager() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public static int getBucketId(String path) {
        return path.toLowerCase().hashCode();
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
    
    private class MyOrientationEventListener
    		extends OrientationEventListener {
    	public MyOrientationEventListener(Context context) {
    		super(context);
    	}

		@Override
		public void onOrientationChanged(int orientation) {
		    // We keep the last known orientation. So if the user first orient
		    // the camera then point the camera to floor or sky, we still have
		    // the correct orientation.
		    if (orientation == ORIENTATION_UNKNOWN) {
		    	return;
		    }
		    mOrientation = Util.roundOrientation(orientation, mOrientation);
		    int orient = (mOrientation + Util.getDisplayRotation(PanoActivity.this)) % 360;
		    if (mOrientationCompensation != orient) {
		    	mOrientationCompensation = orient;
		    }
		    
		    mPanoController.onOrientationChanged(orientation);
		}
    }
    
	class MyAppBridge extends AppBridge implements CameraScreenNail.Listener {
		private ScreenNail mCameraScreenNail;
		private Server mServer;
		
		@Override
		public ScreenNail attachScreenNail() {
			if (mCameraScreenNail == null) {
				if (ApiHelper.HAS_SURFACE_TEXTURE) {
					mCameraScreenNail = new CameraScreenNail(this);
				} else {
					Bitmap b = BitmapFactory.decodeResource(getResources(),
							R.drawable.wallpaper_picker_preview);
					mCameraScreenNail = new StaticBitmapScreenNail(b);
				}
			}
			
			return mCameraScreenNail;
		}
		
		@Override
		public void detachScreenNail() {
			mCameraScreenNail = null;
		}
		
		public ScreenNail getCameraScreenNail() {
            return mCameraScreenNail;
        }
		
		@Override
		public boolean onSingleTapUp(int x, int y) {
			return PanoActivity.this.onSingleTapUp(x, y);
		}

		@Override
		public void onFullScreenChanged(boolean full) {
			PanoActivity.this.onFullScreenChanged(full);
		}
		
		@Override
		public void requestRender() {
			//getGLRoot().requestRenderForced();
		}

		@Override
		public void onPreviewTextureCopied() {
			PanoActivity.this.onPreviewTextureCopied();
			
		}

		@Override
		public void onCaptureTextureCopied() {
			PanoActivity.this.onCaptureTextureCopied();
		}

		@Override
		public void setServer(Server server) {
			mServer = server;
		}

		@Override
		public boolean isPanorama() {
			return PanoActivity.this.isPanoramaActivity();
		}

		@Override
		public boolean isStaticCamera() {
			return !ApiHelper.HAS_SURFACE_TEXTURE;
		}


	}

	public void onPreviewTextureCopied() {
		mPanoController.onPreviewTextureCopied();
	}
	
	public void onFullScreenChanged(boolean full) {
	}

	public boolean onSingleTapUp(int x, int y) {
		return true;
	}

	public void onCaptureTextureCopied() {
		mPanoController.onCaptureTextureCopied();
    }
	
	public boolean isPanoramaActivity() {
        return false;
    }
}
