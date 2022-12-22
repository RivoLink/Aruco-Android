package min3d.core;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import min3d.Min3d;
import min3d.vos.Light;

public class ManagedLightList 
{
	// List of Light objects
	private ArrayList<Light> _lights;

	// Map Light objects to GL_LIGHT indices
	private HashMap<Light, Integer> _lightToGlIndex;

	// 'Pool' of available GL_LIGHT id's
	private ArrayList<Integer> _availGlIndices;

	// Array of which GL_LIGHTS are enabled, where index corresponds to
	// GL_LIGHT[index]
	private boolean[] _glIndexEnabled;

	// Array of dirty flags, where index corresponds to GL_LIGHT[index]
	private boolean[] _glIndexEnabledDirty;

	// "GL index" here means an int from 0 to 8 that corresponds to
	// the int constants GL10.GL_LIGHT0 to GL10.GL_LIGHT7

	public ManagedLightList() 
	{
		reset();
	}

	public void reset() 
	{
		Log.i(Min3d.TAG, "ManagedLightList.reset()");

		_availGlIndices = new ArrayList<Integer>();
		for (int i = 0; i < Renderer.NUM_GLLIGHTS; i++) {
			_availGlIndices.add(i);
		}

		_lightToGlIndex = new HashMap<Light, Integer>();

		_glIndexEnabled = new boolean[Renderer.NUM_GLLIGHTS];
		_glIndexEnabledDirty = new boolean[Renderer.NUM_GLLIGHTS];
		for (int i = 0; i < Renderer.NUM_GLLIGHTS; i++) {
			_glIndexEnabled[i] = false;
			_glIndexEnabledDirty[i] = true;
		}

		_lights = new ArrayList<Light>();
	}

	public boolean add(Light $light) 
	{
		if (_lights.contains($light)) {
			return false;
		}

		if (_lights.size() > Renderer.NUM_GLLIGHTS)
			throw new Error("Exceeded maximum number of Lights");

		boolean result = _lights.add($light);

		int glIndex = _availGlIndices.remove(0);

		_lightToGlIndex.put($light, glIndex);

		_glIndexEnabled[glIndex] = true;
		_glIndexEnabledDirty[glIndex] = true;
		
		return result;
	}

	public void remove(Light $light) 
	{
		boolean result = _lights.remove($light);

		if (!result) return;

		int glIndex = _lightToGlIndex.get($light);
		
		_availGlIndices.add(glIndex);

		_glIndexEnabled[glIndex] = false;
		_glIndexEnabledDirty[glIndex] = true;
	}

	public void removeAll() 
	{
		reset();
	}

	public int size() 
	{
		return _lights.size();
	}

	public Light get(int $index) 
	{
		return _lights.get($index);
	}

	public Light[] toArray() {
		return (Light[]) _lights.toArray(new Light[_lights.size()]);
	}

	/**
	 * Used by Renderer
	 */
	int getGlIndexByLight(Light $light) /* package-private */
	{
		return _lightToGlIndex.get($light);
	}

	/**
	 * Used by Renderer
	 */
	Light getLightByGlIndex(int $glIndex) /* package-private */
	{
		for (int i = 0; i < _lights.size(); i++) 
		{
			Light light = _lights.get(i);
			if (_lightToGlIndex.get(light) == $glIndex)
				return light;
		}
		return null;
	}

	/**
	 * Used by Renderer
	 */
	boolean[] glIndexEnabledDirty() /* package-private */
	{
		return _glIndexEnabledDirty;
	}

	/**
	 * Used by Renderer
	 */
	boolean[] glIndexEnabled() /* package-private */
	{
		return _glIndexEnabled;
	}
}
