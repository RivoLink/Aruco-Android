package mg.rivolink.app.aruco.renderer;

import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import mg.rivolink.app.aruco.model.Cube;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;

/**
 * Render a pair of tumbling cubes.
 */
public class CubeRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "CubeRenderer";

	private Mat rvec;
	private Mat tvec;
	
	public CubeRenderer(boolean useTranslucentBackground) {
		mTranslucentBackground = useTranslucentBackground;
		mCube = new Cube();
	}

	public void setVectors(Mat tvec, Mat rvec){
		this.tvec = tvec;
		this.rvec = rvec;
	}

	private void combine(GL10 gl){
		// Convert rvec to rotation matrix
		Mat rotationMatrix = new Mat(3, 3, CvType.CV_64F);
		Calib3d.Rodrigues(rvec, rotationMatrix);

		// Convert rotation matrix and translation vector to OpenGL matrix
		double[] rotationData = new double[9];
		rotationMatrix.get(0, 0, rotationData);

		float[] glRotation = new float[16];
//			glRotation[0] = (float) rotationData[0];
//			glRotation[1] = (float) rotationData[3];
//			glRotation[2] = (float) rotationData[6];
		glRotation[0] = 1;
		glRotation[1] = 0;
		glRotation[2] = 0;
		glRotation[3] = 0;

//			glRotation[4] = (float) rotationData[1];
//			glRotation[5] = (float) rotationData[4];
//			glRotation[6] = (float) rotationData[7];
		glRotation[4] = 0;
		glRotation[5] = 1;
		glRotation[6] = 0;
		glRotation[7] = 0;

//			glRotation[8] = (float) rotationData[2];
//			glRotation[9] = (float) rotationData[5];
//			glRotation[10] = (float) rotationData[8];
		glRotation[8] = 0;
		glRotation[9] = 0;
		glRotation[10] = 1;
		glRotation[11] = 0;

		glRotation[12] = 0;
		glRotation[13] = 0;
		glRotation[14] = 0;
		glRotation[15] = 1;

		float[] glTranslation = new float[16];
		glTranslation[0] = 1;
		glTranslation[1] = 0;
		glTranslation[2] = 0;
		glTranslation[3] = 0;

		glTranslation[4] = 0;
		glTranslation[5] = 1;
		glTranslation[6] = 0;
		glTranslation[7] = 0;

		glTranslation[8] = 0;
		glTranslation[9] = 0;
		glTranslation[10] = -1;
		glTranslation[11] = 0;

		glTranslation[12] = (float) tvec.get(0, 0)[0];
		glTranslation[13] = (float) tvec.get(0, 0)[1];
		glTranslation[14] = (float) tvec.get(0, 0)[2];
		glTranslation[15] = 1;

		// Combine rotation and translation matrices
		float[] glModelView = new float[16];
		Matrix.multiplyMM(glModelView, 0, glTranslation, 0, glRotation, 0);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glMultMatrixf(glModelView, 0);
	}

	public void onDrawFrameT(GL10 gl){
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		if((tvec != null) && (rvec != null)) {
			int width = 1080;
			int height = 2280;

			double scaleFactor = 2.0 / Math.min(width, height);
			double[] tvecScaled = new double[3];

			tvecScaled[0] = tvec.get(0, 0)[0] * scaleFactor;
			tvecScaled[1] = -tvec.get(0, 0)[1] * scaleFactor; // negate y-axis for OpenGL
			tvecScaled[2] = -tvec.get(0, 0)[2] * scaleFactor; // negate z-axis for OpenGL

			Log.d(TAG, String.format("%f %f %f ", (float) tvecScaled[0], (float) tvecScaled[1], (float) tvecScaled[2]));

//			gl.glTranslatef((float) tvecScaled[0], (float) tvecScaled[1], (float) tvecScaled[2]);
			gl.glTranslatef((float) tvecScaled[0], (float) tvecScaled[1], -3.0f);

//			marker 1 : D/CubeRenderer: -0,001434 0,002782 -0,005245
//			marker 2 : D/CubeRenderer: -0,001442 0,002631 -0,004557
		}

//		gl.glTranslatef(0, 0, -3.0f);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		mCube.draw(gl);
	}

	boolean done = false;

	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		if((tvec != null) && (rvec != null)){
			//rvec.put(0,0, new double[]{0.4, 0, 0});
			
			double[] rvecScaled = rvec.get(0, 0);

			// Apply the rotation from rvec
			Mat rotationMatrix = new Mat();
			Calib3d.Rodrigues(rvec, rotationMatrix);

			double[] rotationMatrixData = new double[9];
			rotationMatrix.get(0, 0, rotationMatrixData);

//			rotationMatrixData = new double[]{
//				1.0, 0, 0,
//				0, 1.0, 0,
//				0, 0, 1.0,
//			};

			if(!done){
				done = true;

				Log.d(TAG, String.format("%.2f, %.2f, %.2f", (float) rvecScaled[0], (float) rvecScaled[1], (float) rvecScaled[2]));
				Log.d(TAG, "-------------");

				float r = (float)Math.toDegrees(rvecScaled[0]);
				Log.d(TAG, String.format("rad: %.2f, deg: %.2f",rvecScaled[0], r));
				Log.d(TAG, "-------------");
				
				Log.d(TAG, String.format("%.2f, %.2f, %.2f", rotationMatrixData[0], rotationMatrixData[1], rotationMatrixData[2]));
				Log.d(TAG, String.format("%.2f, %.2f, %.2f", rotationMatrixData[3], rotationMatrixData[4], rotationMatrixData[5]));
				Log.d(TAG, String.format("%.2f, %.2f, %.2f", rotationMatrixData[6], rotationMatrixData[7], rotationMatrixData[8]));
			}

			float[] rotationData = new float[16];
			rotationData[0] = (float) rotationMatrixData[0];
			rotationData[1] = (float) rotationMatrixData[1];
			rotationData[2] = (float) rotationMatrixData[2];
			rotationData[3] = 0;

			rotationData[4] = (float) rotationMatrixData[3];
			rotationData[5] = (float) rotationMatrixData[4];
			rotationData[6] = (float) rotationMatrixData[5];
			rotationData[7] = 0;

			rotationData[8] = (float) rotationMatrixData[6];
			rotationData[9] = (float) rotationMatrixData[7];
			rotationData[10] = (float) rotationMatrixData[8];
			rotationData[11] = 0;

			rotationData[12] = 0;
			rotationData[13] = 0;
			rotationData[14] = 0;
			rotationData[15] = 1;

			gl.glMultMatrixf(rotationData, 0);
		}

		gl.glTranslatef(0, 0, -10.0f);

		// Draw the cube
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		mCube.draw(gl);
	}

	public void onDrawFrameMaybe(GL10 gl) {
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5, 0, 0, 0, 0, 1, 0); // Set camera position and target

		if((tvec != null) && (rvec != null)){
			// Rotate the cube based on rvec
			gl.glRotatef((float)Math.toDegrees(rvec.get(0, 0)[0]), 1, 0, 0);
			gl.glRotatef((float)Math.toDegrees(rvec.get(0, 0)[1]), 0, 1, 0);
			gl.glRotatef((float)Math.toDegrees(rvec.get(0, 0)[2]), 0, 0, 1);

			// Translate the cube based on tvec
			gl.glTranslatef((float)tvec.get(0, 0)[0], (float)-tvec.get(0, 0)[1], (float)tvec.get(0, 0)[2]);

			// Draw the cube
			mCube.draw(gl);
		}
	}

	public void onDrawFrameOLD(GL10 gl) {
		/*
		 * Usually, the first thing one might want to do is to clear the screen.
		 * The most efficient way of doing this is to use glClear().
		 */

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		/*
		 * Now we're ready to draw some 3D objects
		 */

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -3.0f);
		gl.glRotatef(mAngle, 0, 1, 0);
		gl.glRotatef(mAngle * 0.25f, 1, 0, 0);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

		if((tvec != null) && (rvec != null)){
			// ----- ChatGPT


			// ----- ChatGPT
		}

		mCube.draw(gl);

		/*
		gl.glRotatef(mAngle * 2.0f, 0, 1, 1);
		gl.glTranslatef(0.5f, 0.5f, 0.5f);

		mCube.draw(gl);

		mAngle += 1.2f;
		*/
	}

	float ratio;

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */

		ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		/*
		 * By default, OpenGL enables features that improve quality but reduce
		 * performance. One might want to tweak that especially on software
		 * renderer.
		 */
		gl.glDisable(GL10.GL_DITHER);

		/*
		 * Some one-time OpenGL initialization can be made here probably based
		 * on features of this particular context
		 */
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

		if (mTranslucentBackground) {
			gl.glClearColor(0, 0, 0, 0);
		} else {
			gl.glClearColor(1, 1, 1, 1);
		}
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

	private boolean mTranslucentBackground;
	private Cube mCube;
	private float mAngle;
}
