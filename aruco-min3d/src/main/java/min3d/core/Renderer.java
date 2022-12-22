package min3d.core;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import min3d.Min3d;
import min3d.Shared;
import min3d.animation.AnimationObject3d;
import min3d.vos.FrustumManaged;
import min3d.vos.Light;
import min3d.vos.RenderType;
import min3d.vos.TextureVo;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;


public class Renderer implements GLSurfaceView.Renderer
{
	public static final int NUM_GLLIGHTS = 8;

	private GL10 _gl;
	private Scene _scene;
	private TextureManager _textureManager;

	private float _surfaceAspectRatio;
	
	private IntBuffer _scratchIntBuffer;
	private FloatBuffer _scratchFloatBuffer;
	private boolean _scratchB;
	

	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 1000; 
	private boolean _logFps = false;
	private long _frameCount = 0;
	private float _fps = 0;
	private long _timeLastSample;
	private ActivityManager _activityManager;
	private ActivityManager.MemoryInfo _memoryInfo;


	public Renderer(Scene $scene)
	{
		_scene = $scene;

		_scratchIntBuffer = IntBuffer.allocate(4);
		_scratchFloatBuffer = FloatBuffer.allocate(4);
		
		_textureManager = new TextureManager();
		Shared.textureManager(_textureManager); 
		
		_activityManager = (ActivityManager) Shared.context().getSystemService( Context.ACTIVITY_SERVICE );
		_memoryInfo = new ActivityManager.MemoryInfo();
	}

	public void onSurfaceCreated(GL10 $gl, EGLConfig eglConfig) 
	{
		Log.i(Min3d.TAG, "Renderer.onSurfaceCreated()");
		
		RenderCaps.setRenderCaps($gl);
		
		setGl($gl);

		reset();
		
		_scene.init();
	}
	
	public void onSurfaceChanged(GL10 gl, int w, int h) 
	{
		Log.i(Min3d.TAG, "Renderer.onSurfaceChanged()");
		
		setGl(_gl);
		_surfaceAspectRatio = (float)w / (float)h;
		
		_gl.glViewport(0, 0, w, h);
		_gl.glMatrixMode(GL10.GL_PROJECTION);
		_gl.glLoadIdentity();
		
		updateViewFrustrum();
	}
	
	public void onDrawFrame(GL10 gl)
	{
		// Update 'model'
		_scene.update();
		
		// Update 'view'
		drawSetup();
		drawScene();

		if (_logFps) doFps();
	}
	
	//
	
	/**
	 *  Accessor to the GL object, in case anything outside this class wants to do 
	 *  bad things with it :)
	 */
	public GL10 gl()
	{
		return _gl;
	}

	/**
	 * Returns last sampled framerate (logFps must be set to true) 
	 */
	public float fps()
	{
		return _fps;
	}
	/**
	 * Return available system memory in bytes
	 */
	public long availMem()
	{
		_activityManager.getMemoryInfo(_memoryInfo);
		return _memoryInfo.availMem;
	}
	
	protected void drawSetup()
	{
		// View frustrum
		
		if (_scene.camera().frustum.isDirty()) {
			updateViewFrustrum();
		}
		 
		// Camera 
		
		_gl.glMatrixMode(GL10.GL_MODELVIEW);
		_gl.glLoadIdentity();

		GLU.gluLookAt(_gl, 
			_scene.camera().position.x,_scene.camera().position.y,_scene.camera().position.z,
			_scene.camera().target.x,_scene.camera().target.y,_scene.camera().target.z,
			_scene.camera().upAxis.x,_scene.camera().upAxis.y,_scene.camera().upAxis.z);
		
		// Background color
		
		if (_scene.backgroundColor().isDirty())
		{
			_gl.glClearColor( 
				(float)_scene.backgroundColor().r() / 255f, 
				(float)_scene.backgroundColor().g() / 255f, 
				(float)_scene.backgroundColor().b() / 255f, 
				(float)_scene.backgroundColor().a() / 255f);
			_scene.backgroundColor().clearDirtyFlag();
		}
		
		_gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		drawSetupLights();
		
		// Always on:
		_gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	}
	
	protected void drawSetupLights()
	{
		// GL_LIGHTS enabled/disabled based on enabledDirty list
		for (int glIndex = 0; glIndex < NUM_GLLIGHTS; glIndex++)
		{
			if (_scene.lights().glIndexEnabledDirty()[glIndex] == true)
			{
				if (_scene.lights().glIndexEnabled()[glIndex] == true) 
				{
					_gl.glEnable(GL10.GL_LIGHT0 + glIndex);
					
					// make light's properties dirty to force update
					_scene.lights().getLightByGlIndex(glIndex).setAllDirty();
				} 
				else 
				{
					_gl.glDisable(GL10.GL_LIGHT0 + glIndex);
				}
				
				_scene.lights().glIndexEnabledDirty()[glIndex] = false; // clear dirtyflag
			}
		}
		
		// Lights' properties 

		Light[] lights = _scene.lights().toArray();
		for (int i = 0; i < lights.length; i++)
		{
			Light light = lights[i];
			
			if (light.isDirty()) // .. something has changed
			{
				// Check all of Light's properties for dirty 
				
				int glLightId = GL10.GL_LIGHT0 + _scene.lights().getGlIndexByLight(light);
				
				if (light.position.isDirty())
				{
					light.commitPositionAndTypeBuffer();
					_gl.glLightfv(glLightId, GL10.GL_POSITION, light._positionAndTypeBuffer);
					light.position.clearDirtyFlag();
				}
				if (light.ambient.isDirty()) 
				{
					light.ambient.commitToFloatBuffer();
					_gl.glLightfv(glLightId, GL10.GL_AMBIENT, light.ambient.floatBuffer());
					light.ambient.clearDirtyFlag();
				}
				if (light.diffuse.isDirty()) 
				{
					light.diffuse.commitToFloatBuffer();
					_gl.glLightfv(glLightId, GL10.GL_DIFFUSE, light.diffuse.floatBuffer());
					light.diffuse.clearDirtyFlag();
				}
				if (light.specular.isDirty())
				{
					light.specular.commitToFloatBuffer();
					_gl.glLightfv(glLightId, GL10.GL_SPECULAR, light.specular.floatBuffer());
					light.specular.clearDirtyFlag();
				}
				if (light.emissive.isDirty())
				{
					light.emissive.commitToFloatBuffer();
					_gl.glLightfv(glLightId, GL10.GL_EMISSION, light.emissive.floatBuffer());
					light.emissive.clearDirtyFlag();
				}

				if (light.direction.isDirty())
				{
					light.direction.commitToFloatBuffer();
					_gl.glLightfv(glLightId, GL10.GL_SPOT_DIRECTION, light.direction.floatBuffer());
					light.direction.clearDirtyFlag();
				}
				if (light._spotCutoffAngle.isDirty())
				{
					_gl.glLightf(glLightId, GL10.GL_SPOT_CUTOFF, light._spotCutoffAngle.get());
				}
				if (light._spotExponent.isDirty())
				{
					_gl.glLightf(glLightId, GL10.GL_SPOT_EXPONENT, light._spotExponent.get());
				}

				if (light._isVisible.isDirty()) 
				{
					if (light.isVisible()) {
						_gl.glEnable(glLightId);
					} else {
						_gl.glDisable(glLightId);
					}
					light._isVisible.clearDirtyFlag();
				}

				if (light._attenuation.isDirty())
				{
					_gl.glLightf(glLightId, GL10.GL_CONSTANT_ATTENUATION, light._attenuation.getX());
					_gl.glLightf(glLightId, GL10.GL_LINEAR_ATTENUATION, light._attenuation.getY());
					_gl.glLightf(glLightId, GL10.GL_QUADRATIC_ATTENUATION, light._attenuation.getZ());
				}
				
				light.clearDirtyFlag();
			}
		}
	}

	protected void drawScene()
	{
		if(_scene.fogEnabled() == true) {
			_gl.glFogf(GL10.GL_FOG_MODE, _scene.fogType().glValue());
			_gl.glFogf(GL10.GL_FOG_START, _scene.fogNear());
			_gl.glFogf(GL10.GL_FOG_END, _scene.fogFar());
			_gl.glFogfv(GL10.GL_FOG_COLOR, _scene.fogColor().toFloatBuffer() );
			_gl.glEnable(GL10.GL_FOG);
		} else {
			_gl.glDisable(GL10.GL_FOG);
		}

		for (int i = 0; i < _scene.children().size(); i++)
		{
			Object3d o = _scene.children().get(i);
			if(o.animationEnabled())
			{
				((AnimationObject3d)o).update();
			}
			drawObject(o);
		}		
	}
	
	//boolean customResult = o.customRenderer(_gl); 
	//if (customResult) return;


	protected void drawObject(Object3d $o)
	{
		if ($o.isVisible() == false) return;		

		// Various per-object settings:
		
		// Normals

		if ($o.hasNormals() && $o.normalsEnabled()) {
			$o.vertices().normals().buffer().position(0);
			_gl.glNormalPointer(GL10.GL_FLOAT, 0, $o.vertices().normals().buffer());
			_gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		}
		else {
			_gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}
		
		// Is lighting enabled for object...
		
		/*
		// *** this version not working properly on emulator - why not? ***
		_scratchIntBuffer.position(0);
		_gl.glGetIntegerv(GL10.GL_LIGHTING, _scratchIntBuffer);
		if (useLighting != _scratchIntBuffer.get(0))
		{
			if (useLighting == 1) {
				_gl.glEnable(GL10.GL_LIGHTING);
			} else {
				_gl.glDisable(GL10.GL_LIGHTING);
			}
		}
		*/
		
		boolean useLighting = (_scene.lightingEnabled() && $o.hasNormals() && $o.normalsEnabled() && $o.lightingEnabled());
		if (useLighting) {
			_gl.glEnable(GL10.GL_LIGHTING);
		} else {
			_gl.glDisable(GL10.GL_LIGHTING);
		}
		
		// Shademodel
		
		_gl.glGetIntegerv(GL11.GL_SHADE_MODEL, _scratchIntBuffer);
		if ($o.shadeModel().glConstant() != _scratchIntBuffer.get(0)) {
			_gl.glShadeModel($o.shadeModel().glConstant());
		}
		
		// Colors: either per-vertex, or per-object

		if ($o.hasVertexColors() && $o.vertexColorsEnabled()) {
			$o.vertices().colors().buffer().position(0);
			_gl.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, $o.vertices().colors().buffer());
			_gl.glEnableClientState(GL10.GL_COLOR_ARRAY); 
		}
		else {
			_gl.glColor4f(
				(float)$o.defaultColor().r / 255f, 
				(float)$o.defaultColor().g / 255f, 
				(float)$o.defaultColor().b / 255f, 
				(float)$o.defaultColor().a / 255f
			);
			_gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}
		
		// Colormaterial
		
		_gl.glGetIntegerv(GL10.GL_COLOR_MATERIAL, _scratchIntBuffer);
		_scratchB = (_scratchIntBuffer.get(0) != 0);
		if ($o.colorMaterialEnabled() != _scratchB) {
			if ($o.colorMaterialEnabled())
				_gl.glEnable(GL10.GL_COLOR_MATERIAL);
			else
				_gl.glDisable(GL10.GL_COLOR_MATERIAL);
		}
		
		// Point size
		
		if ($o.renderType() == RenderType.POINTS) 
		{
			if ($o.pointSmoothing()) 
				_gl.glEnable(GL10.GL_POINT_SMOOTH);
			else
				_gl.glDisable(GL10.GL_POINT_SMOOTH);
			
			_gl.glPointSize($o.pointSize());
		}

		// Line properties
		
		if ($o.renderType() == RenderType.LINES || $o.renderType() == RenderType.LINE_STRIP || $o.renderType() == RenderType.LINE_LOOP) 
		{
			if ( $o.lineSmoothing() == true) {
				_gl.glEnable(GL10.GL_LINE_SMOOTH);
			}
			else {
				_gl.glDisable(GL10.GL_LINE_SMOOTH);
			}

			_gl.glLineWidth($o.lineWidth());
		}

		// Backface culling 
		
		if ($o.doubleSidedEnabled()) {
		    _gl.glDisable(GL10.GL_CULL_FACE);
		} 
		else {
		    _gl.glEnable(GL10.GL_CULL_FACE);
		}
		

		drawObject_textures($o);

		
		// Matrix operations in modelview

		_gl.glPushMatrix();
		
		_gl.glTranslatef($o.position().x, $o.position().y, $o.position().z);
		
		_gl.glRotatef($o.rotation().x, 1,0,0);
		_gl.glRotatef($o.rotation().y, 0,1,0);
		_gl.glRotatef($o.rotation().z, 0,0,1);
		
		_gl.glScalef($o.scale().x, $o.scale().y, $o.scale().z);
		
		// Draw

		$o.vertices().points().buffer().position(0);
		_gl.glVertexPointer(3, GL10.GL_FLOAT, 0, $o.vertices().points().buffer());

		if (! $o.ignoreFaces())
		{
			int pos, len;
			
			if (! $o.faces().renderSubsetEnabled()) {
				pos = 0;
				len = $o.faces().size();
			}
			else {
				pos = $o.faces().renderSubsetStartIndex() * FacesBufferedList.PROPERTIES_PER_ELEMENT;
				len = $o.faces().renderSubsetLength();
			}

			$o.faces().buffer().position(pos);

			_gl.glDrawElements(
					$o.renderType().glValue(),
					len * FacesBufferedList.PROPERTIES_PER_ELEMENT, 
					GL10.GL_UNSIGNED_SHORT, 
					$o.faces().buffer());
		}
		else
		{
			_gl.glDrawArrays($o.renderType().glValue(), 0, $o.vertices().size());
		}
		
		//
		// Recurse on children
		//
		
		if ($o instanceof Object3dContainer)
		{
			Object3dContainer container = (Object3dContainer)$o;
			
			for (int i = 0; i < container.children().size(); i++)
			{
				Object3d o = container.children().get(i);
				drawObject(o);
			}
		}
		
		// Restore matrix
		
		_gl.glPopMatrix();
	}
	
	private void drawObject_textures(Object3d $o)
	{
		// iterate thru object's textures
		
		for (int i = 0; i < RenderCaps.maxTextureUnits(); i++)
		{
			_gl.glActiveTexture(GL10.GL_TEXTURE0 + i);
			_gl.glClientActiveTexture(GL10.GL_TEXTURE0 + i); 

			if ($o.hasUvs() && $o.texturesEnabled())
			{
				$o.vertices().uvs().buffer().position(0);
				_gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, $o.vertices().uvs().buffer());

				TextureVo textureVo = ((i < $o.textures().size())) ? textureVo = $o.textures().get(i) : null;

				if (textureVo != null)
				{
					// activate texture
					int glId = _textureManager.getGlTextureId(textureVo.textureId);
					_gl.glBindTexture(GL10.GL_TEXTURE_2D, glId);
				    _gl.glEnable(GL10.GL_TEXTURE_2D);
					_gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

					int minFilterType = _textureManager.hasMipMap(textureVo.textureId) ? GL10.GL_LINEAR_MIPMAP_NEAREST : GL10.GL_NEAREST; 
					_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilterType);
					_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // (OpenGL default)
					
					// do texture environment settings
					for (int j = 0; j < textureVo.textureEnvs.size(); j++)
					{
						_gl.glTexEnvx(GL10.GL_TEXTURE_ENV, textureVo.textureEnvs.get(j).pname, textureVo.textureEnvs.get(j).param);
					}
					
					// texture wrapping settings
					_gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, (textureVo.repeatU ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE));
					_gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, (textureVo.repeatV ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE));		

					// texture offset, if any
					if (textureVo.offsetU != 0 || textureVo.offsetV != 0)
					{
						_gl.glMatrixMode(GL10.GL_TEXTURE);
						_gl.glLoadIdentity();
						_gl.glTranslatef(textureVo.offsetU, textureVo.offsetV, 0);
						_gl.glMatrixMode(GL10.GL_MODELVIEW); // .. restore matrixmode
					}
				}
				else
				{
					_gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
				    _gl.glDisable(GL10.GL_TEXTURE_2D);
					_gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				}
			}
			else
			{
				_gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
			    _gl.glDisable(GL10.GL_TEXTURE_2D);
				_gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			}
		}
	}
	
	/**
	 * Used by TextureManager
	 */
	int uploadTextureAndReturnId(Bitmap $bitmap, boolean $generateMipMap) /*package-private*/
	{
		int glTextureId;
		
		int[] a = new int[1];
		_gl.glGenTextures(1, a, 0); // create a 'texture name' and put it in array element 0
		glTextureId = a[0];
		_gl.glBindTexture(GL10.GL_TEXTURE_2D, glTextureId);
		
		if($generateMipMap && _gl instanceof GL11) {
			_gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		} else {
			_gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
		}

		// 'upload' to gpu
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, $bitmap, 0);
		
		return glTextureId;
	}
	

	/**
	 * Used by TextureManager
	 */
	void deleteTexture(int $glTextureId) /*package-private*/
	{
		int[] a = new int[1];
		a[0] = $glTextureId;
		_gl.glDeleteTextures(1, a, 0);
	}
	
	protected void updateViewFrustrum()
	{
		FrustumManaged vf = _scene.camera().frustum;
		float n = vf.shortSideLength() / 2f;

		float lt, rt, btm, top;
		
		lt  = vf.horizontalCenter() - n*_surfaceAspectRatio;
		rt  = vf.horizontalCenter() + n*_surfaceAspectRatio;
		btm = vf.verticalCenter() - n*1; 
		top = vf.verticalCenter() + n*1;

		if (_surfaceAspectRatio > 1) {
			lt *= 1f/_surfaceAspectRatio;
			rt *= 1f/_surfaceAspectRatio;
			btm *= 1f/_surfaceAspectRatio;
			top *= 1f/_surfaceAspectRatio;
		}
		
		_gl.glMatrixMode(GL10.GL_PROJECTION);
		_gl.glLoadIdentity();
		_gl.glFrustumf(lt,rt, btm,top, vf.zNear(), vf.zFar());
		
		vf.clearDirtyFlag();
	}

	/**
	 * If true, framerate and memory is periodically calculated and Log'ed,
	 * and gettable thru fps() 
	 */
	public void logFps(boolean $b)
	{
		_logFps = $b;
		
		if (_logFps) { // init
			_timeLastSample = System.currentTimeMillis();
			_frameCount = 0;
		}
	}
	
	private void setGl(GL10 $gl)
	{
		_gl = $gl;
	}
	
	private void doFps()
	{
		_frameCount++;

		long now = System.currentTimeMillis();
		long delta = now - _timeLastSample;
		if (delta >= FRAMERATE_SAMPLEINTERVAL_MS)
		{
			_fps = _frameCount / (delta/1000f); 

			_activityManager.getMemoryInfo(_memoryInfo);
			Log.v(Min3d.TAG, "FPS: " + Math.round(_fps) + ", availMem: " + Math.round(_memoryInfo.availMem/1048576) + "MB");

			_timeLastSample = now;
			_frameCount = 0;
		}
	}
	
	private void reset()
	{
		// Reset TextureManager
		Shared.textureManager().reset();

		// Do OpenGL settings which we are using as defaults, or which we will not be changing on-draw
		
	    // Explicit depth settings
		_gl.glEnable(GL10.GL_DEPTH_TEST);									
		_gl.glClearDepthf(1.0f);
		_gl.glDepthFunc(GL10.GL_LESS);										
		_gl.glDepthRangef(0,1f);											
		_gl.glDepthMask(true);												

		// Alpha enabled
		_gl.glEnable(GL10.GL_BLEND);										
		_gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); 	
		
		// "Transparency is best implemented using glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) 
		// with primitives sorted from farthest to nearest."

		// Texture
		_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST); // (OpenGL default is GL_NEAREST_MIPMAP)
		_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // (is OpenGL default)
		
		// CCW frontfaces only, by default
		_gl.glFrontFace(GL10.GL_CCW);
	    _gl.glCullFace(GL10.GL_BACK);
	    _gl.glEnable(GL10.GL_CULL_FACE);
	    
	    // Disable lights by default
	    for (int i = GL10.GL_LIGHT0; i < GL10.GL_LIGHT0 + NUM_GLLIGHTS; i++) {
	    	_gl.glDisable(i);
	    }

		//
		// Scene object init only happens here, when we get GL for the first time
		//
	}
}
