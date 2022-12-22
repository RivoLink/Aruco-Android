package min3d;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import min3d.core.Object3d;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Utils 
{
	public static final float DEG = (float)(Math.PI / 180f);
		
	private static final int BYTES_PER_FLOAT = 4;  
	
	/**
	 * Convenience method to create a Bitmap given a Context's drawable resource ID. 
	 */
	public static Bitmap makeBitmapFromResourceId(Context $context, int $id)
	{
		InputStream is = $context.getResources().openRawResource($id);
		
		Bitmap bitmap;
		try {
		   bitmap = BitmapFactory.decodeStream(is);
		} finally {
		   try {
		      is.close();
		   } catch(IOException e) {
		      // Ignore.
		   }
		}
	      
		return bitmap;
	}
	
	/**
	 * Convenience method to create a Bitmap given a drawable resource ID from the application Context. 
	 */
	public static Bitmap makeBitmapFromResourceId(int $id)
	{
		return makeBitmapFromResourceId(Shared.context(), $id);
	}
	
	/**
	 * Add two triangles to the Object3d's faces using the supplied indices
	 */
	public static void addQuad(Object3d $o, int $upperLeft, int $upperRight, int $lowerRight, int $lowerLeft)
	{
		$o.faces().add((short)$upperLeft, (short)$lowerRight, (short)$upperRight);
		$o.faces().add((short)$upperLeft, (short)$lowerLeft, (short)$lowerRight);
	}
	
	public static FloatBuffer makeFloatBuffer3(float $a, float $b, float $c)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(3 * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = b.asFloatBuffer();
		buffer.put($a);
		buffer.put($b);
		buffer.put($c);
		buffer.position(0);
		return buffer;
	}

	public static FloatBuffer makeFloatBuffer4(float $a, float $b, float $c, float $d)
	{
		ByteBuffer b = ByteBuffer.allocateDirect(4 * BYTES_PER_FLOAT);
		b.order(ByteOrder.nativeOrder());
		FloatBuffer buffer = b.asFloatBuffer();
		buffer.put($a);
		buffer.put($b);
		buffer.put($c);
		buffer.put($d);
		buffer.position(0);
		return buffer;
	}
}
