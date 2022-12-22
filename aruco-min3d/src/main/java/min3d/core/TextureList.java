package min3d.core;

import java.util.ArrayList;

import min3d.Shared;
import min3d.vos.TextureVo;

/**
 * Manages a list of TextureVo's used by Object3d's.
 * This allows an Object3d to use multiple textures. 
 * 
 * If more textures are added than what's supported by the hardware  
 * running the application, the extra items are ignored by Renderer
 * 
 * Uses a subset of ArrayList's methods. 
 */
public class TextureList  
{
	private ArrayList<TextureVo> _t;
	
	
	public TextureList()
	{
		_t = new ArrayList<TextureVo>();
	}
	
	/**
	 * Adds item to the list 
	 */
	public boolean add(TextureVo $texture)
	{
		if (! Shared.textureManager().contains($texture.textureId)) return false;
		return _t.add($texture);
	}
	
	/**
	 * Adds item at the given position to the list 
	 */
	public void add(int $index, TextureVo $texture)
	{
		_t.add($index, $texture);
	}
	
	/**
	 * Adds a new TextureVo with the given textureId to the list, and returns that textureVo  
	 */
	public TextureVo addById(String $textureId)
	{
		if (! Shared.textureManager().contains($textureId)) {
			throw new Error("Could not create TextureVo using textureId \"" + $textureId + "\". TextureManager does not contain that id.");
		}
		
		TextureVo t = new TextureVo($textureId);
		_t.add(t);
		return t;
	}
	
	/**
	 * Adds texture as the sole item in the list, replacing any existing items  
	 */
	public boolean addReplace(TextureVo $texture)
	{
		_t.clear();
		return _t.add($texture);
	}
	
	/**
	 * Removes item from the list 
	 */
	public boolean remove(TextureVo $texture)
	{
		return _t.remove($texture);
	}
	
	/**
	 * Removes item with the given textureId from the list 
	 */
	public boolean removeById(String $textureId)
	{
		TextureVo t = this.getById($textureId);
		if (t == null) {
			throw new Error("No match in TextureList for id \"" + $textureId + "\"");
		}
		return _t.remove(t);
	}
	
	public void removeAll()
	{
		for (int i = 0; i < _t.size(); i++)
			_t.remove(0);
	}
	
	/**
	 * Get item from the list which is at the given index position 
	 */
	public TextureVo get(int $index)
	{
		return _t.get($index);
	}
	
	/**
	 * Gets item from the list which has the given textureId
	 */
	public TextureVo getById(String $textureId)
	{
		for (int i = 0; i < _t.size(); i++) {
			String s = _t.get(i).textureId;
			if ($textureId == s) {
				TextureVo t = _t.get(i);
				return t;
			}
		}
		return null;
	}
	
	public int size()
	{
		return _t.size();
	}
	
	public void clear()
	{
		_t.clear();
	}
	
	/**
	 * Return a TextureVo array of TextureList's items 
	 */
	public TextureVo[] toArray()
	{
		Object[] a = _t.toArray();
		TextureVo[] ret = new TextureVo[a.length];
		for (int i = 0; i < _t.size(); i++)
		{
			ret[i] = (TextureVo)_t.get(i);
		}
		return ret;
	}
	
	/**
	 * Returns a String Array of the textureIds of each of the items in the list 
	 */
	public String[] getIds()
	{
		// BTW this makes a casting error. Why?
		// (TextureVo[])_t.toArray();

		String[] a = new String[_t.size()];
		for (int i = 0; i < _t.size(); i++)
		{
			a[i] = _t.get(i).textureId;
		}
		return a;
	}
}
