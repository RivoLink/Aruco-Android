package min3d.objectPrimitives;

import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Face;
import min3d.vos.Number3d;
import min3d.vos.Uv;

/**
 * Example of a more complex programmatically-drawn object.
 * 
 * Unfortunately, because the object's faces share as many vertices as possible,
 * shading is incorrect on the cylinder surfaces.
 *  
 * To fix would require duplicating vertices, adding new normals, 
 * and generally reworking algorithm. Yek.
 */
public class HollowCylinder extends Object3dContainer 
{
	private double DEG = Math.PI / 180;
	
	private int _segs;
	private float _radiusOuter;
	private float _radiusInner;
	private float _height;
	

	public HollowCylinder(float $radiusOuter, float $radiusInner, float $height, int $segs)
	{
		super($segs * 4, $segs * 8);
		
		_segs = $segs;
		_height = $height;
		_radiusOuter = $radiusOuter;
		_radiusInner = $radiusInner;
		
		addHorizontalSurface(false, _height / +2);
		addHorizontalSurface(true, _height / -2);
		addVerticalSurface(true);
		addVerticalSurface(false);
	}
	
	private void addHorizontalSurface(boolean $isTopSide, float $zOffset)
	{
		int indexOffset = _vertices.size();
		float step = (float)((360.0 / _segs) * DEG);

		// verts
		
		Color4 col = $isTopSide ? new Color4(255,0,0,255) : new Color4(0,255,0,255);
		
		for (int i = 0; i < _segs; i++)
		{
			float angle = (float)i * step;

			// outer 
			float x1 		= (float) Math.sin(angle) * _radiusOuter;
			float y1 		= (float) Math.cos(angle) * _radiusOuter;
			float z1 		= $zOffset; 
			Uv uv1 			= new Uv(x1,y1);
			Number3d n1 	= new Number3d(0,0, $isTopSide ? -1 : +1);
			this.vertices().addVertex(new Number3d(x1,y1,z1), uv1, n1, col);

			// inner 
			float x2 		= (float) Math.sin(angle) * _radiusInner;
			float y2 		= (float) Math.cos(angle) * _radiusInner;
			float z2 		= $zOffset; 
			Uv uv2			= new Uv(x2,y2);
			Number3d n2	= new Number3d(0,0, $isTopSide ? -1 : +1);
			this.vertices().addVertex(new Number3d(x2,y2,z2), uv2, n2, col);
		}
		
		// indicies
		
		for (int i = 2; i <= _segs; i++)
		{
			int a = indexOffset  +  i*2 - 3 - 1;
			int b = indexOffset  +  i*2 - 2 - 1;
			int c = indexOffset  +  i*2 - 1 - 1;
			int d = indexOffset  +  i*2 - 0 - 1;
			addQuad(a,b,c,d, $isTopSide);
		}		
	
		int a = indexOffset  +  _segs*2 - 1 - 1; // ... connect last segment
		int b = indexOffset  +  _segs*2 - 0 - 1;
		int c = indexOffset  +  0;
		int d = indexOffset  +  1;
		addQuad(a,b,c,d, $isTopSide);
	}
	
	// 
	private void addVerticalSurface(boolean $isOuter)
	{
		int off = (int)(_vertices.size() / 2); 
		
		for (int i = 0; i < _segs - 1; i++)
		{
			int ul = i*2;
			int bl = ul + off;
			int ur = i*2 + 2;
			int br = ur + off;
			
			if (!$isOuter) {
				ul++;
				bl++;
				ur++;
				br++;
			}
			addQuad(ul,bl,ur,br, $isOuter);
		}
		
		int ul = (_segs-1)*2;
		int bl = ul + off;
		int ur = 0*2;
		int br = ur + off;

		if (!$isOuter) {
			ul++;
			bl++;
			ur++;
			br++;
		}

		addQuad(ul,bl,ur,br, $isOuter);
	}
	
	private void addQuad(int ul, int bl, int ur, int br, boolean $flipped)
	{
		Face t;
		
		if (! $flipped)
		{
			_faces.add((short)ul,(short)bl,(short)ur);
			_faces.add((short)bl,(short)br,(short)ur);
		}
		else
		{
			_faces.add((short)ur,(short)br,(short)ul);
			_faces.add((short)br,(short)bl,(short)ul);
		}
	}
}
