package mg.rivolink.app.aruco.renderer;

import android.opengl.GLSurfaceView;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import mg.rivolink.app.aruco.model.Cube2;

public class CubeRenderer2 implements GLSurfaceView.Renderer {

    private List<Cube2> mCubes;

    private Mat cameraMatrix;
    private MatOfDouble distCoeffs;

    public CubeRenderer2() {
        mCubes = new ArrayList<>();
    }

    public void addCube(Mat tvec, Mat rvec){
        Cube2 cube = new Cube2();
        cube.setVectors(tvec, rvec);

        mCubes.add(cube);
    }

    public void setCameraParams(Mat cameraMatrix, MatOfDouble distCoeffs){
        this.cameraMatrix = cameraMatrix;
        this.distCoeffs = distCoeffs;
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glTranslatef(0, 0, -10.0f);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        for (Cube2 cube : mCubes) {
            cube.draw(gl);
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(1, 1, 1, 1);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }
}
