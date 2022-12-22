package min3d.vos;

import javax.microedition.khronos.opengles.GL10;

public enum FogType {
	LINEAR (GL10.GL_LINEAR),
	EXP (GL10.GL_EXP),
	EXP2 (GL10.GL_EXP2);
	
	private final int _glValue;
	
	private FogType(int $glValue)
	{
		_glValue = $glValue;
	}
	
	public int glValue()
	{
		return _glValue;
	}
}
