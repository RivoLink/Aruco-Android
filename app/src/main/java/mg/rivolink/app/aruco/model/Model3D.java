package mg.rivolink.app.aruco.model;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import org.apache.commons.io.IOUtils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import mg.rivolink.app.aruco.R;

public class Model3D {

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private List<String> verticesList;
    private List<String> facesList;

    private int program;

    public Model3D(Context context) {
        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(context.getAssets().open("torus.obj"));
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.startsWith("v ")) {
                    verticesList.add(line);
                } else if(line.startsWith("f ")) {
                    facesList.add(line);
                }
            }
            scanner.close();

            // Create buffer for vertices
            ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
            buffer1.order(ByteOrder.nativeOrder());
            verticesBuffer = buffer1.asFloatBuffer();

            // Create buffer for faces
            ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
            buffer2.order(ByteOrder.nativeOrder());
            facesBuffer = buffer2.asShortBuffer();

            for(String vertex: verticesList) {
                String coords[] = vertex.split(" ");
                float x = Float.parseFloat(coords[1]);
                float y = Float.parseFloat(coords[2]);
                float z = Float.parseFloat(coords[3]);
                verticesBuffer.put(x);
                verticesBuffer.put(y);
                verticesBuffer.put(z);
            }
            verticesBuffer.position(0);

            for(String face: facesList) {
                String vertexIndices[] = face.split(" ");
                short vertex1 = Short.parseShort(vertexIndices[1]);
                short vertex2 = Short.parseShort(vertexIndices[2]);
                short vertex3 = Short.parseShort(vertexIndices[3]);
                facesBuffer.put((short)(vertex1 - 1));
                facesBuffer.put((short)(vertex2 - 1));
                facesBuffer.put((short)(vertex3 - 1));
            }
            facesBuffer.position(0);

            InputStream vertexShaderStream = context.getResources().openRawResource(R.raw.vertex_shader);
            String vertexShaderCode = IOUtils.toString(vertexShaderStream, Charset.defaultCharset());
            vertexShaderStream.close();

            InputStream fragmentShaderStream = context.getResources().openRawResource(R.raw.fragment_shader);
            String fragmentShaderCode = IOUtils.toString(fragmentShaderStream, Charset.defaultCharset());
            fragmentShaderStream.close();

            int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, vertexShaderCode);

            int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

            GLES20.glCompileShader(vertexShader);
            GLES20.glCompileShader(fragmentShader);

            program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);

            GLES20.glUseProgram(program);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float[] getModelViewMatrix(/** Mat rvec, Mat tvec */){
        // double[] rvecArr = rvec.get(0, 0);
        // double[] tvecArr = tvec.get(0, 0);

        double[] rvecArr = {0.1234, -0.5678, 0.9123};
        double[] tvecArr = {1.2345, -2.3456, 3.4567};

        double[] rvec3 = Arrays.copyOfRange(rvecArr, 0, 3);
        double[] tvec3 = Arrays.copyOfRange(tvecArr, 0, 3);

        Mat rotation = new Mat();
        Calib3d.Rodrigues(new MatOfDouble(rvec3), rotation);

        float[] modelViewMatrix = new float[16];
        Matrix.setIdentityM(modelViewMatrix, 0);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                modelViewMatrix[i * 4 + j] = (float) rotation.get(i, j)[0];
            }
            modelViewMatrix[12 + i] = (float) tvec3[i];
        }

        return modelViewMatrix;
    }

    public void draw() {
        int position = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 3 * 4, verticesBuffer);

        float[] projectionMatrix = new float[16];
        Matrix.frustumM(projectionMatrix, 0, -1, 1, -1, 1, 2, 9);

        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0, 3, -4, 0, 0, 0, 0, 1, 0f);


        //double[] rvec3 = {0.1234, -0.5678, 0.9123};
        //double[] tvec3 = {1.2345, -2.3456, 3.4567};

        double[] rvec3 = {0.0, 0.0, 2.0};
        double[] tvec3 = {0.0, 0.0, 0.0};

        //Mat rotation = new Mat(3, 3, CvType.CV_32FC1, Scalar.all(0));
        Mat rotation = new Mat();
        Calib3d.Rodrigues(new MatOfDouble(rvec3), rotation);


        float[] modelViewMatrix = new float[16];
        Matrix.setIdentityM(modelViewMatrix, 0);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                modelViewMatrix[i * 4 + j] = (float) rotation.get(i, j)[0];
            }
            modelViewMatrix[12 + i] = (float) tvec3[i];
        }


        float[] productMatrix = new float[16];
        Matrix.multiplyMM(productMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(productMatrix, 0, productMatrix, 0, modelViewMatrix, 0);

        int matrix = GLES20.glGetUniformLocation(program, "matrix");
        GLES20.glUniformMatrix4fv(matrix, 1, false, productMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        GLES20.glDisableVertexAttribArray(position);
    }
}
