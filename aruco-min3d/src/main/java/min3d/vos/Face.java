package min3d.vos;

/**
 * Simple VO with three properties representing vertex indicies. 
 * Is not necessary for functioning of engine, just a convenience.
 */
public class Face 
{
	public short a;
	public short b;
	public short c;
	
	public Face(short $a, short $b, short $c)
	{
		a = $a;
		b = $b;
		c = $c;
	}
	
	/**
	 * Convenience method to cast int arguments to short's 
	 */
	public Face(int $a, int $b, int $c)
	{
		a = (short) $a;
		b = (short) $b;
		c = (short) $c;
	}
}
