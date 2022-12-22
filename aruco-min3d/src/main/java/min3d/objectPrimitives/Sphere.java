package min3d.objectPrimitives;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Number3d;


/**
 * Creates a sphere.
 * Vertex colors are assigned randomly across the 'latitudes' of the sphere,
 */
public class Sphere extends Object3dContainer
{
	private float _radius;
	private int _cols;
	private int _rows;
	
	
	public Sphere(float $radius, int $columns, int $rows, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		super(
			($columns+1) * ($rows+1),
			$columns * $rows * 2,
			$useUvs,
			$useNormals,
			$useVertexColors
		);

		_cols = $columns;
		_rows = $rows;
		_radius = $radius;

		build();
	}

	public Sphere(float $radius, int $columns, int $rows)
	{
		super(
				($columns+1) * ($rows+1),
				$columns * $rows * 2,
				true,
				true,
				true
			);

			_cols = $columns;
			_rows = $rows;
			_radius = $radius;
			
			build();
	} 
	
	public Sphere(float $radius, int $columns, int $rows, Color4 color)
	{
		super(
				($columns+1) * ($rows+1),
				$columns * $rows * 2,
				true,
				true,
				true
		);
		defaultColor(color);
		_cols = $columns;
		_rows = $rows;
		_radius = $radius;
		
		build();
	}
	
	private void build()
	{
		int r, c;
		
		Number3d n = new Number3d();
		Number3d pos = new Number3d();
		Number3d posFull = new Number3d();

		if( defaultColor() == null ) defaultColor(new Color4());
		// Build vertices
				
		for (r = 0; r <= _rows; r++)
		{
			float v = (float)r / (float)_rows; // [0,1]
			float theta1 = v * (float)Math.PI; // [0,PI]

			n.setAll(0,1,0);
			n.rotateZ(theta1); 

			// each 'row' assigned random color. for the hell of it.
			
			for (c = 0; c <= _cols; c++)
			{
				float u = (float)c / (float)_cols; // [0,1]
				float theta2 = u * (float)(Math.PI * 2f); // [0,2PI]
				pos.setAllFrom(n);
				pos.rotateY(theta2);
				
				posFull.setAllFrom(pos);
				posFull.multiply(_radius);
				
				
				this.vertices().addVertex(posFull.x,posFull.y,posFull.z,  u,v,  pos.x,pos.y,pos.z,  defaultColor().r,defaultColor().g,defaultColor().b,defaultColor().a);
			}
		}


		// Add faces

		int colLength = _cols + 1;
		
		for (r = 0; r < _rows; r++)
		{
			int offset = r * colLength; 
			
			for (c = 0; c < _cols; c++)
			{
				int ul = offset  +  c;
				int ur = offset  +  c+1;
				int br = offset  +  (int)(c + 1 + colLength);
				int bl = offset  +  (int)(c + 0 + colLength);
				
				Utils.addQuad(this, ul,ur,br,bl);
			}
		}
	}
}
