package com.lucasdnd.androidimagetests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private Camera mCamera;
	private CameraPreview mPreview;
	private FrameLayout overlayView;
	private boolean isLandscape;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Orientation detector
		SensorManager sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(new SensorEventListener() {
			int orientation = -1;

			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.values[1] < 6.5 && event.values[1] > -6.5) {
					if (orientation != 1) {
						isLandscape = true;
					}
					orientation = 1;
				} else {
					if (orientation != 0) {
						isLandscape = false;
					}
					orientation = 0;
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}

		}, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		final FrameLayout overlayView = (FrameLayout) findViewById(R.id.flash);

		mPreview = new CameraPreview(this, mCamera, preview);
		preview.addView(mPreview);

		preview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

				// Create a Bitmap of the screen
				Bitmap bitmap;
				View v1 = getWindow().getDecorView().getRootView();
				v1.setDrawingCacheEnabled(true);
				bitmap = Bitmap.createBitmap(v1.getDrawingCache());

				// Rotate if necessary
				if (isLandscape) {
					Matrix matrix = new Matrix();
					matrix.postRotate(90f);
					Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
					bitmap = rotatedBitmap;
				}

				v1.setDrawingCacheEnabled(false);

				// Save!
				try {

					OutputStream fout = null;
					File file = File.createTempFile("pixelated_" + System.currentTimeMillis(), ".jpg", storageDir);

					fout = new FileOutputStream(file);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
					fout.flush();
					fout.close();

					Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					File f = new File(file.getPath());
					Uri contentUri = Uri.fromFile(f);
					mediaScanIntent.setData(contentUri);
					sendBroadcast(mediaScanIntent);

					// Take picture animation
					ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(overlayView, "backgroundColor", new ArgbEvaluator(), 0xFFFFFFFF,
							0x00FFFFFF);
					backgroundColorAnimator.setDuration(250);
					backgroundColorAnimator.start();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}
}
