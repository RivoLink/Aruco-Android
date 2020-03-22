package mg.rivolink.app.aruco;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import mg.rivolink.app.aruco.utils.CameraParameters;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends Activity implements CvCameraViewListener2{

	private Mat cameraMatrix;
	private Mat distCoeffs;
	
	private CameraBridgeViewBase camera;

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

	private boolean loadCameraParams(){
		cameraMatrix=Mat.eye(3,3,CvType.CV_64FC1);
        distCoeffs=Mat.zeros(5,1,CvType.CV_64FC1);
		return CameraParameters.tryLoad(this,cameraMatrix,distCoeffs);
	}
	
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
		// TODO: Implement this method
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
		// TODO: Implement this method
		return inputFrame.rgba();
	}

	@Override
	public void onCameraViewStopped(){
		// TODO: Implement this method
	}

}

