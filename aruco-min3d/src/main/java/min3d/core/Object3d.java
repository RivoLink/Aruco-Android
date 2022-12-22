package min3d.core;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

import min3d.interfaces.IObject3dContainer;
import min3d.vos.Color4;
import min3d.vos.Number3d;
import min3d.vos.RenderType;
import min3d.vos.ShadeModel;

/**
 * @author Lee
 */
public class Object3d
{
	private String _name;
	
	private RenderType _renderType = RenderType.TRIANGLES;
	
	private boolean _isVisible = true;
	private boolean _vertexColorsEnabled = true;
	private boolean _doubleSidedEnabled = false;
	private boolean _texturesEnabled = true;
	private boolean _normalsEnabled = true;
	private boolean _ignoreFaces = false;
	private boolean _colorMaterialEnabled = false;
	private boolean _lightingEnabled = true;

	private Number3d _position = new Number3d(0,0,0);
	private Number3d _rotation = new Number3d(0,0,0);
	private Number3d _scale = new Number3d(1,1,1);

	private Color4 _defaultColor = new Color4();
	
	private ShadeModel _shadeModel = ShadeModel.SMOOTH;
	private float _pointSize = 3f;
	private boolean _pointSmoothing = true;
	private float _lineWidth = 1f;
	private boolean _lineSmoothing = false;

	
	protected ArrayList<Object3d> _children;
	
	protected Vertices _vertices; 
	protected TextureList _textures;
	protected FacesBufferedList _faces;

	protected boolean _animationEnabled = false;
	
	private Scene _scene;
	private IObject3dContainer _parent;

	/**
	 * Maximum number of vertices and faces must be specified at instantiation.
	 */
	public Object3d(int $maxVertices, int $maxFaces)
	{
		_vertices = new Vertices($maxVertices, true,true,true);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
	}
	
	/**
	 * Adds three arguments 
	 */
	public Object3d(int $maxVertices, int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		_vertices = new Vertices($maxVertices, $useUvs,$useNormals,$useVertexColors);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3d(Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		_vertices = $vertices;
		_faces = $faces;
		_textures = $textures;
	}
	
	/**
	 * Holds references to vertex position list, vertex u/v mappings list, vertex normals list, and vertex colors list
	 */
	public Vertices vertices()
	{
		return _vertices;
	}

	/**
	 * List of object's faces (ie, index buffer) 
	 */
	public FacesBufferedList faces()
	{
		return _faces;
	}
	
	public TextureList textures()
	{
		return _textures;
	}
	
	/**
	 * Determines if object will be rendered.
	 * Default is true. 
	 */
	public boolean isVisible()
	{
		return _isVisible;
	}
	public void isVisible(Boolean $b)
	{
		_isVisible = $b;
	}
	
	/**
	 * Determines if backfaces will be rendered (ie, doublesided = true).
	 * Default is false.
	 */
	public boolean doubleSidedEnabled()
	{
		return _doubleSidedEnabled;
	}
	public void doubleSidedEnabled(boolean $b)
	{
		_doubleSidedEnabled = $b;
	}
	
	/**
	 * Determines if object uses GL_COLOR_MATERIAL or not.
	 * Default is false.
	 */
	public boolean colorMaterialEnabled()
	{
		return _colorMaterialEnabled;
	}
	
	public boolean lightingEnabled() {
		return _lightingEnabled;
	}

	public void lightingEnabled(boolean $b) {
		this._lightingEnabled = $b;
	}

	public void colorMaterialEnabled(boolean $b)
	{
		_colorMaterialEnabled = $b;
	}

	/**
	 * Determines whether animation is enabled or not. If it is enabled
	 * then this should be an AnimationObject3d instance.
	 * This is part of the Object3d class so there's no need to cast
	 * anything during the render loop when it's not necessary.
	 */
	public boolean animationEnabled()
	{
		return _animationEnabled;
	}
	public void animationEnabled(boolean $b)
	{
		_animationEnabled = $b;
	}
	/**
	 * Determines if per-vertex colors will be using for rendering object.
	 * If false, defaultColor property will dictate object color.
	 * If object has no per-vertex color information, setting is ignored.
	 * Default is true. 
	 */
	public boolean vertexColorsEnabled()
	{
		return _vertexColorsEnabled;
	}
	public void vertexColorsEnabled(Boolean $b)
	{
		_vertexColorsEnabled = $b;
	}

	/**
	 * Determines if textures (if any) will used for rendering object.
	 * Default is true.  
	 */
	public boolean texturesEnabled()
	{
		return _texturesEnabled;
	}
	public void texturesEnabled(Boolean $b)
	{
		_texturesEnabled = $b;
	}
	
	/**
	 * Determines if object will be rendered using vertex light normals.
	 * If false, no lighting is used on object for rendering.
	 * Default is true.
	 */
	public boolean normalsEnabled()
	{
		return _normalsEnabled;
	}
	public void normalsEnabled(boolean $b)
	{
		_normalsEnabled = $b;
	}

	/**
	 * When true, Renderer draws using vertex points list, rather than faces list.
	 * (ie, using glDrawArrays instead of glDrawElements) 
	 * Default is false.
	 */
	public boolean ignoreFaces()
	{
		return _ignoreFaces;
	}
	public void ignoreFaces(boolean $b)
	{
		_ignoreFaces = $b;
	}	
	
	/**
	 * Options are: TRIANGLES, LINES, and POINTS
	 * Default is TRIANGLES.
	 */
	public RenderType renderType()
	{
		return _renderType;
	}
	public void renderType(RenderType $type)
	{
		_renderType = $type;
	}
	
	/**
	 * Possible values are ShadeModel.SMOOTH and ShadeModel.FLAT.
	 * Default is ShadeModel.SMOOTH.
	 * @return
	 */
	public ShadeModel shadeModel()
	{
		return _shadeModel;
	}
	public void shadeModel(ShadeModel $shadeModel)
	{
		_shadeModel = $shadeModel;
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList points()
	{
		return _vertices.points();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public UvBufferList uvs()
	{
		return _vertices.uvs();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList normals()
	{
		return _vertices.normals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Color4BufferList colors()
	{
		return _vertices.colors();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasUvs()
	{
		return _vertices.hasUvs();
	}

	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasNormals()
	{
		return _vertices.hasNormals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasVertexColors()
	{
		return _vertices.hasColors();
	}


	/**
	 * Clear object for garbage collection.
	 */
	public void clear()
	{
		if (this.vertices().points() != null) 	this.vertices().points().clear();
		if (this.vertices().uvs() != null) 		this.vertices().uvs().clear();
		if (this.vertices().normals() != null) 	this.vertices().normals().clear();
		if (this.vertices().colors() != null) 	this.vertices().colors().clear();
		if (_textures != null) 					_textures.clear();
		
		if (this.parent() != null) 				this.parent().removeChild(this);
	}

	//

	/**
	 * Color used to render object, but only when colorsEnabled is false.
	 */
	public Color4 defaultColor()
	{
		return _defaultColor;
	}
	
	public void defaultColor(Color4 color) {
		_defaultColor = color;
	}

	/**
	 * X/Y/Z position of object. 
	 */
	public Number3d position()
	{
		return _position;
	}
	
	/**
	 * X/Y/Z euler rotation of object, using Euler angles.
	 * Units should be in degrees, to match OpenGL usage. 
	 */
	public Number3d rotation()
	{
		return _rotation;
	}

	/**
	 * X/Y/Z scale of object.
	 */
	public Number3d scale()
	{
		return _scale;
	}
	
	/**
	 * Point size (applicable when renderType is POINT)
	 * Default is 3. 
	 */
	public float pointSize()
	{
		return _pointSize; 
	}
	public void pointSize(float $n)
	{
		_pointSize = $n;
	}

	/**
	 * Point smoothing (anti-aliasing), applicable when renderType is POINT.
	 * When true, points look like circles rather than squares.
	 * Default is true.
	 */
	public boolean pointSmoothing()
	{
		return _pointSmoothing;
	}
	public void pointSmoothing(boolean $b)
	{
		_pointSmoothing = $b;
	}

	/**
	 * Line width (applicable when renderType is LINE)
	 * Default is 1. 
	 * 
	 * Remember that maximum line width is OpenGL-implementation specific, and varies depending 
	 * on whether lineSmoothing is enabled or not. Eg, on Nexus One,  lineWidth can range from
	 * 1 to 8 without smoothing, and can only be 1f with smoothing. 
	 */
	public float lineWidth()
	{
		return _lineWidth;
	}
	public void lineWidth(float $n)
	{
		_lineWidth = $n;
	}
	
	/**
	 * Line smoothing (anti-aliasing), applicable when renderType is LINE
	 * Default is false.
	 */
	public boolean lineSmoothing()
	{
		return _lineSmoothing;
	}
	public void lineSmoothing(boolean $b)
	{
		_lineSmoothing = $b;
	}
	
	/**
	 * Convenience property 
	 */
	public String name()
	{
		return _name;
	}
	public void name(String $s)
	{
		_name = $s;
	}
	
	public IObject3dContainer parent()
	{
		return _parent;
	}
	
	//
	
	void parent(IObject3dContainer $container) /*package-private*/
	{
		_parent = $container;
	}
	
	/**
	 * Called by Scene
	 */
	void scene(Scene $scene) /*package-private*/
	{
		_scene = $scene;
	}
	/**
	 * Called by DisplayObjectContainer
	 */
	Scene scene() /*package-private*/
	{
		return _scene;
	}
	
	/**
	 * Can be overridden to create custom draw routines on a per-object basis, 
	 * rather than using Renderer's built-in draw routine. 
	 * 
	 * If overridden, return true instead of false.
	 */
	public Boolean customRenderer(GL10 gl)
	{
		return false;
	}
	
	public Object3d clone()
	{
		Vertices v = _vertices.clone();
		FacesBufferedList f = _faces.clone();
			
		Object3d clone = new Object3d(v, f, _textures);
		
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		
		return clone;
	}
}
