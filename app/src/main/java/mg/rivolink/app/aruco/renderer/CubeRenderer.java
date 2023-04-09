package mg.rivolink.app.aruco.renderer;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import mg.rivolink.app.aruco.model.Cube;
import org.opencv.core.*;

/**
 * Render a pair of tumbling cubes.
 */
public class CubeRenderer implements GLSurfaceView.Renderer {

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

	public void onDrawFrame(GL10 gl) {
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

		// ----- ChatGPT

		// Convert rvec to rotation matrix
		Mat rotationMatrix = new Mat(3, 3, CvType.CV_64F);
		Calib3d.Rodrigues(rvec, rotationMatrix);

		// Convert rotation matrix and translation vector to OpenGL matrix
		double[] rotationData = new double[9];
		rotationMatrix.get(0, 0, rotationData);

		float[] glRotation = new float[16];
		glRotation[0] = (float) rotationData[0];
		glRotation[1] = (float) rotationData[3];
		glRotation[2] = (float) rotationData[6];
		glRotation[3] = 0;

		glRotation[4] = (float) rotationData[1];
		glRotation[5] = (float) rotationData[4];
		glRotation[6] = (float) rotationData[7];
		glRotation[7] = 0;

		glRotation[8] = (float) rotationData[2];
		glRotation[9] = (float) rotationData[5];
		glRotation[10] = (float) rotationData[8];
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
		glTranslation[13] = (float) tvec.get(1, 0)[0];
		glTranslation[14] = (float) tvec.get(2, 0)[0];
		glTranslation[15] = 1;

		// Combine rotation and translation matrices
		float[] glModelView = new float[16];
		Matrix.multiplyMM(glModelView, 0, glTranslation, 0, glRotation, 0);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glMultMatrixf(glModelView, 0);

		// ----- ChatGPT

		mCube.draw(gl);

		/*
		gl.glRotatef(mAngle * 2.0f, 0, 1, 1);
		gl.glTranslatef(0.5f, 0.5f, 0.5f);

		mCube.draw(gl);

		mAngle += 1.2f;
		*/
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */

		float ratio = (float) width / height;
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
