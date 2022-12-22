package min3d.core;

import min3d.Shared;
import min3d.interfaces.ISceneController;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;

/**
 * Extend this class when creating your min3d-based Activity. 
 * Then, override initScene() and updateScene() for your main
 * 3D logic.
 * 
 * Override onCreateSetContentView() to change layout, if desired.
 * 
 * To update 3d scene-related variables from within the the main UI thread,  
 * override onUpdateScene() and onUpdateScene() as needed.
 */
public class RendererActivity extends Activity implements ISceneController
{
	public Scene scene;
	protected GLSurfaceView _glSurfaceView;
	
	protected Handler _initSceneHander;
	protected Handler _updateSceneHander;
	
    private boolean _renderContinuously;
    

	final Runnable _initSceneRunnable = new Runnable() 
	{
        public void run() {
            onInitScene();
        }
    };
    
	final Runnable _updateSceneRunnable = new Runnable() 
    {
        public void run() {
            onUpdateScene();
        }
    };
    

    @Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		_initSceneHander = new Handler();
		_updateSceneHander = new Handler();
		
		//
		// These 4 lines are important.
		//
		Shared.context(this);
		scene = new Scene(this);
		Renderer r = new Renderer(scene);
		Shared.renderer(r);
		
		_glSurfaceView = new GLSurfaceView(this);
        glSurfaceViewConfig();
		_glSurfaceView.setRenderer(r);
		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		
        onCreateSetContentView();
	}
    
    /**
     * Any GlSurfaceView settings that needs to be executed before 
     * GLSurfaceView.setRenderer() can be done by overriding this method. 
     * A couple examples are included in comments below.
     */
    protected void glSurfaceViewConfig()
    {
	    // Example which makes glSurfaceView transparent (along with setting scene.backgroundColor to 0x0)
	    // _glSurfaceView.setEGLConfigChooser(8,8,8,8, 16, 0);
	    // _glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

		// Example of enabling logging of GL operations 
		// _glSurfaceView.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | GLSurfaceView.DEBUG_LOG_GL_CALLS);
    }
	
	protected GLSurfaceView glSurfaceView()
	{
		return _glSurfaceView;
	}
	
	/**
	 * Separated out for easier overriding...
	 */
	protected void onCreateSetContentView()
	{
		setContentView(_glSurfaceView);
	}
	
	@Override
	protected void onResume() 
	{
		super.onResume();
		_glSurfaceView.onResume();
	}
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		_glSurfaceView.onPause();
	}

	/**
	 * Instantiation of Object3D's, setting their properties, and adding Object3D's 
	 * to the scene should be done here. Or any point thereafter.
	 * 
	 * Note that this method is always called after GLCanvas is created, which occurs
	 * not only on Activity.onCreate(), but on Activity.onResume() as well.
	 * It is the user's responsibility to build the logic to restore state on-resume.
	 */
	public void initScene()
	{
	}

	/**
	 * All manipulation of scene and Object3D instance properties should go here.
	 * Gets called on every frame, right before rendering.   
	 */
	public void updateScene()
	{
	}
	
    /**
     * Called _after_ scene init (ie, after initScene).
     * Unlike initScene(), gets called from the UI thread.
     */
    public void onInitScene()
    {
    }
    
    /**
     * Called _after_ updateScene()
     * Unlike initScene(), gets called from the UI thread.
     */
    public void onUpdateScene()
    {
    }
    
    /**
     * Setting this to false stops the render loop, and initScene() and onInitScene() will no longer fire.
     * Setting this to true resumes it. 
     */
    public void renderContinuously(boolean $b)
    {
    	_renderContinuously = $b;
    	if (_renderContinuously)
    		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    	
    	else
    		_glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    
	public Handler getInitSceneHandler()
	{
		return _initSceneHander;
	}
	
	public Handler getUpdateSceneHandler()
	{
		return _updateSceneHander;
	}

    public Runnable getInitSceneRunnable()
    {
    	return _initSceneRunnable;
    }
	
    public Runnable getUpdateSceneRunnable()
    {
    	return _updateSceneRunnable;
    }
}
