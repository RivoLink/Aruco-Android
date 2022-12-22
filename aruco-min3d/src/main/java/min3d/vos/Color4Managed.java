package min3d.vos;

import java.nio.FloatBuffer;

import min3d.Utils;
import min3d.interfaces.IDirtyParent;

/**
 * Same functionality as Color4, but uses proper accessors to r,g,b, and a properties, 
 * rather than VO-style public variables, so that 'dirty flag' can be managed properly.
 * 
 * It is also backed by a FloatBuffer.
 */
public class Color4Managed extends AbstractDirtyManaged
{
	private short _r;
	private short _g;
	private short _b;
	private short _a;
	
	private FloatBuffer _fb;
	
	
	public Color4Managed(IDirtyParent $parent)
	{
		super($parent);
		
		_r = (short)255;
		_g = (short)255;
		_b = (short)255;
		_a = (short)255;

		_fb = this.toFloatBuffer();
		
		setDirtyFlag();
		
	}
	
	public Color4Managed(short $r, short $g, short $b, short $a, IDirtyParent $parent)
	{
		super($parent);
		
		_r = $r;
		_g = $g;
		_b = $b;
		_a = $a;

		_fb = this.toFloatBuffer();

		setDirtyFlag();
	}

	/**
	 * Convenience method which casts the int arguments to short for you. 
	 */
	public Color4Managed(int $r, int $g, int $b, int $a, IDirtyParent $parent)
	{
		super($parent);

		_r = (short)$r;
		_g = (short)$g;
		_b = (short)$b;
		_a = (short)$a;

		_fb = this.toFloatBuffer();

		setDirtyFlag();
	}

	/**
	 *  Convenience method to set all properties in one line.
	 */
	public void setAll(short $r, short $g, short $b, short $a)
	{
		_r = $r;
		_g = $g;
		_b = $b;
		_a = $a;

		setDirtyFlag();
	}
	
	public void setAll(int $r, int $g, int $b, int $a)
	{
		setAll((short)$r, (short)$g, (short)$b, (short)$a);
	}
	
	public Color4 toColor4()
	{
		return new Color4(_r,_g,_b,_a);
	}
	
	/**
	 * Convenience method to set all properties off one 32-bit rgba value 
	 */
	public void setAll(long $argb32)
	{
		_a = (short) (($argb32 >> 24) & 0x000000FF);
		_r = (short) (($argb32 >> 16) & 0x000000FF);
		_g = (short) (($argb32 >> 8) & 0x000000FF);
		_b = (short) (($argb32) & 0x000000FF);		
		
		setDirtyFlag();
	}
	
	public void setAll(Color4 $color)
	{
		setAll($color.r, $color.g, $color.b, $color.a);
	}

	public short r()
	{
		return _r;
	}
	public void r(short $r)
	{
		_r = $r;
		setDirtyFlag();
	}
	
	public short g()
	{
		return _g;
	}
	public void g(short $g)
	{
		_g = $g;
		setDirtyFlag();
	}
	
	public short b()
	{
		return _b;
	}
	public void b(short $b)
	{
		_b = $b;
		setDirtyFlag();
	}
	
	public short a()
	{
		return _a;
	}
	public void a(short $a)
	{
		_a = $a;
		setDirtyFlag();
	}

	/**
	 * Convenience method
	 */
	public FloatBuffer toFloatBuffer()
	{
		return Utils.makeFloatBuffer4(
			(float)r() / 255f,
			(float)g() / 255f,
			(float)b() / 255f,
			(float)a() / 255f
		);
	}

	/**
	 * Convenience method
	 */
	public void toFloatBuffer(FloatBuffer $floatBuffer)
	{
		$floatBuffer.position(0);
		$floatBuffer.put((float)r() / 255f);
		$floatBuffer.put((float)g() / 255f);
		$floatBuffer.put((float)b() / 255f);
		$floatBuffer.put((float)a() / 255f);
		$floatBuffer.position(0);
	}
	
	//
	
	/**
	 * Used by Renderer
	 */
	public FloatBuffer floatBuffer()
	{
		return _fb;
	}

	/**
	 * Used by Renderer
	 */
	public void commitToFloatBuffer()
	{
		this.toFloatBuffer(_fb);
	}

	@Override
	public String toString()
	{
		return "r:" + _r + ", g:" + _g + ", b:" + _b + ", a:" + _a;
	}

	
}
