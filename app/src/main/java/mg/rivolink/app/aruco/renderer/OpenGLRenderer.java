package mg.rivolink.app.aruco.renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

import org.opencv.aruco.Aruco;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint2f;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    // the 3D model to be drawn on top of the Aruco marker
    private Cube mCube;

    // the detected Aruco markers and camera calibration parameters
    private List<Mat> mCorners;
    private int[] mIds;
    private Mat mCameraMatrix;
    private MatOfDouble mDistCoeffs;

    // OpenGL variables
    private FloatBuffer mVertexBuffer;
    private int mProgramId;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMvpMatrixHandle;
    private float[] mMvpMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    public OpenGLRenderer() {
        // initialize the 3D model
        mCube = new Cube();
    }

    public void setMarkers(MatOfPoint2f[] corners, int[] ids) {
        // update the detected Aruco markers
        mCorners = corners;
        mIds = ids;
    }

    public void setCameraParams(Mat cameraMatrix, MatOfDouble distCoeffs) {
        // update the camera calibration parameters
        mCameraMatrix = cameraMatrix;
        mDistCoeffs = distCoeffs;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Initialize the vertex buffer
        float[] cubeVertices = mCube.getVertices();
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(cubeVertices);
        mVertexBuffer.position(0);

        // Load the vertex shader
        int vertexShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderId, vertexShaderCode);
        GLES20.glCompileShader(vertexShaderId);

        // Load the fragment shader
        int fragmentShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderId, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShaderId);

        // Create the shader program
        mProgramId = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgramId, vertexShaderId);
        GLES20.glAttachShader(mProgramId, fragmentShaderId);
        GLES20.glLinkProgram(mProgramId);

        // Get the attribute and uniform locations
        mPositionHandle = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        mColorHandle = GLES20.glGetUniformLocation(mProgramId, "uColor");
        mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uMvpMatrix");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Set the viewport
        GLES20.glViewport(0, 0, width, height);

        // Compute the projection matrix
        float aspectRatio = (float) width / height;
        float left = -aspectRatio;
        float right = aspectRatio;
        float bottom = -1.0f;
        float top = 1.0f;
        float near = 1.0f;
        float far = 100.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        // Set the camera position
        // Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Use the program
        GLES20.glUseProgram(mProgramId);

        // Set the view matrix
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        // Update the 3D model position and orientation
        if (mCorners != null && mIds != null && mCorners.size() > 0) {
            // get the Aruco marker pose
            Mat rvec = new Mat();
            Mat tvec = new Mat();
            Aruco.estimatePoseSingleMarkers(mCorners, 0.05f, mCameraMatrix, mDistCoeffs, rvec, tvec);

            // convert rotation vector to rotation matrix
            Mat rotMat = new Mat();
            Calib3d.Rodrigues(rvec, rotMat);

            // calculate the model matrix
            float[] modelMatrix = new float[16];
            modelMatrix[0] = (float) rotMat.get(0, 0)[0];
            modelMatrix[1] = (float) rotMat.get(0, 1)[0];
            modelMatrix[2] = (float) rotMat.get(0, 2)[0];
            modelMatrix[3] = 0.0f;
            modelMatrix[4] = (float) rotMat.get(1, 0)[0];
            modelMatrix[5] = (float) rotMat.get(1, 1)[0];
            modelMatrix[6] = (float) rotMat.get(1, 2)[0];
            modelMatrix[7] = 0.0f;
            modelMatrix[8] = (float) rotMat.get(2, 0)[0];
            modelMatrix[9] = (float) rotMat.get(2, 1)[0];
            modelMatrix[10] = (float) rotMat.get(2, 2)[0];
            modelMatrix[11] = 0.0f;
            modelMatrix[12] = (float) tvec.get(0, 0)[0];
            modelMatrix[13] = (float) tvec.get(0, 0)[1];
            modelMatrix[14] = (float) tvec.get(0, 0)[2];
            modelMatrix[15] = 1.0f;

            // calculate the model-view-projection matrix
            float[] mvpMatrix = new float[16];
            Matrix.multiplyMM(mvpMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
            Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mvpMatrix, 0);

            // Set the MVP matrix
            int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgramId, "uMVPMatrix");
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

            // Set the vertex attribute pointers
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Draw the 3D model
            GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 16, mColorBuffer);
            GLES20.glEnableVertexAttribArray(mColorHandle);

            // Draw the model
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumVertices);

            // Disable vertex attribute arrays
            GLES20.glDisableVertexAttribArray(mPositionHandle);
            GLES20.glDisableVertexAttribArray(mColorHandle);
        }
    }

    public void onDrawFrame_Maybe(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // render the cube on top of the detected Aruco marker
        if (mCorners != null && mCorners.length > 0) {
            // estimate the pose of the Aruco marker
            Mat rvec = new Mat();
            Mat tvec = new Mat();
            Aruco.estimatePoseSingleMarkers(mCorners, 0.05f, mCameraMatrix, mDistCoeffs, rvec, tvec);

            // transform the rotation and translation vectors to OpenGL compatible format
            float[] rotMatrix = new float[9];
            MatOfFloat mofRotMatrix = new MatOfFloat(rotMatrix);
            Calib3d.Rodrigues(rvec, mofRotMatrix);
            mMvpMatrix[0] = rotMatrix[0];
            mMvpMatrix[1] = rotMatrix[1];
            mMvpMatrix[2] = rotMatrix[2];
            mMvpMatrix[3] = 0.0f;
            mMvpMatrix[4] = rotMatrix[3];
            mMvpMatrix[5] = rotMatrix[4];
            mMvpMatrix[6] = rotMatrix[5] * -1.0f;
            mMvpMatrix[7] = 0.0f;
            mMvpMatrix[8] = rotMatrix[6];
            mMvpMatrix[9] = rotMatrix[7] * -1.0f;
            mMvpMatrix[10] = rotMatrix[8] * -1.0f;
            mMvpMatrix[11] = 0.0f;
            mMvpMatrix[12] = (float)tvec.get(0, 0)[0];
            mMvpMatrix[13] = -(float)tvec.get(0, 0)[1];
            mMvpMatrix[14] = -(float)tvec.get(0, 0)[2];
            mMvpMatrix[15] = 1.0f;

            // calculate the model-view-projection matrix
            Matrix.multiplyMM(mModelMatrix, 0, mCube.getTransform(), 0, mCube.getScaleMatrix(), 0);
            Matrix.multiplyMM(mMvpMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
            Matrix.multiplyMM(mMvpMatrix, 0, mProjectionMatrix, 0, mMvpMatrix, 0);

            // draw the cube
            GLES20.glUseProgram(mProgramId);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            GLES20.glUniform4fv(mColorHandle, 1, mCube.getColor(), 0);
            GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mMvpMatrix, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mCube.getNumVertices());
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
