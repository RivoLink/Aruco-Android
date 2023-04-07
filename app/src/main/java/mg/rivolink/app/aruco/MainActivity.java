package mg.rivolink.app.aruco;

import android.app.Activity;
import android.content.Intent;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import mg.rivolink.app.aruco.renderer.OpenGLRenderer;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

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

	private OpenGLRenderer renderer;

	private GLSurfaceView surface;
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

					renderer = new OpenGLRenderer(cameraMatrix, distCoeffs);
					surface.setRenderer(renderer);

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
        camera.setVisibility(View.VISIBLE);
        camera.setCvCameraViewListener(this);

		surface = (GLSurfaceView)findViewById(R.id.main_surface);
		surface.setEGLContextClientVersion(2);
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

			renderer.setMarkers(corners, ids);
			renderer.setCameraParams(cameraMatrix, distCoeffs);

			rvecs = new Mat();
			tvecs = new Mat();

			Aruco.estimatePoseSingleMarkers(corners, SIZE, cameraMatrix, distCoeffs, rvecs, tvecs);

			if(ids.toArray().length > 0){
				Mat tvec = tvecs.row(0);
				Mat rvec = rvecs.row(0);

			}
		}

		return rgb;
	}

	@Override
	public void onCameraViewStopped(){
		rgb.release();
	}
}


