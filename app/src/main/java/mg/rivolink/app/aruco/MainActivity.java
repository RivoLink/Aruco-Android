package mg.rivolink.app.aruco;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import mg.rivolink.app.aruco.utils.CameraParameters;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.DetectorParameters;
import org.opencv.aruco.Dictionary;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements CvCameraViewListener2{

	private Mat cameraMatrix;
	private Mat distCoeffs;

	private CameraBridgeViewBase camera;

	private Mat rgb;
	private Mat gray;

	private Mat rvecs;
	private Mat tvecs;

	private MatOfInt ids;
	private List<Mat> corners;
	private Dictionary dictionary;
	private DetectorParameters parameters;

	private BaseLoaderCallback loaderCallback=new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
            switch(status){
				case LoaderCallbackInterface.SUCCESS:{
					String message=null;
					if(loadCameraParams())
						message=getString(R.string.success_ocv_loading);
					else
						message=getString(R.string.error_camera_params);

					camera.enableView();

					Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
					break;
				}
				default:{
					super.onManagerConnected(status);
					break;
				}
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_layout);

        camera=findViewById(R.id.main_camera);
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);
    }

	@Override
    public void onResume(){
        super.onResume();

		if(OpenCVLoader.initDebug())
			loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		else
			Toast.makeText(this,getString(R.string.error_native_lib),Toast.LENGTH_LONG).show();
    }

	private boolean loadCameraParams(){
		cameraMatrix=Mat.eye(3,3,CvType.CV_64FC1);
        distCoeffs=Mat.zeros(5,1,CvType.CV_64FC1);
		return CameraParameters.tryLoad(this,cameraMatrix,distCoeffs);
	}

	@Override
    public void onPause(){
        super.onPause();
        if(camera!=null)
            camera.disableView();
    }

	@Override
    public void onDestroy(){
        super.onDestroy();
        if (camera!=null)
            camera.disableView();
    }

	@Override
	public void onCameraViewStarted(int width,int height){
		rgb=new Mat();
		corners=new LinkedList<>();
		parameters=DetectorParameters.create();
		dictionary=Aruco.getPredefinedDictionary(Aruco.DICT_6X6_50);
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
		Imgproc.cvtColor(inputFrame.rgba(),rgb,Imgproc.COLOR_RGBA2RGB);
		gray=inputFrame.gray();

		ids=new MatOfInt();
		corners.clear();

		Aruco.detectMarkers(gray,dictionary,corners,ids,parameters);

		if(corners.size()>0){
			Aruco.drawDetectedMarkers(rgb,corners,ids);

			rvecs=new Mat();
			tvecs=new Mat();

			Aruco.estimatePoseSingleMarkers(corners,0.04f,cameraMatrix,distCoeffs,rvecs,tvecs);
			for(int i=0;i<ids.toArray().length;i++){
				Aruco.drawAxis(rgb,cameraMatrix,distCoeffs,rvecs.row(i),tvecs.row(i),0.02f);
			}

		}

		return rgb;
	}

	@Override
	public void onCameraViewStopped(){
		rgb.release();
	}

}

