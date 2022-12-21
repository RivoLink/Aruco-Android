package mg.rivolink.app.aruco;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mg.rivolink.app.aruco.renderer.Renderer3D;
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
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Point3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import org.rajawali3d.view.SurfaceView;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

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

	private Renderer3D renderer;
	private CameraBridgeViewBase camera;
	
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
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_layout);

        camera = ((PortraitCameraLayout)findViewById(R.id.camera_layout)).getCamera();
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);

		renderer = new Renderer3D(this);

		SurfaceView surface = (SurfaceView)findViewById(R.id.main_surface);
		surface.setTransparent(true);
		surface.setSurfaceRenderer(renderer);

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
				draw3dCube(rgb, cameraMatrix, distCoeffs, rvecs.row(i), tvecs.row(i), new Scalar(255, 0, 0));
				//transformModel(rvecs.row(0), tvecs.row(0));
				Aruco.drawAxis(rgb, cameraMatrix, distCoeffs, rvecs.row(i), tvecs.row(i), SIZE/2.0f);
			}
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
	
	boolean toast = false;
	
	void debug(final Object obj){
		if(!toast){
			toast = true;

			runOnUiThread(new Runnable(){
				@Override
				public void run(){
					Toast.makeText(MainActivity.this,obj.toString(), Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	private void transformModel(final Mat rvec, final Mat tvec){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
			
			
				
				double halfSize = SIZE/2.0;

				List<Point3> points = new ArrayList<Point3>();
				points.add(new Point3(-halfSize, -halfSize, 0));

				MatOfPoint3f cubePoints = new MatOfPoint3f();
				cubePoints.fromList(points);

				MatOfPoint2f projectedPoints = new MatOfPoint2f();
				Calib3d.projectPoints(cubePoints, rvec, tvec, cameraMatrix, distCoeffs, projectedPoints);

				List<Point> pts = projectedPoints.toList();
				Point pt = pts.get(0);
				
				//Imgproc.circle(rgb, pt, 10, new Scalar(0, 255, 0, 150), 4);
				
				
				Mat rmat = new Mat();
				Calib3d.Rodrigues(rvec, rmat);
				
		//MatOfDouble projMatrix = new MatOfDouble(3, 4, CvType.CV_64FC1); 
		
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
		
		//MatOfFloat projMatrix = new MatOfFloat(3, 4, CvType.CV_64FC1);
		/*Mat projMatrix = new Mat(3, 4, CvType.CV_64FC1);
		projMatrix.put(0, 0, 1);
		projMatrix.put(0, 1, 0);
		projMatrix.put(0, 2, 0);
		projMatrix.put(0, 3, 0);
		projMatrix.put(1, 0, 0);
		projMatrix.put(1, 1, 1);
		projMatrix.put(1, 2, 0);
		projMatrix.put(1, 3, 0);
		projMatrix.put(2, 0, 0);
		projMatrix.put(2, 1, 0);
		projMatrix.put(2, 2, 1);
		projMatrix.put(2, 3, 0);*/
				
				
				//double[] rVecArray = rmat.toArray();
				
				/*
				//projMatrix 3x4 input projection matrix P
				MatOfDouble projMatrix = new MatOfDouble(3, 4, CvType.CV_32F); 
				projMatrix.put(0, 0, rVecArray[0]);
				projMatrix.put(0, 1, rVecArray[1]);
				projMatrix.put(0, 2, rVecArray[2]);
				projMatrix.put(0, 3, 0);
				projMatrix.put(1, 0, rVecArray[3]);
				projMatrix.put(1, 1, rVecArray[4]);
				projMatrix.put(1, 2, rVecArray[5]);
				projMatrix.put(1, 3, 0);
				projMatrix.put(2, 0, rVecArray[6]);
				projMatrix.put(2, 1, rVecArray[7]);
				projMatrix.put(2, 2, rVecArray[8]);
				projMatrix.put(2, 3, 0);
				*/
				
				MatOfDouble cameraMatrix = new MatOfDouble(3, 3, CvType.CV_32F); //cameraMatrix Output 3x3 camera matrix K.
				MatOfDouble rotMatrix = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrix Output 3x3 external rotation matrix R.
				MatOfDouble transVect = new MatOfDouble(4, 1, CvType.CV_32F); //transVect Output 4x1 translation vector T.
				MatOfDouble rotMatrixX = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixX a rotMatrixX
				MatOfDouble rotMatrixY = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixY a rotMatrixY
				MatOfDouble rotMatrixZ = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixZ a rotMatrixZ
				//MatOfDouble eulerAngles = new MatOfDouble(3, 1, CvType.CV_32F); //eulerAngles Optional three-element vector containing three Euler angles of rotation in degrees.
				
				
				Mat eulerAngles = new Mat(3, 1, CvType.CV_32F);
		
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
		
		//debug(pt);
		
		renderer.transform(
			pt.x,
			pt.y,
			0,

			eulerAngles.get(2,0)[0], //yaw
			eulerAngles.get(1,0)[0], //pitch
			eulerAngles.get(0,0)[0] //roll
		);
				
				
				/*
				Calib3d.decomposeProjectionMatrix(
					projMatrix,
					cameraMatrix,
					rotMatrix,
					transVect,
					rotMatrixX,
					rotMatrixY,
					rotMatrixZ,
					eulerAngles
				);

				/*double[] euler = eulerAngles.toArray();
				
				
				
				
				
				
				
				
				
				
				renderer.transform(
					pt.x,
					pt.y,
					0,
			
					euler[2], //yaw
					euler[1], //pitch
					euler[0] //roll
				);*/
				
		/*
		//Mat rot = Mat.eye(3, 3, CvType.CV_64FC1);
		Mat rotMatrix = new Mat(3,3,CvType.CV_64F);
		Calib3d.Rodrigues(rvec, rotMatrix);

		Mat R_T = new Mat(3,4,CvType.CV_64F);
		Core.hconcat(Arrays.asList(rotMatrix, tvec), R_T);

		Mat projMatrix = new Mat(3,4,CvType.CV_64F);
		Core.multiply(cameraMatrix, R_T, projMatrix);

		Mat eulerAngles = new Mat(3,1,CvType.CV_64F);
		Calib3d.decomposeProjectionMatrix(
			projMatrix, 
			cameraMatrix, 
			rotMatrix,
			tvec,
			new Mat(),
			new Mat(),
			new Mat(),
			eulerAngles
		);
		*/
				
			}
		});
	}
	
	public double[] findEulerAngles(MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, MatOfDMatch matches){

		KeyPoint[] k1 = keypoints1.toArray();
		KeyPoint[] k2 = keypoints2.toArray();


		List<DMatch> matchesList = matches.toList();
		List<KeyPoint> referenceKeypointsList = keypoints2.toList();
		List<KeyPoint> sceneKeypointsList = keypoints1.toList();
		
		// Calculate the max and min distances between keypoints.
		double maxDist = 0.0;
		double minDist = Double.MAX_VALUE;
		for(DMatch match : matchesList) {
			double dist = match.distance;
			if (dist < minDist) {
				minDist = dist;
			}
			if (dist > maxDist) {
				maxDist = dist;
			}
		}
		
		// Identify "good" keypoints based on match distance.
		List<Point3> goodReferencePointsList = new ArrayList<Point3>();
		ArrayList<Point> goodScenePointsList = new ArrayList<Point>();
		double maxGoodMatchDist = 1.75 * minDist;
		for(DMatch match : matchesList) {
			if (match.distance < maxGoodMatchDist) {
				Point kk2 = k2[match.queryIdx].pt;
				Point kk1 = k1[match.trainIdx].pt;

				Point3 point3 = new Point3(kk1.x, kk1.y, 0.0);
				goodReferencePointsList.add(point3);
				goodScenePointsList.add( kk2);
				//sceneKeypointsList.get(match.queryIdx).pt);
			}
		}


		if (goodReferencePointsList.size() < 4 || goodScenePointsList.size() < 4) {
			// There are too few good points to find the pose.
			return null;
		}

		MatOfPoint3f goodReferencePoints = new MatOfPoint3f();
		goodReferencePoints.fromList(goodReferencePointsList);
		MatOfPoint2f goodScenePoints = new MatOfPoint2f();
		goodScenePoints.fromList(goodScenePointsList);

		MatOfDouble mRMat = new MatOfDouble(3, 3, CvType.CV_32F);
		MatOfDouble mTVec = new MatOfDouble(3, 1, CvType.CV_32F);

		// TODO: solve camera intrinsic matrix
		Mat intrinsics = Mat.eye(3, 3, CvType.CV_32F); // dummy camera matrix
		intrinsics.put(0, 0, 400);
		intrinsics.put(1, 1, 400);
		intrinsics.put(0, 2, 640 / 2);
		intrinsics.put(1, 2, 480 / 2);
		Calib3d.solvePnPRansac(goodReferencePoints, goodScenePoints, intrinsics, new MatOfDouble(), mRMat, mTVec);
		MatOfDouble rotCameraMatrix1 = new MatOfDouble(3, 1, CvType.CV_32F);
		double[] rVecArray = mRMat.toArray();
		// Calib3d.Rodrigues(mRMat, rotCameraMatrix1);
		double[] tVecArray = mTVec.toArray();

		MatOfDouble projMatrix = new MatOfDouble(3, 4, CvType.CV_32F); //projMatrix 3x4 input projection matrix P.
		projMatrix.put(0, 0, rVecArray[0]);
		projMatrix.put(0, 1, rVecArray[1]);
		projMatrix.put(0, 2, rVecArray[2]);
		projMatrix.put(0, 3, 0);
		projMatrix.put(1, 0, rVecArray[3]);
		projMatrix.put(1, 1, rVecArray[4]);
		projMatrix.put(1, 2, rVecArray[5]);
		projMatrix.put(1, 3, 0);
		projMatrix.put(2, 0, rVecArray[6]);
		projMatrix.put(2, 1, rVecArray[7]);
		projMatrix.put(2, 2, rVecArray[8]);
		projMatrix.put(2, 3, 0);

		MatOfDouble cameraMatrix = new MatOfDouble(3, 3, CvType.CV_32F); //cameraMatrix Output 3x3 camera matrix K.
		MatOfDouble rotMatrix = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrix Output 3x3 external rotation matrix R.
		MatOfDouble transVect = new MatOfDouble(4, 1, CvType.CV_32F); //transVect Output 4x1 translation vector T.
		MatOfDouble rotMatrixX = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixX a rotMatrixX
		MatOfDouble rotMatrixY = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixY a rotMatrixY
		MatOfDouble rotMatrixZ = new MatOfDouble(3, 3, CvType.CV_32F); //rotMatrixZ a rotMatrixZ
		MatOfDouble eulerAngles = new MatOfDouble(3, 1, CvType.CV_32F); //eulerAngles Optional three-element vector containing three Euler angles of rotation in degrees.

		Calib3d.decomposeProjectionMatrix( projMatrix,
										  cameraMatrix,
										  rotMatrix,
										  transVect,
										  rotMatrixX,
										  rotMatrixY,
										  rotMatrixZ,
										  eulerAngles);

		double[] eulerArray = eulerAngles.toArray();

		return eulerArray;
	}
	
}


