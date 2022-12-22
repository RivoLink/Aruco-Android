package min3d.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import min3d.vos.Color4;


public class Color4BufferList
{
	public static final int PROPERTIES_PER_ELEMENT = 4;
	public static final int BYTES_PER_PROPERTY = 1;

	private ByteBuffer _b;
	private int _numElements;
	
	public Color4BufferList(ByteBuffer $b, int $size)
	{
		_b = ByteBuffer.allocate($b.limit() * BYTES_PER_PROPERTY);
		_b.put($b);
		_numElements = $size;
	}
	
	public Color4BufferList(int $maxElements)
	{
		int numBytes = $maxElements * PROPERTIES_PER_ELEMENT * BYTES_PER_PROPERTY;
		_b = ByteBuffer.allocateDirect(numBytes); 
		_b.order(ByteOrder.nativeOrder());
	}
	
	/**
	 * The number of items in the list. 
	 */
	public int size()
	{
		return _numElements;
	}
	
	/**
	 * The _maximum_ number of items that the list can hold, as defined on instantiation.
	 * (Not to be confused with the Buffer's capacity)
	 */
	public int capacity()
	{
		return _b.capacity() / PROPERTIES_PER_ELEMENT;
	}
	
	/**
	 * Clear object in preparation for garbage collection
	 */
	public void clear()
	{
		_b.clear();
	}

	public Color4 getAsColor4(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return new Color4( _b.get(), _b.get(), _b.get(), _b.get() );
	}
	
	public void putInColor4(int $index, Color4 $color4)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		$color4.r = (short)_b.get();
		$color4.g = (short)_b.get();
		$color4.b = (short)_b.get();
		$color4.a = (short)_b.get();
	}

	public short getPropertyR(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return (short)_b.get();
	}
	public short getPropertyG(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		return (short)_b.get();
	}
	public float getPropertyB(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 2);
		return (short)_b.get();
	}
	public float getPropertyA(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 3);
		return (short)_b.get();
	}
	
	//
	
	public void add(Color4 $c)
	{
		set( _numElements, $c );
		_numElements++;
	}
	
	public void add(short $r, short $g, short $b, short $a)
	{
		set(_numElements, $r, $g, $b, $a);
		_numElements++;
	}
	
	public void set(int $index, Color4 $c)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put((byte)$c.r);
		_b.put((byte)$c.g);
		_b.put((byte)$c.b);
		_b.put((byte)$c.a);
		
		// Rem, OpenGL takes in color in this order: r,g,b,a -- _not_ a,r,g,b
	}

	public void set(int $index, short $r, short $g, short $b, short $a)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put((byte)$r);
		_b.put((byte)$g);
		_b.put((byte)$b);
		_b.put((byte)$a);
	}
	
	public void setPropertyR(int $index, short $r)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put((byte)$r);
	}
	public void setPropertyG(int $index, short $g)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		_b.put((byte)$g);
	}
	public void setPropertyB(int $index, short $b)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 2);
		_b.put((byte)$b);
	}
	public void setPropertyA(int $index, short $a)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 3);
		_b.put((byte)$a);
	}
	
	//
	
	public ByteBuffer buffer()
	{
		return _b;
	}
	
	public Color4BufferList clone()
	{
		_b.position(0);
		Color4BufferList c = new Color4BufferList(_b, size());
		return c;
	}
}
