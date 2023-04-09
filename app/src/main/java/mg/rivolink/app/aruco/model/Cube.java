package mg.rivolink.app.aruco.model;

import java.nio.*;
import javax.microedition.khronos.opengles.*;

/**
 * A vertex shaded cube.
 */
public class Cube
 {
	public Cube() {
		int one = 0x10000;
		int vertices[] = { -one, -one, -one, one, -one, -one, one, one, -one,
			-one, one, -one, -one, -one, one, one, -one, one, one, one,
			one, -one, one, one, };

		int colors[] = { 0, 0, 0, one, one, 0, 0, one, one, one, 0, one, 0,
			one, 0, one, 0, 0, one, one, one, 0, one, one, one, one, one,
			one, 0, one, one, one, };

		byte indices[] = { 0, 4, 5, 0, 5, 1, 1, 5, 6, 1, 6, 2, 2, 6, 7, 2, 7,
			3, 3, 7, 4, 3, 4, 0, 4, 7, 6, 4, 6, 5, 3, 0, 1, 3, 1, 2 };

		// Buffers to be passed to gl*Pointer() functions
		// must be direct, i.e., they must be placed on the
		// native heap where the garbage collector cannot
		// move them.
		//
		// Buffers with multi-byte datatypes (e.g., short, int, float)
		// must have their byte order set to native order

		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asIntBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asIntBuffer();
		mColorBuffer.put(colors);
		mColorBuffer.position(0);

		mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}

	public void draw(GL10 gl) {
		gl.glFrontFace(GL10.GL_CW);
		gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
		gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, 36, GL10.GL_UNSIGNED_BYTE,
						  mIndexBuffer);
	}

	private IntBuffer mVertexBuffer;
	private IntBuffer mColorBuffer;
	private ByteBuffer mIndexBuffer;
}

