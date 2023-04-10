package mg.rivolink.app.aruco.model;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Cube2 {

    private Mat rvec;
    private Mat tvec;

    public Cube2() {
        int one = 0x10000;
        int vertices[] = { -one, -one, -one, one, -one, -one, one, one, -one,
                -one, one, -one, -one, -one, one, one, -one, one, one, one,
                one, -one, one, one, };

        int colors[] = { 0, 0, 0, one, one, 0, 0, one, one, one, 0, one, 0,
                one, 0, one, 0, 0, one, one, one, 0, one, one, one, one, one,
                one, 0, one, one, one, };

        int colorsx[] = { one, one, one, one, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, };

        byte indices[] = { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2, 7,
                3, 3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6, 5, 3, 0, 1, 3, 1, 2 };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asIntBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asIntBuffer();
        mColorBuffer.put(colorsx);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    public void setVectors(Mat tvec, Mat rvec){
        this.tvec = tvec;
        this.rvec = rvec;

        double[] rot = rvec.get(0, 0);
        rvec.put(0, 0, new double[]{
                0, 0, 0.79
        });

        // x, y, y
        //

        Log.d("TAG", String.format("%.2f, %.2f, %.2f", rot[0], rot[1], rot[2]));
    }

    public void draw(GL10 gl) {
        double[] tvecArray = tvec.get(0, 0);
        double[] rvecArray = rvec.get(0, 0);

        // Convert the rvec vector to a rotation matrix
        Mat rotMat = new Mat();
        Calib3d.Rodrigues(rvec, rotMat);

        // Convert the rotation matrix to a float array
        double[] rot = new double[9];
        rotMat.get(0, 0, rot);

        float[] rotArray = new float[]{
                (float)rot[0], (float)rot[1], (float)rot[2], 0,
                (float)rot[3], (float)rot[4], (float)rot[5], 0,
                (float)rot[6], (float)rot[7], (float)rot[8], 0,
                0, 0, 0, 1
        };

        // Set the modelview matrix to the identity matrix
        // gl.glLoadIdentity();

        // Translate the cube to the position given by tvec
        gl.glTranslatef((float)tvecArray[0], (float)tvecArray[1], (float)tvecArray[2]);

        // Apply the rotation given by rvec
        gl.glMultMatrixf(rotArray, 0);

        // Draw the cube
        gl.glFrontFace(GL10.GL_CW);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
    }

    private IntBuffer mVertexBuffer;
    private IntBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
}

