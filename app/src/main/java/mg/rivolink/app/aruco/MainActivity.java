package mg.rivolink.app.aruco;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mg.rivolink.app.aruco.model.Model3D;
import mg.rivolink.app.aruco.renderer.MyGLRenderer;

public class MainActivity extends AppCompatActivity {

	private Model3D model3D;
	private MyGLRenderer myGLRenderer;
	private GLSurfaceView mySurfaceView;

	static {
		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mySurfaceView = new GLSurfaceView(this);
		mySurfaceView.setEGLContextClientVersion(2);

		myGLRenderer = new MyGLRenderer(this, mySurfaceView);
		mySurfaceView.setRenderer(myGLRenderer);

		setContentView(mySurfaceView);
	}

}
