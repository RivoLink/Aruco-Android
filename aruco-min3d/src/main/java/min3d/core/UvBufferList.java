package min3d.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import min3d.vos.Uv;


public class UvBufferList 
{
	public static final int PROPERTIES_PER_ELEMENT = 2;
	public static final int BYTES_PER_PROPERTY = 4;

	private FloatBuffer _b;
	private int _numElements = 0;
	
	public UvBufferList(FloatBuffer $b, int $size)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect($b.limit() * BYTES_PER_PROPERTY); 
		bb.order(ByteOrder.nativeOrder());
		_b = bb.asFloatBuffer();
		_b.put($b);
		_numElements = $size;
	}
	
	public UvBufferList(int $maxElements)
	{
		int numBytes = $maxElements * PROPERTIES_PER_ELEMENT * BYTES_PER_PROPERTY;
		ByteBuffer bb = ByteBuffer.allocateDirect(numBytes); 
		bb.order(ByteOrder.nativeOrder());
		
		_b  = bb.asFloatBuffer();
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
	
	public Uv getAsUv(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return new Uv( _b.get(), _b.get() );
	}
	
	public void putInUv(int $index, Uv $uv)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		$uv.u = _b.get();
		$uv.v = _b.get();
	}

	public float getPropertyU(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return _b.get();
	}
	public float getPropertyV(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		return _b.get();
	}
	
	//
	
	public void add(Uv $uv)
	{
		set( _numElements, $uv );
		_numElements++;
	}
	
	public void add(float $u, float $v)
	{
		set(_numElements, $u, $v);
		_numElements++;
	}
	
	public void set(int $index, Uv $uv)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($uv.u);
		_b.put($uv.v);
	}

	public void set(int $index, float $u, float $v)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($u);
		_b.put($v);
	}
	
	public void setPropertyU(int $index, float $u)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($u);
	}
	public void setPropertyV(int $index, float $v)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		_b.put($v);
	}
	
	//
	
	public FloatBuffer buffer()
	{
		return _b;
	}
	
	public UvBufferList clone()
	{
		_b.position(0);
		UvBufferList c = new UvBufferList(_b, size());
		return c;
	}
}
