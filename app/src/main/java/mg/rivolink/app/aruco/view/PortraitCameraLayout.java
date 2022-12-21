package mg.rivolink.app.aruco.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.opencv.android.CameraBridgeViewBase;

public class PortraitCameraLayout extends LinearLayout {

	private CameraBridgeViewBase camera;

	public PortraitCameraLayout(Context context){
		super(context);
		createCamera(context);
	}

	public PortraitCameraLayout(Context context, AttributeSet attrs){
		super(context, attrs);
		createCamera(context);
	}

	private void createCamera(Context context){
		camera = new PortraitCameraView(context, 0);
		addView(camera);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		camera.measure(widthMeasureSpec, heightMeasureSpec);
	}

	public CameraBridgeViewBase getCamera(){
		return camera;
	}
}

