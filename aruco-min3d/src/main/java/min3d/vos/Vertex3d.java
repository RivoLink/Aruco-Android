package min3d.vos;


/**
 * Container holding VO's of vertex-related information.
 * Not required for operation of framework, but may be helpful as a convenience. 
 */
public class Vertex3d 
{
	public Number3d		position = new Number3d();
	public Uv 			uv;								
	public Number3d		normal;							
	public Color4 		color;					
	
	
	public Vertex3d()
	{
	}
}
