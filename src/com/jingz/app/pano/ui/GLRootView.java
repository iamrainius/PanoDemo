package com.jingz.app.pano.ui;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Process;
import android.util.AttributeSet;
import android.view.View;

import com.jingz.app.pano.R;
import com.jingz.app.pano.util.ApiHelper;
import com.jingz.app.pano.util.Utils;

//public class GLRootView extends View {
public class GLRootView extends GLSurfaceView implements
		GLSurfaceView.Renderer, GLRoot {

	private static final String TAG = "GLRootView";

	private static final int FLAG_INITIALIZED = 1;
	private static final int FLAG_NEED_LAYOUT = 2;

	private int mFlags;
	private final ReentrantLock mRenderLock = new ReentrantLock();

	private final GalleryEGLConfigChooser mEglConfigChooser = new GalleryEGLConfigChooser();

	private GL11 mGL;

	private GLCanvas mCanvas;

	private boolean mFreeze;
	private final Condition mFreezeCondition =
            mRenderLock.newCondition();

	private boolean mFirstDraw = true;

	private boolean mRenderRequested;

	public GLRootView(Context context) {
		this(context, null);
	}

	public GLRootView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mFlags |= FLAG_INITIALIZED;
		setBackgroundDrawable(null);
		setEGLConfigChooser(mEglConfigChooser);
		setRenderer(this);
		if (ApiHelper.USE_888_PIXEL_FORMAT) {
			getHolder().setFormat(PixelFormat.RGB_888);
		} else {
			getHolder().setFormat(PixelFormat.RGB_565);
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl1, EGLConfig config) {
		GL11 gl = (GL11) gl1;
		mRenderLock.lock();
		try {
			mGL = gl;
			mCanvas = new GLCanvasImpl(gl);
			BasicTexture.invalidateAllTextures();
		} finally {
			mRenderLock.unlock();
		}
		
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onSurfaceChanged(GL10 gl1, int width, int height) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
//		GalleryUtils.setRenderThread();
		GL11 gl = (GL11) gl1;
	    Utils.assertTrue(mGL == gl);
	    mCanvas.setSize(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		AnimationTime.update();
		mRenderLock.lock();
		while (mFreeze) {
            mFreezeCondition.awaitUninterruptibly();
        }
		
		try {
			onDrawFrameLocked(gl);
		} finally {
			mRenderLock.unlock();
		}
		
		// We put a black cover View in front of the SurfaceView and hide it
        // after the first draw. This prevents the SurfaceView being transparent
        // before the first draw.
        if (mFirstDraw) {
            mFirstDraw = false;
            post(new Runnable() {
                    @Override
                    public void run() {
                        View root = getRootView();
                        View cover = root.findViewById(R.id.gl_root_cover);
                        cover.setVisibility(GONE);
                    }
                });
        }
        
        
	}

	private void onDrawFrameLocked(GL10 gl) {
		// release the unbound textures and deleted buffers.
		mCanvas.deleteRecycledResources();
		// reset texture upload limit
        //UploadedTexture.resetUploadLimit();
		mRenderRequested = false;
	}

	@Override
	public void addOnGLIdleListener(OnGLIdleListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerLaunchedAnimation(CanvasAnimation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestRenderForced() {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestLayoutContentPane() {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockRenderThread() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockRenderThread() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentPane(GLView content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOrientationSource(OrientationSource source) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDisplayRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCompensation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Matrix getCompensationMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void freeze() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unfreeze() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLightsOutMode(boolean enabled) {
		// TODO Auto-generated method stub

	}

}
