package min3d.objectPrimitives;

import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Number3d;
import min3d.vos.Uv;
import min3d.vos.Vertex3d;

/**
 * @author Dennis Ippel
 * @author Thomas Pfeiffer
 * @author Tim Knip
 *
 * Torus primitive.
 * This is an adaptation from Sandy's ActionScript 3 class which was adapted from Tim Knip's 
 * ActionScript 2 class. All credits go to Tim Knip.
 * Original sources available at: http://www.suite75.net/svn/papervision3d/tim/as2/org/papervision3d/objects/Torus.as
 * Sandy source available at: http://code.google.com/p/sandy/source/browse/trunk/sandy/as3/trunk/src/sandy/primitive/Torus.as
 *
 */

public class Torus extends Object3dContainer {
	private final int MIN_SEGMENTSW = 3;
	private final int MIN_SEGMENTSH = 2;
	
	private float largeRadius;
	private float smallRadius;
	private int segmentsW;
	private int segmentsH;
	
	public Torus() {
		this(2, 1, 12, 8, new Color4());
	}
	
	public Torus(Color4 color) {
		this(2, 1, 12, 8, color);
	}
	
	public Torus(float largeRadius, float smallRadius, int segmentsW, int segmentsH) {
		this(largeRadius, smallRadius, segmentsW, segmentsH, new Color4());
	}
	
	public Torus(float largeRadius, float smallRadius, int segmentsW, int segmentsH, Color4 color) {
		super(segmentsW * segmentsH * 2 * 3, segmentsW * segmentsH * 2);
		this.largeRadius = largeRadius;
		this.smallRadius = smallRadius;
		this.segmentsW = Math.max(MIN_SEGMENTSW, segmentsW);
		this.segmentsH = Math.max(MIN_SEGMENTSH, segmentsH);
		this.defaultColor(color);
		build();
	}
	
	private void build()
	{
		float r1 = largeRadius;
		float r2 = smallRadius;
		int steps1 = segmentsW;
		int steps2 = segmentsH;
		float step1r = (float) ((2.0 * Math.PI) / steps1);
		float step2r = (float) ((2.0 * Math.PI) / steps2);
		float a1a = 0;
		float a1b = step1r;
		int vcount = 0;
		
		for(float s=0; s<steps1; s++, a1a=a1b, a1b+=step1r) {
			float a2a = 0;
			float a2b = step2r;
			
			for(float s2=0; s2<steps2; s2++, a2a=a2b, a2b+=step2r) {
				Vertex3d v0 = getVertex(a1a, r1, a2a, r2);
				Vertex3d v1 = getVertex(a1b, r1, a2a, r2);
				Vertex3d v2 = getVertex(a1b, r1, a2b, r2);
				Vertex3d v3 = getVertex(a1a, r1, a2b, r2);
				
				float ux1 = s/steps1;
				float ux0 = (s+1)/steps1;
				float uy0 = s2/steps2;
				float uy1 = (s2+1)/steps2;

				vertices().addVertex(v0.position, new Uv(1-ux1, uy0), v0.normal, defaultColor());
				vertices().addVertex(v1.position, new Uv(1-ux0, uy0), v1.normal, defaultColor());
				vertices().addVertex(v2.position, new Uv(1-ux0, uy1), v2.normal, defaultColor());
				vertices().addVertex(v3.position, new Uv(1-ux1, uy1), v3.normal, defaultColor());
				
				faces().add(vcount, vcount+1, vcount+2);
				faces().add(vcount, vcount+2, vcount+3);
				
				vcount += 4;
			}
		}
	}
	
	private Vertex3d getVertex(float a1, float r1, float a2, float r2) {
		Vertex3d vertex = new Vertex3d();
		vertex.normal = new Number3d();
		
		float ca1 = (float)Math.cos(a1);
		float sa1 = (float)Math.sin(a1);
		float ca2 = (float)Math.cos(a2);
		float sa2 = (float)Math.sin(a2);
		
		float centerX = r1 * ca1;
		float centerZ = -r1 * sa1;
		
		vertex.normal.x = ca2 * ca1;
		vertex.normal.y = sa2;
		vertex.normal.z = -ca2 * sa1;
		
		vertex.position.x = centerX + r2 * vertex.normal.x;
		vertex.position.y = r2 * vertex.normal.y;
		vertex.position.z = centerZ + r2 * vertex.normal.z;
				
		return vertex;
	}
}
