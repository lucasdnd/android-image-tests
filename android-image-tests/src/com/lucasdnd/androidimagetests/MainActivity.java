package com.lucasdnd.androidimagetests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	private Camera mCamera;
	private CameraPreview mPreview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Create an instance of Camera
		mCamera = getCameraInstance();

		// Create our Preview view and set it as the content of our activity.
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
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
				v1.setDrawingCacheEnabled(false);

				OutputStream fout = null;
				
				try {
					File imageFile = File.createTempFile("pixelated_" + System.currentTimeMillis(), ".jpg", storageDir);
				
					fout = new FileOutputStream(imageFile);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
					fout.flush();
					fout.close();
					
				    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				    File f = new File(imageFile.getPath());
				    Uri contentUri = Uri.fromFile(f);
				    mediaScanIntent.setData(contentUri);
				    sendBroadcast(mediaScanIntent);
					
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
}
