package min3d.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import min3d.vos.Face;

public class FacesBufferedList
{
	public static final int PROPERTIES_PER_ELEMENT = 3;
	public static final int BYTES_PER_PROPERTY = 2;

	private ShortBuffer _b;
	private int _numElements;

	private int _renderSubsetStartIndex = 0;
	private int _renderSubsetLength = 1;
	private boolean _renderSubsetEnabled = false;
	
	public FacesBufferedList(ShortBuffer $b, int $size)
	{
		ByteBuffer bb = ByteBuffer.allocateDirect($b.limit() * BYTES_PER_PROPERTY); 
		bb.order(ByteOrder.nativeOrder());
		_b = bb.asShortBuffer();
		_b.put($b);
		_numElements = $size;
	}
	
	public FacesBufferedList(int $maxElements)
	{
		ByteBuffer b = ByteBuffer.allocateDirect($maxElements * PROPERTIES_PER_ELEMENT * BYTES_PER_PROPERTY); 
		b.order(ByteOrder.nativeOrder());
		_b = b.asShortBuffer();
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

	public Face get(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return new Face( _b.get(), _b.get(), _b.get() );
	}
	
	public void putInFace(int $index, Face $face)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		$face.a = (short)_b.get();
		$face.b = (short)_b.get();
		$face.c = (short)_b.get();
	}
	
	public short getPropertyA(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		return (short)_b.get();
	}
	public short getPropertyB(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		return (short)_b.get();
	}
	public float getPropertyC(int $index)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 2);
		return (short)_b.get();
	}

	/**
	 * Enables rendering only a subset of faces (renderSubset must be set to true) 
	 * This mechanism could be expanded to render multiple 'subsets' of the list of faces...
	 */
	public void renderSubsetStartIndex(int $num)
	{
		_renderSubsetStartIndex = $num;
	}
	public int renderSubsetStartIndex()
	{
		return _renderSubsetStartIndex;
	}
	public void renderSubsetLength(int $num)
	{
		_renderSubsetLength = $num;
	}
	public int renderSubsetLength()
	{
		return _renderSubsetLength;
	}
	
	/**
	 * If true, Renderer will only draw the faces as defined by 
	 * renderSubsetStartIndex and renderSubsetLength  
	 */
	public boolean renderSubsetEnabled()
	{
		return _renderSubsetEnabled;
	}
	public void renderSubsetEnabled(boolean $b)
	{
		_renderSubsetEnabled = $b;
	}
	
	//
	
	public void add(Face $f)
	{
		set( _numElements, $f );
		_numElements++;
	}
	
	public void add(int $a, int $b, int $c) {
		add((short)$a, (short)$b, (short)$c);
	}
	
	public void add(short $a, short $b, short $c)
	{
		set(_numElements, $a, $b, $c);
		_numElements++;
	}
	
	public void set(int $index, Face $face)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($face.a);
		_b.put($face.b);
		_b.put($face.c);
	}

	public void set(int $index, short $a, short $b, short $c)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($a);
		_b.put($b);
		_b.put($c);
	}
	
	public void setPropertyA(int $index, short $a)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT);
		_b.put($a);
	}
	public void setPropertyB(int $index, short $b)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 1);
		_b.put($b);
	}
	public void setPropertyC(int $index, short $c)
	{
		_b.position($index * PROPERTIES_PER_ELEMENT + 2);
		_b.put($c);
	}
	
	//
	
	public ShortBuffer buffer()
	{
		return _b;
	}
	
	public FacesBufferedList clone()
	{
		_b.position(0);
		FacesBufferedList c = new FacesBufferedList(_b, size());
		return c;
	}
}
