package min3d.interfaces;

import java.util.ArrayList;

import min3d.core.Object3d;

/**
 * Using Actionscript 3 nomenclature for what are essentially "pass-thru" methods to an underlying ArrayList  
 */
public interface IObject3dContainer 
{
	public void addChild(Object3d $child);
	public void addChildAt(Object3d $child, int $index);
	public boolean removeChild(Object3d $child);
	public Object3d removeChildAt(int $index);
	public Object3d getChildAt(int $index);
	public Object3d getChildByName(String $string);
	public int getChildIndexOf(Object3d $o);	
	public int numChildren();
}
