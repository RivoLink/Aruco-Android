package min3d.vos;

import min3d.interfaces.IDirtyManaged;
import min3d.interfaces.IDirtyParent;

/**
 * 'Managed' VO for the view frustrum. Used by Camera.
 */
public class FrustumManaged extends AbstractDirtyManaged 
{
	private float _shortSideLength;
	private float _horizontalCenter;
	private float _verticalCenter;
	private float _zNear;
	private float _zFar;
	
	
	public FrustumManaged(IDirtyParent $parent)
	{
		super($parent);
		
		_horizontalCenter = 0f;
		_verticalCenter = 0f;
		_shortSideLength = 1.0f;
		
		_zNear = 1.0f;
		_zFar = 100.0f;
	}

	public FrustumManaged(float $horizontalCenter, float $verticalCenter, float $shortSideLength, float $zNear, float $zFar, IDirtyParent $parent)
	{
		super($parent);
		
		_horizontalCenter = $horizontalCenter;
		_verticalCenter = $verticalCenter;
		_shortSideLength = $shortSideLength;
		
		_zNear = $zNear;
		_zFar = $zFar;
	}
	
	/**
	 * Defines the length of the shorter side of the horizontal and vertical dimensions. 
	 * (The longer side will be automatically adjusted to preserve pixel aspect ratio)
	 */
	public float shortSideLength() {
		return _shortSideLength;
	}

	public void shortSideLength(float shortSideLength) {
		_shortSideLength = shortSideLength;
		setDirtyFlag();
	}

	public float horizontalCenter() {
		return _horizontalCenter;
	}

	public void horizontalCenter(float horizontalCenter) {
		_horizontalCenter = horizontalCenter;
		setDirtyFlag();
	}

	public float verticalCenter() {
		return _verticalCenter;
	}

	public void verticalCenter(float verticalCenter) {
		_verticalCenter = verticalCenter;
		setDirtyFlag();
	}

	/**
	 * Corresponds to OpenGL glFrustumf param
	 */
	public float zNear() {
		return _zNear;
	}

	public void zNear(float zNear) {
		_zNear = zNear;
		setDirtyFlag();
	}

	/**
	 * Corresponds to OpenGL glFrustumf param
	 */
	public float zFar() {
		return _zFar;
	}

	public void zFar(float zFar) {
		_zFar = zFar;
		setDirtyFlag();
	}
	
	//
	
}
