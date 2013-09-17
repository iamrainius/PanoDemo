package com.jingz.app.pano;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

//import com.android.debug.hv.ViewServer;
import com.jingz.app.pano.ui.ShutterButton;

public class PanoActivity extends Activity {
	
	private FrameLayout mFrame;
	private PanoramaController mPanoController;
	private View mControlsBackground;
	private View mShutterSwitcher;
	private ShutterButton mShutter;
	private boolean mPaused;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pano_activity);
		
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
		//ViewServer.get(this).setFocusedWindow(this);
		
		mPanoController.onResume();
	}
	
	@Override
	protected void onPause() {
		mPaused = true;
		//mOrientationListener.disable();
		mPanoController.onPause();
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
}
