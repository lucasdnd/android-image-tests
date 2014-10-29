package com.lucasdnd.androidimagetests;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

	private final String TAG = "Exception";
	private SurfaceHolder mHolder;
	private Camera mCamera;
	private FrameLayout view;

	// Stuff
	Paint paint = new Paint(Color.RED);
	int size = 10;
	private int[] pixels;
	Size previewSize;
	Random r = new Random();

	public CameraPreview(Context context, Camera camera, FrameLayout view) {
		super(context);

		mCamera = camera;
		
		this.view = view;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// So we can override the draw method
		this.setWillNotDraw(false);

	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setDisplayOrientation(90);
			
			Camera.Parameters parameters = mCamera.getParameters();
			
			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes(); // use this to check which preview sizes you device allows
			parameters.setPreviewSize(800, 480);
			mCamera.setParameters(parameters);
			
			mCamera.startPreview();
			mCamera.setPreviewCallback(this);
			
			previewSize = mCamera.getParameters().getPreviewSize();
			pixels = new int[previewSize.width * previewSize.height];
		
		} catch (IOException e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
			mCamera.setPreviewCallback(this);
			mCamera.getParameters().setPreviewSize(800, 480);
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		decodeYUV420SP(pixels, data, previewSize.width, previewSize.height);
		this.invalidate();
	}

	void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		
//		System.out.println("canvas width = " + canvas.getWidth());
//		System.out.println("canvas height = " + canvas.getHeight());
//		System.out.println("preview width = " + previewSize.width);
//		System.out.println("preview height = " + previewSize.height);
		
		canvas.rotate(90f, 400f, 240f);
		canvas.translate(160f, -80f);
		canvas.scale(1.5f, 1.5f);
		
		for (int i = 0; i < previewSize.width; i += size) {
			for(int j = 0; j < previewSize.height; j += size) {
				int k = 800 * j + i;
				if (k < pixels.length) {
					paint.setColor(pixels[k]);
					canvas.drawRect(i, j, i + size, j + size, paint);
				}
			}
		}
	}
}
