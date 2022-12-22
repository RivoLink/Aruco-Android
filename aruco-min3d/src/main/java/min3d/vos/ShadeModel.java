package min3d.vos;

import javax.microedition.khronos.opengles.GL10;

public enum ShadeModel 
{
	SMOOTH (GL10.GL_SMOOTH),
	FLAT (GL10.GL_FLAT);

	
	private final int _glConstant;
	
	private ShadeModel(int $glConstant)
	{
		_glConstant = $glConstant;
	}
	
	public int glConstant()
	{
		return _glConstant;
	}
}

