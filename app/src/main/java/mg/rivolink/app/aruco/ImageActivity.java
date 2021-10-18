package mg.rivolink.app.aruco;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.net.Uri;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.widget.Toast;
import android.widget.ImageView;

import java.io.InputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import org.opencv.android.Utils;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.CvType;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.aruco.DetectorParameters;

import mg.rivolink.app.aruco.utils.CameraParameters;

public class ImageActivity extends Activity {

	private static Mat cameraMatrix;
	private static Mat distCoeffs;

	private ImageView imageView;
	private Bitmap originalBMP;

	private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this){
		@Override
		public void onManagerConnected(int status){
			if(status == LoaderCallbackInterface.SUCCESS){
				String message = "";

				if(loadCameraParams()){
					message = getString(R.string.info_detecting_markers);
					detectMarkersAsync();
				}
				else {
					message = getString(R.string.error_camera_params);
				}

				Toast.makeText(ImageActivity.this,  message,  Toast.LENGTH_SHORT).show();
			}
			else {
				super.onManagerConnected(status);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_layout);

		imageView = findViewById(R.id.image_view);

		if(getIntent().getData() != null) try {
			Uri uri=getIntent().getData();
			InputStream is = getContentResolver().openInputStream(uri);
			imageView.setImageBitmap(originalBMP = BitmapFactory.decodeStream(is));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume(){
		super.onResume();

		if(OpenCVLoader.initDebug())
			loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		else
			Toast.makeText(this, getString(R.string.error_native_lib), Toast.LENGTH_LONG).show();
	}

	private boolean loadCameraParams(){
		cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1);
		distCoeffs = Mat.zeros(5, 1, CvType.CV_64FC1);
		return CameraParameters.tryLoad(this, cameraMatrix, distCoeffs);
	}

	private void detectMarkersAsync(){
		new Thread(){

			Bitmap bitmap;

			@Override
			public void run() {
				bitmap = detectMarkers(originalBMP);

				ImageActivity.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(bitmap != null)
							imageView.setImageBitmap(bitmap);
						else
							Toast.makeText(ImageActivity.this, getString(R.string.info_no_marker), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}.start();
	}

	private static Bitmap detectMarkers(Bitmap original){
		Bitmap bitmap = null;

		Mat rgba = new Mat();
		Utils.bitmapToMat(original, rgba);

		Mat rgb = new Mat();
		Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB);

		Mat gray = new Mat();
		Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY);

		MatOfInt ids = new MatOfInt();
		List<Mat> corners = new LinkedList<>();
		Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
		DetectorParameters parameters = DetectorParameters.create();

		Aruco.detectMarkers(gray, dictionary, corners, ids, parameters);

		if(corners.size() > 0){
			Aruco.drawDetectedMarkers(rgb, corners, ids);

			Mat rvecs = new Mat();
			Mat tvecs = new Mat();

			Aruco.estimatePoseSingleMarkers(corners, 0.04f, cameraMatrix, distCoeffs, rvecs, tvecs);

			List<Point3> corners4 = new ArrayList<>(4);
			corners4.add(new Point3(-0.02f,0.02f,0));		// Top-Left
			corners4.add(new Point3(0.02f,0.02f,0));		// Top-Right
			corners4.add(new Point3(-0.02f,-0.02f,0));		// Bottom-Left
			corners4.add(new Point3(0.02f,-0.02f,0));		// Bottom-Right

			MatOfPoint3f mcorners = new MatOfPoint3f();
			mcorners.fromList(corners4);

			distCoeffs = new MatOfDouble(distCoeffs);

			for(int i = 0; i < ids.toArray().length; i++){
				Aruco.drawAxis(rgb, cameraMatrix, distCoeffs, rvecs.row(i), tvecs.row(i), 0.02f);

				MatOfPoint2f projected = new MatOfPoint2f();
				Calib3d.projectPoints(mcorners, rvecs.row(i), tvecs.row(i), cameraMatrix, (MatOfDouble)distCoeffs, projected);

				Point[] points = projected.toArray();
				projected.release();

				if(points != null){
					for(Point point:points){
						Imgproc.circle(rgb, point, 10, new Scalar(0, 255, 0, 150), 4);
					}
				}
			}

			mcorners.release();

			rvecs.release();
			tvecs.release();

			bitmap=Bitmap.createBitmap(rgb.width(), rgb.height(), Bitmap.Config.RGB_565);
			Utils.matToBitmap(rgb, bitmap);
		}

		rgba.release();
		rgb.release();
		gray.release();
		ids.release();

		corners.clear();

		return bitmap;
	}

}
