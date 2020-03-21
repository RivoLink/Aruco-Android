package mg.rivolink.app.aruco;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements OnTouchListener,CvCameraViewListener2{

	private static final String LOADING_SUCCESS="Success: OpenCV loaded.";
	private static final String ERROR_NATIVE_LIB="Error: libopencv_java3.so not found for this platform.";

	private CameraBridgeViewBase camera;

	private BaseLoaderCallback loaderCallback=new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
            switch(status){
				case LoaderCallbackInterface.SUCCESS:{
					camera.enableView();
					camera.setOnTouchListener(MainActivity.this);
					Toast.makeText(MainActivity.this,LOADING_SUCCESS,Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this,ERROR_NATIVE_LIB,Toast.LENGTH_LONG).show();
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
	public boolean onTouch(View view,MotionEvent event){
		// TODO: Implement this method
		return false;
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

