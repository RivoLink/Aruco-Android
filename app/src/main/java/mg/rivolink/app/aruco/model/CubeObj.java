package mg.rivolink.app.aruco.model;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import mg.rivolink.app.aruco.R;
import mg.rivolink.app.aruco.loader.WavefrontObjMtlLoader;

public class CubeObj {

    private int mNumVertices;
    private IntBuffer mVertexBuffer;
    private IntBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;

    public CubeObj(Context context) {
        WavefrontObjMtlLoader parser = new WavefrontObjMtlLoader(context, R.raw.cube_obj, R.raw.cube_mtl);

        float[] vertices = parser.getVertices();
        int[] indices = parser.getIndices();
        int[] colors = parser.getColors(); // load colors from MTL file

        mNumVertices = indices.length;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asIntBuffer();

        for (int i = 0; i < vertices.length; i++) {
            mVertexBuffer.put((int) (vertices[i] * 0x10000));
        }
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asIntBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length * 4);
        mIndexBuffer.order(ByteOrder.nativeOrder());
        IntBuffer ib = mIndexBuffer.asIntBuffer();
        ib.put(indices);
        ib.position(0);
    }

    public void draw(GL10 gl) {
        gl.glFrontFace(GL10.GL_CW);
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, mNumVertices, GL10.GL_UNSIGNED_BYTE,
                mIndexBuffer);
    }
}
