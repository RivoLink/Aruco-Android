package mg.rivolink.app.aruco.camera.calibration;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import mg.rivolink.app.aruco.camera.calibration.view.PortraitCameraLayout;

public class CameraCalibrationActivity extends AppCompatActivity 
	implements OnTouchListener, CvCameraViewListener2, CameraCalibrator.OnAddFrameListener {
		
	private Mat rgb;
	private CameraCalibrator calibrator;
	private CameraBridgeViewBase camera;
    
	private final BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this){
        @Override
        public void onManagerConnected(int status){
			if(status == LoaderCallbackInterface.SUCCESS){
				camera.enableView();
				camera.setOnTouchListener(CameraCalibrationActivity.this);

				Toast.makeText(CameraCalibrationActivity.this, getString(R.string.success_ocv_loading), Toast.LENGTH_SHORT).show();
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

        setContentView(R.layout.camera_calibration_layout);

		camera = ((PortraitCameraLayout)findViewById(R.id.camera_layout)).getCamera();
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCvCameraViewListener(this);
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

        if(camera != null)
            camera.disableView();
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		CalibrationResult.onActivityResult(this, requestCode, resultCode, data);
	}
	
	public void onCalibrate(View view){
		if(!calibrator.canCalibrate()){
			Toast.makeText(this, getString(R.string.error_more_frames), Toast.LENGTH_SHORT).show();
		}
		else {
			new AsyncTask<Void,Void,Void>(){
				private double error=0;
				private ProgressDialog progress;

				@Override
				protected void onPreExecute(){
					progress = new ProgressDialog(CameraCalibrationActivity.this);
					progress.setTitle(getString(R.string.calibrating));
					progress.setMessage(getString(R.string.please_wait));
					progress.setCancelable(false);
					progress.setIndeterminate(true);
					progress.show();
				}

				@Override
				protected Void doInBackground(Void... arg0){
					error = calibrator.calibrate();
					return null;
				}

				@Override
				protected void onPostExecute(Void result){
					progress.dismiss();
					calibrator.clear();

					CalibrationResult.save(
						CameraCalibrationActivity.this,
						calibrator.getCameraMatrix(),
						calibrator.getDistorsionCoefficients()
					);

					String resultMessage = getString(R.string.success_calibration) + error;
					Toast.makeText(CameraCalibrationActivity.this, resultMessage, Toast.LENGTH_SHORT).show();

				}
			}.execute();
		}
	}

	@Override
	public boolean onTouch(View view,MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN)
			calibrator.addFrame();
			
		return true;
	}

	@Override
	public void onAddFrame(final boolean added){
		runOnUiThread(new Runnable(){
			@Override
			public void run(){
				String message = null;

				if(added)
					message = getString(R.string.success_adding_frame);
				else
					message = getString(R.string.error_no_marker_detected);
					
				Toast.makeText(CameraCalibrationActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onCameraViewStarted(int width, int height){
		calibrator = new CameraCalibrator(width, height);
		calibrator.setOnAddFrameListener(this);
		
		rgb = new Mat();
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
		Imgproc.cvtColor(inputFrame.rgba(), rgb, Imgproc.COLOR_RGBA2RGB);
		
		calibrator.render(rgb, inputFrame.gray());
		
		return rgb;
	}

	@Override
	public void onCameraViewStopped(){
		rgb.release();
		calibrator.release();
	}

}
