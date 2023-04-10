package mg.rivolink.app.aruco.model;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;

import javax.microedition.khronos.opengles.GL10;

public class CubeIs {
    // Cube variables and methods

    private float size;

    public CubeIs(float size) {
        this.size = size;
    }

    public void draw(GL10 gl, Mat tvec, Mat rvec) {
        double[] tvecArray = tvec.get(0, 0);
        double[] rvecArray = rvec.get(0, 0);

        // Convert the rvec vector to a rotation matrix
        Mat rotMat = new Mat();
        Calib3d.Rodrigues(rvec, rotMat);

        // Convert the rotation matrix to a float array
        float[] rotArray = new float[9];
        rotMat.get(0, 0, rotArray);

        // Set the modelview matrix to the identity matrix
        gl.glLoadIdentity();

        // Translate the cube to the position given by tvec
        gl.glTranslatef((float)tvecArray[0], (float)tvecArray[1], (float)tvecArray[2]);

        // Apply the rotation given by rvec
        gl.glMultMatrixf(rotArray, 0);

        // Draw the cube
        gl.glPushMatrix();
        gl.glScalef(size, size, size);
        gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(-0.5f, -0.5f, -0.5f);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glTranslatef(1.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glTranslatef(0.0f, 1.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glTranslatef(-1.0f, 0.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glTranslatef(0.0f, -1.0f, 0.0f);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
        gl.glPopMatrix();
    }
}
