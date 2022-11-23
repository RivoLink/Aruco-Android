package mg.rivolink.app.aruco.camera.calibration.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

public class LandscapeCameraLayout extends LinearLayout{
	
	private CameraBridgeViewBase camera;

	public LandscapeCameraLayout(Context context){
		super(context);
		createCamera(context);
	}

	public LandscapeCameraLayout(Context context,AttributeSet attrs){
		super(context,attrs);
		createCamera(context);
	}

	private void createCamera(Context context){
		camera=new JavaCameraView(context,0);
		addView(camera);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec,heightMeasureSpec);
		camera.measure(widthMeasureSpec,heightMeasureSpec);
	}

	public CameraBridgeViewBase getCamera(){
		return camera;
	}
}

