package mg.rivolink.app.aruco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;

import android.os.Bundle;

import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mg.rivolink.app.aruco.utils.CameraParameters;
import mg.rivolink.app.aruco.view.PortraitCameraLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import android.view.*;
import min3d.vos.*;
import min3d.core.*;
import min3d.objectPrimitives.*;
import android.widget.*;
import android.graphics.*;
import org.opencv.core.*;

public class MainActivity extends Min3dActivity implements CvCameraViewListener2 {

	public static final float SIZE = 0.04f;

	private Mat cameraMatrix;
	private MatOfDouble distCoeffs;

	private Mat rgb;
	private Mat gray;

	private Mat rvecs;
	private Mat tvecs;

	private MatOfInt ids;
	private List<Mat> corners;
	private Dictionary dictionary;
	private DetectorParameters parameters;

	private CameraBridgeViewBase camera;

	private Object3dContainer cube;

	private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
			if(status == LoaderCallbackInterface.SUCCESS){
				Activity activity = MainActivity.this;

				cameraMatrix = Mat.eye(3, 3, CvType.CV_64FC1);
				distCoeffs = new MatOfDouble(Mat.zeros(5, 1, CvType.CV_64FC1));

				if(CameraParameters.fileExists(activity)){
					CameraParameters.tryLoad(activity, cameraMatrix, distCoeffs);
				}
				else {
					CameraParameters.selectFile(activity);
				}

				camera.enableView();
			}
			else {
				super.onManagerConnected(status);
			}
        }
    };

	@Override
	protected void onCreateSetContentView(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_layout);

        camera = ((PortraitCameraLayout)findViewById(R.id.camera_layout)).getCamera();
        camera.setVisibility(View.VISIBLE);
        camera.setCvCameraViewListener(this);

		LinearLayout surface_layout = (LinearLayout)findViewById(R.id.surface_layout);
		surface_layout.addView(_glSurfaceView);
	}

	@Override
	protected void glSurfaceViewConfig(){
		_glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		_glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}	

	@Override
	public void initScene(){
		cube = new Box(1, 1, 1);
		cube.colorMaterialEnabled(true);

		scene.addChild(cube);
		scene.lights().add(new Light());
		scene.backgroundColor().setAll(0x00000000);
	}

	@Override
	public void updateScene(){
		/*if(point != null){
		 cube.position().set
		 //cube.position().y = y;
		 }
		 cube.rotation().
		 scene.camera().target.setAll(0.5f, 0.5f, 0.5f);
		 */
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		CameraParameters.onActivityResult(this, requestCode, resultCode, data, cameraMatrix, distCoeffs);
	}

	@Override
    public void onResume(){
        super.onResume();

		if(OpenCVLoader.initDebug())
			loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		else
			Toast.makeText(this, getString(R.string.error_native_lib), Toast.LENGTH_LONG).show();
    }

	@Override
    public void onPause(){
        super.onPause();

        if(camera != null)
            camera.disableView();
    }

	@Override
    public void onDestroy(){
        super.onDestroy();

        if (camera != null)
            camera.disableView();
    }

	@Override
	public void onCameraViewStarted(int width, int height){
		rgb = new Mat();
		corners = new LinkedList<>();
		parameters = DetectorParameters.create();
		dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
		if(!CameraParameters.isLoaded()){
			return inputFrame.rgba();
		}

		Imgproc.cvtColor(inputFrame.rgba(), rgb, Imgproc.COLOR_RGBA2RGB);
		gray = inputFrame.gray();

		ids = new MatOfInt();
		corners.clear();

		Aruco.detectMarkers(gray, dictionary, corners, ids, parameters);

		if(corners.size()>0){
			Aruco.drawDetectedMarkers(rgb, corners, ids);

			rvecs = new Mat();
			tvecs = new Mat();

			Aruco.estimatePoseSingleMarkers(corners, SIZE, cameraMatrix, distCoeffs, rvecs, tvecs);
			for(int i = 0;i<ids.toArray().length;i++){
				Mat rvec = rvecs.row(i);
				Mat tvec = tvecs.row(i);

				// Convert rotation vector to rotation matrix
				Mat rmat = new Mat();
				Calib3d.Rodrigues(rvec, rmat);

				Mat projMatrix = new Mat(3, 4, CvType.CV_64FC1);
				projMatrix.put(0, 0, rmat.get(0,0)[0]);
				projMatrix.put(0, 1, rmat.get(0,1)[0]);
				projMatrix.put(0, 2, rmat.get(0,2)[0]);
				projMatrix.put(0, 3, 0);
				projMatrix.put(1, 0, rmat.get(1,0)[0]);
				projMatrix.put(1, 1, rmat.get(1,1)[0]);
				projMatrix.put(1, 2, rmat.get(1,2)[0]);
				projMatrix.put(1, 3, 0);
				projMatrix.put(2, 0, rmat.get(2,0)[0]);
				projMatrix.put(2, 1, rmat.get(2,1)[0]);
				projMatrix.put(2, 2, rmat.get(2,2)[0]);
				projMatrix.put(2, 3, 0);

				// Convert rotation matrix to Euler angles
				Mat eulerAngles = new Mat(3, 1, CvType.CV_16UC4);
				Calib3d.decomposeProjectionMatrix(
					projMatrix,
					new Mat(),
					new Mat(),
					new Mat(),
					new Mat(), 
					new Mat(), 
					new Mat(), 
					eulerAngles
				);

				// Position and orient the 3D model using the pose information
				/*cube.position().setAll(
				 (float)tvec.get(0,0)[0], 
				 (float)tvec.get(1,0)[0],
				 (float)tvec.get(2,0)[0]
				 );*/
				cube.rotation().setAll(
					(float)eulerAngles.get(0,0)[0], 
					(float)eulerAngles.get(1,0)[0],
					(float)eulerAngles.get(2,0)[0]
				);
			}

			//renderer.drawFrame();
		}

		return rgb;
	}


	@Override
	public void onCameraViewStopped(){
		rgb.release();
	}

	public void draw3dCube(Mat frame, Mat cameraMatrix, MatOfDouble distCoeffs, Mat rvec, Mat tvec, Scalar color){
		double halfSize = SIZE/2.0;

		List<Point3> points = new ArrayList<Point3>();
		points.add(new Point3(-halfSize, -halfSize, 0));
		points.add(new Point3(-halfSize,  halfSize, 0));
		points.add(new Point3( halfSize,  halfSize, 0));
		points.add(new Point3( halfSize, -halfSize, 0));
		points.add(new Point3(-halfSize, -halfSize, SIZE));
		points.add(new Point3(-halfSize,  halfSize, SIZE));
		points.add(new Point3( halfSize,  halfSize, SIZE));
		points.add(new Point3( halfSize, -halfSize, SIZE));

		MatOfPoint3f cubePoints = new MatOfPoint3f();
		cubePoints.fromList(points);

		MatOfPoint2f projectedPoints = new MatOfPoint2f();
		Calib3d.projectPoints(cubePoints, rvec, tvec, cameraMatrix, distCoeffs, projectedPoints);

		List<Point> pts = projectedPoints.toList();

	    for(int i=0; i<4; i++){
	        Imgproc.line(frame, pts.get(i), pts.get((i+1)%4), color, 2);
	        Imgproc.line(frame, pts.get(i+4), pts.get(4+(i+1)%4), color, 2);
	        Imgproc.line(frame, pts.get(i), pts.get(i+4), color, 2);
	    }	        
	}

}


