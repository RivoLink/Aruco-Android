package min3d.core;

import java.util.ArrayList;

import min3d.Min3d;
import min3d.interfaces.IDirtyParent;
import min3d.interfaces.IObject3dContainer;
import min3d.interfaces.ISceneController;
import min3d.vos.CameraVo;
import min3d.vos.Color4;
import min3d.vos.Color4Managed;
import min3d.vos.FogType;
import android.util.Log;


public class Scene implements IObject3dContainer, IDirtyParent
{
	private ArrayList<Object3d> _children = new ArrayList<Object3d>();

	private ManagedLightList _lights;
	private CameraVo _camera;
	
	private Color4Managed _backgroundColor;
	private boolean _lightingEnabled;
	
	private Color4 _fogColor;
	private float _fogFar;
	private float _fogNear;
	private FogType _fogType;
	private boolean _fogEnabled;

	private ISceneController _sceneController;
	

	public Scene(ISceneController $sceneController) 
	{
		_sceneController = $sceneController;
		_lights = new ManagedLightList();
		_fogColor = new Color4(255, 255, 255, 255);
		_fogNear = 0;
		_fogFar = 10;
		_fogType = FogType.LINEAR;
		_fogEnabled = false;
	}

	/**
	 * Allows you to use any Class implementing ISceneController
	 * to drive the Scene...
	 * @return
	 */
	public ISceneController sceneController()
	{
		return _sceneController;
	}
	public void sceneController(ISceneController $sceneController)
	{
		_sceneController = $sceneController;
	}
	
	//
	
	/**
	 * Resets Scene to default settings.
	 * Removes and clears any attached Object3ds.
	 * Resets light list.
	 */
	public void reset()
	{
		clearChildren(this);

		_children = new ArrayList<Object3d>();

		_camera = new CameraVo();
		
		_backgroundColor = new Color4Managed(0,0,0,255, this);
		
		_lights = new ManagedLightList();
		
		lightingEnabled(true);
	}
	
	/**
	 * Adds Object3d to Scene. Object3d's must be added to Scene in order to be rendered
	 * Returns always true. 
	 */
	public void addChild(Object3d $o)
	{
		if (_children.contains($o)) return;
		
		_children.add($o);
		
		$o.parent(this);
		$o.scene(this);
	}
	
	public void addChildAt(Object3d $o, int $index)
	{
		if (_children.contains($o)) return;

		_children.add($index, $o);
	}
	
	/**
	 * Removes Object3d from Scene.
	 * Returns false if unsuccessful
	 */
	public boolean removeChild(Object3d $o)
	{
		$o.parent(null);
		$o.scene(null);
		return _children.remove($o);
	}
	
	public Object3d removeChildAt(int $index)
	{
		Object3d o = _children.remove($index);
		
		if (o != null) {
			o.parent(null);
			o.scene(null);
		}
		return o;
	}
	
	public Object3d getChildAt(int $index)
	{
		return _children.get($index);
	}
	
	/**
	 * TODO: Use better lookup 
	 */
	public Object3d getChildByName(String $name)
	{
		for (int i = 0; i < _children.size(); i++)
		{
			if (_children.get(0).name() == $name) return _children.get(0); 
		}
		return null;
	}
	
	public int getChildIndexOf(Object3d $o)
	{
		return _children.indexOf($o);
	}
	
	public int numChildren()
	{
		return _children.size();
	}

	/**
	 * Scene's camera
	 */
	public CameraVo camera()
	{
		return _camera;
	}
	public void camera(CameraVo $camera)
	{
		_camera = $camera;
	}
	
	/**
	 * Scene instance's background color
	 */
	public Color4Managed backgroundColor()
	{
		return _backgroundColor;
	}

	/**
	 * Lights used by the Scene 
	 */
	public ManagedLightList lights()
	{
		return _lights;
	}

	/**
	 * Determines if lighting is enabled for Scene. 
	 */
	public boolean lightingEnabled()
	{
		return _lightingEnabled;
	}
	
	public void lightingEnabled(boolean $b)
	{
		_lightingEnabled = $b;
	}
	
	//

	/*
	public boolean backgroundTransparent() {
		return _backgroundTransparent;
	}

	public void backgroundTransparent(boolean backgroundTransparent) {
		this._backgroundTransparent = backgroundTransparent;
	}
	*/

	public Color4 fogColor() {
		return _fogColor;
	}

	public void fogColor(Color4 _fogColor) {
		this._fogColor = _fogColor;
	}

	public float fogFar() {
		return _fogFar;
	}

	public void fogFar(float _fogFar) {
		this._fogFar = _fogFar;
	}

	public float fogNear() {
		return _fogNear;
	}

	public void fogNear(float _fogNear) {
		this._fogNear = _fogNear;
	}

	public FogType fogType() {
		return _fogType;
	}

	public void fogType(FogType _fogType) {
		this._fogType = _fogType;
	}

	public boolean fogEnabled() {
		return _fogEnabled;
	}

	public void fogEnabled(boolean _fogEnabled) {
		this._fogEnabled = _fogEnabled;
	}

	/**
	 * Used by Renderer 
	 */
	void init() /*package-private*/ 
	{
		Log.i(Min3d.TAG, "Scene.init()");
		
		this.reset();
		
		_sceneController.initScene();
		_sceneController.getInitSceneHandler().post(_sceneController.getInitSceneRunnable());
	}
	
	void update()
	{
		_sceneController.updateScene();
		_sceneController.getUpdateSceneHandler().post(_sceneController.getUpdateSceneRunnable());
	}
	
	/**
	 * Used by Renderer 
	 */
	ArrayList<Object3d> children() /*package-private*/ 
	{
		return _children;
	}
	
	private void clearChildren(IObject3dContainer $c)
	{
		for (int i = $c.numChildren() - 1; i >= 0; i--)
		{
			Object3d o = $c.getChildAt(i);
			o.clear();
			
			if (o instanceof Object3dContainer)
			{
				clearChildren((Object3dContainer)o);
			}
		}
	}	
	
	public void onDirty()
	{
		//
	}
}
