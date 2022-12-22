package min3d.objectPrimitives;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;


/**
 * Note how each 'face' (quad) of the box uses its own set of 4 vertices each, 
 * rather than sharing with adjacent faces.  This allows for each face to be 
 * texture mapped, normal'ed, and colored independently of the others. 
 * 
 * Object origin is center of box. 
 */
public class Box extends Object3dContainer
{
	private Color4[] _cols;
	private float _width;
	private float _height;
	private float _depth;
	

	public Box(float $width, float $height, float $depth, Color4[] $sixColor4s, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		super(4*6, 2*6, $useUvs,$useNormals,$useVertexColors);
		
		_width = $width;
		_height = $height;
		_depth = $depth;
		
		if ($sixColor4s != null)
		{
			_cols = $sixColor4s;
		}
		else
		{
			_cols = new Color4[6];
			_cols[0] = new Color4(255,0,0,255);
			_cols[1] = new Color4(0,255,0,255);
			_cols[2] = new Color4(0,0,255,255);
			_cols[3] = new Color4(255,255,0,255);
			_cols[4] = new Color4(0,255,255,255);
			_cols[5] = new Color4(255,0,255,255);
		}
		
		make();
	}
	
	public Box(float $width, float $height, float $depth, Color4[] $sixColor4s)
	{
		this($width,$height,$depth, $sixColor4s, true,true,true);
	}
	
	public Box(float $width, float $height, float $depth, Color4 color)
	{
		this($width,$height,$depth, new Color4[] { color, color, color, color, color, color }, true,true,true);
	}

	public Box(float $width, float $height, float $depth)
	{
		this($width,$height,$depth, null,  true,true,true);
	}

	private void make()
	{
		float w = _width / 2f;
		float h = _height / 2f;
		float d = _depth / 2f;

		short ul, ur, lr, ll;
		
		// front
		ul = this.vertices().addVertex(-w,+h,+d,	0f,0f,	0,0,1,	_cols[0].r,_cols[0].g,_cols[0].b,_cols[0].a);
		ur = this.vertices().addVertex(+w,+h,+d,	1f,0f,	0,0,1,	_cols[0].r,_cols[0].g,_cols[0].b,_cols[0].a);
		lr = this.vertices().addVertex(+w,-h,+d,	1f,1f,	0,0,1,	_cols[0].r,_cols[0].g,_cols[0].b,_cols[0].a);
		ll = this.vertices().addVertex(-w,-h,+d,	0f,1f,	0,0,1,	_cols[0].r,_cols[0].g,_cols[0].b,_cols[0].a);
		Utils.addQuad(this, ul,ur,lr,ll);
		
		// right
		ul = this.vertices().addVertex(+w,+h,+d,	0f,0f,	1,0,0,	_cols[1].r,_cols[1].g,_cols[1].b,_cols[1].a);
		ur = this.vertices().addVertex(+w,+h,-d,	1f,0f,	1,0,0,	_cols[1].r,_cols[1].g,_cols[1].b,_cols[1].a);
		lr = this.vertices().addVertex(+w,-h,-d,	1f,1f,	1,0,0,	_cols[1].r,_cols[1].g,_cols[1].b,_cols[1].a);
		ll = this.vertices().addVertex(+w,-h,+d,	0f,1f,	1,0,0,	_cols[1].r,_cols[1].g,_cols[1].b,_cols[1].a);
		Utils.addQuad(this, ul,ur,lr,ll);

		// back
		ul = this.vertices().addVertex(+w,+h,-d,	0f,0f,	0,0,-1,	_cols[2].r,_cols[2].g,_cols[2].b,_cols[2].a);
		ur = this.vertices().addVertex(-w,+h,-d,	1f,0f,	0,0,-1,	_cols[2].r,_cols[2].g,_cols[2].b,_cols[2].a);
		lr = this.vertices().addVertex(-w,-h,-d,	1f,1f,	0,0,-1,	_cols[2].r,_cols[2].g,_cols[2].b,_cols[2].a);
		ll = this.vertices().addVertex(+w,-h,-d,	0f,1f,	0,0,-1,	_cols[2].r,_cols[2].g,_cols[2].b,_cols[2].a);
		Utils.addQuad(this, ul,ur,lr,ll);
	
		// left
		ul = this.vertices().addVertex(-w,+h,-d,	0f,0f,	-1,0,0,	_cols[3].r,_cols[3].g,_cols[3].b,_cols[3].a);
		ur = this.vertices().addVertex(-w,+h,+d,	1f,0f,	-1,0,0,	_cols[3].r,_cols[3].g,_cols[3].b,_cols[3].a);
		lr = this.vertices().addVertex(-w,-h,+d,	1f,1f,	-1,0,0,	_cols[3].r,_cols[3].g,_cols[3].b,_cols[3].a);
		ll = this.vertices().addVertex(-w,-h,-d,	0f,1f,	-1,0,0,	_cols[3].r,_cols[3].g,_cols[3].b,_cols[3].a);
		Utils.addQuad(this, ul,ur,lr,ll);
		
		// top
		ul = this.vertices().addVertex(-w,+h,-d,	0f,0f,	0,1,0,	_cols[4].r,_cols[4].g,_cols[4].b,_cols[4].a);
		ur = this.vertices().addVertex(+w,+h,-d,	1f,0f,	0,1,0,	_cols[4].r,_cols[4].g,_cols[4].b,_cols[4].a);
		lr = this.vertices().addVertex(+w,+h,+d,	1f,1f,	0,1,0,	_cols[4].r,_cols[4].g,_cols[4].b,_cols[4].a);
		ll = this.vertices().addVertex(-w,+h,+d,	0f,1f,	0,1,0,	_cols[4].r,_cols[4].g,_cols[4].b,_cols[4].a);
		Utils.addQuad(this, ul,ur,lr,ll);

		// bottom
		ul = this.vertices().addVertex(-w,-h,+d,	0f,0f,	0,-1,0,	_cols[5].r,_cols[5].g,_cols[5].b,_cols[5].a);
		ur = this.vertices().addVertex(+w,-h,+d,	1f,0f,	0,-1,0,	_cols[5].r,_cols[5].g,_cols[5].b,_cols[5].a);
		lr = this.vertices().addVertex(+w,-h,-d,	1f,1f,	0,-1,0,	_cols[5].r,_cols[5].g,_cols[5].b,_cols[5].a);
		ll = this.vertices().addVertex(-w,-h,-d,	0f,1f,	0,-1,0,	_cols[5].r,_cols[5].g,_cols[5].b,_cols[5].a);
		Utils.addQuad(this, ul,ur,lr,ll);
	}
}
