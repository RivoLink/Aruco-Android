package min3d.vos;

import javax.microedition.khronos.opengles.GL10;

public enum RenderType 
{
	POINTS (GL10.GL_POINTS),
	LINES (GL10.GL_LINES),
	LINE_LOOP (GL10.GL_LINE_LOOP),
	LINE_STRIP (GL10.GL_LINE_STRIP),
	TRIANGLES (GL10.GL_TRIANGLES),
	TRIANGLE_STRIP (GL10.GL_TRIANGLE_STRIP),
	TRIANGLE_FAN (GL10.GL_TRIANGLE_FAN);
	
	private final int _glValue;
	
	private RenderType(int $glValue)
	{
		_glValue = $glValue;
	}
	
	public int glValue()
	{
		return _glValue;
	}
}