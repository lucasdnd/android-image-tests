package com.lucasdnd.androidimagetests;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

	private SurfaceHolder mHolder;
	private Camera mCamera;
	private FrameLayout view;

	// Stuff
	Paint paint = new Paint(Color.RED);
	int size = 10; // try min 8
	private int[] pixels;
	Size previewSize;
	Random r = new Random();

	public CameraPreview(Context context, Camera camera, FrameLayout view) {

		super(context);

		mCamera = camera;
		this.view = view;

		// Add the Surface callback
		mHolder = getHolder();
		mHolder.addCallback(this);

		// So we can override the draw method
		this.setWillNotDraw(false);

	}

	private void startSurface(SurfaceHolder holder) throws Exception {

		mCamera.setPreviewDisplay(holder);
		mCamera.setDisplayOrientation(90);

		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes(); // use this to check which preview sizes you device allows

		parameters.setPreviewSize(previewSizes.get(3).width, previewSizes.get(3).height);
		mCamera.setParameters(parameters);

		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		previewSize = mCamera.getParameters().getPreviewSize();
		pixels = new int[previewSize.width * previewSize.height];
	}

	public void surfaceCreated(SurfaceHolder holder) {

		// The Surface has been created, now tell the camera where to draw the preview.
		try {
			startSurface(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

		if (mHolder.getSurface() == null) {
			return;
		}

		// Stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Start preview
		try {
			startSurface(holder);
		} catch (Exception e) {
			e.printStackTrace();
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

		// System.out.println("canvas width = " + canvas.getWidth());
		// System.out.println("canvas height = " + canvas.getHeight());
		// System.out.println("preview width = " + previewSize.width);
		// System.out.println("preview height = " + previewSize.height);

		canvas.translate(0f, canvas.getHeight() / 2f - previewSize.height / 2f);
		canvas.rotate(90f, previewSize.width / 2f, previewSize.height / 2f);
		canvas.scale(1.7f, 1.7f, previewSize.width / 2f, previewSize.height / 2f);

		for (int i = 0; i < previewSize.width; i += size) {
			for (int j = 0; j < previewSize.height; j += size) {
				paint.setColor(pixels[previewSize.width * j + i]);
				canvas.drawRect(i, j, i + size, j + size, paint);
			}
		}
	}
}
